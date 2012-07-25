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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.IOUtil;
import org.junit.Test;

public class XCodeLifecycleTest extends XCodeTest
{

  @Test
  public void testLifecycle() throws Exception
  {
    final String testName = Thread.currentThread().getStackTrace()[1].getMethodName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    test(testName, new File(getTestRootDirectory(), "straight-forward/MyLibrary"), "pom.xml", "deploy",
          THE_EMPTY_LIST,
          THE_EMPTY_MAP, remoteRepositoryDirectory);

    assertBuildEnvironmentPropertiesFile(testName, "MyLibrary");
    
    final String myLibArtifactFilePrefix = Constants.GROUP_ID_WITH_SLASH + "/MyLibrary/" + Constants.LIB_VERSION
          + "/MyLibrary-" + Constants.LIB_VERSION;

    assertTrue(new File(remoteRepositoryDirectory, myLibArtifactFilePrefix + "-Release-iphoneos.headers.tar").exists());
    assertTrue(new File(remoteRepositoryDirectory, myLibArtifactFilePrefix + "-Release-iphonesimulator.headers.tar")
      .exists());
    assertTrue(new File(remoteRepositoryDirectory, myLibArtifactFilePrefix + "-Debug-iphoneos.headers.tar").exists());
    assertTrue(new File(remoteRepositoryDirectory, myLibArtifactFilePrefix + "-Debug-iphonesimulator.headers.tar")
      .exists());
    assertTrue(new File(remoteRepositoryDirectory, myLibArtifactFilePrefix + "-MyLibrary.xcode-bundle-zip").exists());
    assertTrue(new File(remoteRepositoryDirectory, myLibArtifactFilePrefix + "-MyLibrary.raw.xcode-bundle-zip").exists());
    assertTrue(new File(remoteRepositoryDirectory, myLibArtifactFilePrefix + "-Resources~Another.xcode-bundle-zip").exists());

    File versionFileLib = new File(remoteRepositoryDirectory, myLibArtifactFilePrefix + "-versions.xml");
    assertTrue(versionFileLib.exists());
    compareFileContent(new File("src/test/resources/MyLibrary-1.0.0-versions.xml"), versionFileLib);

    //set the OTA URL explicitly, do not expect it from settings.xml
    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    additionalSystemProperties.put("mios.ota-service.url", "http://apple-ota.wdf.sap.corp:8080/ota-service/HTML");
    additionalSystemProperties.put("xcode.app.defaultConfigurations", "Release");
    additionalSystemProperties.put("xcode.app.defaultSdks", "iphoneos");


    // ------------------------------------------------------------------------------------------
    // --- Now build the app
    // ------------------------------------------------------------------------------------------
    Verifier appVerifier = test(testName, new File(getTestRootDirectory(), "straight-forward/MyApp"), "pom.xml",
          "deploy",
          THE_EMPTY_LIST,
          additionalSystemProperties, remoteRepositoryDirectory);

    final String myAppVersionRepoDir = Constants.GROUP_ID_WITH_SLASH + "/MyApp/" + Constants.APP_VERSION;
    final String myAppArtifactFilePrefix = myAppVersionRepoDir + "/MyApp-" + Constants.APP_VERSION;

    // we built only the Xcode config Release for iphoneos SDK. All other combinations must not exist:
    assertTrue(new File(remoteRepositoryDirectory, myAppArtifactFilePrefix + "-Release-iphoneos.ipa").exists());
    assertFalse(new File(remoteRepositoryDirectory, myAppArtifactFilePrefix + "-Debug-iphoneos.ipa").exists());
    assertFalse(new File(remoteRepositoryDirectory, myAppArtifactFilePrefix + "-Release-iphonesimulator.ipa").exists());
    assertFalse(new File(remoteRepositoryDirectory, myAppArtifactFilePrefix + "-Debug-iphonesimulator.ipa").exists());

    
    // check if IPA file contains the file Payload/MyApp.app/ResourceRules.plist
    File extractedIpaFolder = tmpFolder.newFolder("ipa");
    extractFileWithShellScript(new File(remoteRepositoryDirectory, myAppArtifactFilePrefix + "-Release-iphoneos.ipa"),
          extractedIpaFolder);
    File extractedFile = new File(extractedIpaFolder, "Payload/MyApp.app/ResourceRules.plist");
    assertTrue(extractedFile.isFile());
    
    File appstoreUploadFile = new File(remoteRepositoryDirectory, myAppArtifactFilePrefix + "-Release-iphoneos-app.zip");
    assertTrue(appstoreUploadFile.exists());

    File appstoreFile = tmpFolder.newFolder("appstoreFile");
    extractFileWithShellScript(appstoreUploadFile, appstoreFile);

    File[] files = appstoreFile.listFiles();

    assertTrue(files.length == 1 && files[0].getName().equals("MyApp.app"));
    
    assertBuildEnvironmentPropertiesFile(testName, "MyApp");
    
    File versionsXmlInIpa = new File(extractedIpaFolder, "Payload/MyApp.app/versions.xml");
    File versionsXmlInAppZip = new File(appstoreFile, "MyApp.app/versions.xml");
    assertTrue(versionsXmlInIpa.exists());
    assertTrue(versionsXmlInAppZip.exists());
    File versionsTestFile = new File("src/test/resources/MyApp-1.0.0-versions.xml");
    compareFileContent(versionsTestFile, versionsXmlInIpa);
    compareFileContent(versionsTestFile, versionsXmlInAppZip);
    CodeSignManager.verify(new File(extractedIpaFolder, "Payload/MyApp.app"));
    CodeSignManager.verify(new File(appstoreFile, "MyApp.app"));

    final String appIdSuffix = extractAppIdSuffixFromLogFile(new File(appVerifier.getBasedir(),
          appVerifier.getLogFileName()));

    File otaHtmlFileActualRelease = new File(remoteRepositoryDirectory, myAppArtifactFilePrefix
          + "-Release-iphoneos-ota.htm");
    assertTrue(otaHtmlFileActualRelease.exists());

    final String otaFileNameSuffix = appIdSuffix == null ? "-iphoneos-ota.htm" : "-" + appIdSuffix
          + "-iphoneos-ota.htm";

    compareFileContent(new File("src/test/resources/MyApp-Release-" + Constants.APP_VERSION + otaFileNameSuffix),
          otaHtmlFileActualRelease);

    assertTrue(new File(remoteRepositoryDirectory, myAppArtifactFilePrefix + "-AppStoreMetadata.zip").exists());

    File versionFileApp = new File(remoteRepositoryDirectory, myAppArtifactFilePrefix + "-versions.xml");
    assertTrue(versionFileApp.exists());
    compareFileContent(versionsTestFile, versionFileApp);
  }

