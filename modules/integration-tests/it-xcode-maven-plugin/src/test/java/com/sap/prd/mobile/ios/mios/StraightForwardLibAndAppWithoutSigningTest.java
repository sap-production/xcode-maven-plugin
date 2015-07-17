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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.maven.it.Verifier;
import org.junit.BeforeClass;
import org.junit.Test;

public class StraightForwardLibAndAppWithoutSigningTest extends XCodeTest
{
  private static File remoteRepositoryDirectory = null, appTestBaseDir = null;
  private static String dynamicVersion = null,
        myLibArtifactFilePrefix = null,
        testName = null,
        myAppVersionRepoDir = null,
        myAppArtifactFilePrefix = null;

  private static Verifier appVerifier = null;

  private static File extractedIpaFolder = null, appstoreFolder = null, archiveArtifactsDir;

  @BeforeClass
  public static void __setup() throws Exception
  {

    dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());
    myLibArtifactFilePrefix = TestConstants.GROUP_ID_WITH_SLASH + "/MyLibrary/" + dynamicVersion + "/MyLibrary-"
          + dynamicVersion;
    testName = StraightForwardLibAndAppWithoutSigningTest.class.getName() + File.separator
          + Thread.currentThread().getStackTrace()[1].getMethodName();

    remoteRepositoryDirectory = getRemoteRepositoryDirectory(StraightForwardLibAndAppWithoutSigningTest.class.getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);

    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    additionalSystemProperties.put("mios.ota-service.url", "http://apple-ota.wdf.sap.corp:8080/ota-service/HTML");
    additionalSystemProperties.put("xcode.app.defaultConfigurations", "Release");
    additionalSystemProperties.put("xcode.app.defaultSdks", "iphoneos");
    additionalSystemProperties.put("archive.dir", "archive");
    additionalSystemProperties.put("xcode.useSymbolicLinks", Boolean.TRUE.toString());

    test(testName, new File(getTestRootDirectory(), "straight-forward/MyLibrary"), "deploy",
          THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements, new NullProjectModifier());

    List<String> commandLineArgs = new ArrayList<String>();
    commandLineArgs.add("-Dxcode.codeSigningRequired=false");
    commandLineArgs.add("-Dxcode.codeSignIdentity=\"\"");
    
    appVerifier = test(testName, new File(getTestRootDirectory(), "straight-forward/MyApp"),
          "deploy",
          commandLineArgs,
          additionalSystemProperties, pomReplacements, new NullProjectModifier());

    myAppVersionRepoDir = TestConstants.GROUP_ID_WITH_SLASH + "/MyApp/" + dynamicVersion;
    myAppArtifactFilePrefix = myAppVersionRepoDir + "/MyApp-" + dynamicVersion;

    final File tmpFolder = new File(getTargetDirectory(), "tests/tmp");
    tmpFolder.deleteOnExit();

    extractedIpaFolder = new File(tmpFolder, "ipa");
    extractFileWithShellScript(new File(remoteRepositoryDirectory, myAppArtifactFilePrefix + "-Release-iphoneos.ipa"),
          extractedIpaFolder);

    File appstoreUploadFile = new File(remoteRepositoryDirectory, myAppArtifactFilePrefix + "-Release-iphoneos-app.zip");
    assertTrue(appstoreUploadFile.exists());

    appstoreFolder = new File(tmpFolder, "appstoreFolder");
    appstoreFolder.deleteOnExit();
    extractFileWithShellScript(appstoreUploadFile, appstoreFolder);

    appTestBaseDir = new File(appVerifier.getBasedir());

