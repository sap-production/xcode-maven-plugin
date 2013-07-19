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

import static com.sap.prd.mobile.ios.mios.XCodeIpaPackageMojo.getIpaClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

/**
 * Generates over the air html files and prepares the generated artifacts for deployment.
 * 
 * @goal generate-ota-html
 */

public class XCodeOtaHtmlGeneratorMojo extends BuildContextAwareMojo
{

  final static String PARAMETER_PREFIX = "mios.ota-service";
  final static String OTA_CLASSIFIER_APPENDIX = "-ota";
  final static String OTA_HTML_FILE_APPENDIX = "htm";

  /**
   * To enable the generation of Over-The-Air deployment HTML files you have to specify the URL to
   * your "HTML" OTA Service (e.g. <code>http://server/ota-service/HTML</code>).<br/>
   * Here you find more information about the <a
   * href="https://github.com/sap-production/OTAService">OTA Service Github Project</a>.<br/>
   * 
   * @parameter expression="${mios.ota-service.url}"
   */
  private URL miosOtaServiceUrl;

  /**
   * @parameter expression="${mios.ota-service.buildHtmlTemplate}"
   */
  private String buildHtmlTemplate;

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

        final File otaHtmlFile = new File(XCodeBuildLayout.getAppFolder(getXCodeCompileDirectory(), configuration,
              sdk), fixedProductName + ".htm");

        final String otaClassifier = getOtaHtmlClassifier(configuration, sdk);
        final String ipaClassifier = getIpaClassifier(configuration, sdk);

        try {
          PListAccessor plistAccessor = getInfoPListAccessor(XCodeContext.SourceCodeLocation.WORKING_COPY,
                configuration, sdk);

          final OTAManager otaManager = new OTAManager(miosOtaServiceUrl, productName,
                plistAccessor.getStringValue(PListAccessor.KEY_BUNDLE_IDENTIFIER),
                plistAccessor.getStringValue(PListAccessor.KEY_BUNDLE_VERSION),
                ipaClassifier, otaClassifier, buildHtmlTemplate, getProperties());

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

  private Map<String, String> getProperties()
  {
    Map<String, String> result = new HashMap<String, String>();
    Set<String> keys = this.getKeys(null);
    for (String key : keys) {
      String value = this.getProperty(key);
      result.put(key, value);
    }
    return result;
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
