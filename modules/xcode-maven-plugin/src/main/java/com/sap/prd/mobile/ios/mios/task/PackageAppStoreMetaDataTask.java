package com.sap.prd.mobile.ios.mios.task;

/*
 * #%L
 * Xcode Maven Plugin
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

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;

import com.sap.prd.mobile.ios.mios.PListAccessor;
import com.sap.prd.mobile.ios.mios.XCodeException;
import com.sap.prd.mobile.ios.mios.buddy.PlistAccessorBuddy;

public class PackageAppStoreMetaDataTask
{
  private Log log;
  private MavenProject mavenProject;
  private File appStoreMetadata;
  private ArchiverManager archiverManager;
  private MavenProjectHelper projectHelper;
  private Set<String> configurations;
  private File xcodeSourceDir;

  public PackageAppStoreMetaDataTask setLog(Log log)
  {
    this.log = log;
    return this;
  }

  public PackageAppStoreMetaDataTask setMavenProject(MavenProject mavenProject)
  {
    this.mavenProject = mavenProject;
    return this;
  }

  public PackageAppStoreMetaDataTask setAppStoreMetadata(File appStoreMetadata)
  {
    this.appStoreMetadata = appStoreMetadata;
    return this;
  }

  public PackageAppStoreMetaDataTask setArchiverManager(ArchiverManager archiverManager)
  {
    this.archiverManager = archiverManager;
    return this;
  }

  public PackageAppStoreMetaDataTask setProjectHelper(MavenProjectHelper projectHelper)
  {
    this.projectHelper = projectHelper;
    return this;
  }

  public PackageAppStoreMetaDataTask setConfigurations(Set<String> configurations)
  {
    this.configurations = configurations;
    return this;
  }

  public PackageAppStoreMetaDataTask setXcodeSourceDir(File xcodeSourceDir)
  {
    this.xcodeSourceDir = xcodeSourceDir;
    return this;
  }

  public void execute() throws XCodeException
  {

    try {
      final String bundleIdentifier = getBundleIdentifier();
      final File appStoreMetaDataFolder = new File(appStoreMetadata, bundleIdentifier);

      if (appStoreMetaDataFolder.exists() && appStoreMetaDataFolder.list().length != 0) {

        File appStoreMetaDataFile = new File(new File(mavenProject.getBuild().getDirectory()), "AppStoreMetadata.zip");

        Archiver archiver = archiverManager.getArchiver("zip");
        archiver.addDirectory(appStoreMetaDataFolder, new String[] { "**/*" }, null);
        archiver.setDestFile(appStoreMetaDataFile);
        archiver.createArchive();
        log.info("AppStore MetaData packaged in (" + appStoreMetaDataFile + ")");

        projectHelper.attachArtifact(mavenProject, "zip", "AppStoreMetaData", appStoreMetaDataFile);
      }
      else {
        log.info(
          "AppStore MetaData packaging skipped. Folder " + appStoreMetaDataFolder.getAbsolutePath()
                + " does not exist .");
      }
    }
    catch (Exception ex) {
      throw new XCodeException("Could not package the app store meta data: " + ex.getMessage(), ex);
    }

  }

  private String getBundleIdentifier() throws IOException, XCodeException
  {
    String bundleIdentifier = null;
    for (String configuration : configurations) {

      PListAccessor plistAccessor = PlistAccessorBuddy.getInfoPListAccessor(mavenProject, xcodeSourceDir,
            configuration, "iphoneos");
      String _bundleIdentifier = plistAccessor.getStringValue(PListAccessor.KEY_BUNDLE_IDENTIFIER);

      if (bundleIdentifier == null)
        bundleIdentifier = _bundleIdentifier;
      else if (!bundleIdentifier.equals(_bundleIdentifier))
        throw new IllegalStateException("Different bundle identifiers defined (" + _bundleIdentifier + "/"
              + bundleIdentifier + ")");
    }
    return bundleIdentifier;
  }


}
