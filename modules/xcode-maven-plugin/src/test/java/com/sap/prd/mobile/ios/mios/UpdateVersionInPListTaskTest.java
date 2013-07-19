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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.LogManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class UpdateVersionInPListTaskTest
{


  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

  private File infoPListMaster = new File("src/test/resources/MyApp-Info.plist");
  private File infoPlist = null;

  @BeforeClass
  public static void setupStatic() {
    LogManager.getLogManager().addLogger(new XCodePluginLogger());
  }
  
  @Before
  public void setup() throws IOException
  {

    infoPlist = tmpFolder.newFile(infoPListMaster.getName());
    FileUtils.copyFile(infoPListMaster, infoPlist);
  }

  @Test
  public void testUpdateCFBundleShortVersionString() throws Exception
  {
    final String newVersion = "1.2." + System.currentTimeMillis();
    new UpdateCFBundleShortVersionStringInPListTask().setPListFile(infoPlist).setVersion(newVersion)
      .execute();
    assertVersion("CFBundleShortVersionString", newVersion, infoPlist);
  }

  @Test
  public void testUpdateCFBundleShortVersionStringWithSnapshotVersion() throws Exception
  {
    final String newVersion = "1.2." + System.currentTimeMillis();
    final String newSnapshotVersion = newVersion + "-SNAPSHOT";
    new UpdateCFBundleShortVersionStringInPListTask().setPListFile(infoPlist).setVersion(newSnapshotVersion)
      .execute();
    assertVersion("CFBundleShortVersionString", newVersion, infoPlist);
  }

  public void testUpdateCFBundleShortVersionStringWithFourDotVersion() throws Exception
  {
    final String newVersion = "1.2.3." + System.currentTimeMillis();
    new UpdateCFBundleShortVersionStringInPListTask().setPListFile(infoPlist).setVersion(newVersion)
      .execute();
    assertVersion("CFBundleVersion", "1.2.3", infoPlist);
  }

  @Test
  public void testUpdateCFBundleVersion() throws Exception
  {
    final String newVersion = "1.2." + System.currentTimeMillis();
    new UpdateCFBundleVersionInPListTask().setPListFile(infoPlist).setVersion(newVersion).execute();
    assertVersion("CFBundleVersion", newVersion, infoPlist);
  }

  @Test
  public void testUpdateCFBundleVersionWithSnapshotVersion() throws Exception
  {
    final String newVersionWithoutSnapshot = "1.2." + System.currentTimeMillis();
    final String newVersionWithSnapshot = newVersionWithoutSnapshot + "-SNAPSHOT";
    new UpdateCFBundleVersionInPListTask().setPListFile(infoPlist).setVersion(newVersionWithSnapshot)
      .execute();
    assertVersion("CFBundleVersion", newVersionWithoutSnapshot, infoPlist);
  }

  private static void assertVersion(final String key, final String version, final File infoPList) throws IOException
  {

    //<key>CFBundleVersion</key>
    //<string>1.2.3</string>

    final BufferedReader br = new BufferedReader(new FileReader(infoPList));

    String versionInPlist = null;

    try {
      for (String line; (line = br.readLine()) != null;) {
        if (!line.trim().equals("<key>" + key + "</key>")) {
          continue;
        }
        else {
          versionInPlist = br.readLine();
          break;
        }
      }

      if (versionInPlist != null) {
        assertEquals("Version update in plist file did not succeed.", "<string>" + version + "</string>",
              versionInPlist.trim());
      }
      else {
        fail(key + " not found in plist.");
      }

    }
    finally {
      IOUtils.closeQuietly(br);
    }
  }
}
