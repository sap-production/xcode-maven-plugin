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
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.IOUtil;
import org.junit.Test;
import org.junit.rules.TestName;

public class XCodeLifecycleTest extends XCodeTest
{

  @Test
  private void compareFilesContainingDynamicVersions(final String dynamicVersion, File template, File versionFileLib)
        throws FileNotFoundException, IOException
  {
      String toBeTestedAgainst = IOUtils.toString(new FileInputStream(template)).replaceAll("\\$\\{dynamicVersion\\}", dynamicVersion);
      Assert.assertEquals(toBeTestedAgainst, IOUtils.toString(new FileInputStream(versionFileLib)).replaceAll("\\$\\{dynamicVersion\\}", dynamicVersion));
  }
  
  @Test
  public void testLifecycleWithSnapshotDependency() throws Exception
  {
    final String testName = getTestName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(testName);

    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, "1.0." +  String.valueOf(System.currentTimeMillis()));
    
    test(testName, new File(getTestRootDirectory(), "straight-forward-with-snapshot-dependency/MyLibrary"), "deploy",
          THE_EMPTY_LIST,
          THE_EMPTY_MAP, pomReplacements);

    Verifier verifier = test(testName, new File(getTestRootDirectory(), "straight-forward-with-snapshot-dependency/MyApp"), "deploy",
          THE_EMPTY_LIST,
          THE_EMPTY_MAP, pomReplacements);
    
