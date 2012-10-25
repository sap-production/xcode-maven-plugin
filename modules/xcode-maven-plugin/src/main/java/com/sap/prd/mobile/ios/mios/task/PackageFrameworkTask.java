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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import com.sap.prd.mobile.ios.mios.EffectiveBuildSettings;
import com.sap.prd.mobile.ios.mios.Forker;
import com.sap.prd.mobile.ios.mios.FrameworkStructureValidator;
import com.sap.prd.mobile.ios.mios.XCodeException;

public class PackageFrameworkTask
{
  private Log log;
  private MavenProject mavenProject;
  private String primaryFmwkConfiguration;


  public PackageFrameworkTask setLog(Log log)
  {
    this.log = log;
    return this;
  }

  public PackageFrameworkTask setMavenProject(MavenProject mavenProject)
  {
    this.mavenProject = mavenProject;
    return this;
  }

  public PackageFrameworkTask setPrimaryFmwkConfiguration(String primaryFmwkConfiguration)
  {
    this.primaryFmwkConfiguration = primaryFmwkConfiguration;
    return this;
  }

  public void execute() throws XCodeException
  {

    EffectiveBuildSettings effBuildSettings = new EffectiveBuildSettings(mavenProject, primaryFmwkConfiguration,
          "iphoneos");
    String productName = effBuildSettings.getBuildSetting(EffectiveBuildSettings.PRODUCT_NAME);
    String builtProductsDirName = effBuildSettings.getBuildSetting(EffectiveBuildSettings.BUILT_PRODUCTS_DIR);

    File builtProductsDir = new File(builtProductsDirName);
    String fmwkDirName = productName + ".framework";
    File fmwkDir = new File(builtProductsDir, fmwkDirName);

    validateFrmkStructure(fmwkDir);

    String artifactName = productName + ".xcode-framework-zip";
    zipFmwk(builtProductsDir, artifactName, fmwkDirName);

    File mainArtifact = new File(builtProductsDir, artifactName);
    mavenProject.getArtifact().setFile(mainArtifact);
    log.info("Main artifact file '" + mainArtifact + "' attached for " + mavenProject.getArtifact());
  }

  private void zipFmwk(File workingDirectory, String artifactName, String zipDirName) throws XCodeException
  {
    try {
      String[] zipCmd = new String[] { "zip", "-r", "-y", "-q", artifactName, zipDirName };
      log.info("Executing: " + StringUtils.join(zipCmd, ' '));
      int exitCode = Forker.forkProcess(System.out, workingDirectory, zipCmd);
      if (exitCode != 0) {
        throw new XCodeException("Could not package the Xcode framework.");
      }
    }
    catch (IOException e) {
      throw new XCodeException("Could not package the Xcode framework.", e);
    }
  }

  private void validateFrmkStructure(File fmwkDir) throws XCodeException
  {
    FrameworkStructureValidator fmwkValidator = new FrameworkStructureValidator(fmwkDir);
    List<String> validationErrors = fmwkValidator.validate();
    if (!validationErrors.isEmpty()) {
      throw new XCodeException("The validation of the built framework '" + fmwkDir.getAbsolutePath()
            + "' failed: " + validationErrors);
    }
  }

  
}
