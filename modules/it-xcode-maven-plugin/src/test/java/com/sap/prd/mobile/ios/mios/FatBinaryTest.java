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
import org.junit.Test;

public class FatBinaryTest extends XCodeTest
{
  private static String dynamicVersion = String.valueOf(System.currentTimeMillis()); 
  
  private static boolean remoteRepoInitialized = false;

  @Test
  public void testUsePreferredFatLib() throws Exception
  {   
    final String testName = Thread.currentThread().getStackTrace()[1].getMethodName();

    File remoteRepo = prepare();

    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    additionalSystemProperties.put("xcode.preferFatLibs", Boolean.TRUE.toString());

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepo.getAbsolutePath());
    pomReplacements.setProperty(DYNAMIC_VERSION, dynamicVersion);
    
    
    test(testName, new File(getTestRootDirectory(), "straight-forward-fat-libs/MyApp"), "pom.xml", "install",
          THE_EMPTY_LIST,
          additionalSystemProperties, pomReplacements);
    
    final File testRootDir = getTestExecutionDirectory(testName, "MyApp");
    
    Assert.assertTrue(new File(testRootDir, "target/xcode-deps/libs/Release/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a").exists());
    Assert.assertFalse(new File(testRootDir, "target/libs/Release-iphoneos/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a").exists());    
    Assert.assertFalse(new File(testRootDir, "target/libs/Release-iphonesimulator/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a").exists());    
  }
  
  @Test
  public void testUseThinLibs() throws Exception
  {
    final String testName = Thread.currentThread().getStackTrace()[1].getMethodName();

    File remoteRepo = prepare();

    Map<String, String> additionalSystemProperties = new HashMap<String, String>();

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepo.getAbsolutePath());
    pomReplacements.setProperty(DYNAMIC_VERSION, dynamicVersion);

    
    test(testName, new File(getTestRootDirectory(), "straight-forward-fat-libs/MyApp"), "pom.xml", "initialize",
          THE_EMPTY_LIST,
          additionalSystemProperties, pomReplacements);
    
    final File testRootDir = getTestExecutionDirectory(testName, "MyApp");
    
    Assert.assertFalse(new File(testRootDir, "target/xcode-deps/libs/Release/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a").exists());
    Assert.assertTrue(new File(testRootDir, "target/libs/Release-iphoneos/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a").exists());    
    Assert.assertTrue(new File(testRootDir, "target/libs/Release-iphonesimulator/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a").exists());    
  }
  
  @Test
  public void testPrefereFatLibForNonExistingFatLibs() throws Exception
  {
    final String testName = Thread.currentThread().getStackTrace()[1].getMethodName();

    File remoteRepo = prepare();

    final File fatLibReleaseRemoteRepo = new File(remoteRepo, "com/sap/ondevice/production/ios/tests/MyLibrary/1.0." + dynamicVersion + "/MyLibrary-1.0." + dynamicVersion + "-Release-fat-binary.a");
    
    if(!fatLibReleaseRemoteRepo.delete())
      throw new IOException("Cannot delete release fat lib file: " + fatLibReleaseRemoteRepo);
    
    Verifier verifier = new Verifier(getTestExecutionDirectory(testName, "MyApp").getAbsolutePath());
    verifier.deleteArtifacts("com.sap.ondevice.production.ios.tests");
    
    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    additionalSystemProperties.put("xcode.preferFatLibs", Boolean.TRUE.toString());

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepo.getAbsolutePath());
    pomReplacements.setProperty(DYNAMIC_VERSION, dynamicVersion);
    
    test(verifier, testName, new File(getTestRootDirectory(), "straight-forward-fat-libs/MyApp"), "pom.xml", "initialize",
          THE_EMPTY_LIST,
          additionalSystemProperties, pomReplacements);
    
    final File testRootDir = getTestExecutionDirectory(testName, "MyApp");
    
    final File fatLibReleaseInProject = new File(testRootDir, "target/xcode-deps/libs/Release/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a");
    final File thinLibReleaseInProject = new File(testRootDir, "target/libs/Release-iphoneos/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a");
    
    Assert.assertFalse("Fat lib '" + fatLibReleaseInProject + " does exist but was not expected to be present." , fatLibReleaseInProject.exists());
    Assert.assertTrue("Thin lib for release '" + thinLibReleaseInProject + "does exist but was not expected to be present.", thinLibReleaseInProject.exists());    
    Assert.assertTrue(new File(testRootDir, "target/libs/Release-iphonesimulator/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a").exists());    

    Assert.assertTrue(new File(testRootDir, "target/xcode-deps/libs/Debug/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a").exists());
    Assert.assertFalse(new File(testRootDir, "target/libs/Debug-iphoneos/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a").exists());    
    Assert.assertFalse(new File(testRootDir, "target/libs/Debug-iphonesimulator/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a").exists());    
  }

  @Test
  public void testFallbackToFatLibs() throws Exception
  {
    final String testName = Thread.currentThread().getStackTrace()[1].getMethodName();

    File remoteRepo = prepare();

    final File thinLibReleaseIPhoneOsRemoteRepo = new File(remoteRepo, "com/sap/ondevice/production/ios/tests/MyLibrary/1.0." + dynamicVersion + "/MyLibrary-1.0." + dynamicVersion + "-Release-iphoneos.a");

    
    if(!thinLibReleaseIPhoneOsRemoteRepo.delete())
      throw new IOException("Cannot delete release fat lib file: " + thinLibReleaseIPhoneOsRemoteRepo);
    
    Verifier verifier = new Verifier(getTestExecutionDirectory(testName, "MyApp").getAbsolutePath());
    verifier.deleteArtifacts("com.sap.ondevice.production.ios.tests");
    
    Map<String, String> additionalSystemProperties = new HashMap<String, String>();

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepo.getAbsolutePath());
    pomReplacements.setProperty(DYNAMIC_VERSION, dynamicVersion);
    
    test(verifier, testName, new File(getTestRootDirectory(), "straight-forward-fat-libs/MyApp"), "pom.xml", "initialize",
          THE_EMPTY_LIST,
          additionalSystemProperties, pomReplacements);
    
    final File testRootDir = getTestExecutionDirectory(testName, "MyApp");
    
    final File fatLibReleaseInProject = new File(testRootDir, "target/xcode-deps/libs/Release/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a");
    final File thinLibReleaseInProject = new File(testRootDir, "target/libs/Release-iphoneos/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a");
    
    Assert.assertTrue("Fat lib '" + fatLibReleaseInProject + " does not exist but was expected to be present." , fatLibReleaseInProject.exists());
    Assert.assertFalse("Thin lib for release '" + thinLibReleaseInProject + "exist but must not be present.", thinLibReleaseInProject.exists());    
    Assert.assertFalse(new File(testRootDir, "target/libs/Release-iphonesimulator/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a").exists());    

    Assert.assertFalse(new File(testRootDir, "target/xcode-deps/libs/Debug/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a").exists());
    Assert.assertTrue(new File(testRootDir, "target/libs/Debug-iphoneos/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a").exists());    
    Assert.assertTrue(new File(testRootDir, "target/libs/Debug-iphonesimulator/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a").exists());    
  }

  
  private File prepare() throws Exception
  {

    final String testName = "fatLibPreparation";

    File remoteRepoDir = new File(new File(new File(".").getCanonicalFile(), "target"), "remoteRepo/FatBinaryTest");
    
    File copyOfRemoteRepoDir = new File(remoteRepoDir.getParentFile(), "FatBinaryTest-" + System.currentTimeMillis());
    
    if (!remoteRepoInitialized) {

      Properties pomReplacements = new Properties();
      pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepoDir.getAbsolutePath());
      pomReplacements.setProperty(DYNAMIC_VERSION, dynamicVersion);
      
      if(remoteRepoDir.exists())
        com.sap.prd.mobile.ios.mios.FileUtils.deleteDirectory(remoteRepoDir);
      
      test(testName, new File(getTestRootDirectory(), "straight-forward-fat-libs/MyLibrary"), "pom.xml", "deploy",
            THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements);
      remoteRepoInitialized = true;
    }

    FileUtils.copyDirectory(remoteRepoDir, copyOfRemoteRepoDir);

    return copyOfRemoteRepoDir;
  }
}

