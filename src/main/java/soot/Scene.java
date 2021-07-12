package soot;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 1999 Raja Vallee-Rai
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.MagicNumberFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pxb.android.axml.AxmlReader;
import pxb.android.axml.AxmlVisitor;
import pxb.android.axml.NodeVisitor;

import soot.dexpler.DalvikThrowAnalysis;
import soot.jimple.spark.internal.ClientAccessibilityOracle;
import soot.jimple.spark.internal.PublicAndProtectedAccessibility;
import soot.jimple.spark.pag.SparkField;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.ContextSensitiveCallGraph;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;
import soot.jimple.toolkits.pointer.SideEffectAnalysis;
import soot.options.CGOptions;
import soot.options.Options;
import soot.toolkits.exceptions.PedanticThrowAnalysis;
import soot.toolkits.exceptions.ThrowAnalysis;
import soot.toolkits.exceptions.UnitThrowAnalysis;
import soot.util.ArrayNumberer;
import soot.util.Chain;
import soot.util.HashChain;
import soot.util.IterableNumberer;
import soot.util.MapNumberer;
import soot.util.Numberer;
import soot.util.StringNumberer;
import soot.util.WeakMapNumberer;

/**
 * Manages the SootClasses of the application being analyzed.
 */
public class Scene {
  private static final Logger logger = LoggerFactory.getLogger(Scene.class);

  private static final int defaultSdkVersion = 15;

  protected final Map<String, RefType> nameToClass = new ConcurrentHashMap<String, RefType>();

  protected final ArrayNumberer<Kind> kindNumberer = new ArrayNumberer<Kind>(
      new Kind[] { Kind.INVALID, Kind.STATIC, Kind.VIRTUAL, Kind.INTERFACE, Kind.SPECIAL, Kind.CLINIT, Kind.THREAD,
          Kind.EXECUTOR, Kind.ASYNCTASK, Kind.FINALIZE, Kind.INVOKE_FINALIZE, Kind.PRIVILEGED, Kind.NEWINSTANCE });

  protected final Set<String> reservedNames = new HashSet<String>();
  @SuppressWarnings("unchecked")
  protected final Set<String>[] basicclasses = new Set[4];

  protected Chain<SootClass> classes = new HashChain<SootClass>();
  protected Chain<SootClass> applicationClasses = new HashChain<SootClass>();
  protected Chain<SootClass> libraryClasses = new HashChain<SootClass>();
  protected Chain<SootClass> phantomClasses = new HashChain<SootClass>();

  protected IterableNumberer<Type> typeNumberer = new ArrayNumberer<Type>();
  protected Numberer<Unit> unitNumberer = new MapNumberer<Unit>();
  protected StringNumberer subSigNumberer = new StringNumberer();
  protected IterableNumberer<SootClass> classNumberer;
  protected Numberer<SparkField> fieldNumberer;
  protected IterableNumberer<SootMethod> methodNumberer;
  protected IterableNumberer<Local> localNumberer;
  protected Numberer<Context> contextNumberer;

  protected Hierarchy activeHierarchy;
  protected FastHierarchy activeFastHierarchy;
  protected SideEffectAnalysis activeSideEffectAnalysis;
  protected PointsToAnalysis activePointsToAnalysis;
  protected CallGraph activeCallGraph;
  protected ReachableMethods reachableMethods;
  protected List<SootMethod> entryPoints;
  protected ContextSensitiveCallGraph cscg;
  protected ClientAccessibilityOracle accessibilityOracle;

  protected String sootClassPath;
  protected List<SootClass> dynamicClasses;
  protected LinkedList<String> excludedPackages;
  protected boolean allowsPhantomRefs = false;
  protected SootClass mainClass;

  protected boolean incrementalBuild = false;
  protected boolean doneResolving = false;

  // Two default values for constructing ExceptionalUnitGraphs:
  private ThrowAnalysis defaultThrowAnalysis;
  private List<String> pkgList;
  private int stateCount = 0;

  private final Map<String, Integer> maxAPIs = new HashMap<String, Integer>();
  private AndroidVersionInfo androidSDKVersionInfo;
  private int androidAPIVersion = -1;

  public Scene(Singletons.Global g) {
    setReservedNames();

    // load soot.class.path system property, if defined
    String scp = System.getProperty("soot.class.path");
    if (scp != null) {
      setSootClassPath(scp);
    }

    if (Options.v().weak_map_structures()) {
      this.classNumberer = new WeakMapNumberer<SootClass>();
      this.fieldNumberer = new WeakMapNumberer<SparkField>();
      this.methodNumberer = new WeakMapNumberer<SootMethod>();
      this.localNumberer = new WeakMapNumberer<Local>();
    } else {
      this.classNumberer = new ArrayNumberer<SootClass>();
      this.fieldNumberer = new ArrayNumberer<SparkField>();
      this.methodNumberer = new ArrayNumberer<SootMethod>();
      this.localNumberer = new ArrayNumberer<Local>();
    }

    addSootBasicClasses();

    determineExcludedPackages();
  }

  public static Scene v() {
    if (ModuleUtil.module_mode()) {
      return G.v().soot_ModuleScene();
    } else {
      return G.v().soot_Scene();
    }
  }

  private void determineExcludedPackages() {
    final Options options = Options.v();
    LinkedList<String> excludedPackages;
    {
      List<String> exclude = options.exclude();
      if (exclude == null) {
        excludedPackages = new LinkedList<String>();
      } else {
        excludedPackages = new LinkedList<String>(exclude);
      }
    }

    // do not kill contents of the APK if we want a working new APK afterwards
    if (!options.include_all()) {
      int fmt = options.output_format();
      if (fmt != Options.output_format_dex && fmt != Options.output_format_force_dex) {
        excludedPackages.add("java.*");
        excludedPackages.add("sun.*");
        excludedPackages.add("javax.*");
        excludedPackages.add("com.sun.*");
        excludedPackages.add("com.ibm.*");
        excludedPackages.add("org.xml.*");
        excludedPackages.add("org.w3c.*");
        excludedPackages.add("apple.awt.*");
        excludedPackages.add("com.apple.*");
      }
    }

    this.excludedPackages = excludedPackages;
  }

  public void setMainClass(SootClass m) {
    mainClass = m;
    if (!m.declaresMethod(getSubSigNumberer().findOrAdd("void main(java.lang.String[])"))) {
      throw new RuntimeException("Main-class has no main method!");
    }
  }

  /**
   * Returns a set of tokens which are reserved. Any field, class, method, or local variable with such a name will be quoted.
   */
  public Set<String> getReservedNames() {
    return reservedNames;
  }

  /**
   * If this name is in the set of reserved names, then return a quoted version of it. Else pass it through. If the name
   * consists of multiple parts separated by dots, the individual names are checked as well.
   */
  public String quotedNameOf(String s) {
    // Pre-check: Is there a chance that we need to escape something?
    // If not, skip the transformation altogether.
    boolean found = s.indexOf('-') > -1;
    if (!found) {
      for (String token : reservedNames) {
        if (s.contains(token)) {
          found = true;
          break;
        }
      }
    }
    if (!found) {
      return s;
    }

    StringBuilder res = new StringBuilder(s.length());
    for (String part : s.split("\\.")) {
      if (res.length() > 0) {
        res.append('.');
      }
      if ((!part.isEmpty() && part.charAt(0) == '-') || reservedNames.contains(part)) {
        res.append('\'').append(part).append('\'');
      } else {
        res.append(part);
      }
    }
    return res.toString();
  }

  /**
   * This method is the inverse of quotedNameOf(). It takes a possible escaped class and reconstructs the original version of
   * it.
   *
   * @param s
   *          The possibly escaped name
   * @return The original, non-escaped name
   */
  public static String unescapeName(String s) {
    // If the name is not escaped, there is nothing to do here
    if (s.indexOf('\'') < 0) {
      return s;
    }

    StringBuilder res = new StringBuilder(s.length());
    for (String part : s.split("\\.")) {
      if (res.length() > 0) {
        res.append('.');
      }
      int len = part.length();
      if (len > 1 && part.charAt(0) == '\'' && part.charAt(len - 1) == '\'') {
        res.append(part.substring(1, len - 1));
      } else {
        res.append(part);
      }
    }
    return res.toString();
  }

  public boolean hasMainClass() {
    if (mainClass == null) {
      setMainClassFromOptions();
    }
    return mainClass != null;
  }

