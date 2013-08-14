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

import java.io.File;
import java.util.Properties;

import org.junit.Test;

public class XCodeDSymTest extends XCodeTest
{

  @Test
  public void testDSYM() throws Exception
  {

    final String dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());
    final String testName = getTestName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass()
      .getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);

    test(testName, new File(getTestRootDirectory(), "straight-forward/MyLibrary"),
          "deploy", THE_EMPTY_LIST,
          THE_EMPTY_MAP, pomReplacements, new NullProjectModifier());

    test(testName, new File(
          getTestRootDirectory(), "straight-forward/MyApp"),
          "deploy", THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements, new NullProjectModifier());

    final String configuration = "Release";

    File dsymUploadFile = new File(remoteRepositoryDirectory,
            TestConstants.GROUP_ID_WITH_SLASH + "/MyApp/" + dynamicVersion + "/MyApp-" + dynamicVersion + "-"
                    + configuration + "-iphoneos-app.dSYM.zip");
    
    assertTrue(dsymUploadFile.exists());
    
    final File tmpFolder = new File(getTargetDirectory(), "tests/tmp");
    tmpFolder.deleteOnExit();
    File dsymFolder = new File(tmpFolder, "dsym");
    dsymFolder.deleteOnExit();
    extractFileWithShellScript(dsymUploadFile, dsymFolder);

    assertTrue(new File(dsymFolder, "MyApp.app.dSYM").exists());
    assertTrue(new File(new File(new File(dsymFolder, "MyApp.app.dSYM"),"Contents"), "Info.plist").exists());
  }
}
