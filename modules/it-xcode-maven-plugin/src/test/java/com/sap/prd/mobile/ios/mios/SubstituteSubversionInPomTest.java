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

import org.apache.maven.it.util.FileUtils;
import org.junit.Test;

public class SubstituteSubversionInPomTest extends XCodeTest
{

  private static final String PROJECT_SUBVERSION_PROPNAME = "xcode.projectSubVersion";

  /**
   * Checks if the parameterized project version gets correctly uploaded into the repository
   */
  @Test
  public void testProjectSubVersionPropReplacement() throws Exception
  {
    final String testName = Thread.currentThread().getStackTrace()[1].getMethodName();
    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());
    prepareRemoteRepository(remoteRepositoryDirectory);

    // copy the app project
    File intermediateAppDir = new File(new File(".").getAbsolutePath(), "target/tests/"
          + getClass().getName() + "/" + testName + "/intermediate/MyApp");
    FileUtils.copyDirectoryStructure(new File(getTestRootDirectory(), "var-version-app/MyApp"), intermediateAppDir);

    // now override the provisioning profile with the xcode.provisioningProfile property
    Map<String, String> additionalSystemProperties = new HashMap<String, String>();
    additionalSystemProperties.put(PROJECT_SUBVERSION_PROPNAME, ".666-MILESTONE");
    test(testName, intermediateAppDir, "pom.xml", "deploy", THE_EMPTY_LIST, additionalSystemProperties,
          remoteRepositoryDirectory);

    File deployedPom = new File(remoteRepositoryDirectory, "com/sap/ondevice/production/ios/tests/MyApp/1.0.666-MILESTONE/MyApp-1.0.666-MILESTONE.pom");
    assertTrue(deployedPom.isFile());
    String pomContent = FileUtils.fileRead(deployedPom);
    assertTrue(pomContent.contains(".666-MILESTONE</version>"));

  }
}