  public SootClass getMainClass() {
    if (!hasMainClass()) {
      throw new RuntimeException("There is no main class set!");
    }
    return mainClass;
  }

  public SootMethod getMainMethod() {
    if (!hasMainClass()) {
      throw new RuntimeException("There is no main class set!");
    }

    SootMethod mainMethod = mainClass.getMethodUnsafe("main",
        Collections.singletonList(ArrayType.v(RefType.v("java.lang.String"), 1)), VoidType.v());
    if (mainMethod == null) {
      throw new RuntimeException("Main class declares no main method!");
    }
    return mainMethod;
  }

  public void setSootClassPath(String p) {
    sootClassPath = p;
    SourceLocator.v().invalidateClassPath();
  }

  public void extendSootClassPath(String newPathElement) {
    sootClassPath += File.pathSeparatorChar + newPathElement;
    SourceLocator.v().extendClassPath(newPathElement);
  }

  public String getSootClassPath() {
    if (sootClassPath == null) {
      // First, check Options for a classpath
      String cp = Options.v().soot_classpath();
      // If no classpath is given via Options, just use the default.
      // Otherwise, if the prepend flag is set, append the default.
      if (cp == null || cp.isEmpty()) {
        cp = defaultClassPath();
      } else if (Options.v().prepend_classpath()) {
        cp += File.pathSeparatorChar + defaultClassPath();
      }
      List<String> dirs = new LinkedList<String>();
      dirs.addAll(Options.v().process_dir());
      // Add process-jar-dirs
      List<String> jarDirs = Options.v().process_jar_dir();
      if (!jarDirs.isEmpty()) {
        for (String jarDirName : jarDirs) {
          File jarDir = new File(jarDirName);
          File[] contents = jarDir.listFiles();
          for (File f : contents) {
            if (f.getAbsolutePath().endsWith(".jar")) {
              dirs.add(f.getAbsolutePath());
            }
          }
        }
      }
      // Add process-dirs (if applicable)
      if (!dirs.isEmpty()) {
        StringBuilder pds = new StringBuilder();
        for (String path : dirs) {
          if (!cp.contains(path)) {
            pds.append(path).append(File.pathSeparatorChar);
          }
        }
        cp = pds.append(cp).toString();
      }
      // Set the new classpath
      sootClassPath = cp;
    }

    return sootClassPath;
  }

  /**
   * Returns the max Android API version number available in directory 'dir'
   *
   * @param dir
   * @return
   */
  private int getMaxAPIAvailable(String dir) {
    Integer mapi = this.maxAPIs.get(dir);
    if (mapi != null) {
      return mapi;
    }

    File d = new File(dir);
    if (!d.exists()) {
      throw new AndroidPlatformException(
          String.format("The Android platform directory you have specified (%s) does not exist. Please check.", dir));
    }

    File[] files = d.listFiles();
    if (files == null) {
      return -1;
    }

    int maxApi = -1;
    for (File f : files) {
      String name = f.getName();
      if (f.isDirectory() && name.startsWith("android-")) {
        try {
          int v = Integer.decode(name.split("android-")[1]);
          if (v > maxApi) {
            maxApi = v;
          }
        } catch (NumberFormatException ex) {
          // We simply ignore directories that do not follow the
          // Android naming structure
        }
      }
    }
    this.maxAPIs.put(dir, maxApi);
    return maxApi;
  }

  public String getAndroidJarPath(String jars, String apk) {
    int APIVersion = getAndroidAPIVersion(jars, apk);

    String jarPath = jars + File.separatorChar + "android-" + APIVersion + File.separatorChar + "android.jar";

    // check that jar exists
    File f = new File(jarPath);
    if (!f.isFile()) {
      throw new AndroidPlatformException(String.format("error: target android.jar %s does not exist.", jarPath));
    }
    return jarPath;
  }

  public int getAndroidAPIVersion() {
    return androidAPIVersion > 0 ? androidAPIVersion
        : (Options.v().android_api_version() > 0 ? Options.v().android_api_version() : defaultSdkVersion);
  }

  private int getAndroidAPIVersion(String jars, String apk) {
    // Do we already have an API version?
    if (androidAPIVersion > 0) {
      return androidAPIVersion;
    }

    // get path to appropriate android.jar
    File jarsF = new File(jars);
    if (!jarsF.exists()) {
      throw new AndroidPlatformException(
          String.format("Android platform directory '%s' does not exist!", jarsF.getAbsolutePath()));
    }
    if (apk != null && !(new File(apk)).exists()) {
      throw new RuntimeException("file '" + apk + "' does not exist!");
    }

    // Use the default if we don't have any other information
    androidAPIVersion = defaultSdkVersion;

    // Do we have an explicit API version?
    if (Options.v().android_api_version() > 0) {
      androidAPIVersion = Options.v().android_api_version();
    } else if (apk != null) {
      if (apk.toLowerCase().endsWith(".apk")) {
        androidAPIVersion = getTargetSDKVersion(apk, jars);
      }
    }

    // If we don't have that API version installed, we take the most recent one we have
    final int maxAPI = getMaxAPIAvailable(jars);
    if (maxAPI > 0 && androidAPIVersion > maxAPI) {
      androidAPIVersion = maxAPI;
    }

    // If the platform version is missing in the middle, we take the next one
    while (androidAPIVersion < maxAPI) {
      String jarPath = jars + File.separatorChar + "android-" + androidAPIVersion + File.separatorChar + "android.jar";
      if (new File(jarPath).exists()) {
        break;
      }
      androidAPIVersion++;
    }

    return androidAPIVersion;
  }

  public static class AndroidVersionInfo {

    public int sdkTargetVersion = -1;
    public int minSdkVersion = -1;
    public int platformBuildVersionCode = -1;

    private static AndroidVersionInfo get(InputStream manifestIS) {
      final AndroidVersionInfo versionInfo = new AndroidVersionInfo();
      final AxmlVisitor axmlVisitor = new AxmlVisitor() {
        private String nodeName = null;

        @Override
        public void attr(String ns, String name, int resourceId, int type, Object obj) {
          super.attr(ns, name, resourceId, type, obj);

          if (nodeName != null && name != null) {
            if (nodeName.equals("manifest")) {
              if (name.equals("platformBuildVersionCode")) {
                versionInfo.platformBuildVersionCode = Integer.valueOf("" + obj);
              }
            } else if (nodeName.equals("uses-sdk")) {
              // Obfuscated APKs often remove the attribute names and use the resourceId instead
              // Therefore it is better to check for both variants
              if (name.equals("targetSdkVersion") || (name.isEmpty() && resourceId == 16843376)) {
                versionInfo.sdkTargetVersion = Integer.valueOf(String.valueOf(obj));
              } else if (name.equals("minSdkVersion") || (name.isEmpty() && resourceId == 16843276)) {
                versionInfo.minSdkVersion = Integer.valueOf(String.valueOf(obj));
              }
            }
          }
        }

        @Override
        public NodeVisitor child(String ns, String name) {
          nodeName = name;
          return this;
        }
      };
      try {
        AxmlReader xmlReader = new AxmlReader(IOUtils.toByteArray(manifestIS));
        xmlReader.accept(axmlVisitor);
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
      }
      return versionInfo;
    }
  }

