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

import static com.sap.prd.mobile.ios.mios.task.PackageIpaTask.getIpaClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import com.sap.prd.mobile.ios.mios.buddy.PlistAccessorBuddy;
import com.sap.prd.mobile.ios.mios.buddy.ProductNameBuddy;

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

        if (configuration == null || configuration.isEmpty())
          throw new IllegalStateException("Invalid configuration: '" + configuration + "'.");

        String productName = ProductNameBuddy.getProductName(getLog(), this.productName, project, sdk,
              configuration);
        String strippedProductName = ProductNameBuddy.stripProductName(productName);

        getLog().info(
              "Using product name '" + productName + " (fixed product name '" + strippedProductName + "')"
                    + "' for configuration '" + configuration + "' and sdk '" + sdk + "'.");

        final File otaHtmlFile = new File(XCodeBuildLayout.getAppFolder(getXCodeCompileDirectory(), configuration,
              sdk), strippedProductName + ".htm");

        final String otaClassifier = getOtaHtmlClassifier(configuration, sdk);
        final String ipaClassifier = getIpaClassifier(configuration, sdk);

        try {
          PListAccessor plistAccessor = PlistAccessorBuddy.getInfoPListAccessor(project, getXCodeCompileDirectory(),
                configuration, sdk);
          final OTAManager otaManager = new OTAManager(miosOtaServiceUrl, productName,
                plistAccessor.getStringValue(PListAccessor.KEY_BUNDLE_IDENTIFIER),
                plistAccessor.getStringValue(PListAccessor.KEY_BUNDLE_VERSION), ipaClassifier,
                otaClassifier);

          if (otaManager.generateOtaHTML()) {

            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(otaHtmlFile), "UTF-8"));

            try {
              otaManager.writeOtaHtml(pw);
              getLog().info(
                    "OTA HTML file '" + otaHtmlFile + "' created for configuration '" + configuration + "' and sdk '"
                          + sdk + "'");
              prepareOtaHtmlFileForDeployment(project, otaClassifier, otaHtmlFile);
              getLog().info(
                    "OTA HTML file '" + otaHtmlFile + "' attached as additional artifact for configuration '"
                          + configuration + "' and sdk '"
                          + sdk + "'");
            }
            finally {
              pw.close();
            }
          }
          else {
            getLog().info(
                  "OTA HTML file '" + otaHtmlFile + "' was not created for configuration '" + configuration
                        + "' and sdk '" + sdk + "'");
          }
        }
        catch (IOException e) {
          throw new MojoExecutionException("Cannot create OTA HTML file. Check log for details.", e);
        }
        catch (XCodeException e) {
          throw new MojoExecutionException("Cannot create OTA HTML file. Check log for details.", e);
        }
      }
    }

  }

  /**
   * Generates the classifier used for OTA HTML deployment
   * 
   * @param configuration
   * @param sdk
   * @return
   */
  static String getOtaHtmlClassifier(String configuration, String sdk)
  {
    return configuration + "-" + sdk + OTA_CLASSIFIER_APPENDIX;
  }

  private void prepareOtaHtmlFileForDeployment(final MavenProject mavenProject, final String classifier,
        final File otaHtmlFile)
  {
    projectHelper.attachArtifact(mavenProject, OTA_HTML_FILE_APPENDIX, classifier, otaHtmlFile);
  }

}
