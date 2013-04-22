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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.junit.Test;

public class DependencyToZipToCopyTest extends XCodeTest
{

  @Test
  public void testPrepare() throws Exception
  {

    final String testName = getTestName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    final File zipRepository = new File(new File(".").getCanonicalFile(), "src/test/zipRepository");

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, "1.0." + String.valueOf(System.currentTimeMillis()));
    pomReplacements.setProperty(PROP_NAME_ZIP_REPO_DIR, zipRepository.getAbsolutePath());

    test(testName, new File(getTestRootDirectory(), "straight-forward/MyApp"),
          "com.sap.prd.mobile.ios.mios:xcode-maven-plugin:" + getMavenXcodePluginVersion() + ":prepare-xcode-build",
          THE_EMPTY_LIST,
          null, pomReplacements, new ProjectModifier() {

            @Override
            void execute() throws Exception
            {
              final File pom = new File(testExecutionDirectory, "pom.xml");
              FileInputStream fis = null;
              FileOutputStream fos = null;

              try {
                fis = new FileInputStream(pom);
                final Model model = new MavenXpp3Reader().read(fis);
                fis.close();
                Xpp3Dom configuration = new Xpp3Dom("configuration");
                StringReader reader = new StringReader(
                      "<configuration><additionalPackagingTypes><html5>COPY</html5></additionalPackagingTypes></configuration>"
                      ); 
                configuration.addChild(Xpp3DomBuilder.build(reader));
                model.getBuild().getPlugins().get(0).setConfiguration(configuration);
                
                model.getDependencies().clear();
                Dependency dep = new Dependency();
                dep.setGroupId("com.sap.ondevice.production.ios.tests");
                dep.setArtifactId("MyZipWithDep");
                dep.setVersion("1.0.0");
                dep.setType("html5");
                model.addDependency(dep);
                
                Plugin plugin = new Plugin();
                plugin.setGroupId("com.sap.prd.types");
                plugin.setArtifactId("types-maven-plugin");
                plugin.setVersion("0.0.1-SNAPSHOT");
                plugin.setExtensions(true);
                model.getBuild().getPlugins().add(plugin);
                
                Repo
                
                fos = new FileOutputStream(pom);
                new MavenXpp3Writer().write(fos, model);
              }
              finally {
                IOUtils.closeQuietly(fis);
                IOUtils.closeQuietly(fos);
              }
            }
          });

    File tmp = new File(getTestExecutionDirectory(testName, "MyApp"), "target/xcode-deps/additional-copied-artifacts/"
          + Constants.GROUP_ID + "/MyZip/MyZip-1.0.0.zip");

    Assert.assertTrue("File '" + tmp + "' not found", tmp.exists());
  }
}
