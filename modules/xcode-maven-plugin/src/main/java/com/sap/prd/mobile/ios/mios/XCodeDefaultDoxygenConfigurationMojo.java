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
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Sets default values needed during Doxygen generation.
 * 
 * @goal set-default-doxygen-configuration
 */
public class XCodeDefaultDoxygenConfigurationMojo extends BuildContextAwareMojo
{

  /**
   * @parameter expression="${doxygen.default.config}" default-value="Release"
   */
  private String doxygenDefaultConfig;

  /**
   * @parameter expression="${doxygen.default.sdk}" default-value="iphoneos"
   */
  private String doxygenDefaultSdk;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    final Properties projectProperties = project.getProperties();
    boolean defaultsFound = false;
    for (String configuration : getConfigurations()) {
      for (String sdk : getSDKs()) {

        if (doxygenDefaultConfig.equals(configuration) && doxygenDefaultSdk.equals(sdk)) {
          defaultsFound = true;

          XCodeContext context = getXCodeContext(XCodeContext.SourceCodeLocation.WORKING_COPY, configuration, sdk);

          try {
            String doxygenInputDir = EffectiveBuildSettings.getBuildSetting(context, EffectiveBuildSettings.PUBLIC_HEADERS_FOLDER_PATH);

            String configurationBuildDir = EffectiveBuildSettings.getBuildSetting(context, EffectiveBuildSettings.CONFIGURATION_BUILD_DIR);

            projectProperties.setProperty("doxygen.input.dir",
                  new File(configurationBuildDir, doxygenInputDir).getAbsolutePath());
          }
          catch (XCodeException e) {
            throw new MojoExecutionException(e.getMessage(), e);
          }
        }

      }
    }
    if (!defaultsFound) {
      throw new MojoExecutionException(
            "Doxygen Generation failed. The build has not been performed for default configuration '"
                  + doxygenDefaultConfig + "' and default sdk '" + doxygenDefaultSdk + "'");
    }

  }
}
