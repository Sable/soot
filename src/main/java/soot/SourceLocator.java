package soot;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2004 Ondrej Lhotak
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jf.dexlib2.iface.DexFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.JavaClassProvider.JarException;
import soot.asm.AsmClassProvider;
import soot.asm.AsmJava9ClassProvider;
import soot.cache.CacheClassProvider;
import soot.dexpler.DexFileProvider;
import soot.options.Options;

/**
 * Provides utility methods to retrieve an input stream for a class name, given a classfile, or jimple or baf output files.
 */
public class SourceLocator {
  private static final Logger logger = LoggerFactory.getLogger(SourceLocator.class);
  protected Set<ClassLoader> additionalClassLoaders = new HashSet<ClassLoader>();
  protected List<ClassProvider> classProviders;
  protected List<String> classPath;
  private List<String> sourcePath;
  protected boolean java9Mode = false;

  protected final LoadingCache<String, ClassSourceType> pathToSourceType
      = CacheBuilder.newBuilder().initialCapacity(60).maximumSize(500).softValues()
          .concurrencyLevel(Runtime.getRuntime().availableProcessors()).build(new CacheLoader<String, ClassSourceType>() {
            @Override
            public ClassSourceType load(String path) throws Exception {
              File f = new File(path);
              if (!f.exists() && !Options.v().ignore_classpath_errors()) {
                throw new Exception("Error: The path '" + path + "' does not exist.");
              }
              if (!f.canRead() && !Options.v().ignore_classpath_errors()) {
                throw new Exception("Error: The path '" + path + "' exists but is not readable.");
              }
              if (f.isFile()) {
                if (path.endsWith(".zip")) {
                  return ClassSourceType.zip;
                } else if (path.endsWith(".jar")) {
                  return ClassSourceType.jar;
                } else if (Scene.isApk(new File(path))) {
                  return ClassSourceType.apk;
                } else if (path.endsWith(".dex")) {
                  return ClassSourceType.dex;
                } else {
                  return ClassSourceType.unknown;
                }
              }
              return ClassSourceType.directory;
            }
          });
  protected final LoadingCache<String, Set<String>> archivePathsToEntriesCache
      = CacheBuilder.newBuilder().initialCapacity(60).maximumSize(500).softValues()
          .concurrencyLevel(Runtime.getRuntime().availableProcessors()).build(new CacheLoader<String, Set<String>>() {
            @Override
            public Set<String> load(String archivePath) throws Exception {
              ZipFile archive = null;
              try {
                archive = new ZipFile(archivePath);
                Set<String> ret = new HashSet<String>();
                Enumeration<? extends ZipEntry> it = archive.entries();
                while (it.hasMoreElements()) {
                  ret.add(it.nextElement().getName());
                }
                return ret;
              } finally {
                if (archive != null) {
                  archive.close();
                }
              }
            }
          });
  /**
   * Set containing all dex files that were appended to the classpath later on. The classes from these files are not yet
   * loaded and are still missing from dexClassIndex.
   */
  private Set<String> dexClassPathExtensions;
  /**
   * The index that maps classes to the files they are defined in. This is necessary because a dex file can hold multiple
   * classes.
   */
  private Map<String, File> dexClassIndex;

  public SourceLocator(Singletons.Global g) {
  }

  public static SourceLocator v() {
    if (ModuleUtil.module_mode()) {
      return G.v().soot_ModulePathSourceLocator();
    }
    return G.v().soot_SourceLocator();
  }

  /**
   * Create the given directory and all parent directories if {@code dir} is non-null.
   *
   * @param dir
   */
  public static void ensureDirectoryExists(File dir) {
    if (dir != null && !dir.exists()) {
      try {
        dir.mkdirs();
      } catch (SecurityException se) {
        logger.debug("Unable to create " + dir);
        throw new CompilationDeathException(CompilationDeathException.COMPILATION_ABORTED);
      }
    }
  }

