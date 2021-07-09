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
import com.google.common.cache.ForwardingLoadingCache;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jf.dexlib2.iface.DexFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.JavaClassProvider.JarException;
import soot.asm.AsmClassProvider;
import soot.asm.AsmJava9ClassProvider;
import soot.dexpler.DexFileProvider;
import soot.options.Options;
import soot.util.SharedCloseable;

/**
 * Provides utility methods to retrieve an input stream for a class name, given a classfile, or jimple or baf output files.
 */
public class SourceLocator {
  private static final Logger logger = LoggerFactory.getLogger(SourceLocator.class);

  protected final Set<ClassLoader> additionalClassLoaders = new HashSet<ClassLoader>();
  protected List<ClassProvider> classProviders;
  protected List<String> classPath;
  protected List<String> sourcePath;
  protected boolean java9Mode = false;

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

  // NOTE: Capacity here is based on the number of paths where classes are
  // loaded from. This is typically quite small. However, the maximum
  // size must be able to contain all paths in the classpath or else
  // methods such as lookupInClassPath(..) that search for a file by
  // traversing each path in the classpath will cause cache thrashing.
  private static final int PATH_CACHE_CAPACITY = 1000;

  final SharedZipFileCacheWrapper archivePathToZip = new SharedZipFileCacheWrapper(5, PATH_CACHE_CAPACITY);

