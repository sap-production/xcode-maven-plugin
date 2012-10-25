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
package com.sap.prd.mobile.ios.mios.task;

import java.io.File;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import com.sap.prd.mobile.ios.mios.XCodeBuildLayout;
import com.sap.prd.mobile.ios.mios.XCodeException;

public class AttachLibArtifactTask
{
  private Log log;
  private MavenProjectHelper projectHelper;
  private String configuration;
  private String sdk;
  private File xcodeBuildDir;
  private MavenProject mavenProject;

  public AttachLibArtifactTask setMavenProject(MavenProject mavenProject)
  {
    this.mavenProject = mavenProject;
    return this;
  }

  public AttachLibArtifactTask setLog(Log log)
  {
    this.log = log;
    return this;
  }

  public AttachLibArtifactTask setConfiguration(String configuration)
  {
    this.configuration = configuration;
    return this;
  }

  public AttachLibArtifactTask setSdk(String sdk)
  {
    this.sdk = sdk;
    return this;
  }

  public AttachLibArtifactTask setProjectHelper(MavenProjectHelper projectHelper)
  {
    this.projectHelper = projectHelper;
    return this;
  }

  public AttachLibArtifactTask setXcodeBuildDir(File xcodeBuildDir)
  {
    this.xcodeBuildDir = xcodeBuildDir;
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

    File lib = XCodeBuildLayout.getBinary(xcodeBuildDir, configuration, sdk, mavenProject.getArtifactId());

    if (!lib.exists()) {
      throw new XCodeException(lib.getAbsolutePath() + " should be attached as build articact but does not exist.");
      }
    String classifier = configuration + "-" + sdk;

    projectHelper.attachArtifact(mavenProject, "a", classifier, lib);

    log.info("Library file '" + lib + "' attached as side artifact for '" + mavenProject.getArtifact()
          + "' with classifier '" + classifier + "'.");
  }


}