  /**
   * Explodes a class path into a list of individual class path entries.
   */
  public static List<String> explodeClassPath(String classPath) {
    List<String> ret = new ArrayList<String>();
    // the classpath is split at every path separator which is not escaped
    String regex = "(?<!\\\\)" + Pattern.quote(File.pathSeparator);
    for (String originalDir : classPath.split(regex)) {
      try {
        String canonicalDir = new File(originalDir).getCanonicalPath();
        if (originalDir.equals(ModulePathSourceLocator.DUMMY_CLASSPATH_JDK9_FS)) {
          SourceLocator.v().java9Mode = true;
          continue;
        }
        ret.add(canonicalDir);
      } catch (IOException e) {
        throw new CompilationDeathException("Couldn't resolve classpath entry " + originalDir + ": " + e);
      }
    }
    return ret;
  }

  /**
   * Given a class name, uses the soot-class-path to return a ClassSource for the given class.
   */
  public ClassSource getClassSource(String className) {
    if (classPath == null) {
      classPath = explodeClassPath(Scene.v().getSootClassPath());
    }
    if (classProviders == null) {
      setupClassProviders();
    }
    JarException ex = null;
    for (ClassProvider cp : classProviders) {
      try {
        ClassSource ret = cp.find(className);
        if (ret != null) {
          return ret;
        }
      } catch (JarException e) {
        ex = e;
      }
    }
    if (ex != null) {
      throw ex;
    }
    for (final ClassLoader cl : additionalClassLoaders) {
      try {
        ClassSource ret = new ClassProvider() {

          @Override
          public ClassSource find(String className) {
            String fileName = className.replace('.', '/') + ".class";
            InputStream stream = cl.getResourceAsStream(fileName);
            if (stream == null) {
              return null;
            }
            return new CoffiClassSource(className, stream, fileName);
          }

        }.find(className);
        if (ret != null) {
          return ret;
        }
      } catch (JarException e) {
        ex = e;
      }
    }
    if (ex != null) {
      throw ex;
    }
    if (className.startsWith("soot.rtlib.tamiflex.")) {
      String fileName = className.replace('.', '/') + ".class";
      ClassLoader cl = getClass().getClassLoader();
      if (cl == null) {
        return null;
      }
      InputStream stream = cl.getResourceAsStream(fileName);
      if (stream != null) {
        return new CoffiClassSource(className, stream, fileName);
      }
    }
    return null;
  }

  public void additionalClassLoader(ClassLoader c) {
    additionalClassLoaders.add(c);
  }

  protected void setupClassProviders() {
    classProviders = new LinkedList<ClassProvider>();
    ClassProvider classFileClassProvider = Options.v().coffi() ? new CoffiClassProvider() : new AsmClassProvider();
    if (this.java9Mode) {
      classProviders.add(new AsmJava9ClassProvider());
    }
    switch (Options.v().src_prec()) {
      case Options.src_prec_cache:
        System.out.println("Using the cache with default source provider chain.");
        classProviders.add(new CacheClassProvider());
        classProviders.add(classFileClassProvider);
        classProviders.add(new JimpleClassProvider());
        classProviders.add(new JavaClassProvider());
        break;
      case Options.src_prec_only_cache:
        System.out.println("Using the cache only source provider chain.");
        classProviders.add(new CacheClassProvider());
        break;
      case Options.src_prec_class:
        classProviders.add(classFileClassProvider);
        classProviders.add(new JimpleClassProvider());
        classProviders.add(new JavaClassProvider());
        break;
      case Options.src_prec_only_class:
        classProviders.add(classFileClassProvider);
        break;
      case Options.src_prec_java:
        classProviders.add(new JavaClassProvider());
        classProviders.add(classFileClassProvider);
        classProviders.add(new JimpleClassProvider());
        break;
      case Options.src_prec_jimple:
        classProviders.add(new JimpleClassProvider());
        classProviders.add(classFileClassProvider);
        classProviders.add(new JavaClassProvider());
        break;
      case Options.src_prec_apk:
        classProviders.add(new DexClassProvider());
        classProviders.add(classFileClassProvider);
        classProviders.add(new JavaClassProvider());
        classProviders.add(new JimpleClassProvider());
        break;
      case Options.src_prec_apk_c_j:
        classProviders.add(new DexClassProvider());
        classProviders.add(classFileClassProvider);
        classProviders.add(new JimpleClassProvider());
        break;
      default:
        throw new RuntimeException("Other source precedences are not currently supported.");
    }
  }

  public void setClassProviders(List<ClassProvider> classProviders) {
    this.classProviders = classProviders;
  }

  public List<String> classPath() {
    return classPath;
  }

  public void invalidateClassPath() {
    classPath = null;
    dexClassIndex = null;
  }