  // NOTE: Soft and weak references are useless here since the value is an
  // enum type and strings are interned.
  protected final LoadingCache<String, ClassSourceType> pathToSourceType
        = CacheBuilder.newBuilder().initialCapacity(5).maximumSize(PATH_CACHE_CAPACITY)
        .concurrencyLevel(Runtime.getRuntime().availableProcessors())
        .build(new CacheLoader<String, ClassSourceType>() {
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
              switch (path.substring(path.length() - 4)) {
                case ".zip":
                  return ClassSourceType.zip;
                case ".jar":
                  return ClassSourceType.jar;
                case ".dex":
                  return ClassSourceType.dex;
                default:
                  return Scene.isApk(new File(path)) ? ClassSourceType.apk : ClassSourceType.unknown;
              }
            } else if (f.isDirectory()) {
              return ClassSourceType.directory;
            } else {
              throw new Exception("Error: The path '" + path + "' is neither file nor directory.");
            }
          }
        });

  // NOTE: Considering that the uses of this cache hold a reference to the
  // returned value in a very limited scope combined with the softValues
  // directive means that the values will almost always be softly
  // reachable (and not strongly reachable) and thus could all be cleared
  // out if the garbage collector needs the memory.
  protected final LoadingCache<String, Set<String>> archivePathToEntriesCache
        = CacheBuilder.newBuilder().initialCapacity(5).maximumSize(PATH_CACHE_CAPACITY).softValues()
        .concurrencyLevel(Runtime.getRuntime().availableProcessors())
        .build(new CacheLoader<String, Set<String>>() {
          @Override
          public Set<String> load(String archivePath) throws Exception {
            try (SharedCloseable<ZipFile> archive = archivePathToZip.getRef(archivePath)) {
              Set<String> ret = new HashSet<String>();
              for (Enumeration<? extends ZipEntry> it = archive.get().entries(); it.hasMoreElements();) {
                ret.add(it.nextElement().getName());
              }
              return Collections.unmodifiableSet(ret);
            }
          }
        });

  public SourceLocator(Singletons.Global g) {
  }

  public static SourceLocator v() {
    return ModuleUtil.module_mode() ? G.v().soot_ModulePathSourceLocator() : G.v().soot_SourceLocator();
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
    final String regex = "(?<!\\\\)" + Pattern.quote(File.pathSeparator);
    for (String originalDir : classPath.split(regex)) {
      if (!originalDir.isEmpty()) {
        try {
          String canonicalDir = new File(originalDir).getCanonicalPath();
          if (ModulePathSourceLocator.DUMMY_CLASSPATH_JDK9_FS.equals(originalDir)) {
            SourceLocator.v().java9Mode = true;
            continue;
          }
          ret.add(canonicalDir);
        } catch (IOException e) {
          throw new CompilationDeathException("Couldn't resolve classpath entry " + originalDir + ": " + e);
        }
      }
    }
    return Collections.unmodifiableList(ret);
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
            return (stream == null) ? null : new CoffiClassSource(className, stream, fileName);
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
      ClassLoader cl = getClass().getClassLoader();
      if (cl != null) {
        String fileName = className.replace('.', '/') + ".class";
        InputStream stream = cl.getResourceAsStream(fileName);
        if (stream != null) {
          return new CoffiClassSource(className, stream, fileName);
        }
      }
    }
    return null;
  }

  public void additionalClassLoader(ClassLoader c) {
    additionalClassLoaders.add(c);
  }

  protected void setupClassProviders() {
    final List<ClassProvider> classProviders = new LinkedList<ClassProvider>();
    if (this.java9Mode) {
      classProviders.add(new AsmJava9ClassProvider());
    }
    final ClassProvider classFileClassProvider = Options.v().coffi() ? new CoffiClassProvider() : new AsmClassProvider();
    switch (Options.v().src_prec()) {
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
    this.classProviders = classProviders;
  }

  public void setClassProviders(List<ClassProvider> classProviders) {
    this.classProviders = classProviders;
  }

  public List<String> classPath() {
    return classPath;
  }

  public void invalidateClassPath() {
    this.classPath = null;
    this.dexClassIndex = null;
  }

  public List<String> sourcePath() {
    List<String> sourcePath = this.sourcePath;
    if (sourcePath == null) {
      sourcePath = new ArrayList<String>();
      for (String dir : classPath) {
        ClassSourceType cst = getClassSourceType(dir);
        if (cst != ClassSourceType.apk && cst != ClassSourceType.jar && cst != ClassSourceType.zip) {
          sourcePath.add(dir);
        }
      }
      this.sourcePath = sourcePath;
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
    // FIXME: AD the dummy_classpath_variable should be replaced with a more stable concept
    if (ModulePathSourceLocator.DUMMY_CLASSPATH_JDK9_FS.equals(aPath)) {
      ArrayList<String> foundClasses = new ArrayList<String>();
      for (List<String> classesInModule : ModulePathSourceLocator.v().getClassUnderModulePath("jrt:/").values()) {
        foundClasses.addAll(classesInModule);
      }
      return foundClasses;
    }

    List<String> classes = new ArrayList<String>();
    ClassSourceType cst = getClassSourceType(aPath);
    if (cst == ClassSourceType.apk || cst == ClassSourceType.dex) {
      // Get the dex file from an apk
      try {
        for (DexFileProvider.DexContainer<? extends DexFile> dex : DexFileProvider.v().getDexFromSource(new File(aPath))) {
          classes.addAll(DexClassProvider.classesOfDex(dex.getBase().getDexFile()));
        }
      } catch (IOException e) {
        throw new CompilationDeathException("Error reading dex source", e);
      }
    } else if (cst == ClassSourceType.jar || cst == ClassSourceType.zip) {
      // load Java class files from ZIP and JAR
      try (SharedCloseable<ZipFile> archive = archivePathToZip.getRef(aPath)) {
        for (Enumeration<? extends ZipEntry> entries = archive.get().entries(); entries.hasMoreElements();) {
          ZipEntry entry = entries.nextElement();
          String entryName = entry.getName();
          if (entryName.endsWith(".class") || entryName.endsWith(".jimple")) {
            classes.add(prefix + entryName.substring(0, entryName.lastIndexOf('.')).replace('/', '.'));
          }
        }
      } catch (Throwable e) {
        throw new CompilationDeathException("Error reading archive '" + aPath + "'", e);
      }

      // we might have dex files inside the archive
      try {
        for (DexFileProvider.DexContainer<? extends DexFile> dex : DexFileProvider.v().getDexFromSource(new File(aPath))) {
          classes.addAll(DexClassProvider.classesOfDex(dex.getBase().getDexFile()));
        }
      } catch (CompilationDeathException e) {
        // There might be cases where there is no dex file within a JAR or ZIP file...
      } catch (IOException e) {
        // Ignore unreadable files
        logger.debug(e.getMessage());
      }
    } else if (cst == ClassSourceType.directory) {
      File file = new File(aPath);
      File[] files = file.listFiles();
      if (files == null) {
        files = new File[] { file };
      }
      for (File element : files) {
        if (element.isDirectory()) {
          classes.addAll(getClassesUnder(aPath + File.separatorChar + element.getName(), prefix + element.getName() + '.'));
        } else {
          String fileName = element.getName();
          if (fileName.endsWith(".class")) {
            classes.add(prefix + fileName.substring(0, fileName.lastIndexOf(".class")));
          } else if (fileName.endsWith(".jimple")) {
            classes.add(prefix + fileName.substring(0, fileName.lastIndexOf(".jimple")));
          } else if (fileName.endsWith(".java")) {
            classes.add(prefix + fileName.substring(0, fileName.lastIndexOf(".java")));
          } else if (fileName.endsWith(".dex")) {
            try {
              for (DexFileProvider.DexContainer<? extends DexFile> dex : DexFileProvider.v().getDexFromSource(element)) {
                classes.addAll(DexClassProvider.classesOfDex(dex.getBase().getDexFile()));
              }
            } catch (IOException e) {
              /* Ignore unreadable files */
              logger.debug(e.getMessage());
            }
          }
        }
      }
    } else {
      throw new RuntimeException("Invalid class source type " + cst + " for " + aPath);
    }
    return classes;
  }

  public String getFileNameFor(SootClass c, int rep) {
    if (rep == Options.output_format_none) {
      return null;
    }

    StringBuilder b = new StringBuilder();

    if (!Options.v().output_jar()) {
      b.append(getOutputDir());
    }

    if ((b.length() > 0) && (b.charAt(b.length() - 1) != File.separatorChar)) {
      b.append(File.separatorChar);
    }

    switch (rep) {
      case Options.output_format_dava:
        return getDavaFilenameFor(c, b);
      case Options.output_format_class:
        b.append(c.getName().replace('.', File.separatorChar));
        break;
      case Options.output_format_template:
        b.append(c.getName().replace('.', '_'));
        b.append("_Maker");
        break;
      default:
        // Generate tree structure for Jimple output or generation
        // fails for deep hierarchies ("file name too long").
        if ((rep == Options.output_format_jimple || rep == Options.output_format_shimple) && Options.v().hierarchy_dirs()) {
          b.append(c.getName().replace('.', File.separatorChar));
        } else {
          b.append(c.getName());
        }
        break;
    }
    b.append(getExtensionFor(rep));
    return b.toString();
  }

  private String getDavaFilenameFor(SootClass c, StringBuilder b) {
    b.append("dava").append(File.separatorChar);
    ensureDirectoryExists(new File(b.toString() + "classes"));

    b.append("src").append(File.separatorChar);
    String fixedPackageName = c.getJavaPackageName();
    if (!fixedPackageName.isEmpty()) {
      b.append(fixedPackageName.replace('.', File.separatorChar)).append(File.separatorChar);
    }

    ensureDirectoryExists(new File(b.toString()));

    b.append(c.getShortJavaStyleName()).append(".java");

    return b.toString();
  }

  /* This is called after sootClassPath has been defined. */
  public Set<String> classesInDynamicPackage(String str) {
    HashSet<String> set = new HashSet<String>(0);
    StringTokenizer strtok = new StringTokenizer(Scene.v().getSootClassPath(), File.pathSeparator);
    while (strtok.hasMoreTokens()) {
      String path = strtok.nextToken();
      if (getClassSourceType(path) != ClassSourceType.directory) {
        continue;
      }
      // For jimple files
      for (String filename : getClassesUnder(path)) {
        if (filename.startsWith(str)) {
          set.add(filename);
        }
      }

      // For class files
      StringBuilder sb = new StringBuilder(path);
      sb.append(File.separatorChar);
      for (StringTokenizer tok = new StringTokenizer(str, "."); tok.hasMoreTokens();) {
        sb.append(tok.nextToken());
        if (tok.hasMoreTokens()) {
          sb.append(File.separatorChar);
        }
      }

      for (String string : getClassesUnder(sb.toString())) {
        set.add(str + '.' + string);
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
    final String output_dir = Options.v().output_dir();
    if (output_dir.isEmpty()) {
      // Default if -output-dir was not set
      dir = new File("sootOutput");
    } else {
      dir = new File(output_dir);
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
    final String output_dir = Options.v().output_dir();
    if (output_dir.isEmpty()) {
      // Default if -output-dir was not set
      dir = new File("sootOutput/out.jar");
    } else {
      dir = new File(output_dir);
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

  protected FoundFile lookupInDir(String dir, String fileName) {
    File f = new File(dir, fileName);
    return (f.exists() && f.canRead()) ? new FoundFile(f) : null;
  }

  protected FoundFile lookupInArchive(String archivePath, String fileName) {
    Set<String> entryNames = null;
    try {
      entryNames = archivePathToEntriesCache.get(archivePath);
    } catch (Exception e) {
      throw new RuntimeException(
          "Error: Failed to retrieve the archive entries list for the archive at path '" + archivePath + "'.", e);
    }
    return entryNames.contains(fileName) ? new FoundFile(archivePath, fileName) : null;
  }

  /**
   * Returns the name of the class in which the (possibly inner) class className appears.
   */
  public String getSourceForClass(String className) {
    int i = className.indexOf('$');
    if (i > -1) {
      // class is an inner class and will be in Outer of Outer$Inner
      return className.substring(0, i);
    } else {
      return className;
    }
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
      Set<String> dexClassPathExtensions = this.dexClassPathExtensions;
      if (dexClassPathExtensions == null) {
        dexClassPathExtensions = new HashSet<String>();
        this.dexClassPathExtensions = dexClassPathExtensions;
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

  public enum ClassSourceType {
    jar, zip, apk, dex, directory, jrt, unknown
  }

  static class SharedZipFileCacheWrapper {

    // NOTE: the SharedResourceCache here is intentionally wrapped within
    // SharedZipFileCacheWrapper which has only one public method since the
    // complete safe implementation of the cache has not been given (only
    // get(K) is implemented safely). However, the SharedResourceCache
    // could be extended to a fully implemented LoadingCache and be
    // extracted from this inner class to be used more broadly.
    private static class SharedResourceCache<K, V extends Closeable> extends ForwardingLoadingCache<K, SharedCloseable<V>> {

      private static class DelayedRemovalListener<K, V extends SharedCloseable<?>> implements RemovalListener<K, V> {

        private static final BiFunction<Object, Integer, Integer> INC = new BiFunction<Object, Integer, Integer>() {
          @Override
          public Integer apply(Object t, Integer u) {
            return u == null ? 1 : u + 1;
          }
        };

        private static final BiFunction<Object, Integer, Integer> DEC = new BiFunction<Object, Integer, Integer>() {
          @Override
          public Integer apply(Object t, Integer u) {
            return u == 1 ? null : u - 1;
          }
        };

        private final ConcurrentHashMap<K, Integer> delayed = new ConcurrentHashMap<K, Integer>();
        private final Queue<RemovalNotification<K, V>> delayQueue = new ConcurrentLinkedQueue<RemovalNotification<K, V>>();

        @Override
        public void onRemoval(RemovalNotification<K, V> rn) {
          // NOTE: multiple threads may call onRemoval() concurrently
          //
          // Might as well process first b/c even if 'rn' is added, it is
          // very unlikely that it can be removed since the removal listener
          // could be triggered after a call to get(..) which puts the key
          // on the delay queue anyway.
          process();
          removeOrEnqueue(rn, delayQueue);
        }

        public void delay(K key) {
          Integer val = delayed.compute(key, INC);
          assert (val != null && val > 0);
        }

        public void release(K key) {
          Integer val = delayed.compute(key, DEC);
          assert (val == null || val > 0);
          // NOTE: 'null' return value means it was removed from
          // the map because the count reached 0.
          process();
        }

        private void process() {
          Queue<RemovalNotification<K, V>> delayFurther = new LinkedList<RemovalNotification<K, V>>();
          for (RemovalNotification<K, V> rn; (rn = delayQueue.poll()) != null;) {
            removeOrEnqueue(rn, delayFurther);
          }
          delayQueue.addAll(delayFurther);
        }

        private void removeOrEnqueue(RemovalNotification<K, V> rn, Queue<RemovalNotification<K, V>> q) {
          if (delayed.containsKey(rn.getKey())) {
            q.offer(rn);
          } else {
            V val = rn.getValue();
            assert (val != null); // no soft values allowed
            val.release();
          }
        }
      }

      private final LoadingCache<K, SharedCloseable<V>> delegate;
      private final DelayedRemovalListener<K, SharedCloseable<V>> removalListener;

      public SharedResourceCache(int initSize, int maxSize, final CacheLoader<K, V> loader) {
        this.removalListener = new DelayedRemovalListener<K, SharedCloseable<V>>();
        // NOTE: values must be strong references or else they could
        // be garbage collected before they are closed.
        this.delegate = CacheBuilder.newBuilder().initialCapacity(initSize).maximumSize(maxSize)
            .concurrencyLevel(Runtime.getRuntime().availableProcessors()).expireAfterAccess(15, TimeUnit.SECONDS)
            .removalListener(removalListener).build(new CacheLoader<K, SharedCloseable<V>>() {
              @Override
              public SharedCloseable<V> load(K key) throws Exception {
                return new SharedCloseable<V>(loader.load(key));
              }
            });
      }

      @Override
      protected final LoadingCache<K, SharedCloseable<V>> delegate() {
        return delegate;
      }

      @Override
      public final SharedCloseable<V> get(K key) throws ExecutionException {
        removalListener.delay(key);
        try {
          return super.get(key).acquire(); // increments
        } finally {
          removalListener.release(key);
        }
      }
    }

    private final SharedResourceCache<String, ZipFile> cache;

    public SharedZipFileCacheWrapper(int initSize, int maxSize) {
      this.cache = new SharedResourceCache<String, ZipFile>(initSize, maxSize, new CacheLoader<String, ZipFile>() {
        @Override
        public ZipFile load(String archivePath) throws Exception {
          return new ZipFile(archivePath);
        }
      });
    }

    /**
     * Return the opened {@link ZipFile} wrapped in a {@link SharedCloseable} that is already retained. When the user is done
     * with the {@link ZipFile}, simply call the {@link SharedCloseable#release()} method rather than directly closing the
     * {@link ZipFile} in case there are any other references to it.
     *
     * @param archivePath
     *
     * @return
     *
     * @throws ExecutionException
     */
    public SharedCloseable<ZipFile> getRef(String archivePath) throws ExecutionException {
      return cache.get(archivePath);
    }
  }
}
