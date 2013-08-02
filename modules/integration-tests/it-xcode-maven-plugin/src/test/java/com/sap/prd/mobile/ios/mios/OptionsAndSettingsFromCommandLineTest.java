/*
 * #%L
 * xcode-maven-plugin
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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class OptionsAndSettingsFromCommandLineTest extends XCodeTest
{
  private static File remoteRepositoryDirectory = null, dsymFile = null;
  private static String dynamicVersion = null, testName = null;

  private static Properties pomReplacements = new Properties();

  private static File remoteRepoGAVFolder;

  @BeforeClass
  public static void __setup() throws Exception
  {
    dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());

    testName = OptionsAndSettingsTest.class.getName() + File.separator
          + Thread.currentThread().getStackTrace()[1].getMethodName();

    remoteRepositoryDirectory = getRemoteRepositoryDirectory(OptionsAndSettingsTest.class.getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    remoteRepoGAVFolder = new File(remoteRepositoryDirectory, "com/sap/ondevice/production/ios/tests/MyApp/" + dynamicVersion);

    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);

    test(testName, new File(getTestRootDirectory(), "straight-forward/MyLibrary"), "deploy",
          THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements, new NullProjectModifier());

    dsymFile = new File(remoteRepoGAVFolder, "MyApp-" + dynamicVersion + "-Release-iphoneos-app.dSYM.zip");

  }

  @Before
  public void _setup() throws IOException{

    if(remoteRepoGAVFolder.exists())
      FileUtils.deleteDirectory(remoteRepoGAVFolder);
  }

  @Test
  public void testDymFilesDoesNotExist() throws Exception
  {
    Map<String, String> additionalSystemProperties = new HashMap<String, String>();

    additionalSystemProperties.put("xcode.settings.DEBUG_INFORMATION_FORMAT", "dwarf");
    additionalSystemProperties.put("xcode.settings.GCC_GENERATE_DEBUGGING_SYMBOLS", "YES");

    Verifier verifier = test(testName + "-noDsym", new File(getTestRootDirectory(), "straight-forward/MyApp"), "deploy",
          THE_EMPTY_LIST, additionalSystemProperties, pomReplacements, new NullProjectModifier());

    File pom = new File(remoteRepoGAVFolder, "MyApp-" + dynamicVersion + ".pom");

    assertTrue("The pom of the app (" + pom + ") does not exist.", pom.isFile());

    assertFalse(
          "Dsym file '"
                + dsymFile
                + "' does exist. It was expected that dSyms are not created according to the settings provided on command line.",
          dsymFile.isFile());


    File dsymInsideXcodeBuildDir = new File(verifier.getBasedir(), "target/checkout/src/xcode/build/Release-iphoneos/MyApp.app.dSYM");

    assertFalse("dSyms has not been created inside the xcode build directory: " + dsymInsideXcodeBuildDir, dsymInsideXcodeBuildDir.exists());

  }

  @Test
  public void testDymFilesDoesExist() throws Exception
  {
    Verifier verifier = test(testName + "-withDsym", new File(getTestRootDirectory(), "straight-forward/MyApp"), "deploy",
          THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements, new NullProjectModifier());

    File remoteRepoGAVFolder = new File(remoteRepositoryDirectory, "com/sap/ondevice/production/ios/tests/MyApp/" + dynamicVersion);
    File pom = new File(remoteRepoGAVFolder, "MyApp-" + dynamicVersion + ".pom");

    assertTrue("The pom of the app (" + pom + ") does not exist.", pom.isFile());

    assertTrue(
          "Dsym file '"
                + dsymFile
                + "' not does exist. It was expected that dSyms are created.",
          dsymFile.isFile());

    File dsymInsideXcodeBuildDir = new File(verifier.getBasedir(), "target/checkout/src/xcode/build/Release-iphoneos/MyApp.app.dSYM");
    
    assertTrue("dSyms has been created inside the xcode build directory: " + dsymInsideXcodeBuildDir, dsymInsideXcodeBuildDir.exists());
  }

}
