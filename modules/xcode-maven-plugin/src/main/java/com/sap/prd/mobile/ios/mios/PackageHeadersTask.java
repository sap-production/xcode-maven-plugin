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

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;

class PackageHeadersTask
{
  private Log log;
  private ArchiverManager archiverManager;
  private MavenProjectHelper projectHelper;
  private String configuration;
  private String sdk;
  private File compileDir;
  private MavenProject mavenProject;

  public PackageHeadersTask setMavenProject(MavenProject mavenProject)
  {
    this.mavenProject = mavenProject;
    return this;
  }

  public PackageHeadersTask setLog(Log log)
  {
    this.log = log;
    return this;
  }

  public PackageHeadersTask setConfiguration(String configuration)
  {
    this.configuration = configuration;
    return this;
  }

  public PackageHeadersTask setSdk(String sdk)
  {
    this.sdk = sdk;
    return this;
  }

  public PackageHeadersTask setArchiverManager(ArchiverManager archiverManager)
  {
    this.archiverManager = archiverManager;
    return this;
  }

  public PackageHeadersTask setProjectHelper(MavenProjectHelper projectHelper)
  {
    this.projectHelper = projectHelper;
    return this;
  }

  public PackageHeadersTask setCompileDir(File compileDir)
  {
    this.compileDir = compileDir;
    return this;
  }

  /**
   * PAckages the headers for one configurations and sdk and registers the resulting tar file as
   * Maven artifact
   */
  public void execute() throws XCodeException
  {

    File buildDir = XCodeBuildLayout.getBuildDir(compileDir);
    File headerDir = XCodeBuildLayout.getPublicHeadersDirectory(
          new File(mavenProject.getBuild().getDirectory()), buildDir, configuration, sdk);

    if (!headerDir.canRead())
      return;

    File headersFile = new File(mavenProject.getBuild().getDirectory() + "/" + configuration + "-" + sdk
          + "/headers.tar");

    archive("tar", headerDir, headersFile, new String[] { "**/*.h" }, null);
    log.info("header tar file created (" + headersFile + ")");

    projectHelper.attachArtifact(mavenProject, "headers.tar", configuration + "-" + sdk, headersFile);

    log.info("Headers packaged for configuration '" + configuration + "' and sdk '" + sdk + "' .");

  }

  private final void archive(final String archiverType, final File rootDir, final File archive,
        final String[] includes, final String[] excludes) throws XCodeException
  {
    try {
      final Archiver archiver = archiverManager.getArchiver(archiverType);
      archiver.addDirectory(rootDir, includes, excludes);
      archiver.setDestFile(archive);
      archiver.createArchive();
    }
    catch (Exception ex) {
      throw new XCodeException("Could not archive folder '" + rootDir + "' into '" + archive + "': " + ex.getMessage(),
            ex);
    }
  }

}
