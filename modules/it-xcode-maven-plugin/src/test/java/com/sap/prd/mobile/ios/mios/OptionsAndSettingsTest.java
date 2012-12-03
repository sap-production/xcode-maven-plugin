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

import java.io.File;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

public class OptionsAndSettingsTest extends XCodeTest
{
  private static File remoteRepositoryDirectory = null;
  private static String dynamicVersion = null,
        testName = null;

  @BeforeClass
  public static void __setup() throws Exception {

    dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());
    testName = OptionsAndSettingsTest.class.getName() +  File.separator + Thread.currentThread().getStackTrace()[1].getMethodName();

    remoteRepositoryDirectory = getRemoteRepositoryDirectory(OptionsAndSettingsTest.class.getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);

    test(testName, new File(getTestRootDirectory(), "optionsAndSettings/MyLibrary"), "compile",
          THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements);
  }
  
  @Test
  public void testStraightForward() throws Exception
  {
  }
}
