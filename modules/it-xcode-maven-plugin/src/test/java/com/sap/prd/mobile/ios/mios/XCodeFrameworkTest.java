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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Test;

public class XCodeFrameworkTest extends XCodeTest
{
  @Test
  public void createRealFramework() throws Exception
  {
    String testName = Thread.currentThread().getStackTrace()[1].getMethodName();
    createAndValidateFmwk(testName, "MyRealFramework");
  }

  @Test
  public void createFakeFramework() throws Exception
  {
    final String testName = Thread.currentThread().getStackTrace()[1].getMethodName();
    createAndValidateFmwk(testName, "MyFakeFramework");
  }


  @Test
  public void buildLibAsFramework() throws Exception
  {    
    final String testName = Thread.currentThread().getStackTrace()[1].getMethodName();
    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());
    prepareRemoteRepository(remoteRepositoryDirectory);
    File projectDirectory = new File(getTestRootDirectory(), "framework/MyLibrary");
    Verifier verifier = new Verifier(getTestExecutionDirectory(testName, projectDirectory.getName()).getAbsolutePath());

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());   
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, "1.0." + String.valueOf(System.currentTimeMillis()));
    
    try {
      test(verifier, testName, projectDirectory, "deploy",
            THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements);
      fail("Expected the Maven call to fail due to missing framework build result.");
    }
    catch (VerificationException ex) {
    }
    verifier
      .verifyTextInLog("target/checkout/src/xcode/build/Release-iphoneos/MyLibrary.framework' is not a directory");
  }

  private void createAndValidateFmwk(String testName, String fmwkName) throws IOException, Exception
  {
    String dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());
    
    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());
    prepareRemoteRepository(remoteRepositoryDirectory);
    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);
    test(testName, new File(getTestRootDirectory(), "framework/" + fmwkName), "deploy",
          THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements);

    final String frameworkArtifactFilePrefix = Constants.GROUP_ID_WITH_SLASH + "/" + fmwkName + "/" + dynamicVersion + "/" + fmwkName
          + "-" + dynamicVersion;
    File repoArtifact = new File(remoteRepositoryDirectory, frameworkArtifactFilePrefix + ".xcode-framework-zip");
    assertTrue(repoArtifact.exists());
    File extractedFrameworkFolder = tmpFolder.newFolder("frmw" + fmwkName);
    extractFileWithShellScript(repoArtifact, extractedFrameworkFolder);
    validateFrameworkStructure(extractedFrameworkFolder, fmwkName);
  }



  private void validateFrameworkStructure(File parentFolder, String frameworkName) throws IOException
  {
    File fwkFolder = new File(parentFolder, frameworkName + ".framework");
    FrameworkStructureValidator fmwkValidator = new FrameworkStructureValidator(fwkFolder);
    List<String> errMsgs = fmwkValidator.validate();
    assertTrue("The framework does not have a valid structure: " + errMsgs, errMsgs.isEmpty());
  }

  @Test
  public void testUseFramework() throws Exception
  {
    final String dynamicVersion = "1.0.0";
    
    final String testName = Thread.currentThread().getStackTrace()[1].getMethodName();
    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());
    prepareRemoteRepository(remoteRepositoryDirectory);

    final File frameworkRepository = new File(new File(".").getCanonicalFile(), "src/test/frameworkRepository");

    final Map<String, String> additionalSystemParameters = new HashMap<String, String>();
    additionalSystemParameters.put("configuration", "Release");
    additionalSystemParameters.put("sdk", "iphoneos");

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_FRWK_REPO_DIR, frameworkRepository.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);

    test(testName, new File(getTestRootDirectory(), "framework/MyApp"),
          "deploy",
          THE_EMPTY_LIST,
          additionalSystemParameters, pomReplacements);

    Assert.assertTrue(new File(getTestExecutionDirectory(testName, "MyApp"), "target/xcode-deps/frameworks/"
          + Constants.GROUP_ID
          + "/MyFramework/MyFramework.framework").exists());

    final String myAppVersionRepoDir = Constants.GROUP_ID_WITH_SLASH + "/MyApp/" + dynamicVersion;
    final String myAppArtifactFilePrefix = myAppVersionRepoDir + "/MyApp-" + dynamicVersion;
    File xcodeprojAppZip = new File(remoteRepositoryDirectory, myAppArtifactFilePrefix + "-"
          + XCodePackageXcodeprojMojo.XCODEPROJ_WITH_DEPS_CLASSIFIER + ".zip");
    assertTrue(xcodeprojAppZip.exists());
    assertUnpackAndCompile(xcodeprojAppZip);
  }
  
  @Test
  public void testFrameworkInProjectZip() throws Exception
  {
    final String dynamicVersion = "1.0." + System.currentTimeMillis();
    
    final String testName = Thread.currentThread().getStackTrace()[1].getMethodName();
    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());
    prepareRemoteRepository(remoteRepositoryDirectory);

    final File frameworkRepository = new File(new File(".").getCanonicalFile(), "src/test/frameworkRepository");

    final Map<String, String> additionalSystemParameters = new HashMap<String, String>();
    additionalSystemParameters.put("configuration", "Release");
    additionalSystemParameters.put("sdk", "iphoneos");

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_FRWK_REPO_DIR, frameworkRepository.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);

    test(testName, new File(getTestRootDirectory(), "framework/FrameworkInProjectZip/MyLibrary"),
          "deploy",
          THE_EMPTY_LIST,
          additionalSystemParameters, pomReplacements);

    Verifier verifier = test(testName, new File(getTestRootDirectory(), "framework/FrameworkInProjectZip/MyApp"),
          "install",
          THE_EMPTY_LIST,
          additionalSystemParameters, pomReplacements);
    
    File projectZipFile = new File(verifier.getBasedir(), "target/MyApp-xcodeproj-with-deps.zip");
    assertTrue("The file containing the zipped project does not exist at '" + projectZipFile + "'.", projectZipFile.exists());
    
    File tmpDir = new File(verifier.getBasedir(), "target/tmp");
    tmpDir = tmpDir.getCanonicalFile();
    
    if(tmpDir.exists())
      FileUtils.deleteDirectory(tmpDir);
    
    if(!tmpDir.mkdirs())
      throw new IOException("Could not create temp dir for expanding the project zip file at '" + tmpDir + "'.");
    
    int exitCode = Forker.forkProcess(System.out, tmpDir, "unzip", "../" + projectZipFile.getName());
    
    if(exitCode != 0)
      throw new IOException("Could not unzip file '" + projectZipFile + "' into directory '" + tmpDir + "'. Exit code is: " + exitCode);
    
    
    File headerFile = new File(tmpDir, "target/headers/Release-iphoneos/com.sap.ondevice.production.ios.tests/MyLibrary/PrintOutObject.h");
    assertTrue("Header file '" + headerFile + "' does not exist.", headerFile.exists());
    assertFalse("Header file '" + headerFile + "' is a symbolic link, but should be a real file.", FileUtils.isSymbolicLink(headerFile));

    File libFile = new File(tmpDir, "target/libs/Release-iphoneos/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a");
    assertTrue("Library file '" + libFile + "' does not exist.", libFile.exists());
    assertFalse("Library file '" + libFile + "' is a symbolic link, but should be a real file.", FileUtils.isSymbolicLink(libFile));
    
    File headersInFramework = new File(tmpDir, "target/xcode-deps/frameworks/com.sap.ondevice.production.ios.tests/MyFramework/MyFramework.framework/Headers");
    assertTrue("Header file '" + headersInFramework + "' is not a symbolic link, but should be a symbolic link.", FileUtils.isSymbolicLink(headersInFramework));
   
    File libInFramework = new File(tmpDir, "target/xcode-deps/frameworks/com.sap.ondevice.production.ios.tests/MyFramework/MyFramework.framework/MyFramework");
    assertTrue("Library file '" + libInFramework + "' is not a symbolic link, but should be a symbolic link.", FileUtils.isSymbolicLink(libInFramework));  

    File resourcesInFramework = new File(tmpDir, "target/xcode-deps/frameworks/com.sap.ondevice.production.ios.tests/MyFramework/MyFramework.framework/Resources");
    assertTrue("Resources folder in framework '" + resourcesInFramework + "' is not a symbolic link, but should be a symbolic link.", FileUtils.isSymbolicLink(resourcesInFramework));      
  }

  @Test
  public void testFrameworkInProjectZipWithFatLibrary() throws Exception
  {
    final String dynamicVersion = "1.0." + System.currentTimeMillis();
    
    final String testName = Thread.currentThread().getStackTrace()[1].getMethodName();
    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());
    prepareRemoteRepository(remoteRepositoryDirectory);

    final File frameworkRepository = new File(new File(".").getCanonicalFile(), "src/test/frameworkRepository");

    final Map<String, String> additionalSystemParameters = new HashMap<String, String>();
    additionalSystemParameters.put("configuration", "Release");
    additionalSystemParameters.put("sdk", "iphoneos");

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_FRWK_REPO_DIR, frameworkRepository.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);

    test(testName, new File(getTestRootDirectory(), "framework/FrameworkInProjectZip/MyLibrary"),
          "deploy",
          THE_EMPTY_LIST,
          additionalSystemParameters, pomReplacements);

    additionalSystemParameters.put("xcode.preferFatLibs", Boolean.TRUE.toString());
    
    Verifier verifier = test(testName, new File(getTestRootDirectory(), "framework/FrameworkInProjectZip/MyApp"), 
          "install",
          THE_EMPTY_LIST,
          additionalSystemParameters, pomReplacements);
    
    File projectZipFile = new File(verifier.getBasedir(), "target/MyApp-xcodeproj-with-deps.zip");
    assertTrue("The file containing the zipped project does not exist at '" + projectZipFile + "'.", projectZipFile.exists());
    
    File tmpDir = new File(verifier.getBasedir(), "target/tmp");
    tmpDir = tmpDir.getCanonicalFile();
    
    if(tmpDir.exists())
      FileUtils.deleteDirectory(tmpDir);
    
    if(!tmpDir.mkdirs())
      throw new IOException("Could not create temp dir for expanding the project zip file at '" + tmpDir + "'.");
    
    int exitCode = Forker.forkProcess(System.out, tmpDir, "unzip", "../" + projectZipFile.getName());
    
    if(exitCode != 0)
      throw new IOException("Could not unzip file '" + projectZipFile + "' into directory '" + tmpDir + "'");
    
    
    File headerFile = new File(tmpDir, "target/headers/Release-iphoneos/com.sap.ondevice.production.ios.tests/MyLibrary/PrintOutObject.h");
    assertTrue("Header file '" + headerFile + "' does not exist.", headerFile.exists());
    assertFalse("Header file '" + headerFile + "' is a symbolic link, but should be a real file.", FileUtils.isSymbolicLink(headerFile));

    File libFile = new File(tmpDir, "target/xcode-deps/libs/Release/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a");
    assertTrue("Fat library file '" + libFile + "' does not exist.", libFile.exists());
    assertFalse("Fat library file '" + libFile + "' is a symbolic link, but should be a real file.", FileUtils.isSymbolicLink(libFile));
    
    File headersInFramework = new File(tmpDir, "target/xcode-deps/frameworks/com.sap.ondevice.production.ios.tests/MyFramework/MyFramework.framework/Headers");
    assertTrue("Header file '" + headersInFramework + "' is not a symbolic link, but should be a symbolic link.", FileUtils.isSymbolicLink(headersInFramework));
   
    File libInFramework = new File(tmpDir, "target/xcode-deps/frameworks/com.sap.ondevice.production.ios.tests/MyFramework/MyFramework.framework/MyFramework");
    assertTrue("Library file '" + libInFramework + "' is not a symbolic link, but should be a symbolic link.", FileUtils.isSymbolicLink(libInFramework));  

    File resourcesInFramework = new File(tmpDir, "target/xcode-deps/frameworks/com.sap.ondevice.production.ios.tests/MyFramework/MyFramework.framework/Resources");
    assertTrue("Resources folder in framework '" + resourcesInFramework + "' is not a symbolic link, but should be a symbolic link.", FileUtils.isSymbolicLink(resourcesInFramework));  
  }
}