  private int getTargetSDKVersion(String apkFile, String platformJARs) {
    // get AndroidManifest
    ZipFile archive = null;
    try {
      InputStream manifestIS = null;
      try {
        archive = new ZipFile(apkFile);
        for (Enumeration<? extends ZipEntry> entries = archive.entries(); entries.hasMoreElements();) {
          ZipEntry entry = entries.nextElement();
          // We are dealing with the Android manifest
          if ("AndroidManifest.xml".equals(entry.getName())) {
            manifestIS = archive.getInputStream(entry);
            break;
          }
        }
      } catch (Exception e) {
        throw new RuntimeException("Error when looking for manifest in apk: " + e);
      }

      if (manifestIS == null) {
        logger.debug("Could not find sdk version in Android manifest! Using default: " + defaultSdkVersion);
        return defaultSdkVersion;
      }

      // process AndroidManifest.xml
      androidSDKVersionInfo = AndroidVersionInfo.get(manifestIS);
    } finally {
      if (archive != null) {
        try {
          archive.close();
        } catch (IOException e) {
          throw new RuntimeException("Error when looking for manifest in apk: " + e);
        }
      }
    }

    int maxAPI = getMaxAPIAvailable(platformJARs);
    int APIVersion = -1;
    if (androidSDKVersionInfo.sdkTargetVersion != -1) {
      if (androidSDKVersionInfo.sdkTargetVersion > maxAPI && androidSDKVersionInfo.minSdkVersion != -1
          && androidSDKVersionInfo.minSdkVersion <= maxAPI) {
        logger.warn("Android API version '" + androidSDKVersionInfo.sdkTargetVersion
            + "' not available, using minApkVersion '" + androidSDKVersionInfo.minSdkVersion + "' instead");
        APIVersion = androidSDKVersionInfo.minSdkVersion;
      } else {
        APIVersion = androidSDKVersionInfo.sdkTargetVersion;
      }
    } else if (androidSDKVersionInfo.platformBuildVersionCode != -1) {
      if (androidSDKVersionInfo.platformBuildVersionCode > maxAPI && androidSDKVersionInfo.minSdkVersion != -1
          && androidSDKVersionInfo.minSdkVersion <= maxAPI) {
        logger.warn("Android API version '" + androidSDKVersionInfo.platformBuildVersionCode
            + "' not available, using minApkVersion '" + androidSDKVersionInfo.minSdkVersion + "' instead");
        APIVersion = androidSDKVersionInfo.minSdkVersion;
      } else {
        APIVersion = androidSDKVersionInfo.platformBuildVersionCode;
      }
    } else if (androidSDKVersionInfo.minSdkVersion != -1) {
      APIVersion = androidSDKVersionInfo.minSdkVersion;
    } else {
      logger.debug("Could not find sdk version in Android manifest! Using default: " + defaultSdkVersion);
      APIVersion = defaultSdkVersion;
    }

    if (APIVersion <= 2) {
      APIVersion = 3;
    }
    return APIVersion;
  }

  public AndroidVersionInfo getAndroidSDKVersionInfo() {
    return androidSDKVersionInfo;
  }

  public String defaultClassPath() {
    if (Options.v().src_prec() != Options.src_prec_apk) {
      // If we have an apk file on the process dir and do not have a src-prec
      // option that loads APK files, we give a warning
      for (String entry : Options.v().process_dir()) {
        if (entry.toLowerCase().endsWith(".apk")) {
          System.err.println("APK file on process dir, but chosen src-prec does not support loading APKs");
          break;
        }
      }
      String path = defaultJavaClassPath();
      if (path == null) {
        throw new RuntimeException("Error: cannot find rt.jar.");
      }
      return path;
    } else {
      return defaultAndroidClassPath();
    }
  }

  private String defaultAndroidClassPath() {
    // check that android.jar is not in classpath
    String androidJars = Options.v().android_jars();
    String forceAndroidJar = Options.v().force_android_jar();
    if ((androidJars == null || androidJars.isEmpty()) && (forceAndroidJar == null || forceAndroidJar.isEmpty())) {
      throw new RuntimeException("You are analyzing an Android application but did "
          + "not define android.jar. Options -android-jars or -force-android-jar should be used.");
    }

    // Get the platform JAR file. It either directly specified, or
    // we detect it from the target version of the APK we are
    // analyzing
    String jarPath = "";
    if (forceAndroidJar != null && !forceAndroidJar.isEmpty()) {
      jarPath = forceAndroidJar;

      if (Options.v().android_api_version() > 0) {
        androidAPIVersion = Options.v().android_api_version();
      } else if (forceAndroidJar.contains("android-")) {
        Pattern pt = Pattern.compile("\\" + File.separatorChar + "android-(\\d+)" + "\\" + File.separatorChar);
        Matcher m = pt.matcher(forceAndroidJar);
        if (m.find()) {
          androidAPIVersion = Integer.valueOf(m.group(1));
        }
      } else {
        androidAPIVersion = defaultSdkVersion;
      }
    } else if (androidJars != null && !androidJars.isEmpty()) {
      List<String> classPathEntries
          = new ArrayList<String>(Arrays.asList(Options.v().soot_classpath().split(File.pathSeparator)));
      classPathEntries.addAll(Options.v().process_dir());

      String targetApk = "";
      Set<String> targetDexs = new HashSet<String>();
      for (String entry : classPathEntries) {
        if (isApk(new File(entry))) {
          if (targetApk != null && !targetApk.isEmpty()) {
            throw new RuntimeException("only one Android application can be analyzed when using option -android-jars.");
          }
          targetApk = entry;
        }
        if (entry.toLowerCase().endsWith(".dex")) {
          // names are case-insensitive
          targetDexs.add(entry);
        }
      }

      // We need at least one file to process
      if (targetApk == null || targetApk.isEmpty()) {
        if (targetDexs.isEmpty()) {
          throw new RuntimeException("no apk file given");
        }
        jarPath = getAndroidJarPath(androidJars, null);
      } else {
        jarPath = getAndroidJarPath(androidJars, targetApk);
      }
    }

    // We must have a platform JAR file when analyzing Android apps
    if (jarPath.isEmpty()) {
      throw new RuntimeException("android.jar not found.");
    }

    // Check the platform JAR file
    File f = new File(jarPath);
    if (!f.exists()) {
      throw new RuntimeException("file '" + jarPath + "' does not exist!");
    } else {
      logger.debug("Using '" + jarPath + "' as android.jar");
    }

    return jarPath;
  }

