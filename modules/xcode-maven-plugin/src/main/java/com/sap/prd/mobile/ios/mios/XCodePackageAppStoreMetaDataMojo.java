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
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

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

      packageAndAttachAppStoreMetaData();

    }
    catch (IOException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
    catch (NoSuchArchiverException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
    catch (ArchiverException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }

  }

  private void packageAndAttachAppStoreMetaData() throws IOException, NoSuchArchiverException, ArchiverException,
        MojoExecutionException
  {

    final String bundleIdentifier = getBundleIdentifier();

    final File appStoreMetaDataFolder = new File(appStoreMetadata, bundleIdentifier);

    if (appStoreMetaDataFolder.exists() && appStoreMetaDataFolder.list().length != 0) {

      Archiver archiver = archiverManager.getArchiver("zip");

      File destination = new File(new File(project.getBuild().getDirectory()), "AppStoreMetadata.zip");

      archiver.addDirectory(appStoreMetaDataFolder, new String[] { "**/*" }, null);
      archiver.setDestFile(destination);
      archiver.createArchive();
      getLog().info("AppStore MetaData packaged in (" + destination + ")");

      prepareAppStoreMetaDataFileForDeployment(project, destination);
    }
    else {
      getLog().info(
            "AppStore MetaData packaging skipped. Folder " + appStoreMetaDataFolder.getAbsolutePath()
                  + " doesn't exist.");
    }

  }

  private String getBundleIdentifier() throws MojoExecutionException
  {
    String bundleIdentifier = null;
    for (String configuration : getConfigurations()) {
      try {
        PListAccessor plistAccessor = getInfoPListAccessor(configuration, "iphoneos");
        String _bundleIdentifier = plistAccessor.getStringValue(PListAccessor.KEY_BUNDLE_IDENTIFIER);

        if (bundleIdentifier == null)
          bundleIdentifier = _bundleIdentifier;
        else if (!bundleIdentifier.equals(_bundleIdentifier))
          throw new IllegalStateException("Different bundle identifiers defined (" + _bundleIdentifier + "/"
                + bundleIdentifier + ")");
      }
      catch (IOException e) {
        throw new MojoExecutionException(e.getMessage(), e);
      }

    }
    return bundleIdentifier;
  }

  private void prepareAppStoreMetaDataFileForDeployment(final MavenProject mavenProject, final File appStoreMetaDataFile)
  {

    projectHelper.attachArtifact(mavenProject, "zip", "AppStoreMetaData", appStoreMetaDataFile);
  }

}
