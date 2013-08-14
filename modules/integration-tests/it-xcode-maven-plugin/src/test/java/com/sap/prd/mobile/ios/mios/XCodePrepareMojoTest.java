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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.Test;

public class XCodePrepareMojoTest extends XCodeTest
{

  @Test
  public void testPrepare() throws Exception
  {

    final String testName = getTestName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, "1.0." + String.valueOf(System.currentTimeMillis()));

    test(testName, new File(getTestRootDirectory(), "straight-forward/MyLibrary"),
          "deploy", null,
          THE_EMPTY_MAP, pomReplacements, new NullProjectModifier());

    final Map<String, String> additionalSystemParameters = new HashMap<String, String>();
    additionalSystemParameters.put("configuration", "Release");
    additionalSystemParameters.put("sdk", "iphoneos");

    test(testName, new File(getTestRootDirectory(), "straight-forward/MyApp"),
          "com.sap.prd.mobile.ios.mios:xcode-maven-plugin:" + getMavenXcodePluginVersion() + ":prepare-xcode-build",
          THE_EMPTY_LIST,
          additionalSystemParameters, pomReplacements, new NullProjectModifier());

    final String configuration = "Release";

    Assert.assertTrue(new File(getTestExecutionDirectory(testName, "MyApp"), "target/libs/" + configuration
          + "-iphoneos/" + TestConstants.GROUP_ID + "/MyLibrary/libMyLibrary.a").exists());
    Assert.assertTrue(new File(getTestExecutionDirectory(testName, "MyApp"), "target/bundles/" + TestConstants.GROUP_ID
          + "/MyLibrary/MyLibrary.bundle").exists());
    Assert.assertTrue(new File(getTestExecutionDirectory(testName, "MyApp"), "target/bundles/" + TestConstants.GROUP_ID
          + "/MyLibrary/MyLibrary.bundle/test.txt").exists());
    Assert.assertTrue(new File(getTestExecutionDirectory(testName, "MyApp"), "target/bundles/" + TestConstants.GROUP_ID
          + "/MyLibrary/MyLibrary.raw.bundle/test.txt").exists());
    Assert.assertTrue(new File(getTestExecutionDirectory(testName, "MyApp"), "target/bundles/" + TestConstants.GROUP_ID
          + "/MyLibrary/MyLibrary.raw.bundle/testDirectory/test.txt").exists());
    Assert.assertTrue(new File(getTestExecutionDirectory(testName, "MyApp"), "target/bundles/" + TestConstants.GROUP_ID
          + "/MyLibrary/Resources/Another.bundle/test.txt").exists());
    Assert.assertTrue(new File(getTestExecutionDirectory(testName, "MyApp"), "target/headers/" + configuration
          + "-iphoneos/" + TestConstants.GROUP_ID + "/MyLibrary/PrintOutObject.h").exists());
  }
}
