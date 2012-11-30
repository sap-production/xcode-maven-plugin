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

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Test;

public class OTATests extends XCodeTest
{
  @Test
  public void testOTAUrlIsSetToEmpty() throws Exception
  {
    final String testName = getTestName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, "1.0" + String.valueOf(System.currentTimeMillis()));

    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    additionalSystemProperties.put("mios.ota-service.url", "");
    test(testName, new File(getTestRootDirectory(), "straight-forward/MyLibrary"), "deploy", THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements, new NullProjectModifier());
    
    final File projectDirectory = new File(getTestRootDirectory(), "straight-forward/MyApp");
    Verifier verifier = new Verifier(getTestExecutionDirectory(testName, projectDirectory.getName()).getAbsolutePath());
    try {
      verifier = test(verifier, testName, new File(getTestRootDirectory(), "straight-forward/MyApp"),
            "deploy", THE_EMPTY_LIST, additionalSystemProperties, pomReplacements, new NullProjectModifier());

    }
    catch (VerificationException e) {
      //
      // This exception is expected.
      // Below we check for the reason in the log file.
      //
    }

    verifier.verifyTextInLog("Unable to convert '' to an URL");
    verifier.verifyTextInLog("java.net.MalformedURLException: no protocol");
  }

  @Test
  public void testOTAUrlIsNotUrl() throws Exception
  {
    final String testName = getTestName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, "1.0." + String.valueOf(System.currentTimeMillis()));

    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    String otaWrongURL = "htp://apple-ota.wdf.sap.corp:8080/ota-service/HTML";
    additionalSystemProperties.put("mios.ota-service.url", otaWrongURL);

    test(testName, new File(getTestRootDirectory(), "straight-forward/MyLibrary"), "deploy", THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements, new NullProjectModifier());
    
    final File projectDirectory = new File(getTestRootDirectory(), "straight-forward/MyApp");

    Verifier verifier = new Verifier(getTestExecutionDirectory(testName, projectDirectory.getName()).getAbsolutePath());

    try {
      test(verifier, testName, projectDirectory, "deploy",
            THE_EMPTY_LIST,
            additionalSystemProperties, pomReplacements, new NullProjectModifier());
    }
    catch (VerificationException ex) {
      //
      // This exception is expected.
      // Below we check for the reason in the log file.
      //
    }

    verifier.verifyTextInLog("java.net.MalformedURLException: unknown protocol: htp");
    verifier.verifyTextInLog("Unable to convert '" + otaWrongURL + "' to an URL");

  }
}
