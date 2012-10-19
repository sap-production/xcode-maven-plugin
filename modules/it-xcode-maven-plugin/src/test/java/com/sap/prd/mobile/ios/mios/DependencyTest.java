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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import org.junit.Test;

public class DependencyTest extends XCodeTest
{


  @Test
  public void testTransitiveDependency() throws Exception
  {
    final String testName = Thread.currentThread().getStackTrace()[1].getMethodName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass()
      .getName());

    prepareRemoteRepository(remoteRepositoryDirectory);
    
    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    
    test(testName, new File(getTestRootDirectory(), "dependencies/MyLibraryB"), "pom.xml",
          "deploy", THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements);

    test(testName, new File(getTestRootDirectory(), "dependencies/MyLibraryA"), "pom.xml",
          "deploy", THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements);

    test(testName, new File(getTestRootDirectory(), "dependencies/MyApp"), "pom.xml",
          "deploy", THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements);

    final String configuration = "Release";

    final File testExecutionDirectory = getTestExecutionDirectory(testName, "MyApp");
    assertTrue(new File(testExecutionDirectory, "target/headers/" + configuration + "-iphoneos/" + Constants.GROUP_ID
          + "/MyLibraryA").exists());
    assertTrue(new File(testExecutionDirectory, "target/headers/" + configuration + "-iphoneos/" + Constants.GROUP_ID
          + "/MyLibraryB").exists());

    assertTrue(new File(testExecutionDirectory, "target/libs/" + configuration + "-iphoneos/" + Constants.GROUP_ID
          + "/MyLibraryA").exists());
    assertTrue(new File(testExecutionDirectory, "target/libs/" + configuration + "-iphoneos/" + Constants.GROUP_ID
          + "/MyLibraryB").exists());

    // check if the zipped xcodeproj can be built after unzipping
    final String myLibAVersionRepoDir = Constants.GROUP_ID_WITH_SLASH + "/MyLibraryA/" + Constants.LIB_VERSION;
    final String myLibAArtifactFilePrefix = myLibAVersionRepoDir + "/MyLibraryA-" + Constants.LIB_VERSION;
    File xcodeprojLibAZip = new File(remoteRepositoryDirectory, myLibAArtifactFilePrefix + "-"
          + XCodePackageXcodeprojMojo.XCODEPROJ_WITH_DEPS_CLASSIFIER + ".zip");
    assertTrue(xcodeprojLibAZip.exists());
    assertUnpackAndCompile(xcodeprojLibAZip);

    final String myAppVersionRepoDir = Constants.GROUP_ID_WITH_SLASH + "/MyApp/" + Constants.APP_VERSION;
    final String myAppArtifactFilePrefix = myAppVersionRepoDir + "/MyApp-" + Constants.APP_VERSION;
    File xcodeprojAppZip = new File(remoteRepositoryDirectory, myAppArtifactFilePrefix + "-"
          + XCodePackageXcodeprojMojo.XCODEPROJ_WITH_DEPS_CLASSIFIER + ".zip");
    assertTrue(xcodeprojAppZip.exists());
    File unpackDir = assertUnpackAndCompile(xcodeprojAppZip);
    assertTrue(new File(unpackDir, "pom.xml").exists());
    assertTrue(new File(unpackDir, "sync.info").exists());
    assertTrue(new File(unpackDir, "src/xcode").exists());
    assertTrue(new File(unpackDir, "target/libs").exists());
    assertTrue(new File(unpackDir, "target/headers").exists());
    // provided via additionalSourcePaths plugin configuration:
    assertTrue(new File(unpackDir, "src/docs/readme.txt").exists());
    assertTrue(new File(unpackDir, "target/versions.xml").exists());
    // excludes tests ecpedted dirs and files
    assertTrue(new File(unpackDir, "src/xcode/packageExcludeTest").exists());
    assertTrue(new File(unpackDir, "src/xcode/packageExcludeTest/keep.txt").exists());
    assertTrue(new File(unpackDir, "src/xcode/packageExcludeTest/keep/keep.txt").exists());
    // exclude test files and dirs not to be packaged
    assertFalse(new File(unpackDir, "src/xcode/packageExcludeTest/doNotPackage.txt").exists());
    assertFalse(new File(unpackDir, "src/xcode/packageExcludeTest/file.tmp").exists());
    assertFalse(new File(unpackDir, "src/xcode/packageExcludeTest/excludeDir").exists());
    assertFalse(new File(unpackDir, "src/xcode/packageExcludeTest/tmp").exists());
  }


}

