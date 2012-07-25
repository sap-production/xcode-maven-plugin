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

import junit.framework.Assert;

import org.junit.Test;

public class XCodeFrameworkTest extends XCodeTest
{

  @Test
  public void testUseFramework() throws Exception
  {

    final String testName = Thread.currentThread().getStackTrace()[1].getMethodName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    final File frameworkRepository = new File(new File(".").getCanonicalFile(), "src/test/frameworkRepository");

    final Map<String, String> additionalSystemParameters = new HashMap<String, String>();
    additionalSystemParameters.put("configuration", "Release");
    additionalSystemParameters.put("sdk", "iphoneos");

    test(testName, new File(getTestRootDirectory(), "framework/MyApp"), "pom.xml",
          "deploy",
          THE_EMPTY_LIST,
          additionalSystemParameters, remoteRepositoryDirectory, frameworkRepository);

    Assert.assertTrue(new File(getTestExecutionDirectory(testName, "MyApp"), "target/xcode-deps/frameworks/"
          + Constants.GROUP_ID
          + "/MyFramework/MyFramework.framework").exists());

    final String myAppVersionRepoDir = Constants.GROUP_ID_WITH_SLASH + "/MyApp/" + Constants.APP_VERSION;
    final String myAppArtifactFilePrefix = myAppVersionRepoDir + "/MyApp-" + Constants.APP_VERSION;
    File xcodeprojAppZip = new File(remoteRepositoryDirectory, myAppArtifactFilePrefix + "-"
          + XCodePackageXcodeprojMojo.XCODEPROJ_WITH_DEPS_CLASSIFIER + ".zip");
    assertTrue(xcodeprojAppZip.exists());
    assertUnpackAndCompile(xcodeprojAppZip);

  }
}
