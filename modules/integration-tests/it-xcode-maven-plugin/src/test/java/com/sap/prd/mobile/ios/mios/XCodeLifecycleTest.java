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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.model.Model;
import org.junit.Test;

public class XCodeLifecycleTest extends XCodeTest
{
  @Test
  public void testDontUseSymbolicLinksForSnapshotDependencies() throws Exception
  {
    final String testName = getTestName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(testName);

    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, "1.0." + String.valueOf(System.currentTimeMillis()));

    test(testName, new File(getTestRootDirectory(), "straight-forward/MyLibrary"), "deploy",
          THE_EMPTY_LIST,
          THE_EMPTY_MAP, pomReplacements, new AppendSnapshotToProjectVersionProjectModifier());

    Verifier verifier = test(testName, new File(getTestRootDirectory(), "straight-forward/MyApp"), "deploy",
          THE_EMPTY_LIST,
          THE_EMPTY_MAP, pomReplacements, new AbstractProjectModifier() {

            @Override
            void execute() throws Exception
            {
              final File pom = new File(testExecutionDirectory, "pom.xml");

              final Model model = getModel(pom);
              model.getDependencies().get(0).setVersion(model.getDependencies().get(0).getVersion() + "-SNAPSHOT");
              persistModel(pom, model);
            }
          });

    assertFalse(FileUtils.isSymbolicLink(new File(verifier.getBasedir()
          + "/target/libs/Release-iphoneos/com.sap.ondevice.production.ios.tests/MyLibrary/libMyLibrary.a")));
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
            pomReplacements, new NullProjectModifier());

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
          THE_EMPTY_MAP, pomReplacements, new NullProjectModifier());

    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    additionalSystemProperties.put("xcode.artifactIdSuffix", "release");
    additionalSystemProperties.put("mios.ota-service.url", "http://apple-ota.wdf.sap.corp:8080/ota-service/HTML");

    test(testName, new File(getTestRootDirectory(), "straight-forward/MyApp"), "deploy",
          THE_EMPTY_LIST,
          additionalSystemProperties, pomReplacements, new NullProjectModifier());

    final String configuration = "Release";

    assertTrue(new File(remoteRepositoryDirectory,
          TestConstants.GROUP_ID_WITH_SLASH + "/MyApp_release/" + dynamicVersion + "/MyApp_release-"
                + dynamicVersion + "-"
                + configuration + "-iphoneos.ipa").exists());

    assertTrue(new File(remoteRepositoryDirectory,
          TestConstants.GROUP_ID_WITH_SLASH + "/MyApp_release/" + dynamicVersion + "/MyApp_release-"
                + dynamicVersion + "-AppStoreMetadata.zip")
      .exists());
  }

  @Test
  public void testDeviantSourceDirectory() throws Exception
  {
    final File testRootDirectory = getTestRootDirectory();
    final File testSourceDirLib = new File(testRootDirectory, "straight-forward/MyLibrary");
    final File testSourceDirApp = new File(testRootDirectory, "straight-forward/MyApp");
    final File alternateTestSourceDirApp = new File(testRootDirectory, "deviant-source-directory/MyApp");

    class RelocateProjectProjectModifier extends AbstractProjectModifier
    {

      @Override
      void execute() throws Exception
      {

        final String relocationTarget = "abc";
        final File pom = new File(testExecutionDirectory, "pom.xml");

        final Model model = getModel(pom);
        model.getProperties().setProperty("xcode.sourceDirectory", relocationTarget);
        persistModel(pom, model);

        File src = new File(testExecutionDirectory, "src/xcode");
        org.apache.commons.io.FileUtils.copyDirectory(src, new File(testExecutionDirectory, relocationTarget));
        org.apache.commons.io.FileUtils.deleteDirectory(src);

        final String filePath = relocationTarget + "/MyApp.xcodeproj/project.pbxproj";
        org.apache.commons.io.FileUtils.copyFile(new File(alternateTestSourceDirApp, filePath), new File(
              testExecutionDirectory, filePath));
      }
    }

    final String testName = getTestName();
    final String dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);

    test(testName, testSourceDirLib, "deploy",
          THE_EMPTY_LIST,
          THE_EMPTY_MAP, pomReplacements, new RelocateProjectProjectModifier());

    test(testName, testSourceDirApp, "deploy",
          THE_EMPTY_LIST,
          THE_EMPTY_MAP, pomReplacements, new RelocateProjectProjectModifier());

    final String configuration = "Release";

    assertTrue(new File(remoteRepositoryDirectory,
          TestConstants.GROUP_ID_WITH_SLASH + "/MyApp/" + dynamicVersion + "/MyApp-" + dynamicVersion + "-"
                + configuration + "-iphoneos.ipa").exists());
  }

  @Test
  public void testXCodeSourceDirEqualsMavenSourceDirectory() throws Exception
  {
    final File testRootDirectory = getTestRootDirectory();
    final File testSourceDirLib = new File(testRootDirectory, "straight-forward/MyLibrary");
    final File testSourceDirApp = new File(testRootDirectory, "straight-forward/MyApp");
    final File alternateTestSourceDirApp = new File(testRootDirectory, "deviant-source-directory-2/MyApp");

    class RelocateProjectProjectModifier extends AbstractProjectModifier
    {

      @Override
      void execute() throws Exception
      {

        final String relocationTarget = "";
        final File pom = new File(testExecutionDirectory, "pom.xml");

        final Model model = getModel(pom);
        model.getProperties().setProperty("xcode.sourceDirectory", relocationTarget);
        persistModel(pom, model);

        File src = new File(testExecutionDirectory, "src/xcode");
        org.apache.commons.io.FileUtils.copyDirectory(src, new File(testExecutionDirectory, relocationTarget));
        org.apache.commons.io.FileUtils.deleteDirectory(src);

        final String filePath = relocationTarget + "/MyApp.xcodeproj/project.pbxproj";
        org.apache.commons.io.FileUtils.copyFile(new File(alternateTestSourceDirApp, filePath), new File(
              testExecutionDirectory, filePath));
      }
    }

    final String testName = getTestName();
    final String dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);

    test(testName, testSourceDirLib, "deploy",
          THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements, new RelocateProjectProjectModifier());

    test(testName, testSourceDirApp, "deploy",
          THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements, new RelocateProjectProjectModifier());

    final String configuration = "Release";

    assertTrue(new File(remoteRepositoryDirectory,
          TestConstants.GROUP_ID_WITH_SLASH + "/MyApp/" + dynamicVersion + "/MyApp-" + dynamicVersion + "-"
                + configuration + "-iphoneos.ipa").exists());
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
          THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements, new NullProjectModifier());

    test(new XCodeTestParameters(null, testName, new File(getTestRootDirectory(), "straight-forward/MyApp"),
          Arrays.asList(new String[] { "initialize", "initialize" }),
          THE_EMPTY_LIST, THE_EMPTY_MAP, propertiesToStringMap(pomReplacements), new NullProjectModifier()));
  }
}
