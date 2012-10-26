/*
 * #%L
 * xcode-maven-plugin
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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.manager.ArchiverManager;

import com.sap.prd.mobile.ios.mios.task.PackageAppStoreMetaDataTask;

/**
 * Packages the AppStore metadata information and prepares the generated artifact for deployment.
 * 
 * @goal package-metadata
 * 
 */
public class XCodePackageAppStoreMetaDataMojo extends AbstractXCodeMojo
{


  /**
   * @component role="org.codehaus.plexus.archiver.manager.ArchiverManager"
   * @required
   */
  private ArchiverManager archiverManager;

  /**
   * @component
   */
  private MavenProjectHelper projectHelper;

  /**
   * The folders which contains additional metatdata needed for the upload of the app into AppStore.
   *  
   * @parameter expression="${xcode.appStoreMetadataDirectory}"
   *            default-value="${project.basedir}/src/AppStoreMetadata";
   */
  private File appStoreMetadata;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {

    try {

      PackageAppStoreMetaDataTask task = new PackageAppStoreMetaDataTask();
      task.setLog(getLog()).setAppStoreMetadata(appStoreMetadata).setArchiverManager(archiverManager)
        .setMavenProject(project).setProjectHelper(projectHelper).setXcodeSourceDir(getXCodeSourceDirectory())
        .setConfigurations(getConfigurations());
      task.execute();
    }
    catch (Exception e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }

  }
}