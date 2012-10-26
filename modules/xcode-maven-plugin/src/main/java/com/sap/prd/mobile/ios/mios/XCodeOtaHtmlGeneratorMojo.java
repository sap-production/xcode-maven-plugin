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

import java.net.URL;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProjectHelper;

import com.sap.prd.mobile.ios.mios.task.GenerateOtaHtmlTask;

/**
 * Generates over the air html files and prepares the generated artifacts for deployment.
 * 
 * @goal generate-ota-html
 */

public class XCodeOtaHtmlGeneratorMojo extends AbstractXCodeMojo
{

  final static String OTA_CLASSIFIER_APPENDIX = "-ota";
  final static String OTA_HTML_FILE_APPENDIX = "htm";
  /**
   * @parameter expression="${mios.ota-service.url}"
   */
  private URL miosOtaServiceUrl;

  /**
   * @component
   */
  private MavenProjectHelper projectHelper;

  /**
   * @parameter expression="${product.name}"
   */
  private String productName;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {

    final Set<String> sdks = getSDKs();
    final Set<String> configurations = getConfigurations();

    if (configurations == null || configurations.size() == 0)
      throw new MojoExecutionException("Invalid configuration: \"" + configurations + "\".");

    if (sdks == null || sdks.size() == 0)
      throw new MojoExecutionException("Invalid sdks: \"" + sdks + "\".");

    for (final String configuration : configurations) {
      for (final String sdk : sdks) {

        try {
          GenerateOtaHtmlTask task = new GenerateOtaHtmlTask();
          task.setLog(getLog()).setCompileDir(getXCodeCompileDirectory()).setMavenProject(project)
            .setMiosOtaServiceUrl(miosOtaServiceUrl).setProductName(productName).setProjectHelper(projectHelper)
            .setConfiguration(configuration).setSdk(sdk);
          task.execute();
        }
        catch (XCodeException e) {
          throw new MojoExecutionException("Cannot create OTA HTML file: " + e.getMessage(), e);
        }
      }
    }
  }

}
