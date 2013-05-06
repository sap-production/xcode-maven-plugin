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
import java.util.Properties;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Test;

public class DependencyToZipToBundleTestWithInvalidUnarchiverId extends XCodeTest
{

  @Test(expected=VerificationException.class)
  public void testPrepare() throws Throwable
  {

    final String testName = getTestName();

    final File testSourceDirApp = new File(getTestRootDirectory(), "straight-forward/MyApp");
    final File alternateTestSourceDirApp = new File(getTestRootDirectory(), "straight-forward-transitive-bundles");

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    final File zipRepository = new File(new File(".").getCanonicalFile(), "src/test/zipRepository");

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, "1.0." + String.valueOf(System.currentTimeMillis()));
    pomReplacements.setProperty(PROP_NAME_ZIP_REPO_DIR, zipRepository.getAbsolutePath());

    final ProjectModifier projectModifier = new ChainProjectModifier(new FileCopyProjectModifier(
          alternateTestSourceDirApp, "pom.xml"), new ProjectModifier() {

      @Override
      public void execute() throws Exception
      {
        final File pom = new File(testExecutionDirectory, "pom.xml");
        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
          fis = new FileInputStream(pom);
          final Model model = new MavenXpp3Reader().read(fis);
          fis.close();
          fos = new FileOutputStream(pom);
          Plugin plugin = model.getBuild().getPlugins().get(0);
          Xpp3Dom config = ((Xpp3Dom) plugin.getConfiguration());
          config.getChild("additionalPackagingTypes").getChild("html5").setValue("BUNDLE");
          
          Xpp3Dom packagingTypeDescriptors = new Xpp3Dom("packagingTypeDescriptors");
          Xpp3Dom packagingTypeDescriptor = new Xpp3Dom("packagingTypeDescriptor");
          Xpp3Dom packagingType = new Xpp3Dom("packagingType");
          Xpp3Dom action = new Xpp3Dom("action");
          Xpp3Dom unarchiverId = new Xpp3Dom("unarchiverId");
          packagingType.setValue("html5");
          action.setValue("BUNDLE");
          unarchiverId.setValue("hugooo");

          packagingTypeDescriptor.addChild(packagingType);
          packagingTypeDescriptor.addChild(action);
          packagingTypeDescriptor.addChild(unarchiverId);
          packagingTypeDescriptors.addChild(packagingTypeDescriptor);
          config.addChild(packagingTypeDescriptors);
          new MavenXpp3Writer().write(fos, model);
        }
        finally {
          IOUtils.closeQuietly(fis);
          IOUtils.closeQuietly(fos);
        }
      }
    });

    
    Verifier v = new Verifier(getTestExecutionDirectory(testName, "MyApp").getAbsolutePath());

    try {
    test(v, testName, testSourceDirApp,
          "com.sap.prd.mobile.ios.mios:xcode-maven-plugin:" + getMavenXcodePluginVersion() + ":prepare-xcode-build",
          THE_EMPTY_LIST,
          null, pomReplacements, projectModifier);
        Assert.fail("An exception was expected durin test but did not occure.");
    } catch(Exception ex) {
       v.verifyTextInLog(" org.codehaus.plexus.archiver.manager.NoSuchArchiverException: No such archiver: 'hugooo'.");
       throw ex;
    }
  }

}