  private void assertBuildEnvironmentPropertiesFile(String testName, String projectName) throws IOException,
        FileNotFoundException
  {
    File buildEnvironmentDump = new File(new File(getTestExecutionDirectory(testName, projectName), "target"),
          XCodeAppendBuildPhaseMojo.getBuildEnvironmentFileName("Release", "iphoneos"));
    assertTrue(buildEnvironmentDump.exists());
    Properties properties = new Properties();
    properties.load(new FileInputStream(buildEnvironmentDump));
    assertEquals(projectName, properties.getProperty("PRODUCT_NAME"));
  }

  private String extractAppIdSuffixFromLogFile(File logFile) throws IOException
  {
    BufferedReader reader = new BufferedReader(new FileReader(logFile));
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
    final String testName = Thread.currentThread().getStackTrace()[1].getMethodName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    additionalSystemProperties.put("xcode.appIdSuffix", "release");
    additionalSystemProperties.put("xcode.forbidLibBuild", "true");
    additionalSystemProperties.put("mios.ota-service.url", "http://apple-ota.wdf.sap.corp:8080/ota-service/HTML");

    final File projectDirectory = new File(getTestRootDirectory(), "straight-forward/MyLibrary");

    Verifier verifier = new Verifier(getTestExecutionDirectory(testName, projectDirectory.getName()).getAbsolutePath());

    try {
      test(verifier, testName, projectDirectory, "pom.xml", "deploy", THE_EMPTY_LIST, additionalSystemProperties,
            remoteRepositoryDirectory, null);

      Assert.fail("Library was build instead of a failure.");
    }
    catch (VerificationException ex) {
      //
      // This exception is expected.
      // Below we check for the reason in the log file.
      //
    }

    final String message = "xcode-library detected";

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
    final String testName = Thread.currentThread().getStackTrace()[1].getMethodName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    test(testName, new File(getTestRootDirectory(), "straight-forward/MyLibrary"), "pom.xml", "deploy",
          THE_EMPTY_LIST,
          THE_EMPTY_MAP, remoteRepositoryDirectory);

    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    additionalSystemProperties.put("xcode.artifactIdSuffix", "release");
    additionalSystemProperties.put("mios.ota-service.url", "http://apple-ota.wdf.sap.corp:8080/ota-service/HTML");

    test(testName, new File(getTestRootDirectory(), "straight-forward/MyApp"), "pom.xml", "deploy",
          THE_EMPTY_LIST,
          additionalSystemProperties, remoteRepositoryDirectory);

    final String configuration = "Release";

    assertTrue(new File(remoteRepositoryDirectory,
          Constants.GROUP_ID_WITH_SLASH + "/MyApp~release/" + Constants.APP_VERSION + "/MyApp~release-"
                + Constants.APP_VERSION + "-"
                + configuration + "-iphoneos.ipa").exists());

    assertTrue(new File(remoteRepositoryDirectory,
          Constants.GROUP_ID_WITH_SLASH + "/MyApp~release/" + Constants.APP_VERSION + "/MyApp~release-"
                + Constants.APP_VERSION + "-AppStoreMetadata.zip")
      .exists());
  }

