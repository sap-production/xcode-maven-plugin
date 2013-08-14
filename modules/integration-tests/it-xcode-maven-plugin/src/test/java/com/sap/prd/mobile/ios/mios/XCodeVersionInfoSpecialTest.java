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
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.FileUtils;
import org.apache.maven.it.util.IOUtil;
import org.junit.Test;

public class XCodeVersionInfoSpecialTest extends XCodeTest
{

  @Test
  public void testVersionInfoWithoutDependentInformation() throws Exception
  {
    final String testName = getTestName();
    final String dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);

// this copy of MyLibrary doesn't create versions.xml
    Verifier verifier = test(testName, new File(getTestRootDirectory(), "versions-info/MyLibrary"),
          "deploy",
          THE_EMPTY_LIST,
          THE_EMPTY_MAP, pomReplacements, new NullProjectModifier());

    File libraryVersionsInfo = new File(remoteRepositoryDirectory,
          TestConstants.GROUP_ID_WITH_SLASH + "/MyLibrary/" + dynamicVersion + "/MyLibrary-" + dynamicVersion
                + "-versions.xml");
    libraryVersionsInfo.delete();
    verifier.deleteArtifacts(TestConstants.GROUP_ID_WITH_SLASH);
    assertTrue(!libraryVersionsInfo.exists());

    test(testName, new File(getTestRootDirectory(), "versions-info/MyApp"), "deploy",
          THE_EMPTY_LIST,
          THE_EMPTY_MAP, pomReplacements, new NullProjectModifier());

    File versionFileAppExpected = new File("src/test/resources/MyApp-1.0.0-without-dependent-info-versions.xml")
      .getAbsoluteFile();

    File versionFileApp = new File(remoteRepositoryDirectory,
          TestConstants.GROUP_ID_WITH_SLASH + "/MyApp/" + dynamicVersion + "/MyApp-" + dynamicVersion
                + "-versions.xml");
    assertTrue(versionFileApp.exists());

    final InputStream actualVersionFileApp = new FileInputStream(versionFileApp), expectedVersionFileApp = new FileInputStream(
          versionFileAppExpected);

    try {
      Assert.assertEquals(
            IOUtil.toString(expectedVersionFileApp, "UTF-8").replaceAll("\\$\\{dynamicVersion\\}", dynamicVersion),
            IOUtil.toString(actualVersionFileApp, "UTF-8"));
    }
    finally {
      IOUtil.close(actualVersionFileApp);
      IOUtil.close(expectedVersionFileApp);
    }

  }

  @Test
  public void testFailOnMissingSyncInfo() throws Exception
  {
    final String testName = getTestName();
    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());
    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, "1.0." + String.valueOf(System.currentTimeMillis()));

    // copy lib to intermediate folder and removed the sync.info file
    File intermediateDir = new File(new File(".").getAbsolutePath(), "target/tests/"
          + getClass().getName() + "/" + testName + "/intermediate/MyLib");
    FileUtils.copyDirectoryStructure(new File(getTestRootDirectory(), "versions-info/MyLibrary"), intermediateDir);
    File syncInfoFile = new File(intermediateDir, "sync.info");
    assertTrue("expected that the file " + syncInfoFile.getCanonicalPath() + " exists", syncInfoFile.isFile());
    syncInfoFile.delete();

    // This test should fail due to the missing sync.info file
    try {
      Map<String, String> additionalSystemProperties = new HashMap<String, String>();
      additionalSystemProperties.put("xcode.failOnMissingSyncInfo", "true");
      test(testName, intermediateDir, "install", THE_EMPTY_LIST, additionalSystemProperties,
            pomReplacements, new NullProjectModifier());
      fail("Expected the Maven call to fail due to a missing info.sync file.");
    }
    catch (VerificationException ex) {
      return;
    }

  }
}
