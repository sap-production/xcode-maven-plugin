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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Packages the framework built by Xcode and prepares the generated artifact for deployment.
 * 
 * @goal package-framework
 * 
 */
public class XCodePackageFrameworkMojo extends BuildContextAwareMojo
{

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {

    final String sdk = "iphoneos";
    String productName = getProductName(getPrimaryFmwkConfiguration(), sdk);
    String builtProductsDirName = null;
    
    try {
      builtProductsDirName = EffectiveBuildSettings.getBuildSetting(getXCodeContext(XCodeContext.SourceCodeLocation.WORKING_COPY, getPrimaryFmwkConfiguration(), sdk), getLog(), EffectiveBuildSettings.BUILT_PRODUCTS_DIR);
    } catch(XCodeException ex) {
      throw new MojoExecutionException(ex.getMessage(), ex);
    }
    
    File builtProductsDir = new File(builtProductsDirName);
    String fmwkDirName = productName + ".framework";
    File fmwkDir = new File(builtProductsDir, fmwkDirName);

    validateFrmkStructure(fmwkDir);

    String artifactName = productName + ".xcode-framework-zip";
    zipFmwk(builtProductsDir, artifactName, fmwkDirName);

    File mainArtifact = new File(builtProductsDir, artifactName);
    project.getArtifact().setFile(mainArtifact);
    getLog().info("Main artifact file '" + mainArtifact + "' attached for " + project.getArtifact());
  }

  private void zipFmwk(File workingDirectory, String artifactName, String zipDirName) throws MojoExecutionException
  {
    try {
      String[] zipCmd = new String[] { "zip", "-r", "-y", "-q", artifactName, zipDirName };
      getLog().info("Executing: " + StringUtils.join(zipCmd, ' '));
      int exitCode = Forker.forkProcess(System.out, workingDirectory, zipCmd);
      if (exitCode != 0) {
        throw new MojoExecutionException("Could not package the Xcode framework.");
      }
    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not package the Xcode framework.", e);
    }
  }

  private void validateFrmkStructure(File fmwkDir) throws MojoExecutionException
  {
    FrameworkStructureValidator fmwkValidator = new FrameworkStructureValidator(fmwkDir);
    List<String> validationErrors = fmwkValidator.validate();
    if (!validationErrors.isEmpty()) {
      throw new MojoExecutionException("The validation of the built framework '" + fmwkDir.getAbsolutePath()
            + "' failed: " + validationErrors);
    }
  }

}
