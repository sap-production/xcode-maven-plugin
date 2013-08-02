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
import java.util.Properties;

import org.apache.maven.it.Verifier;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This tests checks if we are able to handle dependencies which are not
 * of the type xcode-lib or xcode-framework. In order to test this we
 * add simply the xcode-maven-plugin as a normal dependency of type
 * jar (which is anyway the default type). A more realistic test case
 * would be to add a bundle dependency but the effect is the same.
 *
 */
public class ForeignDependencyTest extends XCodeTest
{
  private static File remoteRepositoryDirectory = null;
  private static String dynamicVersion = null, testName = null;

  private static Verifier verifier = null;

  @BeforeClass
  public static void __setup() throws Exception
  {

    dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());
    testName = ForeignDependencyTest.class.getName() + File.separator
          + Thread.currentThread().getStackTrace()[1].getMethodName();

    remoteRepositoryDirectory = getRemoteRepositoryDirectory(ForeignDependencyTest.class.getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);
    
    verifier = test(testName, new File(getTestRootDirectory(), "straight-forward/MyLibrary"), "initialize",
          THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements, new AbstractProjectModifier() {

            @Override
            void execute() throws Exception
            {
              final File pom = new File(testExecutionDirectory, "pom.xml");
              final Model model = getModel(pom);
              final Dependency dependency = new Dependency();
              dependency.setGroupId("com.sap.prd.mobile.ios.mios");
              dependency.setArtifactId("xcode-maven-plugin");
              dependency.setVersion(getMavenXcodePluginVersion());
              dependency.setType("jar");
              model.getDependencies().add(dependency);
              persistModel(pom, model);
            }
    });
  }

  @Test
  public void testForeignDependency() throws Exception
  {
    verifier.verifyTextInLog("[WARNING] Unknown dependency type detected: 'jar'.");
  }
}
