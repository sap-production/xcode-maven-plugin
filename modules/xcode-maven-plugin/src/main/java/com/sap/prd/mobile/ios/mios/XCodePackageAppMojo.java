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
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

/**
 * Packages the application built by Xcode and prepares the generated artifact for deployment.
 * 
 * @goal package-application
 * 
 */
public class XCodePackageAppMojo extends BuildContextAwareMojo
{
  /**
   * @component
   */
  private MavenProjectHelper projectHelper;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {

    for (final String sdk : getSDKs()) {
      for (final String config : getConfigurations()) {
        packageAndAttachAppFolder(sdk, config);
      }
    }

  }

  private void packageAndAttachAppFolder(String sdk, String config) throws MojoExecutionException
  {

    final String productName = getProductName(config, sdk);

    final String fixedProductName = getFixedProductName(productName);

    final File rootDir = XCodeBuildLayout.getAppFolder(getXCodeCompileDirectory(), config, sdk);

    final File destination = zipSubfolder(rootDir, productName + ".app", fixedProductName + ".app.zip", null);

    getLog().info("Application file packaged (" + destination + ")");

    prepareApplicationFileForDeployment(project, config, sdk, destination);
  }

  private void prepareApplicationFileForDeployment(final MavenProject mavenProject, final String configuration,
        final String sdk, final File applicationFile)
  {

    projectHelper.attachArtifact(mavenProject, "zip", configuration + "-" + sdk + "-app", applicationFile);
  }

}
