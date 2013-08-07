/*
 * #%L
 * it-xcode-maven-plugin
 * %%
 * Copyright (C) 2012 SAP AG
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.sap.prd.mobile.ios.mios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.maven.it.Verifier;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Writer;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public abstract class XCodeTest
{

  static final String PROP_NAME_DEPLOY_REPO_DIR = "deployrepo.directory";
  static final String PROP_NAME_FRWK_REPO_DIR = "frwkrepo.directory";
  static final String PROP_NAME_ZIP_REPO_DIR = "ziprepo.directory";
  static final String PROP_NAME_DYNAMIC_VERSION = "dynamicVersion";
  static final String PROP_NAME_FRAMEWORK_VERSION = "frameworkVersion";

  private static File localRepo = null;
  private static String activeProfiles = null;

  /**
   * If VM is started in debug mode the forked Maven processes are started in debug mode as well and
   * stay suspended until the developer connects.
   */
  private static boolean debug = java.lang.management.ManagementFactory
    .getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
  private static int debugPort = 8000;
  private static boolean debugSuspend = true;

  @Rule
  public static TemporaryFolder tmpFolder = new TemporaryFolder();

  @BeforeClass
  public final static void setup() throws IOException, XmlPullParserException
  {
    prepareTestExecutionSettingsFile();
    prepareTestExecutionActiveProfiles();
    setupLocalRepo();
  }

  //
  // Works only when called directly by a junit test method. Otherwise getStrackTrace()[2] below is missleading.
  //
  protected String getTestName()
  {
    return getClass().getName() + File.separator + Thread.currentThread().getStackTrace()[2].getMethodName();
  }

  /**
   * Enables/disables test debugging as Remote Java Application on port 8000. VM will be suspended
   * until connected.
   * 
   * @param enable
   *          true enables, false disables debugging
   */
  protected static void setDebugging(boolean enable)
  {
    setDebugging(enable, 8000, true);
  }

  /**
   * Enables/disables test debugging as Remote Java Application.
   * 
   * @param enable
   *          true enables, false disables debugging
   * @param port
   *          debug port to be used
   * @param suspend
   *          if true VM will be suspended until connected
   */
  protected static void setDebugging(boolean enable, int port, boolean suspend)
  {
    debug = enable;
    debugPort = port;
    debugSuspend = suspend;
  }

  private static void prepareTestExecutionActiveProfiles()
  {
    String _activeProfiles = System.getProperty("com.sap.maven.integration-tests.active-profiles");

    if (_activeProfiles != null && !_activeProfiles.trim().isEmpty())
    {
      activeProfiles = _activeProfiles.trim();
      System.out.println("[INFO] Using active profiles: " + activeProfiles);
    }
  }

  private static void setupLocalRepo()
  {
    //
    // The local repo used during the integration tests must be the same than the local repo used for
    // building the xcode-maven-plugin previously in the build. Otherwise the binary version of the plugin
    // cannot be found or a wrong version will be found.
    // Since the base directory for building the xcode-maven-plugin and the base directory used for the
    // integration tests differs the corresponding path must be absolute.
    //

    String localRepoFilePath = System.getProperty("com.sap.maven.integration-tests.local-repo");

    if (localRepoFilePath == null || localRepoFilePath.isEmpty()) {

      localRepoFilePath = System.getProperty("user.home") + "/.m2/repository";

      System.out
        .println("[WARNING] Local Repository has not been provided. Please provide the local repo with \"-Dmaven.repo.local=\". "
              +
              "Defaulting to '" + localRepoFilePath + "'.");
    }

    localRepo = new File(localRepoFilePath);

    if (!localRepo.isAbsolute())
      throw new RuntimeException("The path to the local repository '" + localRepoFilePath + "' is not absolute. " +
            "Integration tests will only work reliably if that path is absolute.");

    System.out.println("[INFO] Using local repository '" + localRepo + "'.");
  }

  private static void prepareTestExecutionSettingsFile() throws IOException, XmlPullParserException
  {

    //
    // The settings file used for integration tests must be the same than the settings file used for the previously performed
    // build of the xcode-maven-plugin. Since the base directory for building the xcode-maven-plugin and the base directory for the
    // integration tests differs the corresponding path must be absolute.
    // For more details see the comment inside the configuration of the surefire plugin in the pom file of the integration tests.
    //

    String userSettingsFilePath = System.getProperty("com.sap.maven.integration-tests.user-settings");

    if (userSettingsFilePath == null || userSettingsFilePath.isEmpty())
    {
      userSettingsFilePath = System.getProperty("user.home") + "/.m2/settings.xml";

      System.out.println("[WARNING] No settings file has been provided. Please provide the user settings file used " +
            "for integration tests with \"-Dcom.sap.maven.integration-tests.user-settings=\"." +
            "Defaulting to user settings file located at '" + userSettingsFilePath + "'");
    }
    final File userSettingsFile = new File(userSettingsFilePath);

    if (!userSettingsFile.isAbsolute())
      throw new RuntimeException("The path to the user settings file '" + userSettingsFilePath + "' is not absolute. " +
            "Integration tests will only work reliably if that path is absolute.");

    System.out.println("[INFO] Using settings file '" + userSettingsFile + "' for integration tests");

    final File testExecutionSettingsFile = getTestExectutionSettingsFile();

    if (testExecutionSettingsFile.exists())
      return;

    if (!testExecutionSettingsFile.getParentFile().exists() && !testExecutionSettingsFile.getParentFile().mkdirs())
      throw new IOException("Cannot create " + testExecutionSettingsFile.getParentFile());

    final Reader r = new InputStreamReader(new FileInputStream(userSettingsFile), "UTF-8");
    final Writer w = new OutputStreamWriter(new FileOutputStream(testExecutionSettingsFile), "UTF-8");

    try {

      Settings settings = new SettingsXpp3Reader().read(r);
      List<Mirror> mirrors = settings.getMirrors();

      for (Mirror m : mirrors) {

        StringBuilder newMirrorOf = new StringBuilder(256);

        for (String mirror : m.getMirrorOf().split(",")) {

          if (newMirrorOf.length() > 0)
            newMirrorOf.append(",");

          if ("*".equals(mirror))
            newMirrorOf.append("external:").append(mirror);
          else
            newMirrorOf.append(mirror);
        }

        m.setMirrorOf(newMirrorOf.toString());
      }

      new SettingsXpp3Writer().write(w, settings);
      System.out.println("[INFO] User settings file written to '" + testExecutionSettingsFile + "'.");

    }
    finally {
      IOUtils.closeQuietly(r);
      IOUtils.closeQuietly(w);
    }
  }

  private static File getTestExectutionSettingsFile() throws IOException
  {
    return new File(getTestsExecutionDirectory(), "settings.xml").getCanonicalFile();
  }

  protected final static Map<String, String> THE_EMPTY_MAP = Collections.emptyMap();
  protected final static List<String> THE_EMPTY_LIST = Collections.emptyList();

  protected static Verifier test(final String testName, final File projectDirectory,
        final String target, List<String> additionalCommandLineOptions,
        Map<String, String> additionalSystemProperties, Properties pomReplacements, ProjectModifier modifier)
        throws Exception
  {
    return test(null, testName, projectDirectory, target, additionalCommandLineOptions,
          additionalSystemProperties, pomReplacements, modifier);
  }

  protected static Verifier test(final Verifier _verifier, final String testName, final File projectDirectory,
        final String target, List<String> additionalCommandLineOptions,
        Map<String, String> additionalSystemProperties, Properties pomReplacements, ProjectModifier modifier)
        throws Exception
  {
    return test(new XCodeTestParameters(_verifier, testName, projectDirectory, Arrays.asList(new String[] { target }),
          additionalCommandLineOptions, additionalSystemProperties, pomReplacements, modifier));
  }

  protected static Verifier test(XCodeTestParameters params)
        throws Exception
  {

    final PrintStream originalOut = System.out;

    if (params.additionalSystemProperties == null) {
      params.additionalSystemProperties = new HashMap<String, String>();
    }

    final String projectName = params.projectDirectory.getName();

    final Verifier verifier;
    final File testExecutionFolder;

    if (params._verifier != null) {
      testExecutionFolder = new File(params._verifier.getBasedir()).getCanonicalFile();
      verifier = params._verifier;
    }
    else {
      testExecutionFolder = getTestExecutionDirectory(params.testName, projectName);
      verifier = new Verifier(testExecutionFolder.getAbsolutePath());
    }

    prepareTestExectutionFolder(params.projectDirectory, testExecutionFolder);

    params.modifier.setTestExecutionDirectory(testExecutionFolder);
    params.modifier.execute();

    rewritePom(new File(testExecutionFolder, "pom.xml"), params.pomReplacements);

    try {

      final Properties testSystemProperties = filterProperties(System
        .getProperties());

      testSystemProperties.putAll(params.additionalSystemProperties);

      System.out
        .println("SystemProperties used during integration test for '" + params.testName + "/" + projectName + "': \n"
              + testSystemProperties);

      final List<String> commandLineOptions = new ArrayList<String>();

      HashMap<Object, Object> envVars = new HashMap<Object, Object>();
      if (debug) {
        envVars.put("MAVEN_OPTS", "-Xdebug -Xnoagent -Xrunjdwp:server=y,transport=dt_socket,address=" + debugPort
              + ",suspend=" + (debugSuspend ? "y" : "n"));
      }

      {
        commandLineOptions.add("-s");
        commandLineOptions.add(getTestExectutionSettingsFile().getAbsolutePath());
      }

      if (localRepo != null)
      {
        commandLineOptions.add("-Dmaven.repo.local=" + localRepo.getAbsolutePath());
      }

      if (activeProfiles != null && !activeProfiles.trim().isEmpty())
      {
        commandLineOptions.add("-P");
        commandLineOptions.add(activeProfiles);

      }

      if (params.additionalCommandLineOptions != null)
        commandLineOptions.addAll(params.additionalCommandLineOptions);

      verifier.setCliOptions(commandLineOptions);

      verifier.setSystemProperties(testSystemProperties);

      verifier.deleteArtifacts("com.sap.production.ios.tests");

      verifier.setAutoclean(false);
      verifier.executeGoals(params.targets, envVars);

      verifier.verifyErrorFreeLog();

      final File logFile = new File(testExecutionFolder,
            verifier.getLogFileName());

      if (!logFile.exists())
        originalOut.println("Log file '" + logFile
              + "' does not exist.");
      else
        showLog(originalOut, projectName, logFile);

    }
    finally {
      verifier.resetStreams();
      final File logFile = new File(testExecutionFolder,
            verifier.getLogFileName());

      if (!logFile.exists())
        System.out.println("Log file '" + logFile
              + "' does not exist.");
      else
        showLog(System.out, projectName, logFile);
    }
    return verifier;
  }

  protected static File getTestsExecutionDirectory()
  {
    return new File(new File(".").getAbsoluteFile(), "target/tests/");
  }

  protected static File getTestExecutionDirectory(final String testName, final String projectName)
  {
    return new File(
          getTestsExecutionDirectory(), testName + "/" + projectName);
  }

  private static void showLog(PrintStream out, final String projectName, final File logFile)
        throws FileNotFoundException, IOException
  {

    out.println();
    out.println();
    out.println();
    out.println("Log output for project \"" + projectName + "\".");
    out.println();
    out.println();
    out.println();

    InputStream log = null;

    try {

      log = new BufferedInputStream(new FileInputStream(logFile));

      byte[] buff = new byte[1024];

      for (int i; (i = log.read(buff)) != -1;)
        out.write(buff, 0, i);

    }
    finally {
      if (log != null)
        IOUtils.closeQuietly(log);
    }
  }

  private static void rewritePom(File pomFile, Properties pomReplacements)
        throws IOException
  {

    if (!pomReplacements.keySet().contains(PROP_NAME_DYNAMIC_VERSION))
      throw new IllegalStateException("Dynamic version not provided on pom replacements.");

    String pom = IOUtils.toString(new FileInputStream(pomFile));

    if (pom.indexOf("${" + PROP_NAME_DYNAMIC_VERSION + "}") == -1)
      throw new IllegalStateException("Dynamic version is not used in pom file (" + pomFile + ").");

    for (String key : pomReplacements.stringPropertyNames()) {
      pom = pom.replaceAll("\\$\\{" + key + "\\}", pomReplacements.getProperty(key));
    }

    pom = pom.replaceAll("\\$\\{xcode.maven.plugin.version\\}", getMavenXcodePluginVersion());

    final Writer w = new FileWriter(pomFile);
    try {
      w.write(pom);
    }
    finally {
      IOUtils.closeQuietly(w);
    }

    InputStream is = null;
    try {
      is = new FileInputStream(pomFile);
      Model model = new MavenXpp3Reader().read(is);
      List<String> modules = model.getModules();

      if (modules != null && !modules.isEmpty()) {

        for (String module : modules) {
          rewritePom(new File(pomFile.getParent(), module), pomReplacements);
        }
      }
    }
    catch (XmlPullParserException e) {
      throw new RuntimeException(e);
    }
    finally {
      IOUtils.closeQuietly(is);
    }
  }

  private static Properties filterProperties(final Properties props)
  {

    final Properties filteredProperties = new Properties();

    //
    // If we do not filter the altDeploymentRepository this repository is
    // used
    // in the test cases. This is not the intended behavior. For the
    // test cases we would like to use the repository defined in the pom.
    // On the other hand we need altDeploymentRepository in order to be able
    // to deploy to SAP nexus from the team hudson.
    //

    for (final Entry<Object, Object> entry : props.entrySet()) {
      if (entry.getKey().equals("altDeploymentRepository"))
        continue;
      filteredProperties.put(entry.getKey(), entry.getValue());
    }
    return filteredProperties;
  }

  private static void prepareTestExectutionFolder(final File source,
        final File testExecutionFolder) throws IOException
  {
    FileUtils.deleteDirectory(testExecutionFolder);
    org.apache.commons.io.FileUtils.copyDirectory(source, testExecutionFolder);
  }

  protected static File getTestRootDirectory() throws IOException
  {
    return new File(new File(".").getCanonicalFile(), "src/test/projects");
  }

  protected static File getRemoteRepositoryDirectory(final String testName) throws IOException
  {
    return new File(new File(getTargetDirectory(), "remoteRepo"), testName);
  }

  protected static void prepareRemoteRepository(final File remoteRepository)
        throws IOException
  {
    FileUtils.deleteDirectory(remoteRepository);
    if (!remoteRepository.mkdirs())
      throw new IOException("Could not create directory "
            + remoteRepository);
  }

  protected static String getMavenXcodePluginGroupId() throws IOException
  {
    return getProjectProperty("groupId");
  }

  protected static String getMavenXcodePluginArtifactId() throws IOException
  {
    return getProjectProperty("artifactId");
  }

  protected static String getMavenXcodePluginVersion() throws IOException
  {
    return getProjectProperty("version");
  }

  private static String getProjectProperty(String key) throws IOException
  {
    Properties properties = new Properties();
    properties.load(XCodeTest.class.getResourceAsStream("/misc/project.properties"));

    final String value = properties.getProperty("xcode-plugin-" + key);

    if (value.equals("${project." + key + "}"))
      throw new IllegalStateException(
            "Variable ${project." + key
                  + "} was not replaced. May be running \"mvn clean install\" beforehand might solve this issue.");
    return value;
  }

  protected static File getTargetDirectory() throws IOException
  {
    return new File(new File(".").getCanonicalFile(), "target");
  }

  /**
   * Unpacks the zipped xcodeproj and checks if it can be compiled with a direct xcodebuild command
   * in order to verify if all dependencies are present. Code sogning is disabled for these test
   * builds
   * 
   * @return the directory where the zip file has bee extracted to
   */
  protected File assertUnpackAndCompile(File xcodeprojZip) throws IOException
  {
    File tmpXcodeProjDir = tmpFolder.newFolder(xcodeprojZip.getName());
    extractFileWithShellScript(xcodeprojZip, tmpXcodeProjDir);
    assertFalse("checkout dir should not be packaged", new File(tmpXcodeProjDir, "target/checkout").isDirectory());
    int exitcode = Forker.forkProcess(System.out, new File(tmpXcodeProjDir, "src/xcode/"), "xcodebuild", "clean",
          "build", "CODE_SIGN_IDENTITY=", "CODE_SIGNING_REQUIRED=NO");

    assertEquals("Building the unpacked project failed", 0, exitcode);
    return tmpXcodeProjDir;
  }

  protected static void extractFileWithShellScript(File sourceFile, File destinationFolder)
        throws IOException
  {
    File workingDirectory = tmpFolder.newFolder("scriptWorkingDir");
    workingDirectory.deleteOnExit();
    ScriptRunner.copyAndExecuteScript(System.out, "/com/sap/prd/mobile/ios/mios/unzip.sh", workingDirectory,
          sourceFile.getCanonicalPath(), destinationFolder.getCanonicalPath());
  }

}
