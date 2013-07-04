package com.sap.prd.mobile.ios.mios;

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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * 
 * @goal clean
 * 
 */
public class CleanMojo extends AbstractMojo
{

  /**
   * The folder used for hudson archiving
   * 
   * @parameter expression="${archive.dir}" default-value="${project.build.directory}"
   * @readonly
   */
  private File archiveDirectory;

  /**
   * @parameter default-value="${project.build.directory}"
   * @readonly
   */
  private File buildDirectory;

  /**
   * @parameter expression="${basedir}"
   * @readonly
   */
  private File baseDirectory;

  /**
   * @parameter expression="${project}"
   * @readonly
   * @required
   */
  protected MavenProject project;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {

    try {

      if (archiveDirectory.getCanonicalFile().equals(buildDirectory.getCanonicalFile()))
      {
        getLog().info(
              "Archive directory '" + archiveDirectory + "' equals build directory '" + buildDirectory
                    + ". This directory will be cleaned by the standard maven clean plugin. Nothing todo.");
        return;
      }

      if (archiveDirectory.getCanonicalFile().equals(baseDirectory.getCanonicalFile()))
      {
        getLog().error(
              "Archive directory '" + archiveDirectory + "' equals base directory '" + baseDirectory
                    + ". This directory will not be removed.");
        return;
      }

      File projectArchiveFolder = PreDeployMojo.getProjectArchiveFolder(archiveDirectory, project);
      getLog().info("Delete '" + projectArchiveFolder + "'.");
      FileUtils.deleteDirectory(projectArchiveFolder);
    }
    catch (IOException e) {
      throw new MojoFailureException(e.getMessage(), e);
    }
  }
}
