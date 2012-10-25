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
import java.io.IOException;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import com.sap.prd.mobile.ios.mios.AbstractXCodeBuildMojo;
import com.sap.prd.mobile.ios.mios.EffectiveBuildSettings;
import com.sap.prd.mobile.ios.mios.ScriptRunner;
import com.sap.prd.mobile.ios.mios.XCodeBuildLayout;
import com.sap.prd.mobile.ios.mios.XCodeException;

public class PackageAppTask
{

  private Log log;
  private String productName;
  private MavenProjectHelper projectHelper;
  private File compileDir;
  private MavenProject mavenProject;

  private String configuration;

  private String sdk;

  public PackageAppTask setLog(Log log)
  {
    this.log = log;
    return this;
  }

  public PackageAppTask setProductName(String productName)
  {
    this.productName = productName;
    return this;
  }

  public PackageAppTask setMavenProject(MavenProject mavenProject)
  {
    this.mavenProject = mavenProject;
    return this;
  }

  public PackageAppTask setConfiguration(String configuration)
  {
    this.configuration = configuration;
    return this;
  }

  public PackageAppTask setSdk(String sdk)
  {
    this.sdk = sdk;
    return this;
  }

  public PackageAppTask setProjectHelper(MavenProjectHelper projectHelper)
  {
    this.projectHelper = projectHelper;
    return this;
  }

  public PackageAppTask setCompileDir(File compileDir)
  {
    this.compileDir = compileDir;
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

    try {

      final String productName;

      if (this.productName != null) {
        productName = this.productName.trim();

        if (productName.isEmpty())
          throw new IllegalStateException("ProductName from pom file was empty.");

      }
      else {
        productName = EffectiveBuildSettings.getProductName(mavenProject, configuration, sdk);

        if (productName == null || productName.isEmpty())
          throw new IllegalStateException("Product Name not found in effective build settings file");
      }

      final String fixedProductName = AbstractXCodeBuildMojo.getFixedProductName(productName);

      final File rootDir = XCodeBuildLayout.getAppFolder(compileDir, configuration, sdk);

      final File appZipFile = ScriptRunner.zipSubfolder(
            new File(mavenProject.getBuild().getDirectory(), "scripts").getCanonicalFile(),
            rootDir, productName + ".app", fixedProductName + ".app.zip", null);

      log.info("Application file packaged (" + appZipFile + ")");

      projectHelper.attachArtifact(mavenProject, "zip", configuration + "-" + sdk + "-app", appZipFile);
    }
    catch (IOException ex) {
      throw new XCodeException("Could not package the app: " + ex.getMessage(), ex);
    }
  }

}