    assertFalse(FileUtils.isSymbolicLink(new File(verifier.getBasedir() + "/target/libs/Release-iphoneos/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a")));
  }

  private void assertBuildEnvironmentPropertiesFile(String testName, String projectName) throws IOException,
        FileNotFoundException
  {
    File buildEnvironmentDump = new File(new File(getTestExecutionDirectory(testName, projectName), "target"),
          EffectiveBuildSettings.getBuildSettingsFileName("Release", "iphoneos"));
    assertTrue(buildEnvironmentDump.exists());
    Properties properties = new Properties();
    properties.load(new FileInputStream(buildEnvironmentDump));
    assertEquals(projectName, properties.getProperty("PRODUCT_NAME"));
  }

  @SuppressWarnings("resource")
  private String extractAppIdSuffixFromLogFile(File logFile) throws IOException
  {
    BufferedReader reader = new BufferedReader(new FileReader(logFile));
    try {
      String line;
      Pattern p = Pattern.compile("appIdSuffix=(\\w+)");
      while ((line = reader.readLine()) != null)
      {
        Matcher matcher = p.matcher(line);
        if (matcher.find())
        {
          return matcher.group(1);
        }
      }
    } finally {
      IOUtils.closeQuietly(reader);
    }
    return null;
  }

  
  private void compareFileContent(File expectedFile, File actualFile) throws IOException
  {
    final InputStream actualStream = new FileInputStream(actualFile.getAbsoluteFile());
    final InputStream expectedStream = new FileInputStream(expectedFile.getAbsoluteFile());

    try {
      Assert.assertEquals(String.format("File contents differ (expected file: %s, actual file: %s)",
            expectedFile.getName(), actualFile.getName()),
            IOUtil.toString(expectedStream, "UTF-8"), IOUtil.toString(actualStream, "UTF-8"));
    }
    finally {
      IOUtil.close(actualStream);
      IOUtil.close(expectedStream);
    }
  }

  @Test
  public void testSkipLibraryBuild() throws Exception
  {
    final String testName = getTestName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    additionalSystemProperties.put("xcode.appIdSuffix", "release");
    additionalSystemProperties.put("xcode.forbidLibBuild", "true");
    additionalSystemProperties.put("mios.ota-service.url", "http://apple-ota.wdf.sap.corp:8080/ota-service/HTML");

    final File projectDirectory = new File(getTestRootDirectory(), "straight-forward/MyLibrary");

    Verifier verifier = new Verifier(getTestExecutionDirectory(testName, projectDirectory.getName()).getAbsolutePath());

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, "1.0." + String.valueOf(System.currentTimeMillis()));
    
    try {
      test(verifier, testName, projectDirectory, "deploy", THE_EMPTY_LIST, additionalSystemProperties,
            pomReplacements);

      Assert.fail("Library was build instead of a failure.");
    }
    catch (VerificationException ex) {
      //
      // This exception is expected.
      // Below we check for the reason in the log file.
      //
    }

    final String message = "xcode-library or xcode-framework detected";

    try {

      verifier.verifyTextInLog(message);
    }
    catch (VerificationException ex) {
      Assert.fail("Expected log message (" + message + ") was not present.");
    }
  }

  @Test
  public void testChangeArtifactId() throws Exception
  {
    final String dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());
    final String testName = getTestName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);
    
    test(testName, new File(getTestRootDirectory(), "straight-forward/MyLibrary"), "deploy",
          THE_EMPTY_LIST,
          THE_EMPTY_MAP, pomReplacements);

    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    additionalSystemProperties.put("xcode.artifactIdSuffix", "release");
    additionalSystemProperties.put("mios.ota-service.url", "http://apple-ota.wdf.sap.corp:8080/ota-service/HTML");

    test(testName, new File(getTestRootDirectory(), "straight-forward/MyApp"), "deploy",
          THE_EMPTY_LIST,
          additionalSystemProperties, pomReplacements);

    final String configuration = "Release";

    assertTrue(new File(remoteRepositoryDirectory,
          Constants.GROUP_ID_WITH_SLASH + "/MyApp_release/" + dynamicVersion + "/MyApp_release-"
                + dynamicVersion + "-"
                + configuration + "-iphoneos.ipa").exists());

    assertTrue(new File(remoteRepositoryDirectory,
          Constants.GROUP_ID_WITH_SLASH + "/MyApp_release/" + dynamicVersion + "/MyApp_release-"
                + dynamicVersion + "-AppStoreMetadata.zip")
      .exists());
  }

  @Test
  public void testDeviantSourceDirectory() throws Exception
  {
    final String testName = getTestName();
    final String dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());
    
    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);

    test(testName, new File(getTestRootDirectory(), "deviant-source-directory/MyLibrary"), "deploy",
          THE_EMPTY_LIST,
          THE_EMPTY_MAP, pomReplacements);

    test(testName, new File(getTestRootDirectory(), "deviant-source-directory/MyApp"), "deploy",
          THE_EMPTY_LIST,
          THE_EMPTY_MAP, pomReplacements);

    // Below we use internal knowledge from the pom: when running in
    // production profile the configuration is also "Production".
    final String configuration = "Release";

    assertTrue(new File(remoteRepositoryDirectory,
          Constants.GROUP_ID_WITH_SLASH + "/MyApp/" + dynamicVersion + "/MyApp-" + dynamicVersion + "-"
                + configuration + "-iphoneos.ipa").exists());
  }

  @Test
  public void testXCodeSourceDirEqualsMavenSourceDirectory() throws Exception
  {
    final String testName = getTestName();
    final String dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());
    
    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);
    
    test(testName, new File(getTestRootDirectory(), "deviant-source-directory-2/MyLibrary"), "deploy",
          THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements);

    test(testName, new File(getTestRootDirectory(), "deviant-source-directory-2/MyApp"), "deploy",
          THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements);

    final String configuration = "Release";

    assertTrue(new File(remoteRepositoryDirectory,
          Constants.GROUP_ID_WITH_SLASH + "/MyApp/" + dynamicVersion + "/MyApp-" + dynamicVersion + "-"
                + configuration + "-iphoneos.ipa").exists());
  }

  @Test
  public void testOTAUrlIsSetToEmpty() throws Exception
  {
    final String testName = getTestName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());  
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, "1.0" + String.valueOf(System.currentTimeMillis()));
    
    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    additionalSystemProperties.put("mios.ota-service.url", "");

    test(testName, new File(getTestRootDirectory(), "straight-forward/MyLibrary"), "deploy", THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements);
    
    final File projectDirectory = new File(getTestRootDirectory(), "straight-forward/MyApp");
    Verifier verifier = new Verifier(getTestExecutionDirectory(testName, projectDirectory.getName()).getAbsolutePath());
    try {
      verifier = test(verifier, testName, new File(getTestRootDirectory(), "straight-forward/MyApp"),
            "deploy", THE_EMPTY_LIST, additionalSystemProperties, pomReplacements);

    }
    catch (VerificationException e) {
      //
      // This exception is expected.
      // Below we check for the reason in the log file.
      //
    }

    verifier.verifyTextInLog("Unable to convert '' to an URL");
    verifier.verifyTextInLog("java.net.MalformedURLException: no protocol");
  }

  @Test
  public void testOTAUrlIsNotUrl() throws Exception
  {
    final String testName = getTestName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, "1.0." + String.valueOf(System.currentTimeMillis()));
    
    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    String otaWrongURL = "htp://apple-ota.wdf.sap.corp:8080/ota-service/HTML";
    additionalSystemProperties.put("mios.ota-service.url", otaWrongURL);

    test(testName, new File(getTestRootDirectory(), "straight-forward/MyLibrary"), "deploy", THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements);
    
    final File projectDirectory = new File(getTestRootDirectory(), "straight-forward/MyApp");

    Verifier verifier = new Verifier(getTestExecutionDirectory(testName, projectDirectory.getName()).getAbsolutePath());

    try {
      test(verifier, testName, projectDirectory, "deploy",
            THE_EMPTY_LIST,
            additionalSystemProperties, pomReplacements);
    }
    catch (VerificationException ex) {
      //
      // This exception is expected.
      // Below we check for the reason in the log file.
      //
    }

    verifier.verifyTextInLog("java.net.MalformedURLException: unknown protocol: htp");
    verifier.verifyTextInLog("Unable to convert '" + otaWrongURL + "' to an URL");

  }

  @Test
  public void testInitializeTwice() throws Exception
  {
    final String testName = getTestName();
  
    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());
  
    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, "1.0." + String.valueOf(System.currentTimeMillis()));
    
    test(null, testName, new File(getTestRootDirectory(), "straight-forward/MyLibrary"), "install",
          THE_EMPTY_LIST,
          THE_EMPTY_MAP, pomReplacements);
    
    test(null, testName, new File(getTestRootDirectory(), "straight-forward/MyApp"), Arrays.asList(new String[] {"initialize", "initialize"}),
          THE_EMPTY_LIST,
          THE_EMPTY_MAP, pomReplacements);
  }    
}
