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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.logging.LogManager;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AppIDUpdateTest extends XCodeTest
{

  private static File projectDirectory;
  private static final Log log = new SystemStreamLog();

  @BeforeClass
  public static void setupProjectDirectory()
  {
    projectDirectory = new File(new File(".").getAbsoluteFile(), "target/tests/"
          + AppIDUpdateTest.class.getName());

  }

  @Before
  public void ensureCleanProjectDirectoryAndFilterPom() throws Exception
  {
    log.info("Cleaning directory: " + projectDirectory);
    ensureCleanProjectDirectoryAndFilterPom(projectDirectory);
  }

  @After
  public void cleanupProjectDirectory() throws Exception
  {
    // MacFileUtil.setWritableRecursive(true, projectDirectory);
    // MacFileUtil.deleteDirectory(projectDirectory);
  }

  @Test
  public void testUpdateAppID() throws Exception
  {

    LogManager.getLogManager().addLogger(new XCodePluginLogger());

    File infoPlistFile = new File(projectDirectory, "MyApp/src/xcode/MyApp-Info.plist");

    PListAccessor plistAccessor = new PListAccessor(infoPlistFile);

    assertEquals("Precondition not fulfilled, wrong AppId in Info Plist.", "com.sap.tip.production.inhouse.epdist",
          plistAccessor.getStringValue(PListAccessor.KEY_BUNDLE_IDENTIFIER));

    XCodeChangeAppIDMojo.changeAppId(plistAccessor, "internal", null);

    PListAccessor plist = new PListAccessor(infoPlistFile);
    assertEquals("com.sap.tip.production.inhouse.epdist.internal",
          plist.getStringValue(PListAccessor.KEY_BUNDLE_IDENTIFIER));
  }
}