  public List<String> sourcePath() {
    if (sourcePath == null) {
      sourcePath = new ArrayList<String>();
      for (String dir : classPath) {
        ClassSourceType cst = getClassSourceType(dir);
        if (cst != ClassSourceType.apk && cst != ClassSourceType.jar && cst != ClassSourceType.zip) {
          sourcePath.add(dir);
        }
      }
    }
    return sourcePath;
  }

  protected ClassSourceType getClassSourceType(String path) {
    try {
      return pathToSourceType.get(path);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public List<String> getClassesUnder(String aPath) {
    return getClassesUnder(aPath, "");
  }

  public List<String> getClassesUnder(String aPath, String prefix) {
    List<String> classes = new ArrayList<String>();

    // FIXME: AD the dummy_classpath_variable should be replaced with a more stable concept
    if (aPath.equals(ModulePathSourceLocator.DUMMY_CLASSPATH_JDK9_FS)) {
      Collection<List<String>> values = ModulePathSourceLocator.v().getClassUnderModulePath("jrt:/").values();
      ArrayList<String> foundClasses = new ArrayList<>();
      for (List<String> classesInModule : values) {
        foundClasses.addAll(classesInModule);
      }
      return foundClasses;
    }

    ClassSourceType cst = getClassSourceType(aPath);

    // Get the dex file from an apk
    if (cst == ClassSourceType.apk || cst == ClassSourceType.dex) {
      try {
        for (DexFileProvider.DexContainer<? extends DexFile> dex : DexFileProvider.v().getDexFromSource(new File(aPath))) {
          classes.addAll(DexClassProvider.classesOfDex(dex.getBase().getDexFile()));
        }
      } catch (IOException e) {
        throw new CompilationDeathException("Error reading dex source", e);
      }
    }
    // load Java class files from ZIP and JAR
    else if (cst == ClassSourceType.jar || cst == ClassSourceType.zip) {
      ZipFile archive = null;
      try {
        archive = new ZipFile(aPath);
        for (Enumeration<? extends ZipEntry> entries = archive.entries(); entries.hasMoreElements();) {
          ZipEntry entry = entries.nextElement();
          String entryName = entry.getName();
          if (entryName.endsWith(".class") || entryName.endsWith(".jimple")) {
            int extensionIndex = entryName.lastIndexOf('.');
            entryName = entryName.substring(0, extensionIndex);
            entryName = entryName.replace('/', '.');
            classes.add(prefix + entryName);
          }
        }
      } catch (Throwable e) {
        throw new CompilationDeathException("Error reading archive '" + aPath + "'", e);
      } finally {
        try {
          if (archive != null) {
            archive.close();
          }
        } catch (Throwable t) {
          logger.debug("" + t.getMessage());
        }
      }

      // we might have dex files inside the archive
      try {
        for (DexFileProvider.DexContainer<? extends DexFile> container : DexFileProvider.v()
            .getDexFromSource(new File(aPath))) {
          classes.addAll(DexClassProvider.classesOfDex(container.getBase().getDexFile()));
        }
      } catch (CompilationDeathException e) { // There might be cases where there is no dex file within a JAR or ZIP file...
      } catch (IOException e) {
        /* Ignore unreadable files */
        logger.debug("" + e.getMessage());
      }
    } else if (cst == ClassSourceType.directory) {
      File file = new File(aPath);

      File[] files = file.listFiles();
      if (files == null) {
        files = new File[1];
        files[0] = file;
      }

      for (File element : files) {
        if (element.isDirectory()) {
          classes.addAll(getClassesUnder(aPath + File.separatorChar + element.getName(), prefix + element.getName() + "."));
        } else {
          String fileName = element.getName();

          if (fileName.endsWith(".class")) {
            int index = fileName.lastIndexOf(".class");
            classes.add(prefix + fileName.substring(0, index));
          } else if (fileName.endsWith(".jimple")) {
            int index = fileName.lastIndexOf(".jimple");
            classes.add(prefix + fileName.substring(0, index));
          } else if (fileName.endsWith(".java")) {
            int index = fileName.lastIndexOf(".java");
            classes.add(prefix + fileName.substring(0, index));
          } else if (fileName.endsWith(".dex")) {
            try {
              for (DexFileProvider.DexContainer<? extends DexFile> container : DexFileProvider.v()
                  .getDexFromSource(element)) {
                classes.addAll(DexClassProvider.classesOfDex(container.getBase().getDexFile()));
              }
            } catch (IOException e) {
              /* Ignore unreadable files */
              logger.debug("" + e.getMessage());
            }
          }
        }
      }
    } else {
      throw new RuntimeException("Invalid class source type");
    }
    return classes;
  }

  public String getFileNameFor(SootClass c, int rep) {
    if (rep == Options.output_format_none) {
      return null;
    }

    StringBuffer b = new StringBuffer();

    if (!Options.v().output_jar()) {
      b.append(getOutputDir());
    }

    if ((b.length() > 0) && (b.charAt(b.length() - 1) != File.separatorChar)) {
      b.append(File.separatorChar);
    }

    if (rep != Options.output_format_dava) {
      if (rep == Options.output_format_class) {
        b.append(c.getName().replace('.', File.separatorChar));
      } else if (rep == Options.output_format_template) {
        b.append(c.getName().replace('.', '_'));
        b.append("_Maker");
      } else {
        // Generate tree structure for Jimple output or generation
        // fails for deep hierarchies ("file name too long").
        if (Options.v().hierarchy_dirs() && (rep == Options.output_format_jimple || rep == Options.output_format_shimple)) {
          b.append(c.getName().replace('.', File.separatorChar));
        } else {
          b.append(c.getName());
        }
      }
      b.append(getExtensionFor(rep));

      return b.toString();
    }

    return getDavaFilenameFor(c, b);
  }

  private String getDavaFilenameFor(SootClass c, StringBuffer b) {
    b.append("dava");
    b.append(File.separatorChar);
    ensureDirectoryExists(new File(b.toString() + "classes"));

    b.append("src");
    b.append(File.separatorChar);
    String fixedPackageName = c.getJavaPackageName();
    if (!fixedPackageName.equals("")) {
      b.append(fixedPackageName.replace('.', File.separatorChar));
      b.append(File.separatorChar);
    }

    ensureDirectoryExists(new File(b.toString()));

    b.append(c.getShortJavaStyleName());
    b.append(".java");

    return b.toString();
  }

  /* This is called after sootClassPath has been defined. */
  public Set<String> classesInDynamicPackage(String str) {
    HashSet<String> set = new HashSet<String>(0);
    StringTokenizer strtok = new StringTokenizer(Scene.v().getSootClassPath(), String.valueOf(File.pathSeparatorChar));
    while (strtok.hasMoreTokens()) {
      String path = strtok.nextToken();
      if (getClassSourceType(path) != ClassSourceType.directory) {
        continue;
      }
      // For jimple files
      List<String> l = getClassesUnder(path);
      for (String filename : l) {
        if (filename.startsWith(str)) {
          set.add(filename);
        }
      }

      // For class files;
      path = path + File.separatorChar;
      StringTokenizer tokenizer = new StringTokenizer(str, ".");
      while (tokenizer.hasMoreTokens()) {
        path = path + tokenizer.nextToken();
        if (tokenizer.hasMoreTokens()) {
          path = path + File.separatorChar;
        }
      }
      l = getClassesUnder(path);
      for (String string : l) {
        set.add(str + "." + string);
      }
    }
    return set;
  }

  public String getExtensionFor(int rep) {
    switch (rep) {
      case Options.output_format_baf:
        return ".baf";
      case Options.output_format_b:
        return ".b";
      case Options.output_format_jimple:
        return ".jimple";
      case Options.output_format_jimp:
        return ".jimp";
      case Options.output_format_shimple:
        return ".shimple";
      case Options.output_format_shimp:
        return ".shimp";
      case Options.output_format_grimp:
        return ".grimp";
      case Options.output_format_grimple:
        return ".grimple";
      case Options.output_format_class:
        return ".class";
      case Options.output_format_dava:
        return ".java";
      case Options.output_format_jasmin:
        return ".jasmin";
      case Options.output_format_xml:
        return ".xml";
      case Options.output_format_template:
        return ".java";
      case Options.output_format_asm:
        return ".asm";
      default:
        throw new RuntimeException();
    }
  }

  /**
   * Returns the output directory given by {@link Options} or a default if not set. Also ensures that all directories in the
   * path exist.
   *
   * @return the output directory from {@link Options} or a default if not set
   */
  public String getOutputDir() {
    File dir;
    if (Options.v().output_dir().length() == 0) {
      // Default if -output-dir was not set
      dir = new File("sootOutput");
    } else {
      dir = new File(Options.v().output_dir());
      // If a Jar name was given as the output dir
      // get its parent path (possibly empty)
      if (dir.getPath().endsWith(".jar")) {
        dir = dir.getParentFile();
        if (dir == null) {
          dir = new File("");
        }
      }
    }

    ensureDirectoryExists(dir);
    return dir.getPath();
  }

  /**
   * If {@link Options#v()#output_jar()} is set, returns the name of the jar file to which the output will be written. The
   * name of the jar file can be given with the -output-dir option or a default will be used. Also ensures that all
   * directories in the path exist.
   *
   * @return the name of the Jar file to which outputs are written
   */
  public String getOutputJarName() {
    if (!Options.v().output_jar()) {
      return "";
    }

    File dir;
    if (Options.v().output_dir().length() == 0) {
      // Default if -output-dir was not set
      dir = new File("sootOutput/out.jar");
    } else {
      dir = new File(Options.v().output_dir());
      // If a Jar name was not given, then supply default
      if (!dir.getPath().endsWith(".jar")) {
        dir = new File(dir.getPath(), "out.jar");
      }
    }

    ensureDirectoryExists(dir.getParentFile());
    return dir.getPath();
  }

  /**
   * Searches for a file with the given name in the exploded classPath.
   */
  public FoundFile lookupInClassPath(String fileName) {
    for (String dir : classPath) {
      FoundFile ret = null;
      ClassSourceType cst = getClassSourceType(dir);
      if (cst == ClassSourceType.zip || cst == ClassSourceType.jar) {
        ret = lookupInArchive(dir, fileName);
      } else if (cst == ClassSourceType.directory) {
        ret = lookupInDir(dir, fileName);
      }
      if (ret != null) {
        return ret;
      }
    }
    return null;
  }

  private FoundFile lookupInDir(String dir, String fileName) {
    File f = new File(dir, fileName);
    if (f.exists() && f.canRead()) {
      return new FoundFile(f);
    }
    return null;
  }

  protected FoundFile lookupInArchive(String archivePath, String fileName) {
    Set<String> entryNames = null;
    try {
      entryNames = archivePathsToEntriesCache.get(archivePath);
    } catch (Exception e) {
      throw new RuntimeException(
          "Error: Failed to retrieve the archive entries list for the archive at path '" + archivePath + "'.", e);
    }
    if (entryNames.contains(fileName)) {
      return new FoundFile(archivePath, fileName);
    }
    return null;
  }

  /**
   * Returns the name of the class in which the (possibly inner) class className appears.
   */
  public String getSourceForClass(String className) {
    String javaClassName = className;
    int i = className.indexOf("$");
    if (i > -1) {
      // class is an inner class and will be in
      // Outer of Outer$Inner
      javaClassName = className.substring(0, i);
    }
    return javaClassName;
  }

  /**
   * Return the dex class index that maps class names to files
   *
   * @return the index
   */
  public Map<String, File> dexClassIndex() {
    return dexClassIndex;
  }

  /**
   * Set the dex class index
   *
   * @param index
   *          the index
   */
  public void setDexClassIndex(Map<String, File> index) {
    dexClassIndex = index;
  }

  public void extendClassPath(String newPathElement) {
    classPath = null;
    if (newPathElement.endsWith(".dex") || newPathElement.endsWith(".apk")) {
      if (dexClassPathExtensions == null) {
        dexClassPathExtensions = new HashSet<String>();
      }
      dexClassPathExtensions.add(newPathElement);
    }
  }

  /**
   * Gets all files that were added to the classpath later on and that have not yet been processed for the dexClassIndex
   * mapping
   *
   * @return The set of dex or apk files that still need to be indexed
   */
  public Set<String> getDexClassPathExtensions() {
    return this.dexClassPathExtensions;
  }

  /**
   * Clears the set of dex or apk files that still need to be indexed
   */
  public void clearDexClassPathExtensions() {
    this.dexClassPathExtensions = null;
  }

  protected enum ClassSourceType {
    jar, zip, apk, dex, directory, jrt, unknown
  }
}
