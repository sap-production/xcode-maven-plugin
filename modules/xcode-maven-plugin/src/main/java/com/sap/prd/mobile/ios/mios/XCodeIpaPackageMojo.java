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

import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProjectHelper;

import com.sap.prd.mobile.ios.mios.task.PackageIpaTask;

/**
 * Packages the ipa files and prepares the generated artifacts for deployment.
 * 
 * @goal package-ipa
 */
public class XCodeIpaPackageMojo extends AbstractXCodeMojo
{

  /**
   * @component
   */
  private MavenProjectHelper projectHelper;

  /**
   * @parameter expression="${product.name}"
   */
  private String productName;

  @Override
  public void execute() throws MojoExecutionException
  {

    final Set<String> sdks = getSDKs();
    final Set<String> configurations = getConfigurations();

    getLog().debug("Configurations are:" + configurations);
    getLog().debug("SDKs are: " + sdks);

    if (configurations == null || configurations.size() == 0)
      throw new MojoExecutionException("Invalid configuration: \"" + configurations + "\".");

    if (sdks == null || sdks.size() == 0)
      throw new MojoExecutionException("Invalid sdks: \"" + sdks + "\".");

    try {
      for (final String configuration : configurations) {
        for (final String sdk : sdks) {

          PackageIpaTask task = new PackageIpaTask();
          task.setLog(getLog()).setCompileDir(getXCodeCompileDirectory()).setMavenProject(project)
            .setProductName(productName).setProjectHelper(projectHelper).setConfiguration(configuration).setSdk(sdk);
          task.execute();
        }
      }
    }
    catch (XCodeException ex) {
      throw new MojoExecutionException(ex.getMessage(), ex);
    }

  }
}
