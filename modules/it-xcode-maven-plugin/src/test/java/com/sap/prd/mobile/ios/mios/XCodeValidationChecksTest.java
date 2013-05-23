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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Test;

public class XCodeValidationChecksTest extends XCodeTest
{
  @Test
  public void testValidationCheckSkipped() throws Exception
  {
    final String dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());
    final String testName = getTestName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);

    Verifier verifier = test(testName, new File(getTestRootDirectory(), "straight-forward/MyApp"),
          "com.sap.prd.mobile.ios.mios:xcode-maven-plugin:"
                + getMavenXcodePluginVersion() + ":validation-check",
          THE_EMPTY_LIST,
          null, pomReplacements, new NullProjectModifier());

    String message = "Verification check goal has been skipped intentionally since parameter 'xcode.verification.checks.skip' is 'true'.";

    try {
      verifier.verifyTextInLog(message);
    }
    catch (VerificationException ex) {
      Assert.fail("Expected log message (" + message + ") was not present.");
    }
  }

  @Test
  public void testValidationCheckNoProtocolSpecified() throws Exception
  {
    final String dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());
    final String testName = getTestName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);

    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    additionalSystemProperties.put("xcode.verification.checks.skip", "false");
    additionalSystemProperties.put("xcode.verification.checks.definitionFile", (new File(".",
          "src/test/resources/verifications-error.xml")).getAbsolutePath());

    try {
      test(testName, new File(getTestRootDirectory(), "straight-forward/MyApp"),
            "com.sap.prd.mobile.ios.mios:xcode-maven-plugin:"
                  + getMavenXcodePluginVersion() + ":validation-check",
            THE_EMPTY_LIST,
            additionalSystemProperties, pomReplacements, new NullProjectModifier());
    }
    catch (Exception ex) {
      assertTrue(ex.getMessage().contains(
            "Provide a protocol http, file for parameter'xcode.verification.checks.definitionFile'"));
    }
  }

  @Test
  public void testValidationCheckClassPathNotExtendedSpecified() throws Exception
  {
    final String dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());
    final String testName = getTestName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);

    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    additionalSystemProperties.put("xcode.verification.checks.skip", "false");
    additionalSystemProperties.put("xcode.verification.checks.definitionFile", "file:"
          + (new File(".", "src/test/resources/verifications-error.xml")).getAbsolutePath());

    try {
      test(testName, new File(getTestRootDirectory(), "straight-forward/MyApp"),
            "com.sap.prd.mobile.ios.mios:xcode-maven-plugin:"
                  + getMavenXcodePluginVersion() + ":validation-check",
            THE_EMPTY_LIST,
            additionalSystemProperties, pomReplacements, new NullProjectModifier());
    }
    catch (Exception ex) {
      assertTrue(ex.getMessage().contains("May be your classpath has not been properly extended."));
      assertTrue(ex.getMessage().contains(
            "Additional dependencies need to be provided with 'xcode.additionalClasspathElements'."));
    }
  }
}
