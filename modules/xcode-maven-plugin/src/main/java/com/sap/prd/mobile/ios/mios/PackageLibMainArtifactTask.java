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
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;

class PackageLibMainArtifactTask
{
  private Log log;
  private ArchiverManager archiverManager;
  private MavenProject mavenProject;

  public PackageLibMainArtifactTask setLog(Log log)
  {
    this.log = log;
    return this;
  }

  public PackageLibMainArtifactTask setArchiverManager(ArchiverManager archiverManager)
  {
    this.archiverManager = archiverManager;
    return this;
  }

  public PackageLibMainArtifactTask setMavenProject(MavenProject mavenProject)
  {
    this.mavenProject = mavenProject;
    return this;
  }

  /**
   * Packages all the artifacts. The main artifact is set and all side artifacts are attached for
   * deployment.
   * 
   * @param bundles
   * 
   * @param buildDir
   */
  public void execute() throws XCodeException
  {
    File mainArtifactDir = MavenBuildFolderLayout.getFolderForExtractedMainArtifact(mavenProject);
    final File mainArtifactFile = archiveMainArtifact(mavenProject, mainArtifactDir);
    setMainArtifact(mavenProject, mainArtifactFile);
  }

  private File archiveMainArtifact(final MavenProject project, File mainArtifact) throws XCodeException
  {
    final File mainArtifactTarFile = new File(new File(project.getBuild().getDirectory()), "main.artifact.tar");

    archive("tar", mainArtifact, mainArtifactTarFile, new String[] { "**/*" }, null);
    log.info("header tar file created (" + mainArtifactTarFile + ")");

    return mainArtifactTarFile;
  }

  private void setMainArtifact(final MavenProject project, final File mainArtifactTarFile)
  {
    project.getArtifact().setFile(mainArtifactTarFile);
    log.info("Main artifact file '" + mainArtifactTarFile + "' attached for " + project.getArtifact());
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
