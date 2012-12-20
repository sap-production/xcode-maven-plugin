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
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

/**
 * Packages the ipa files and prepares the generated artifacts for deployment.
 * 
 * @goal package-ipa
 */
public class XCodeIpaPackageMojo extends BuildContextAwareMojo
{

  /**
   * @component
   */
  private MavenProjectHelper projectHelper;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {

    final Set<String> sdks = getSDKs();
    final Set<String> configurations = getConfigurations();

    getLog().info("Configurations are:" + configurations);
    getLog().info("SDKs are: " + sdks);

    if (configurations == null || configurations.size() == 0)
      throw new MojoExecutionException("Invalid configuration: \"" + configurations + "\".");

    if (sdks == null || sdks.size() == 0)
      throw new MojoExecutionException("Invalid sdks: \"" + sdks + "\".");

    for (final String configuration : configurations) {
      for (final String sdk : sdks) {

        if (configuration == null || configuration.isEmpty())
          throw new IllegalStateException("Invalid configuration: '" + configuration + "'.");

        final String productName = getProductName(configuration, sdk);
        final String fixedProductName = getFixedProductName(productName);

        getLog().info(
              "Using product name '" + productName + " (fixed product name '" + fixedProductName + "')"
                    + "' for configuration '" + configuration + "' and sdk '" + sdk + "'.");

        File rootDir = XCodeBuildLayout.getAppFolder(getXCodeCompileDirectory(), configuration, sdk);
        final File ipaFile = zipSubfolder(rootDir, productName + ".app", fixedProductName + ".ipa", "Payload/");

        prepareIpaFileForDeployment(project, configuration, sdk, ipaFile);
      }
    }

  }

  private void prepareIpaFileForDeployment(final MavenProject mavenProject, final String configuration,
        final String sdk, final File ipaFile)
  {

    projectHelper.attachArtifact(mavenProject, ipaFile, getIpaClassifier(configuration, sdk));
  }

  /**
   * Generates the classifier used for IPA deployment
   * 
   * @param configuration
   * @param sdk
   * @return
   */
  static String getIpaClassifier(String configuration, String sdk)
  {
    return configuration + "-" + sdk;
  }

}