  @Test
  public void testDeviantSourceDirectory() throws Exception
  {
    final String testName = Thread.currentThread().getStackTrace()[1].getMethodName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    test(testName, new File(getTestRootDirectory(), "deviant-source-directory/MyLibrary"), "pom.xml", "deploy",
          THE_EMPTY_LIST,
          THE_EMPTY_MAP, remoteRepositoryDirectory);

    test(testName, new File(getTestRootDirectory(), "deviant-source-directory/MyApp"), "pom.xml", "deploy",
          THE_EMPTY_LIST,
          THE_EMPTY_MAP, remoteRepositoryDirectory);

    // Below we use internal knowledge from the pom: when running in
    // production profile the configuration is also "Production".
    final String configuration = "Release";

    assertTrue(new File(remoteRepositoryDirectory,
          Constants.GROUP_ID_WITH_SLASH + "/MyApp/" + Constants.APP_VERSION + "/MyApp-" + Constants.APP_VERSION + "-"
                + configuration + "-iphoneos.ipa").exists());
  }

  @Test
  public void testXCodeSourceDirEqualsMavenSourceDirectory() throws Exception
  {
    final String testName = Thread.currentThread().getStackTrace()[1].getMethodName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    test(testName, new File(getTestRootDirectory(), "deviant-source-directory-2/MyLibrary"), "pom.xml", "deploy",
          THE_EMPTY_LIST, THE_EMPTY_MAP, remoteRepositoryDirectory);

    test(testName, new File(getTestRootDirectory(), "deviant-source-directory-2/MyApp"), "pom.xml", "deploy",
          THE_EMPTY_LIST, THE_EMPTY_MAP, remoteRepositoryDirectory);

    final String configuration = "Release";

    assertTrue(new File(remoteRepositoryDirectory,
          Constants.GROUP_ID_WITH_SLASH + "/MyApp/" + Constants.APP_VERSION + "/MyApp-" + Constants.APP_VERSION + "-"
                + configuration + "-iphoneos.ipa").exists());
  }

  @Test
  public void testOTAUrlIsSetToEmpty() throws Exception
  {
    final String testName = Thread.currentThread().getStackTrace()[1].getMethodName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    additionalSystemProperties.put("mios.ota-service.url", "");

    final File projectDirectory = new File(getTestRootDirectory(), "straight-forward/MyApp");
    Verifier verifier = new Verifier(getTestExecutionDirectory(testName, projectDirectory.getName()).getAbsolutePath());
    try {
      verifier = test(verifier, testName, new File(getTestRootDirectory(), "straight-forward/MyApp"), "pom.xml",
            "deploy", THE_EMPTY_LIST,
            additionalSystemProperties, remoteRepositoryDirectory, null);

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
    final String testName = Thread.currentThread().getStackTrace()[1].getMethodName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    String otaWrongURL = "htp://apple-ota.wdf.sap.corp:8080/ota-service/HTML";
    additionalSystemProperties.put("mios.ota-service.url", otaWrongURL);

    final File projectDirectory = new File(getTestRootDirectory(), "straight-forward/MyApp");

    Verifier verifier = new Verifier(getTestExecutionDirectory(testName, projectDirectory.getName()).getAbsolutePath());

    try {
      test(verifier, testName, projectDirectory, "pom.xml", "deploy",
            THE_EMPTY_LIST,
            additionalSystemProperties, remoteRepositoryDirectory, null);
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
}