  public static boolean isApk(File apk) {
    // first check magic number
    // Note that there are multiple magic numbers for different versions of ZIP files, but all of them
    // have "PK" at the beginning. In order to not decline possible future versions of ZIP files which
    // may be supported by the JVM, we only check these two bytes.
    MagicNumberFileFilter apkFilter = new MagicNumberFileFilter(new byte[] { (byte) 0x50, (byte) 0x4B });
    if (!apkFilter.accept(apk)) {
      return false;
    }
    // second check if contains dex file.
    try (ZipFile zf = new ZipFile(apk)) {
      for (Enumeration<? extends ZipEntry> en = zf.entries(); en.hasMoreElements();) {
        ZipEntry z = en.nextElement();
        if ("classes.dex".equals(z.getName())) {
          return true;
        }
      }
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
    return false;
  }

  /**
   * Checks if the version number indicates a Java version >= 9 in order to handle the new virtual filesystem jrt:/
   *
   * @param version
   * @return
   */
  public static boolean isJavaGEQ9(String version) {
    try {
      // We may have versions such as "14-ea"
      int idx = version.indexOf('-');
      if (idx > 0) {
        version = version.substring(0, idx);
      }

      String[] elements = version.split("\\.");
      // string has the form 9.x.x....
      Integer firstVersionDigest = Integer.valueOf(elements[0]);
      if (firstVersionDigest >= 9) {
        return true;
      }
      if (firstVersionDigest == 1 && elements.length > 1) {
        // string has the form 1.9.x.xxx
        return Integer.valueOf(elements[1]) >= 9;
      } else {
        throw new IllegalArgumentException(String.format("Unknown Version number schema %s", version));
      }
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException(String.format("Unknown Version number schema %s", version), ex);
    }
  }

  /**
   * Returns the default class path used for this JVM.
   *
   * @return the default class path (or null if none could be found)
   */
  public static String defaultJavaClassPath() {
    final String javaHome = System.getProperty("java.home");
    StringBuilder sb = new StringBuilder();
    if ("Mac OS X".equals(System.getProperty("os.name"))) {
      // in older Mac OS X versions, rt.jar was split into classes.jar and
      // ui.jar
      String prefix = javaHome + File.separatorChar + ".." + File.separatorChar + "Classes" + File.separatorChar;
      File classesJar = new File(prefix + "classes.jar");
      if (classesJar.exists()) {
        sb.append(classesJar.getAbsolutePath()).append(File.pathSeparatorChar);
      }

      File uiJar = new File(prefix + "ui.jar");
      if (uiJar.exists()) {
        sb.append(uiJar.getAbsolutePath()).append(File.pathSeparatorChar);
      }
    }
    // behavior for Java versions >=9, which do not have a rt.jar file
    final boolean javaGEQ9 = isJavaGEQ9(System.getProperty("java.version"));
    if (javaGEQ9) {
      sb.append(ModulePathSourceLocator.DUMMY_CLASSPATH_JDK9_FS);
      // this is a new basic class in java >= 9 that needs to be laoded
      Scene.v().addBasicClass("java.lang.invoke.StringConcatFactory");
    } else {
      File rtJar = new File(javaHome + File.separatorChar + "lib" + File.separatorChar + "rt.jar");
      if (rtJar.exists() && rtJar.isFile()) {
        // logger.debug("Using JRE runtime: " + rtJar.getAbsolutePath());
        sb.append(rtJar.getAbsolutePath());
      } else {
        // in case we're not in JRE environment, try JDK
        rtJar = new File(javaHome + File.separatorChar + "jre" + File.separatorChar + "lib" + File.separatorChar + "rt.jar");
        if (rtJar.exists() && rtJar.isFile()) {
          // logger.debug("Using JDK runtime: " + rtJar.getAbsolutePath());
          sb.append(rtJar.getAbsolutePath());
        } else {
          // not in JDK either
          return null;
        }
      }
    }

    if (!javaGEQ9
        && (Options.v().whole_program() || Options.v().whole_shimple()
            || Options.v().output_format() == Options.output_format_dava)) {
      // add jce.jar, which is necessary for whole program mode
      // (java.security.Signature from rt.jar imports javax.crypto.Cipher from jce.jar)
      sb.append(File.pathSeparatorChar).append(javaHome).append(File.separatorChar).append("lib").append(File.separatorChar)
          .append("jce.jar");
    }

    return sb.toString();
  }

  public int getState() {
    return this.stateCount;
  }

  protected synchronized void modifyHierarchy() {
    this.stateCount++;
    this.activeHierarchy = null;
    this.activeFastHierarchy = null;
    this.activeSideEffectAnalysis = null;
    this.activePointsToAnalysis = null;
  }

  /**
   * Adds the given class to the Scene. This method marks the given class as a library class and invalidates the class
   * hierarchy.
   *
   * @param c
   *          The class to add
   */
  public void addClass(SootClass c) {
    addClassSilent(c);
    c.setLibraryClass();
    modifyHierarchy();
  }

  /**
   * Adds the given class to the Scene. This method does not handle any dependencies such as invalidating the hierarchy. The
   * class is neither marked as application class, nor library class.
   *
   * @param c
   *          The class to add
   */
  protected void addClassSilent(SootClass c) {
    synchronized (c) {
      if (c.isInScene()) {
        throw new RuntimeException("already managed: " + c.getName());
      }

      if (containsClass(c.getName())) {
        throw new RuntimeException("duplicate class: " + c.getName());
      }

      classes.add(c);

      c.getType().setSootClass(c);
      c.setInScene(true);

      // Phantom classes are not really part of the hierarchy anyway, so
      // we can keep the old one
      if (!c.isPhantom) {
        modifyHierarchy();
      }
      nameToClass.computeIfAbsent(c.getName(), k -> c.getType());
    }
  }

  public void removeClass(SootClass c) {
    if (!c.isInScene()) {
      throw new RuntimeException();
    }

    classes.remove(c);

    if (c.isLibraryClass()) {
      libraryClasses.remove(c);
    } else if (c.isPhantomClass()) {
      phantomClasses.remove(c);
    } else if (c.isApplicationClass()) {
      applicationClasses.remove(c);
    }

    c.getType().setSootClass(null);
    c.setInScene(false);
    modifyHierarchy();
  }

  public boolean containsClass(String className) {
    RefType type = nameToClass.get(className);
    return type != null && type.hasSootClass() && type.getSootClass().isInScene();
  }

  public boolean containsType(String className) {
    return nameToClass.containsKey(className);
  }

  private static int signatureSeparatorIndex(String sig) {
    int len = sig.length();
    if (len < 3 || sig.charAt(0) != '<' || sig.charAt(len - 1) != '>') {
      throw new RuntimeException("oops " + sig);
    }
    int index = sig.indexOf(':');
    if (index < 0) {
      throw new RuntimeException("oops " + sig);
    }
    return index;
  }

  private static String sepIndexToClass(String sig, int index) {
    // Must unescape the class name from a signature because the
    // Scene does not contain the escaped versions of the classes.
    return unescapeName(sig.substring(1, index));
  }

  private static String sepIndexToSubsignature(String sig, int index) {
    return sig.substring(index + 2, sig.length() - 1);
  }

  public static String signatureToClass(String sig) {
    return sepIndexToClass(sig, signatureSeparatorIndex(sig));
  }

  public static String signatureToSubsignature(String sig) {
    return sepIndexToSubsignature(sig, signatureSeparatorIndex(sig));
  }

  public SootField grabField(String fieldSignature) {
    int index = signatureSeparatorIndex(fieldSignature);
    String cname = sepIndexToClass(fieldSignature, index);
    if (!containsClass(cname)) {
      return null;
    }
    String fname = sepIndexToSubsignature(fieldSignature, index);
    return getSootClass(cname).getFieldUnsafe(fname);
  }

  public boolean containsField(String fieldSignature) {
    return grabField(fieldSignature) != null;
  }

  public SootMethod grabMethod(String methodSignature) {
    int index = signatureSeparatorIndex(methodSignature);
    String cname = sepIndexToClass(methodSignature, index);
    if (!containsClass(cname)) {
      return null;
    }
    String mname = sepIndexToSubsignature(methodSignature, index);
    return getSootClass(cname).getMethodUnsafe(mname);
  }

  public boolean containsMethod(String methodSignature) {
    return grabMethod(methodSignature) != null;
  }

  public SootField getField(String fieldSignature) {
    SootField f = grabField(fieldSignature);
    if (f != null) {
      return f;
    }
    throw new RuntimeException("tried to get nonexistent field " + fieldSignature);
  }

  public SootMethod getMethod(String methodSignature) {
    SootMethod m = grabMethod(methodSignature);
    if (m != null) {
      return m;
    }
    throw new RuntimeException("tried to get nonexistent method " + methodSignature);
  }

  /**
   * Attempts to load the given class and all of the required support classes. Returns the original class if it was loaded,
   * or null otherwise.
   */
  public SootClass tryLoadClass(String className, int desiredLevel) {
    /*
     * if(Options.v().time()) Main.v().resolveTimer.start();
     */

    setPhantomRefs(true);
    ClassSource source = SourceLocator.v().getClassSource(className);
    try {
      if (!getPhantomRefs() && source == null) {
        setPhantomRefs(false);
        return null;
      }
    } finally {
      if (source != null) {
        source.close();
      }
    }
    SootClass toReturn = SootResolver.v().resolveClass(className, desiredLevel);
    setPhantomRefs(false);
    return toReturn;

    /*
     * if(Options.v().time()) Main.v().resolveTimer.end();
     */
  }

  /** Loads the given class and all of the required support classes. Returns the first class. */
  public SootClass loadClassAndSupport(String className) {
    SootClass ret = loadClass(className, SootClass.SIGNATURES);
    if (!ret.isPhantom()) {
      ret = loadClass(className, SootClass.BODIES);
    }
    return ret;
  }

  public SootClass loadClass(String className, int desiredLevel) {
    /*
     * if(Options.v().time()) Main.v().resolveTimer.start();
     */

    setPhantomRefs(true);
    SootClass toReturn = SootResolver.v().resolveClass(className, desiredLevel);
    setPhantomRefs(false);

    return toReturn;

    /*
     * if(Options.v().time()) Main.v().resolveTimer.end();
     */
  }

  /**
   * Returns the RefType with the given class name or primitive type.
   *
   * @throws RuntimeException
   *           if the Type for this name cannot be found. Use {@link #getRefTypeUnsafe(String)} to check if type is an
   *           registered RefType.
   */
  public Type getType(String arg) {
    Type t = getTypeUnsafe(arg, false); // Set to false to preserve the original functionality just in case
    if (t == null) {
      throw new RuntimeException("Unknown Type: '" + t + "'");
    }
    return t;
  }

  /**
   * Returns a Type object representing the given type string. It will first attempt to resolve the type as a reference type
   * that currently exists in the Scene. If this fails it will attempt to resolve the type as a java primitive type. Lastly,
   * if phantom refs are allowed it will construct a phantom class for the given type and return a reference type based on
   * the phantom class. Otherwise, this method will return null. Note, if the resolved base type is not null and the string
   * representation of the type is an array, the returned type will be an ArrayType with the base type resolved as described
   * above.
   *
   * Notice: Recommend to use {@link #getTypeUnsafeUnescape}
   *
   * @param arg
   *          A string description of the type
   * @return The Type if it can be resolved and null otherwise
   */
  public Type getTypeUnsafe(String arg) {
    return getTypeUnsafe(arg, true);
  }

  /**
   * The method do the same thing as the method {@link #getTypeUnsafe(String)} except for unescaping the argument.
   *
   * An argument passed into the method {@link #getTypeUnsafe} may be <b>quoted</b>, like them: <br/>
   * <li>1. "sun.reflect.'annotation'.AnnotationType"<li/> <br/>
   * <li>2. "java.lang.'annotation'.Annotation"<li/> <br/>
   *
   * If calling {@link #getTypeUnsafe(String)} with <b>quoted</b> argument, the method maybe return null. <br/>
   *
   * For example, after we call {@link #loadNecessaryClasses}, getTypeUnsafe("java.lang.'annotation'.Annotation") <br/>
   * will return null but getTypeUnsafe("java.lang.annotation.Annotation") will not return null.
   *
   * @param arg
   *          A string description of the type
   * @return The Type if it can be resolved and null otherwise
   */
  public Type getTypeUnsafeUnescape(String arg) {
    String unescapeName = unescapeName(arg);
    return getTypeUnsafe(unescapeName);
  }

  /**
   * Returns a Type object representing the given type string. It will first attempt to resolve the type as a reference type
   * that currently exists in the Scene. If this fails it will attempt to resolve the type as a java primitive type. Lastly,
   * if phantom refs are allowed and phantomNonExist=true it will construct a phantom class for the given type and return a
   * reference type based on the phantom class. Otherwise, this method will return null. Note, if the resolved base type is
   * not null and the string representation of the type is an array, the returned type will be an ArrayType with the base
   * type resolved as described above.
   *
   * @param arg
   *          A string description of the type
   * @param phantomNonExist
   *          Indicates that a phantom class should be created for the given type string and a Type object should be created
   *          based on the phantom class if a class matching the type name does not exists in the scene and phantom refs are
   *          allowed
   * @return The Type if it can be resolved and null otherwise
   */
  public Type getTypeUnsafe(String arg, boolean phantomNonExist) {
    String type = arg.replaceAll("([^\\[\\]]*)(.*)", "$1");
    Type result = getRefTypeUnsafe(type);
    if (result == null) {
      switch (type) {
        case "long":
          result = LongType.v();
          break;
        case "short":
          result = ShortType.v();
          break;
        case "double":
          result = DoubleType.v();
          break;
        case "int":
          result = IntType.v();
          break;
        case "float":
          result = FloatType.v();
          break;
        case "byte":
          result = ByteType.v();
          break;
        case "char":
          result = CharType.v();
          break;
        case "void":
          result = VoidType.v();
          break;
        case "boolean":
          result = BooleanType.v();
          break;
        default:
          if (phantomNonExist && allowsPhantomRefs()) {
            getSootClassUnsafe(type, phantomNonExist);
            result = getRefTypeUnsafe(type);
          }
          break;
      }
    }

    if (result != null) {
      int arrayCount = arg.contains("[") ? arg.replaceAll("([^\\[\\]]*)(.*)", "$2").length() / 2 : 0;
      if (arrayCount != 0) {
        result = ArrayType.v(result, arrayCount);
      }
    }
    return result;
  }

  /**
   * Returns the RefType with the given className.
   *
   * @throws IllegalStateException
   *           if the RefType for this class cannot be found. Use {@link #containsType(String)} to check if type is
   *           registered
   */
  public RefType getRefType(String className) {
    RefType refType = getRefTypeUnsafe(className);
    if (refType == null) {
      throw new IllegalStateException("RefType " + className + " not loaded. "
          + "If you tried to get the RefType of a library class, did you call loadNecessaryClasses()? "
          + "Otherwise please check Soot's classpath.");
    }
    return refType;
  }

  /**
   * Returns the RefType with the given className. Returns null if no type with the given name can be found.
   */
  public RefType getRefTypeUnsafe(String className) {
    return nameToClass.get(className);
  }

  /** Returns the {@link RefType} for {@link Object}. */
  public RefType getObjectType() {
    return getRefType("java.lang.Object");
  }

  /**
   * Returns the SootClass with the given className. If no class with the given name exists, null is returned unless phantom
   * refs are allowed. In this case, a new phantom class is created.
   *
   * The difference with the getSootClass() version is that this version doesn't throw a RuntimeException if the requested
   * class doesn't exist in the Scene. Instead it returns null.
   *
   * @param className
   *          The name of the class to get
   * @return The class if it exists, otherwise null
   */
  public SootClass getSootClassUnsafe(String className) {
    return getSootClassUnsafe(className, true);
  }

  /**
   * Returns the SootClass with the given className. If no class with the given name exists, null is returned unless
   * phantomNonExist=true and phantom refs are allowed. In this case, a new phantom class is created and returned.
   *
   * The difference with the getSootClass() version is that this version doesn't throw a RuntimeException if the requested
   * class doesn't exist in the Scene. Instead it returns null or a phantom class, depending on the flag.
   *
   * @param className
   *          The name of the class to get
   * @param phantomNonExist
   *          Indicates that a phantom class should be created if a class with the given name does not exist and phantom refs
   *          are allowed
   *
   * @return The class if it exists, otherwise null
   */
  public SootClass getSootClassUnsafe(String className, boolean phantomNonExist) {
    RefType type = nameToClass.get(className);
    if (type != null) {
      synchronized (type) {
        if (type.hasSootClass() || !SootClass.INVOKEDYNAMIC_DUMMY_CLASS_NAME.equals(className)) {
          SootClass tsc = type.getSootClass();
          if (tsc != null) {
            return tsc;
          }
        }
      }
    }

    if ((phantomNonExist && allowsPhantomRefs()) || SootClass.INVOKEDYNAMIC_DUMMY_CLASS_NAME.equals(className)) {
      type = getOrAddRefType(className);
      synchronized (type) {
        if (type.hasSootClass()) {
          return type.getSootClass();
        }
        SootClass c = new SootClass(className);
        c.isPhantom = true;
        addClassSilent(c);
        c.setPhantomClass();
        return c;
      }
    }

    return null;
  }

  /**
   * Returns the SootClass with the given className.
   * 
   * @param className
   *          The name of the class to get; throws RuntimeException if this class does not exist.
   */
  public SootClass getSootClass(String className) {
    SootClass sc = getSootClassUnsafe(className);
    if (sc != null) {
      return sc;
    }
    throw new RuntimeException(System.lineSeparator() + "Aborting: can't find classfile " + className);
  }

  /** Returns an backed chain of the classes in this manager. */
  public Chain<SootClass> getClasses() {
    return classes;
  }

  /* The four following chains are mutually disjoint. */

  /**
   * Returns a chain of the application classes in this scene. These classes are the ones which can be freely analysed &
   * modified.
   */
  public Chain<SootClass> getApplicationClasses() {
    return applicationClasses;
  }

  /**
   * Returns a chain of the library classes in this scene. These classes can be analysed but not modified.
   */
  public Chain<SootClass> getLibraryClasses() {
    return libraryClasses;
  }

  /**
   * Returns a chain of the phantom classes in this scene. These classes are referred to by other classes, but cannot be
   * loaded.
   */
  public Chain<SootClass> getPhantomClasses() {
    return phantomClasses;
  }

  Chain<SootClass> getContainingChain(SootClass c) {
    if (c.isApplicationClass()) {
      return getApplicationClasses();
    } else if (c.isLibraryClass()) {
      return getLibraryClasses();
    } else if (c.isPhantomClass()) {
      return getPhantomClasses();
    } else {
      return null;
    }
  }

  /** ************************************************************************* */
  /** Retrieves the active side-effect analysis */
  public SideEffectAnalysis getSideEffectAnalysis() {
    SideEffectAnalysis temp = this.activeSideEffectAnalysis;
    if (temp == null) {
      temp = new SideEffectAnalysis(getPointsToAnalysis(), getCallGraph());
      this.activeSideEffectAnalysis = temp;
    }
    return temp;
  }

  /** Sets the active side-effect analysis */
  public void setSideEffectAnalysis(SideEffectAnalysis sea) {
    activeSideEffectAnalysis = sea;
  }

  public boolean hasSideEffectAnalysis() {
    return activeSideEffectAnalysis != null;
  }

  public void releaseSideEffectAnalysis() {
    activeSideEffectAnalysis = null;
  }

  /** ************************************************************************* */
  /** Retrieves the active pointer analysis */
  public PointsToAnalysis getPointsToAnalysis() {
    PointsToAnalysis temp = this.activePointsToAnalysis;
    if (temp == null) {
      return DumbPointerAnalysis.v();
    }
    return temp;
  }

  /** Sets the active pointer analysis */
  public void setPointsToAnalysis(PointsToAnalysis pa) {
    activePointsToAnalysis = pa;
  }

  public boolean hasPointsToAnalysis() {
    return activePointsToAnalysis != null;
  }

  public void releasePointsToAnalysis() {
    activePointsToAnalysis = null;
  }

  /** ************************************************************************* */
  /** Retrieves the active client accessibility oracle */
  public ClientAccessibilityOracle getClientAccessibilityOracle() {
    ClientAccessibilityOracle temp = this.accessibilityOracle;
    if (temp == null) {
      return PublicAndProtectedAccessibility.v();
    }
    return temp;
  }

  public boolean hasClientAccessibilityOracle() {
    return accessibilityOracle != null;
  }

  public void setClientAccessibilityOracle(ClientAccessibilityOracle oracle) {
    accessibilityOracle = oracle;
  }

  public void releaseClientAccessibilityOracle() {
    accessibilityOracle = null;
  }

  /** ************************************************************************* */
  /** Makes a new fast hierarchy is none is active, and returns the active fast hierarchy. */
  public synchronized FastHierarchy getOrMakeFastHierarchy() {
    FastHierarchy temp = this.activeFastHierarchy;
    if (temp == null) {
      temp = new FastHierarchy();
      this.activeFastHierarchy = temp;
    }
    return temp;
  }

  /** Retrieves the active fast hierarchy */
  public synchronized FastHierarchy getFastHierarchy() {
    FastHierarchy temp = this.activeFastHierarchy;
    if (temp == null) {
      throw new RuntimeException("no active FastHierarchy present for scene");
    }
    return temp;
  }

  /** Sets the active hierarchy */
  public synchronized void setFastHierarchy(FastHierarchy hierarchy) {
    activeFastHierarchy = hierarchy;
  }

  public synchronized boolean hasFastHierarchy() {
    return activeFastHierarchy != null;
  }

  public synchronized void releaseFastHierarchy() {
    activeFastHierarchy = null;
  }

  /** ************************************************************************* */
  /** Retrieves the active hierarchy */
  public synchronized Hierarchy getActiveHierarchy() {
    Hierarchy temp = this.activeHierarchy;
    if (temp == null) {
      temp = new Hierarchy();
      this.activeHierarchy = temp;
    }
    return temp;
  }

  /** Sets the active hierarchy */
  public synchronized void setActiveHierarchy(Hierarchy hierarchy) {
    activeHierarchy = hierarchy;
  }

  public synchronized boolean hasActiveHierarchy() {
    return activeHierarchy != null;
  }

  public synchronized void releaseActiveHierarchy() {
    activeHierarchy = null;
  }

  public boolean hasCustomEntryPoints() {
    return entryPoints != null;
  }

  /** Get the set of entry points that are used to build the call graph. */
  public List<SootMethod> getEntryPoints() {
    List<SootMethod> temp = this.entryPoints;
    if (temp == null) {
      temp = EntryPoints.v().all();
      this.entryPoints = temp;
    }
    return temp;
  }

  /** Change the set of entry point methods used to build the call graph. */
  public void setEntryPoints(List<SootMethod> entryPoints) {
    this.entryPoints = entryPoints;
  }

  public ContextSensitiveCallGraph getContextSensitiveCallGraph() {
    ContextSensitiveCallGraph temp = this.cscg;
    if (temp == null) {
      throw new RuntimeException("No context-sensitive call graph present in Scene. You can bulid one with Paddle.");
    }
    return temp;
  }

  public void setContextSensitiveCallGraph(ContextSensitiveCallGraph cscg) {
    this.cscg = cscg;
  }

  public CallGraph getCallGraph() {
    CallGraph temp = activeCallGraph;
    if (temp == null) {
      throw new RuntimeException("No call graph present in Scene. Maybe you want Whole Program mode (-w).");
    }
    return temp;
  }

  public void setCallGraph(CallGraph cg) {
    this.reachableMethods = null;
    this.activeCallGraph = cg;
  }

  public boolean hasCallGraph() {
    return activeCallGraph != null;
  }

  public void releaseCallGraph() {
    this.activeCallGraph = null;
    this.reachableMethods = null;
  }

  public ReachableMethods getReachableMethods() {
    ReachableMethods temp = this.reachableMethods;
    if (temp == null) {
      temp = new ReachableMethods(getCallGraph(), new ArrayList<MethodOrMethodContext>(getEntryPoints()));
      this.reachableMethods = temp;
    }
    temp.update();
    return temp;
  }

  public void setReachableMethods(ReachableMethods rm) {
    reachableMethods = rm;
  }

  public boolean hasReachableMethods() {
    return reachableMethods != null;
  }

  public void releaseReachableMethods() {
    reachableMethods = null;
  }

  public boolean getPhantomRefs() {
    // if( !Options.v().allow_phantom_refs() ) return false;
    // return allowsPhantomRefs;
    return Options.v().allow_phantom_refs();
  }

  public void setPhantomRefs(boolean value) {
    allowsPhantomRefs = value;
  }

  public boolean allowsPhantomRefs() {
    return getPhantomRefs();
  }

  public Numberer<Kind> kindNumberer() {
    return kindNumberer;
  }

  public IterableNumberer<Type> getTypeNumberer() {
    return typeNumberer;
  }

  public IterableNumberer<SootMethod> getMethodNumberer() {
    return methodNumberer;
  }

  public Numberer<Context> getContextNumberer() {
    return contextNumberer;
  }

  public Numberer<Unit> getUnitNumberer() {
    return unitNumberer;
  }

  public Numberer<SparkField> getFieldNumberer() {
    return fieldNumberer;
  }

  public IterableNumberer<SootClass> getClassNumberer() {
    return classNumberer;
  }

  public StringNumberer getSubSigNumberer() {
    return subSigNumberer;
  }

  public IterableNumberer<Local> getLocalNumberer() {
    return localNumberer;
  }

  public void setContextNumberer(Numberer<Context> n) {
    if (contextNumberer != null) {
      throw new RuntimeException("Attempt to set context numberer when it is already set.");
    }
    contextNumberer = n;
  }

  /**
   * Returns the {@link ThrowAnalysis} to be used by default when constructing CFGs which include exceptional control flow.
   *
   * @return the default {@link ThrowAnalysis}
   */
  public ThrowAnalysis getDefaultThrowAnalysis() {
    if (defaultThrowAnalysis == null) {
      switch (Options.v().throw_analysis()) {
        case Options.throw_analysis_pedantic:
          defaultThrowAnalysis = PedanticThrowAnalysis.v();
          break;
        case Options.throw_analysis_unit:
          defaultThrowAnalysis = UnitThrowAnalysis.v();
          break;
        case Options.throw_analysis_dalvik:
          defaultThrowAnalysis = DalvikThrowAnalysis.v();
          break;
        case Options.throw_analysis_auto_select:
          if (Options.v().src_prec() == Options.src_prec_apk) {
            defaultThrowAnalysis = DalvikThrowAnalysis.v();
          } else {
            defaultThrowAnalysis = UnitThrowAnalysis.v();
          }
          break;
        default:
          throw new IllegalStateException("Options.v().throw_analysis() == " + Options.v().throw_analysis());
      }
    }
    return defaultThrowAnalysis;
  }

  /**
   * Sets the {@link ThrowAnalysis} to be used by default when constructing CFGs which include exceptional control flow.
   *
   * @param ta
   *          the default {@link ThrowAnalysis}.
   */
  public void setDefaultThrowAnalysis(ThrowAnalysis ta) {
    defaultThrowAnalysis = ta;
  }

  private void setReservedNames() {
    Set<String> rn = this.reservedNames;
    rn.add("newarray");
    rn.add("newmultiarray");
    rn.add("nop");
    rn.add("ret");
    rn.add("specialinvoke");
    rn.add("staticinvoke");
    rn.add("tableswitch");
    rn.add("virtualinvoke");
    rn.add("null_type");
    rn.add("unknown");
    rn.add("cmp");
    rn.add("cmpg");
    rn.add("cmpl");
    rn.add("entermonitor");
    rn.add("exitmonitor");
    rn.add("interfaceinvoke");
    rn.add("lengthof");
    rn.add("lookupswitch");
    rn.add("neg");
    rn.add("if");
    rn.add("abstract");
    rn.add("annotation");
    rn.add("boolean");
    rn.add("break");
    rn.add("byte");
    rn.add("case");
    rn.add("catch");
    rn.add("char");
    rn.add("class");
    rn.add("enum");
    rn.add("final");
    rn.add("native");
    rn.add("public");
    rn.add("protected");
    rn.add("private");
    rn.add("static");
    rn.add("synchronized");
    rn.add("transient");
    rn.add("volatile");
    rn.add("interface");
    rn.add("void");
    rn.add("short");
    rn.add("int");
    rn.add("long");
    rn.add("float");
    rn.add("double");
    rn.add("extends");
    rn.add("implements");
    rn.add("breakpoint");
    rn.add("default");
    rn.add("goto");
    rn.add("instanceof");
    rn.add("new");
    rn.add("return");
    rn.add("throw");
    rn.add("throws");
    rn.add("null");
    rn.add("from");
    rn.add("to");
    rn.add("with");
    rn.add("cls");
    rn.add("dynamicinvoke");
    rn.add("strictfp");
  }

  private void addSootBasicClasses() {
    basicclasses[SootClass.HIERARCHY] = new HashSet<String>();
    basicclasses[SootClass.SIGNATURES] = new HashSet<String>();
    basicclasses[SootClass.BODIES] = new HashSet<String>();

    addBasicClass("java.lang.Object");
    addBasicClass("java.lang.Class", SootClass.SIGNATURES);

    addBasicClass("java.lang.Void", SootClass.SIGNATURES);
    addBasicClass("java.lang.Boolean", SootClass.SIGNATURES);
    addBasicClass("java.lang.Byte", SootClass.SIGNATURES);
    addBasicClass("java.lang.Character", SootClass.SIGNATURES);
    addBasicClass("java.lang.Short", SootClass.SIGNATURES);
    addBasicClass("java.lang.Integer", SootClass.SIGNATURES);
    addBasicClass("java.lang.Long", SootClass.SIGNATURES);
    addBasicClass("java.lang.Float", SootClass.SIGNATURES);
    addBasicClass("java.lang.Double", SootClass.SIGNATURES);
    addBasicClass("java.lang.Number", SootClass.SIGNATURES);

    addBasicClass("java.lang.String");
    addBasicClass("java.lang.StringBuffer", SootClass.SIGNATURES);
    addBasicClass("java.lang.Enum", SootClass.SIGNATURES);

    addBasicClass("java.lang.Error");
    addBasicClass("java.lang.AssertionError", SootClass.SIGNATURES);
    addBasicClass("java.lang.Throwable", SootClass.SIGNATURES);
    addBasicClass("java.lang.Exception", SootClass.SIGNATURES);
    addBasicClass("java.lang.NoClassDefFoundError", SootClass.SIGNATURES);
    addBasicClass("java.lang.ReflectiveOperationException", SootClass.SIGNATURES);
    addBasicClass("java.lang.ExceptionInInitializerError");
    addBasicClass("java.lang.RuntimeException");
    addBasicClass("java.lang.ClassNotFoundException");
    addBasicClass("java.lang.ArithmeticException");
    addBasicClass("java.lang.ArrayStoreException");
    addBasicClass("java.lang.ClassCastException");
    addBasicClass("java.lang.IllegalMonitorStateException");
    addBasicClass("java.lang.IndexOutOfBoundsException");
    addBasicClass("java.lang.ArrayIndexOutOfBoundsException");
    addBasicClass("java.lang.NegativeArraySizeException");
    addBasicClass("java.lang.NullPointerException", SootClass.SIGNATURES);
    addBasicClass("java.lang.InstantiationError");
    addBasicClass("java.lang.InternalError");
    addBasicClass("java.lang.OutOfMemoryError");
    addBasicClass("java.lang.StackOverflowError");
    addBasicClass("java.lang.UnknownError");
    addBasicClass("java.lang.ThreadDeath");
    addBasicClass("java.lang.ClassCircularityError");
    addBasicClass("java.lang.ClassFormatError");
    addBasicClass("java.lang.IllegalAccessError");
    addBasicClass("java.lang.IncompatibleClassChangeError");
    addBasicClass("java.lang.LinkageError");
    addBasicClass("java.lang.VerifyError");
    addBasicClass("java.lang.NoSuchFieldError");
    addBasicClass("java.lang.AbstractMethodError");
    addBasicClass("java.lang.NoSuchMethodError");
    addBasicClass("java.lang.UnsatisfiedLinkError");

    addBasicClass("java.lang.Thread");
    addBasicClass("java.lang.Runnable");
    addBasicClass("java.lang.Cloneable");

    addBasicClass("java.io.Serializable");

    addBasicClass("java.lang.ref.Finalizer");

    addBasicClass("java.lang.invoke.LambdaMetafactory");
  }

  public void addBasicClass(String name) {
    addBasicClass(name, SootClass.HIERARCHY);
  }

  public void addBasicClass(String name, int level) {
    basicclasses[level].add(name);
  }

  /**
   * Load just the set of basic classes soot needs, ignoring those specified on the command-line. You don't need to use both
   * this and {@link #loadNecessaryClasses()}, though it will only waste time.
   */
  public void loadBasicClasses() {
    addReflectionTraceClasses();

    int loadedClasses = 0;
    for (int i = SootClass.BODIES; i >= SootClass.HIERARCHY; i--) {
      for (String name : basicclasses[i]) {
        SootClass basicClass = tryLoadClass(name, i);
        if (basicClass != null && !basicClass.isPhantom()) {
          loadedClasses++;
        }
      }
    }
    if (loadedClasses == 0) {
      // Missing basic classes means no Exceptions could be loaded and no Exception hierarchy can
      // lead to non-deterministic Jimple code generation: catch blocks may be removed because of
      // non-existing Exception hierarchy.
      throw new RuntimeException("None of the basic classes could be loaded! Check your Soot class path!");
    }
  }

  public Set<String> getBasicClasses() {
    Set<String> all = new HashSet<String>();
    for (int i = SootClass.BODIES; i >= SootClass.HIERARCHY; i--) {
      all.addAll(basicclasses[i]);
    }
    return all;
  }

  public boolean isBasicClass(String className) {
    for (int i = SootClass.BODIES; i >= SootClass.HIERARCHY; i--) {
      if (basicclasses[i].contains(className)) {
        return true;
      }
    }
    return false;
  }

  protected void addReflectionTraceClasses() {
    Set<String> classNames = new HashSet<String>();

    CGOptions options = new CGOptions(PhaseOptions.v().getPhaseOptions("cg"));
    String log = options.reflection_log();
    if (log != null && !log.isEmpty()) {
      String line = null;
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(log)))) {
        while ((line = reader.readLine()) != null) {
          if (!line.isEmpty()) {
            String[] portions = line.split(";", -1);
            String kind = portions[0];
            String target = portions[1];
            String source = portions[2];
            classNames.add(source.substring(0, source.lastIndexOf('.')));
            switch (kind) {
              case "Class.forName":
                classNames.add(target);
                break;
              case "Class.newInstance":
                classNames.add(target);
                break;
              case "Method.invoke":
              case "Constructor.newInstance":
                classNames.add(signatureToClass(target));
                break;
              case "Field.set*":
              case "Field.get*":
                classNames.add(signatureToClass(target));
                break;
              default:
                throw new RuntimeException("Unknown entry kind: " + kind);
            }
          }
        }
      } catch (Exception e) {
        throw new RuntimeException("Line: '" + line + "'", e);
      }
    }

    for (String c : classNames) {
      addBasicClass(c, SootClass.BODIES);
    }
  }

  public Collection<SootClass> dynamicClasses() {
    List<SootClass> temp = dynamicClasses;
    if (temp == null) {
      throw new IllegalStateException("Have to call loadDynamicClasses() first!");
    }
    return temp;
  }

  protected void loadNecessaryClass(String name) {
    loadClassAndSupport(name).setApplicationClass();
  }

  /**
   * Load the set of classes that soot needs, including those specified on the command-line. This is the standard way of
   * initialising the list of classes soot should use.
   */
  public void loadNecessaryClasses() {
    loadBasicClasses();

    final Options opts = Options.v();
    for (String name : opts.classes()) {
      loadNecessaryClass(name);
    }

    loadDynamicClasses();

    if (opts.oaat()) {
      if (opts.process_dir().isEmpty()) {
        throw new IllegalArgumentException("If switch -oaat is used, then also -process-dir must be given.");
      }
    } else {
      for (String path : opts.process_dir()) {
        for (String cl : SourceLocator.v().getClassesUnder(path)) {
          SootClass theClass = loadClassAndSupport(cl);
          if (!theClass.isPhantom) {
            theClass.setApplicationClass();
          }
        }
      }
    }

    prepareClasses();
    setDoneResolving();
  }

  public void loadDynamicClasses() {
    final ArrayList<SootClass> dynamicClasses = new ArrayList<SootClass>();
    final Options opts = Options.v();

    final HashSet<String> temp = new HashSet<String>(opts.dynamic_class());

    final SourceLocator sloc = SourceLocator.v();
    for (String path : opts.dynamic_dir()) {
      temp.addAll(sloc.getClassesUnder(path));
    }

    for (String pkg : opts.dynamic_package()) {
      temp.addAll(sloc.classesInDynamicPackage(pkg));
    }

    for (String className : temp) {
      dynamicClasses.add(loadClassAndSupport(className));
    }

    // remove non-concrete classes that may accidentally have been loaded
    for (Iterator<SootClass> iterator = dynamicClasses.iterator(); iterator.hasNext();) {
      SootClass c = iterator.next();
      if (!c.isConcrete()) {
        if (opts.verbose()) {
          logger.warn("dynamic class " + c.getName() + " is abstract or an interface, and it will not be considered.");
        }
        iterator.remove();
      }
    }
    this.dynamicClasses = dynamicClasses;
  }

  /**
   * Generate classes to process, adding or removing package marked by command line options.
   */
  protected void prepareClasses() {
    final List<String> optionsClasses = Options.v().classes();
    // Remove/add all classes from packageInclusionMask as per -i option
    Chain<SootClass> processedClasses = new HashChain<SootClass>();
    while (true) {
      Chain<SootClass> unprocessedClasses = new HashChain<SootClass>(getClasses());
      unprocessedClasses.removeAll(processedClasses);
      if (unprocessedClasses.isEmpty()) {
        break;
      }
      processedClasses.addAll(unprocessedClasses);
      for (SootClass s : unprocessedClasses) {
        if (s.isPhantom()) {
          continue;
        }
        if (Options.v().app()) {
          s.setApplicationClass();
        }
        if (optionsClasses.contains(s.getName())) {
          s.setApplicationClass();
          continue;
        }
        if (s.isApplicationClass() && isExcluded(s)) {
          s.setLibraryClass();
        }
        if (isIncluded(s)) {
          s.setApplicationClass();
        }
        if (s.isApplicationClass()) {
          // make sure we have the support
          loadClassAndSupport(s.getName());
        }
      }
    }
  }

  public boolean isExcluded(SootClass sc) {
    return isExcluded(sc.getName());
  }

  public boolean isExcluded(String className) {
    for (String pkg : excludedPackages) {
      if (className.equals(pkg)
          || ((pkg.endsWith(".*") || pkg.endsWith("$*")) && className.startsWith(pkg.substring(0, pkg.length() - 1)))) {
        return !isIncluded(className);
      }
    }
    return false;
  }

  public boolean isIncluded(SootClass sc) {
    return isIncluded(sc.getName());
  }

  public boolean isIncluded(String className) {
    for (String pkg : Options.v().include()) {
      if (className.equals(pkg)
          || ((pkg.endsWith(".*") || pkg.endsWith("$*")) && className.startsWith(pkg.substring(0, pkg.length() - 1)))) {
        return true;
      }
    }
    return false;
  }

  public void setPkgList(List<String> list) {
    pkgList = list;
  }

  public List<String> getPkgList() {
    return pkgList;
  }

  /** Create an unresolved reference to a method. */
  public SootMethodRef makeMethodRef(SootClass declaringClass, String name, List<Type> parameterTypes, Type returnType,
      boolean isStatic) {
    if (PolymorphicMethodRef.handlesClass(declaringClass)) {
      return new PolymorphicMethodRef(declaringClass, name, parameterTypes, returnType, isStatic);
    } else {
      return new SootMethodRefImpl(declaringClass, name, parameterTypes, returnType, isStatic);
    }
  }

  /** Create an unresolved reference to a constructor. */
  public SootMethodRef makeConstructorRef(SootClass declaringClass, List<Type> parameterTypes) {
    return makeMethodRef(declaringClass, SootMethod.constructorName, parameterTypes, VoidType.v(), false);
  }

  /** Create an unresolved reference to a field. */
  public SootFieldRef makeFieldRef(SootClass declaringClass, String name, Type type, boolean isStatic) {
    return new AbstractSootFieldRef(declaringClass, name, type, isStatic);
  }

  /** Returns the list of SootClasses that have been resolved at least to the level specified. */
  public List<SootClass> getClasses(int desiredLevel) {
    List<SootClass> ret = new ArrayList<SootClass>();
    for (SootClass cl : getClasses()) {
      if (cl.resolvingLevel() >= desiredLevel) {
        ret.add(cl);
      }
    }
    return ret;
  }

  public boolean doneResolving() {
    return doneResolving;
  }

  public void setDoneResolving() {
    doneResolving = true;
  }

  void setResolving(boolean value) {
    doneResolving = value;
  }

  public void setMainClassFromOptions() {
    if (mainClass == null) {
      String optsMain = Options.v().main_class();
      if (optsMain != null && !optsMain.isEmpty()) {
        setMainClass(getSootClass(optsMain));
      } else {
        final List<Type> mainArgs = Collections.singletonList(ArrayType.v(RefType.v("java.lang.String"), 1));
        // try to infer a main class from the command line if none is given
        for (String next : Options.v().classes()) {
          SootClass c = getSootClass(next);
          if (c.declaresMethod("main", mainArgs, VoidType.v())) {
            logger.debug("No main class given. Inferred '" + c.getName() + "' as main class.");
            setMainClass(c);
            return;
          }
        }

        // try to infer a main class from the usual classpath if none is given
        for (SootClass c : getApplicationClasses()) {
          if (c.declaresMethod("main", mainArgs, VoidType.v())) {
            logger.debug("No main class given. Inferred '" + c.getName() + "' as main class.");
            setMainClass(c);
            return;
          }
        }
      }
    }
  }

  /**
   * This method returns true when in incremental build mode. Other classes can query this flag and change the way in which
   * they use the Scene, depending on the flag's value.
   */
  public boolean isIncrementalBuild() {
    return incrementalBuild;
  }

  public void initiateIncrementalBuild() {
    this.incrementalBuild = true;
  }

  public void incrementalBuildFinished() {
    this.incrementalBuild = false;
  }

  /**
   * Forces Soot to resolve the class with the given name to the given level, even if resolving has actually already
   * finished.
   */
  public SootClass forceResolve(String className, int level) {
    boolean tmp = doneResolving;
    doneResolving = false;
    SootClass c;
    try {
      c = SootResolver.v().resolveClass(className, level);
    } finally {
      doneResolving = tmp;
    }
    return c;
  }

  public SootClass makeSootClass(String name) {
    return new SootClass(name);
  }

  public SootClass makeSootClass(String name, int modifiers) {
    return new SootClass(name, modifiers);
  }

  public SootMethod makeSootMethod(String name, List<Type> parameterTypes, Type returnType) {
    return new SootMethod(name, parameterTypes, returnType);
  }

  public SootMethod makeSootMethod(String name, List<Type> parameterTypes, Type returnType, int modifiers) {
    return new SootMethod(name, parameterTypes, returnType, modifiers);
  }

  public SootMethod makeSootMethod(String name, List<Type> parameterTypes, Type returnType, int modifiers,
      List<SootClass> thrownExceptions) {
    return new SootMethod(name, parameterTypes, returnType, modifiers, thrownExceptions);
  }

  public SootField makeSootField(String name, Type type, int modifiers) {
    return new SootField(name, type, modifiers);
  }

  public SootField makeSootField(String name, Type type) {
    return new SootField(name, type);
  }

  public RefType getOrAddRefType(String refTypeName) {
    return nameToClass.computeIfAbsent(refTypeName, k -> new RefType(k));
  }

  /**
   * <b>SOOT USERS: DO NOT CALL THIS METHOD!</b>
   *
   * <p>
   * This method is a Soot-internal factory method for generating callgraph objects. It creates non-initialized object that
   * must then be initialized by a callgraph algorithm
   *
   * @return A new callgraph empty object
   */
  public CallGraph internalMakeCallGraph() {
    return new CallGraph();
  }
}
