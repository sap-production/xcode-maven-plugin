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

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Test;

public class OTATests extends XCodeTest
{

  @Test
  public void testOTAUrlIsSetToEmpty() throws Exception
  {
    PreparationResult prep = prepareTest();

    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    additionalSystemProperties.put("mios.ota-service.url", "");

    Verifier verifier = runTest(prep, additionalSystemProperties, Collections.<String> emptyList());

    verifier.verifyTextInLog("Unable to convert '' to an URL");
    verifier.verifyTextInLog("java.net.MalformedURLException: no protocol");
  }

  @Test
  public void testOTAUrlIsNotUrl() throws Exception
  {
    PreparationResult prep = prepareTest();

    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    String otaWrongURL = "htp://apple-ota.wdf.sap.corp:8080/ota-service/HTML";
    additionalSystemProperties.put("mios.ota-service.url", otaWrongURL);

    Verifier verifier = runTest(prep, additionalSystemProperties, Collections.<String> emptyList());

    verifier.verifyTextInLog("java.net.MalformedURLException: unknown protocol: htp");
    verifier.verifyTextInLog("Unable to convert '" + otaWrongURL + "' to an URL");
  }

  @Test
  public void testOTABasicWorks() throws Exception
  {
    PreparationResult prep = prepareTest();

    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    String otaCorrectURL = "http://apple-ota.wdf.sap.corp:8080/ota-service/HTML";
    additionalSystemProperties.put("mios.ota-service.url", otaCorrectURL);

    Verifier verifier = runTest(prep, additionalSystemProperties, Collections.<String> emptyList());

    verifier.verifyErrorFreeLog();
    verifier.verifyTextInLog(":generate-ota-html (default-generate-ota-html) @ MyApp");
    verifier.verifyTextInLog("[INFO] OTA HTML file");
    verifier.verifyTextInLog("/MyApp.htm' created for configuration");
    verifier.verifyTextInLog("-Release-iphoneos-ota.htm' written for ");

    String version = prep.pomReplacements.getProperty(PROP_NAME_DYNAMIC_VERSION);
    String artifactPath = verifier.getArtifactPath("com.sap.ondevice.production.ios.tests", "MyApp",
          version, "htm");
    artifactPath = artifactPath.replace(".htm", "-Release-iphoneos-ota.htm"); //not classifier support
    assertFalse(isEmpty(artifactPath));
    System.out.println(artifactPath);

    assertFileContains(new File(artifactPath),
          "src=\"http://apple-ota.wdf.sap.corp:8080/ota-service/HTML?" +
                "title=MyApp&" +
                "bundleIdentifier=com.sap.tip.production.inhouse.epdist&" +
                "bundleVersion=" + version + "&" +
                "ipaClassifier=Release-iphoneos&" +
                "otaClassifier=Release-iphoneos-ota\"",

          "<script language=\"javascript\" type=\"text/javascript\">" //new template with js referer param 
    );
  }

  @Test
  public void testOTACustomTemplateWorks() throws Exception
  {
    PreparationResult prep = prepareTest();

    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    String otaCorrectURL = "http://apple-ota.wdf.sap.corp:8080/ota-service/HTML";
    additionalSystemProperties.put("mios.ota-service.url", otaCorrectURL);
    additionalSystemProperties.put("mios.ota-service.buildHtmlTemplate", new File(".").getAbsolutePath()
          + "/src/test/resources/otaBuildTemplate2.html");

    List<String> addCommandLineParams = new ArrayList<String>();
    addCommandLineParams.add("-DpKey1=pValue1");
    addCommandLineParams.add("-DpKey2=pValue2");

    Verifier verifier = runTest(prep, additionalSystemProperties, addCommandLineParams);

    verifier.verifyErrorFreeLog();
    verifier.verifyTextInLog(":generate-ota-html (default-generate-ota-html) @ MyApp");
    verifier.verifyTextInLog("[INFO] OTA HTML file");
    verifier.verifyTextInLog("/MyApp.htm' created for configuration");
    verifier.verifyTextInLog("-Release-iphoneos-ota.htm' written for ");

    String version = prep.pomReplacements.getProperty(PROP_NAME_DYNAMIC_VERSION);
    String artifactPath = verifier.getArtifactPath("com.sap.ondevice.production.ios.tests", "MyApp",
          version, "htm");
    artifactPath = artifactPath.replace(".htm", "-Release-iphoneos-ota.htm"); //not classifier support
    assertFalse(isEmpty(artifactPath));
    System.out.println(artifactPath);

    assertFileContains(new File(artifactPath),
          "src=\"http://apple-ota.wdf.sap.corp:8080/ota-service/HTML?" +
                "title=MyApp&" +
                "bundleIdentifier=com.sap.tip.production.inhouse.epdist&" +
                "bundleVersion=" + version + "&" +
                "ipaClassifier=Release-iphoneos&" +
                "otaClassifier=Release-iphoneos-ota\"",

          "My Custom Template!",
          "XX MyValue1=pValue1 XX",
          "YY MyValue2=pValue2 YY");
  }

  private void assertFileContains(File file, String... expectedStrings) throws FileNotFoundException, IOException
  {
    String content;
    FileInputStream fis = new FileInputStream(file);
    try {
      content = IOUtils.toString(fis);
    }
    finally {
      IOUtils.closeQuietly(fis);
    }
    for (String expected : expectedStrings) {
      assertTrue("'" + expected + "' not contained in '" + content + "'", content.contains(expected));
    }
  }

  private PreparationResult prepareTest() throws IOException
  {
    final String testName = getTestName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, "1.0." + String.valueOf(System.currentTimeMillis()));

    return new PreparationResult(testName, pomReplacements);
  }

  private static class PreparationResult
  {

    private final String testName;
    private final Properties pomReplacements;

    public PreparationResult(String testName, Properties pomReplacements)
    {
      this.testName = testName;
      this.pomReplacements = pomReplacements;
    }

  }

  private Verifier runTest(PreparationResult prep, Map<String, String> additionalSystemProperties,
        List<String> addCommandLineOptions) throws IOException,
        Exception
  {
    test(prep.testName, new File(getTestRootDirectory(), "straight-forward/MyLibrary"), "deploy", THE_EMPTY_LIST,
          THE_EMPTY_MAP, prep.pomReplacements, new NullProjectModifier());

    final File projectDirectory = new File(getTestRootDirectory(), "straight-forward/MyApp");

    Verifier verifier = new Verifier(getTestExecutionDirectory(prep.testName, projectDirectory.getName())
      .getAbsolutePath());

    try {
      test(verifier, prep.testName, projectDirectory, "deploy",
            addCommandLineOptions, additionalSystemProperties, prep.pomReplacements, new NullProjectModifier());
    }
    catch (VerificationException ex) {
      //
      // This exception is expected.
      // Below we check for the reason in the log file.
      //
    }
    return verifier;
  }

}
