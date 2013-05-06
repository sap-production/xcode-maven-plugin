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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.BeforeClass;
import org.junit.Test;

public class AlternatePublicHeaderFolderPathTest extends XCodeTest
{
  private static File remoteRepositoryDirectory = null;
  private static String dynamicVersion = null,
        testName = null;


  @BeforeClass
  public static void __setup() throws Exception
  {

    dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());
    testName = AlternatePublicHeaderFolderPathTest.class.getName() + File.separator
          + Thread.currentThread().getStackTrace()[1].getMethodName();

    remoteRepositoryDirectory = getRemoteRepositoryDirectory(AlternatePublicHeaderFolderPathTest.class.getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);

    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    additionalSystemProperties.put("mios.ota-service.url", "http://apple-ota.wdf.sap.corp:8080/ota-service/HTML");
    additionalSystemProperties.put("xcode.app.defaultConfigurations", "Release");
    additionalSystemProperties.put("xcode.app.defaultSdks", "iphoneos");
    additionalSystemProperties.put("archive.dir", "archive");
    additionalSystemProperties.put("xcode.useSymbolicLinks", Boolean.TRUE.toString());

    test(testName, new File(getTestRootDirectory(), "straight-forward/MyLibrary"), "deploy",
          THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements, new AbstractProjectModifier() {

      @Override
      public void execute() throws Exception
      {
        final File pom = new File(testExecutionDirectory, "pom.xml");

        final Model model = getModel(pom);
        Plugin plugin = model.getBuild().getPlugins().get(0);
        Xpp3Dom configuration =  (Xpp3Dom)plugin.getConfiguration();
        Xpp3Dom alternatePublicHeaderFolderPath = new Xpp3Dom("alternatePublicHeaderFolderPath");
        alternatePublicHeaderFolderPath.setValue("MyLibrary");
        configuration.addChild(alternatePublicHeaderFolderPath);

          persistModel(pom, model);
        }
    });
  }

  @Test
  public void testHeadersWithPrefix() throws Exception
  {
    File headersTar = new File(remoteRepositoryDirectory, "com/sap/ondevice/production/ios/tests/MyLibrary/" + dynamicVersion + "/MyLibrary-" + dynamicVersion + "-Release-iphoneos.headers.tar");

    Assert.assertTrue("Headers tar file '" + headersTar + "' does not exist", headersTar.exists());
    
    ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
    PrintStream out =new PrintStream(byteOs);

    try {
      Forker.forkProcess(out, null, new String[] {"tar", "-tf", headersTar.getAbsolutePath()});
    } finally {
      IOUtils.closeQuietly(out);
    }
      final String toc = new String(byteOs.toByteArray());
      final String expectedContent = "include/PrintOutObject.h";
      Assert.assertTrue("Table of content of the headers tar file '" + headersTar
          + "' does not contain the expected content '" + expectedContent + "'. Table of content is: " + toc,
          toc.contains(expectedContent));
  }
}
