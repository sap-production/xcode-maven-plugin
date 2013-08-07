package com.sap.prd.mobile.ios.mios;

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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Copied from ModuleBuild Test. Used to test the CleanMojo. Checking if redirect html files are
 * cleaned correctly.
 */
public class CleanMojoTest extends XCodeTest
{
  private static File remoteRepositoryDirectory = null;
  private static String dynamicVersion = null, testName = null;

  private static File testOneArchiveDirectoryForAll;

  @BeforeClass
  public static void __setup() throws Exception
  {
    dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());
    testName = CleanMojoTest.class.getName() + File.separator
          + Thread.currentThread().getStackTrace()[1].getMethodName();

    remoteRepositoryDirectory = getRemoteRepositoryDirectory(CleanMojoTest.class.getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);

    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    testOneArchiveDirectoryForAll = getTestExecutionDirectory(testName, "moduleBuild/target/artifactRedirect");
    additionalSystemProperties.put("archive.dir", testOneArchiveDirectoryForAll.getAbsolutePath());
    additionalSystemProperties.put("mios.ota-service.url", "http://apple-ota.wdf.sap.corp:8080/ota-service/HTML");

    test(new XCodeTestParameters(null, testName, new File(getTestRootDirectory(), "moduleBuild"), asList("clean", "deploy"),
          THE_EMPTY_LIST, additionalSystemProperties, pomReplacements, new NullProjectModifier()));
  }

  @Test
  public void testRedirectFileForArtifactsOfLibrary() throws IOException, IOException
  {
    testRedirectFile(testOneArchiveDirectoryForAll, "MyLibrary", "1.0.1", "", "pom");
    testRedirectFile(testOneArchiveDirectoryForAll, "MyLibrary", "1.0.1", "", "tar");
    testRedirectFile(testOneArchiveDirectoryForAll, "MyLibrary", "1.0.1", "-Release-iphoneos.headers", "tar");
    testRedirectFile(testOneArchiveDirectoryForAll, "MyLibrary", "1.0.1", "-Release-fat-binary", "a");
    testRedirectFile(testOneArchiveDirectoryForAll, "MyLibrary", "1.0.1", "-Release-iphoneos", "a");
  }

  @Test
  public void testRedirectFileForArtifactsOfApplication() throws IOException, IOException
  {
    testRedirectFile(testOneArchiveDirectoryForAll, "MyApp", "1.0.0", "", "pom");
    testRedirectFile(testOneArchiveDirectoryForAll, "MyApp", "1.0.0", "-Release-iphoneos-app", "zip");
    testRedirectFile(testOneArchiveDirectoryForAll, "MyApp", "1.0.0", "-Release-iphoneos-app.dSYM", "zip");
    testRedirectFile(testOneArchiveDirectoryForAll, "MyApp", "1.0.0", "-Release-iphoneos", "ipa");
    testRedirectFile(testOneArchiveDirectoryForAll, "MyApp", "1.0.0", "-Release-iphoneos-ota", "htm");
  }

  private void testRedirectFile(final File testArchiveDir, final String projecName, final String version,
        String classifier, String type) throws IOException
  {
    final File redirectFile = new File(testArchiveDir,
          "/artifacts/com.sap.ondevice.production.ios.tests/" + projecName + "/" + projecName + classifier + "."
                + (type.equals("htm") ? type : (type + ".htm")));
    assertTrue(redirectFile.getName() + " does not exist at " + redirectFile.getAbsolutePath(), redirectFile.exists());

    InputStream is = null;
    try {
      is = new FileInputStream(redirectFile);
      final String content = IOUtils.toString(is);
      assertTrue("OTA redirect file (" + redirectFile + ") " +
            "for the pom file of the Appliction is invalid. It does not contain a valid reference to "
            + projecName + " pom file.",
            content.contains("/com/sap/ondevice/production/ios/tests/" + projecName + "/" + version + "/"
                  + projecName + "-" + version + classifier + "." + type));
    }
    finally {
      IOUtils.closeQuietly(is);
    }

  }

}