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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.util.FileUtils;
import org.junit.Test;

import com.sap.prd.mobile.ios.mios.xcodeprojreader.BuildConfiguration;
import com.sap.prd.mobile.ios.mios.xcodeprojreader.BuildSettings;
import com.sap.prd.mobile.ios.mios.xcodeprojreader.Plist;
import com.sap.prd.mobile.ios.mios.xcodeprojreader.Project;
import com.sap.prd.mobile.ios.mios.xcodeprojreader.ProjectFile;
import com.sap.prd.mobile.ios.mios.xcodeprojreader.jaxb.JAXBPlistParser;

public class XcodeSigningTest extends XCodeTest
{

  /**
   * Checks if the provisioning profile can be overridden via xcode.provisioningProfile property.
   */
  @Test
  public void testProvisioningProfile() throws Exception
  {
    final String testName = getTestName();
    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass()
      .getName());
    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, "1.0." + String.valueOf(System.currentTimeMillis()));

    // copy the simple app project and modify the pbxproj file by adding an invalid provisioning profile
    File intermediateAppDir = new File(new File(".").getAbsolutePath(), "target/tests/"
          + getClass().getName() + "/" + testName + "/intermediate/MyApp");
    FileUtils.copyDirectoryStructure(new File(getTestRootDirectory(), "simple-app/MyApp"), intermediateAppDir);

    String xcodeProjFileName = intermediateAppDir.getCanonicalPath() + "/src/xcode/MyApp.xcodeproj/project.pbxproj";

    JAXBPlistParser parser = new JAXBPlistParser();
    parser.convert(xcodeProjFileName, xcodeProjFileName);
    Plist plist = parser.load(xcodeProjFileName);
    ProjectFile projectFile = new ProjectFile(plist);
    Project project = projectFile.getProject();
    BuildConfiguration config = project.getTargets().getByName("MyApp").getBuildConfigurationList()
      .getBuildConfigurations().getByName("Release");
    assertNotNull("Could not find the 'Release' build configuration", config);
    BuildSettings buildSettings = config.getBuildSettings();
    buildSettings.getDict().setString("PROVISIONING_PROFILE[sdk=iphoneos*]", "InvalidProvisioningProfile");
    parser.save(plist, xcodeProjFileName);

    // This test should fail due to an invalid provisioning profile
    try {
      test(testName, intermediateAppDir, "compile", THE_EMPTY_LIST, THE_EMPTY_MAP,
            pomReplacements, new NullProjectModifier());
      fail("Expected the Maven call to fail due to an invalid provisioning profile.");
    }
    catch (VerificationException ex) {
    }

    // now override the provisioning profile with the xcode.provisioningProfile property
    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    additionalSystemProperties.put("xcode.provisioningProfile", "");
    additionalSystemProperties.put("xcode.app.defaultConfigurations", "Release"); // skip Debug build
    test(testName, intermediateAppDir, "compile", THE_EMPTY_LIST, additionalSystemProperties,
          pomReplacements, new NullProjectModifier());

  }
}
