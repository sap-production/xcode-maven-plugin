package com.sap.prd.mobile.ios.mios;

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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FatBinaryTest extends XCodeTest
{
  private static String dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());

  private static File masterRemoteRepoDir;

  private File remoteRepoDir;

  static {
    try {
      masterRemoteRepoDir = new File(new File(new File(".").getCanonicalFile(), "target"), "remoteRepo/FatBinaryTest");
    }
    catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @BeforeClass
  public static void __setup() throws Exception
  {
    final String testName = "fatLibPreparation";

    final Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, masterRemoteRepoDir.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);

    if (masterRemoteRepoDir.exists())
      com.sap.prd.mobile.ios.mios.FileUtils.deleteDirectory(masterRemoteRepoDir);
    
    test(testName, new File(getTestRootDirectory(), "straight-forward/MyLibrary"), "deploy",
          THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements, new NullProjectModifier());
  }

  @Before
  public void prepareCopyOfRemoteRepo() throws Exception
  {
    remoteRepoDir = new File(masterRemoteRepoDir.getParentFile(), "FatBinaryTest-" + System.currentTimeMillis());
    FileUtils.copyDirectory(masterRemoteRepoDir, remoteRepoDir);
  }

  @Test
  public void testUsePreferredFatLib() throws Exception
  {   
    final File testSourceDirApp = new File(getTestRootDirectory(), "straight-forward/MyApp");
    final File alternateTestSourceDirApp = new File(getTestRootDirectory(), "straight-forward-fat-libs/MyApp");

    final String testName = getTestName();

    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    additionalSystemProperties.put("xcode.preferFatLibs", Boolean.TRUE.toString());

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepoDir.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);
    
    
    test(testName, testSourceDirApp, "install",
          THE_EMPTY_LIST,
          additionalSystemProperties, pomReplacements, new FileCopyProjectModifier(alternateTestSourceDirApp));
    
    final File testRootDir = getTestExecutionDirectory(testName, "MyApp");

    Assert.assertTrue(new File(testRootDir,
          "target/xcode-deps/libs/Release/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a").exists());
    Assert.assertFalse(new File(testRootDir,
          "target/libs/Release-iphoneos/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a").exists());
    Assert.assertFalse(new File(testRootDir,
          "target/libs/Release-iphonesimulator/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a")
      .exists());
  }

  @Test
  public void testUseThinLibs() throws Exception
  {
    final File testSourceDirApp = new File(getTestRootDirectory(), "straight-forward/MyApp");
    final File alternateTestSourceDirApp = new File(getTestRootDirectory(), "straight-forward-fat-libs/MyApp");

    final String testName = getTestName();

    Map<String, String> additionalSystemProperties = new HashMap<String, String>();

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepoDir.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);

    
    test(testName, testSourceDirApp, "install",
          THE_EMPTY_LIST,
          additionalSystemProperties, pomReplacements, new FileCopyProjectModifier(alternateTestSourceDirApp));
    
    final File testRootDir = getTestExecutionDirectory(testName, "MyApp");

    Assert.assertFalse(new File(testRootDir,
          "target/xcode-deps/libs/Release/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a").exists());
    Assert.assertTrue(new File(testRootDir,
          "target/libs/Release-iphoneos/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a").exists());
    Assert.assertTrue(new File(testRootDir,
          "target/libs/Release-iphonesimulator/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a")
      .exists());
  }

  @Test
  public void testPrefereFatLibForNonExistingFatLibs() throws Exception
  {
    final File testSourceDirectoryApp = new File(getTestRootDirectory(), "straight-forward/MyApp");
    final File alternateTestSourceDirectoryApp = new File(getTestRootDirectory(), "straight-forward-fat-libs/MyApp");
    final String testName = getTestName();

    final File fatLibReleaseRemoteRepo = new File(remoteRepoDir, "com/sap/ondevice/production/ios/tests/MyLibrary/"
          + dynamicVersion + "/MyLibrary-" + dynamicVersion + "-Release-fat-binary.a");

    if (!fatLibReleaseRemoteRepo.delete())
      throw new IOException("Cannot delete release fat lib file: " + fatLibReleaseRemoteRepo);

    Verifier verifier = new Verifier(getTestExecutionDirectory(testName, "MyApp").getAbsolutePath());
    verifier.deleteArtifacts("com.sap.ondevice.production.ios.tests");

    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    additionalSystemProperties.put("xcode.preferFatLibs", Boolean.TRUE.toString());

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepoDir.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);
    
    test(verifier, testName, testSourceDirectoryApp, "install",
          THE_EMPTY_LIST,
          additionalSystemProperties, pomReplacements, new FileCopyProjectModifier(alternateTestSourceDirectoryApp));
    
    final File testRootDir = getTestExecutionDirectory(testName, "MyApp");

    final File fatLibReleaseInProject = new File(testRootDir,
          "target/xcode-deps/libs/Release/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a");
    final File thinLibReleaseInProject = new File(testRootDir,
          "target/libs/Release-iphoneos/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a");

    Assert.assertFalse("Fat lib '" + fatLibReleaseInProject + " does exist but was not expected to be present.",
          fatLibReleaseInProject.exists());
    Assert.assertTrue("Thin lib for release '" + thinLibReleaseInProject
          + "does exist but was not expected to be present.", thinLibReleaseInProject.exists());
    Assert.assertTrue(new File(testRootDir,
          "target/libs/Release-iphonesimulator/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a")
      .exists());

    Assert.assertTrue(new File(testRootDir,
          "target/xcode-deps/libs/Debug/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a").exists());
    Assert.assertFalse(new File(testRootDir,
          "target/libs/Debug-iphoneos/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a").exists());
    Assert.assertFalse(new File(testRootDir,
          "target/libs/Debug-iphonesimulator/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a").exists());
  }

  @Test
  public void testFallbackToFatLibs() throws Exception
  {
    final String testName = getTestName();

    final File thinLibReleaseIPhoneOsRemoteRepo = new File(remoteRepoDir, "com/sap/ondevice/production/ios/tests/MyLibrary/" + dynamicVersion + "/MyLibrary-" + dynamicVersion + "-Release-iphoneos.a");

    final File testSourceDirApp = new File(getTestRootDirectory(), "straight-forward/MyApp");
    final File alternateTestSourceDirApp = new File(getTestRootDirectory(), "straight-forward-fat-libs/MyApp");

    if(!thinLibReleaseIPhoneOsRemoteRepo.delete())
      throw new IOException("Cannot delete release fat lib file: " + thinLibReleaseIPhoneOsRemoteRepo);

    Verifier verifier = new Verifier(getTestExecutionDirectory(testName, "MyApp").getAbsolutePath());
    verifier.deleteArtifacts("com.sap.ondevice.production.ios.tests");

    Map<String, String> additionalSystemProperties = new HashMap<String, String>();

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepoDir.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);
    
    test(verifier, testName, testSourceDirApp, "install",
          THE_EMPTY_LIST,
          additionalSystemProperties, pomReplacements, new FileCopyProjectModifier(alternateTestSourceDirApp));
    
    final File testRootDir = getTestExecutionDirectory(testName, "MyApp");

    final File fatLibReleaseInProject = new File(testRootDir,
          "target/xcode-deps/libs/Release/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a");
    final File thinLibReleaseInProject = new File(testRootDir,
          "target/libs/Release-iphoneos/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a");

    Assert.assertTrue("Fat lib '" + fatLibReleaseInProject + " does not exist but was expected to be present.",
          fatLibReleaseInProject.exists());
    Assert.assertFalse("Thin lib for release '" + thinLibReleaseInProject + "exist but must not be present.",
          thinLibReleaseInProject.exists());
    Assert.assertFalse(new File(testRootDir,
          "target/libs/Release-iphonesimulator/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a")
      .exists());

    Assert.assertFalse(new File(testRootDir,
          "target/xcode-deps/libs/Debug/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a").exists());
    Assert.assertTrue(new File(testRootDir,
          "target/libs/Debug-iphoneos/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a").exists());
    Assert.assertTrue(new File(testRootDir,
          "target/libs/Debug-iphonesimulator/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a").exists());
  }
  
  private static class FileCopyProjectModifier extends ProjectModifier {

      private final File alternateTestSourceDirectory;
    
      FileCopyProjectModifier(File alternateTestSourceDirectory) {
        this.alternateTestSourceDirectory = alternateTestSourceDirectory;
      }
      @Override
      void execute() throws Exception
      {
        final String path = "src/xcode/MyApp.xcodeproj/project.pbxproj";
        FileUtils.copyFile(new File(alternateTestSourceDirectory, path), new File(testExecutionDirectory, path));
      }
  }
}