    archiveArtifactsDir = new File(appTestBaseDir, "archive/artifacts/com.sap.ondevice.production.ios.tests/MyApp");
  }

  @Test
  public void testExistsHeaders() throws Exception
  {
    String[] configurations = new String[] { "Release", "Debug" };
    String[] sdks = new String[] { "iphoneos", "iphonesimulator" };

    for (String configuration : configurations) {
      for (String sdk : sdks) {
        File headersTarFile = new File(remoteRepositoryDirectory, myLibArtifactFilePrefix + "-" + configuration + "-"
              + sdk + ".headers.tar");
        assertTrue("Headers tar file '" + headersTarFile + "' for sdk '" + sdk + "' and configuration '"
              + configuration + "' does not exist.", headersTarFile.exists());

      }
    }
  }

  @Test
  public void testExistsBundles() throws Exception
  {
    String[] bundles = new String[] { "MyLibrary", "MyLibrary.raw", "Resources~Another" };

    for (String bundle : bundles) {

      File _bundle = new File(remoteRepositoryDirectory, myLibArtifactFilePrefix + "-" + bundle + ".xcode-bundle-zip");
      assertTrue("Bundle file '" + _bundle + "' for bundle'" + bundle + "' does not exist.", _bundle.exists());
    }
  }

  @Test
  public void testFatLibrariesExists() throws Exception
  {
    String[] configurations = new String[] { "Release", "Debug" };

    for (String configuration : configurations) {
      File fatLib = new File(remoteRepositoryDirectory, myLibArtifactFilePrefix + "-Release-fat-binary.a");
      assertTrue("Fat library '" + fatLib + "' for configuration '" + configuration + "' does not exist.",
            fatLib.exists());
    }
  }

  @Test
  public void testVersionsFile() throws Exception
  {
    File versionFileLib = new File(remoteRepositoryDirectory, myLibArtifactFilePrefix + "-versions.xml");
    assertTrue(versionFileLib.exists());
    compareFilesContainingDynamicVersions(dynamicVersion,
          new File(".", "src/test/resources/MyLibrary-versions.xml").getAbsoluteFile(), versionFileLib);
  }

  @Test
  public void testVersionFile() throws Exception
  {
    File versionFileApp = new File(remoteRepositoryDirectory, myAppArtifactFilePrefix + "-versions.xml");
    assertTrue(versionFileApp.exists());
    File versionsTestFile = new File("src/test/resources/MyApp-versions.xml");
    compareFilesContainingDynamicVersions(dynamicVersion, versionsTestFile, versionFileApp);
  }

  @Test
  public void testIpaReleaseIPhoneOsExists() throws Exception
  {
    // we built only the Xcode config Release for iphoneos SDK. All other combinations must not exist:
    assertTrue(new File(remoteRepositoryDirectory, myAppArtifactFilePrefix + "-Release-iphoneos.ipa").exists());
  }

  @Test
  public void testIpaDebugIPhoneOsDoesNotExist() throws Exception
  {
    // we built only the Xcode config Release for iphoneos SDK. All other combinations must not exist:
    assertFalse(new File(remoteRepositoryDirectory, myAppArtifactFilePrefix + "-Debug-iphoneos.ipa").exists());
  }

  @Test
  public void testIpaReleaseIPhoneSimulatorDoesNotExist() throws Exception
  {
    // we built only the Xcode config Release for iphoneos SDK. All other combinations must not exist:
    assertFalse(new File(remoteRepositoryDirectory, myAppArtifactFilePrefix + "-Release-iphonesimulator.ipa").exists());
  }

  @Test
  public void testIpaDebugIPhoneSimulatorDoesNotExist() throws Exception
  {
    // we built only the Xcode config Release for iphoneos SDK. All other combinations must not exist:
    assertFalse(new File(remoteRepositoryDirectory, myAppArtifactFilePrefix + "-Debug-iphonesimulator.ipa").exists());
  }

  @Test
  public void testAppNameInAppStoreUploadFile() throws Exception
  {
    File[] files = appstoreFolder.listFiles();
    assertTrue("MyApp.app folder is missing in AppstoreUploadFile.",
          files.length == 1 && files[0].getName().equals("MyApp.app"));
  }

  @Test
  public void testVersionsFileXMLInApplicationFile() throws Exception
  {
    File versionsXmlInIpa = new File(extractedIpaFolder, "Payload/MyApp.app/versions.xml");
    File versionsXmlInAppZip = new File(appstoreFolder, "MyApp.app/versions.xml");
    assertTrue(String.format("Versions file '%s' does exist in the IPA.", versionsXmlInIpa), versionsXmlInIpa.exists());
    assertTrue(String.format("Versions file '%s' does exist in the application zip file.", versionsXmlInAppZip), versionsXmlInAppZip.exists());
    File versionsTestXMLFile = new File("src/test/resources/MyApp-versions-in-IPA.xml");

    compareFilesContainingDynamicVersions(dynamicVersion, versionsTestXMLFile, versionsXmlInAppZip);
  }

  @Test
  public void testVersionsFilePListInApplicationFile() throws Exception
  {
    File versionsPListInIpa = new File(extractedIpaFolder, "Payload/MyApp.app/versions.plist");
    File versionsPListInAppZip = new File(appstoreFolder, "MyApp.app/versions.plist");
    assertTrue(versionsPListInIpa.exists());
    assertTrue(versionsPListInAppZip.exists());
    File versionsTestPListFile = new File("src/test/resources/MyApp-versions.plist");

    compareFilesContainingDynamicVersions(dynamicVersion, versionsTestPListFile, versionsPListInAppZip);
  }

  @Test
  public void testOTAIFrameFile() throws Exception
  {
    final String appIdSuffix = extractAppIdSuffixFromLogFile(new File(appVerifier.getBasedir(),
          appVerifier.getLogFileName()));

    File otaHtmlFileActualRelease = new File(remoteRepositoryDirectory, myAppArtifactFilePrefix
          + "-Release-iphoneos-ota.htm");
    assertTrue(otaHtmlFileActualRelease.exists());

    final String otaFileNameSuffix = appIdSuffix == null ? "-iphoneos-ota.htm" : "-" + appIdSuffix
          + "-iphoneos-ota.htm";
    compareFilesContainingDynamicVersions(dynamicVersion, new File("src/test/resources/MyApp-Release-1.0.0"
          + otaFileNameSuffix),
          otaHtmlFileActualRelease);
  }

  @Test
  public void testOTAPointerFile() throws Exception
  {
    assertTrue("Archive artifacts dir does not exist", archiveArtifactsDir.isDirectory());
    File otaArchiveHtmlFile = new File(archiveArtifactsDir, "MyApp-Release-iphoneos-ota.htm");
    assertTrue("OTA archive HTML file does not exist", otaArchiveHtmlFile.isFile());
    FileInputStream fis = new FileInputStream(otaArchiveHtmlFile);
    try {
      String otaArchiveHtmlContent = IOUtils.toString(fis, "UTF-8");
      assertFalse("${LOCATION} has not been replaced in OTA archive HTML file",
            otaArchiveHtmlContent.contains("${LOCATION}"));
      assertTrue(
            "OTA HTML location has not been written into OTA archive HTML file",
            otaArchiveHtmlContent
              .contains("target/remoteRepo/com.sap.prd.mobile.ios.mios.StraightForwardLibAndAppWithoutSigningTest/com/sap/ondevice/production/ios/tests/MyApp/"
                    + dynamicVersion + "/MyApp-" + dynamicVersion + "-Release-iphoneos-ota.htm"));
    }
    finally {
      IOUtils.closeQuietly(fis);
    }
  }

  @Test
  public void testRedirectFileAppStoreMetadata() throws Exception
  {
    assertRedirectFileExists("MyApp-AppStoreMetaData.zip.htm");
  }

  @Test
  public void testRedirectFileOtaHtm() throws Exception
  {
    assertRedirectFileExists("MyApp-Release-iphoneos-ota.htm");
  }

  @Test
  public void testRedirectFiledSYM() throws Exception
  {
    assertRedirectFileExists("MyApp-Release-iphoneos-app.dSYM.zip.htm");
  }

  @Test
  public void testRedirectFileAppZipHtm() throws Exception
  {
    assertRedirectFileExists("MyApp-Release-iphoneos-app.zip.htm");
  }

  @Test
  public void testRedirectFileIpaHtm() throws Exception
  {
    assertRedirectFileExists("MyApp-Release-iphoneos.ipa.htm");
  }

  @Test
  public void testRedirectFileVersionsXmlHtm() throws Exception
  {
    assertRedirectFileExists("MyApp-versions.xml.htm");
  }

  @Test
  public void testRedirectFilePomHtm() throws Exception
  {
    assertRedirectFileExists("MyApp.pom.htm");
  }

  @Test
  public void testAppStoreMetadataExists() throws Exception
  {
    assertTrue(new File(remoteRepositoryDirectory, myAppArtifactFilePrefix + "-AppStoreMetadata.zip").exists());
  }

  @Test
  public void testLibaryProvidedWithSymbolicLink() throws Exception
  {
    assertTrue(FileUtils.isSymbolicLink(new File(appVerifier.getBasedir()
          + "/target/libs/Release-iphoneos/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a")));

  }

  @Test
  public void testCFBundeShortVersionInInfoPlist() throws Exception
  {
    final File infoPList = new File(appTestBaseDir,
          "target/checkout/src/xcode/build/Release-iphoneos/MyApp.app/Info.plist");
    assertEquals("CFBundleShortVersion in file '" + infoPList + "' is not the expected version '" + dynamicVersion
          + "'.", dynamicVersion,
          new PListAccessor(infoPList).getStringValue(PListAccessor.KEY_BUNDLE_SHORT_VERSION_STRING));
  }

  @Test
  public void testCFBundeVersionInInfoPlist() throws Exception
  {
    final File infoPList = new File(appTestBaseDir,
          "target/checkout/src/xcode/build/Release-iphoneos/MyApp.app/Info.plist");
    assertEquals("CFBundleVErsion in file '" + infoPList + "' is not the expected version '" + dynamicVersion + "'.",
          dynamicVersion, new PListAccessor(infoPList).getStringValue(PListAccessor.KEY_BUNDLE_VERSION));
  }

  @Test
  public void testHeaders() throws Exception
  {
    File headersTar = new File(remoteRepositoryDirectory, "com/sap/ondevice/production/ios/tests/MyLibrary/"
          + dynamicVersion + "/MyLibrary-" + dynamicVersion + "-Release-iphoneos.headers.tar");

    Assert.assertTrue("Headers tar file '" + headersTar + "' does not exist", headersTar.exists());

    ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(byteOs);

    try {
      Forker.forkProcess(out, null, new String[] { "tar", "-tf", headersTar.getAbsolutePath() });
    }
    finally {
      IOUtils.closeQuietly(out);
    }
    final String toc = new String(byteOs.toByteArray());
    final String expectedContent = "PrintOutObject.h";
    final String notExpectedContent = "include/PrintOutObject.h";
    Assert.assertTrue("Table of content of the headers tar file '" + headersTar
          + "' does not contain the expected content '" + expectedContent + "'. Table of content is: " + toc,
          toc.contains(expectedContent));
    Assert.assertFalse("Table of content of the headers tar file '" + headersTar
          + "' does contain not expected content '" + notExpectedContent + "'. Table of content is: " + toc,
          toc.contains(notExpectedContent));

  }

  private static void assertRedirectFileExists(final String name)
  {
    assertTrue("Redirect file '" + name + "' does not exist.", new File(archiveArtifactsDir, name).isFile());
  }

  private static void compareFilesContainingDynamicVersions(final String dynamicVersion, File template,
        File versionFileLib) throws FileNotFoundException, IOException
  {
    String toBeTestedAgainst = IOUtils.toString(new FileInputStream(template)).replaceAll("\\$\\{dynamicVersion\\}",
          dynamicVersion);
    Assert.assertEquals(String.format("File content different: '%s' vs. '%s'",
          template.getAbsolutePath(), versionFileLib.getAbsolutePath()),
          toBeTestedAgainst,
          IOUtils.toString(new FileInputStream(versionFileLib)).replaceAll("\\$\\{dynamicVersion\\}", dynamicVersion));
  }

  @SuppressWarnings("resource")
  private static String extractAppIdSuffixFromLogFile(File logFile) throws IOException
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
    }
    finally {
      IOUtils.closeQuietly(reader);
    }
    return null;
  }
}
