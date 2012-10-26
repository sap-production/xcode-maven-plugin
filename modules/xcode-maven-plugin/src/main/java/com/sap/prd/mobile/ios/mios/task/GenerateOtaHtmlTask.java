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

import static com.sap.prd.mobile.ios.mios.task.PackageIpaTask.getIpaClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import com.sap.prd.mobile.ios.mios.PListAccessor;
import com.sap.prd.mobile.ios.mios.XCodeBuildLayout;
import com.sap.prd.mobile.ios.mios.XCodeException;
import com.sap.prd.mobile.ios.mios.buddy.PlistAccessorBuddy;
import com.sap.prd.mobile.ios.mios.buddy.ProductNameBuddy;
import com.sap.prd.mobile.ios.ota.lib.OtaBuildHtmlGenerator;
import com.sap.prd.mobile.ios.ota.lib.OtaBuildHtmlGenerator.Parameters;

public class GenerateOtaHtmlTask
{

  final static String OTA_HTML_FILE_APPENDIX = "htm";
  final static String OTA_CLASSIFIER_APPENDIX = "-ota";

  private Log log;
  private String productName;
  private MavenProjectHelper projectHelper;
  private MavenProject mavenProject;
  private File compileDir;
  private URL miosOtaServiceUrl;
  private String configuration;
  private String sdk;

  public GenerateOtaHtmlTask setLog(Log log)
  {
    this.log = log;
    return this;
  }

  public GenerateOtaHtmlTask setProductName(String productName)
  {
    this.productName = productName;
    return this;
  }

  public GenerateOtaHtmlTask setMavenProject(MavenProject mavenProject)
  {
    this.mavenProject = mavenProject;
    return this;
  }

  public GenerateOtaHtmlTask setConfiguration(String configuration)
  {
    this.configuration = configuration;
    return this;
  }

  public GenerateOtaHtmlTask setSdk(String sdk)
  {
    this.sdk = sdk;
    return this;
  }

  public GenerateOtaHtmlTask setProjectHelper(MavenProjectHelper projectHelper)
  {
    this.projectHelper = projectHelper;
    return this;
  }

  public GenerateOtaHtmlTask setCompileDir(File compileDir)
  {
    this.compileDir = compileDir;
    return this;
  }

  public GenerateOtaHtmlTask setMiosOtaServiceUrl(URL miosOtaServiceUrl)
  {
    this.miosOtaServiceUrl = miosOtaServiceUrl;
    return this;
  }

  public void execute() throws XCodeException
  {
    String productName = ProductNameBuddy.getProductName(log, this.productName, mavenProject, sdk,
          configuration);
    String strippedProductName = ProductNameBuddy.stripProductName(productName);

    log.info("Using product name '" + productName + " (fixed product name '" + strippedProductName + "')"
                + "' for configuration '" + configuration + "' and sdk '" + sdk + "'.");

    final File otaHtmlFile = new File(XCodeBuildLayout.getAppFolder(compileDir, configuration,
          sdk), strippedProductName + ".htm");
    try {
      
      if (miosOtaServiceUrl != null) {
        generateOtaHtmlFile(productName, otaHtmlFile);
      }
      else {
        log.info("OTA HTML file '" + otaHtmlFile + "' was not created for configuration '" + configuration
                    + "' and sdk '" + sdk + "'");
      }
    }
    catch (IOException e) {
      throw new XCodeException("Cannot create OTA HTML file. Check log for details.", e);
    }
  }


  private void generateOtaHtmlFile(String productName, final File otaHtmlFile) throws XCodeException, IOException,
        UnsupportedEncodingException, FileNotFoundException, MalformedURLException
  {
    PListAccessor plistAccessor = PlistAccessorBuddy.getInfoPListAccessor(mavenProject, compileDir,
          configuration, sdk);
    String bundleIdentifier = plistAccessor.getStringValue(PListAccessor.KEY_BUNDLE_IDENTIFIER);
    String bundleVersion = plistAccessor.getStringValue(PListAccessor.KEY_BUNDLE_VERSION);
    String otaClassifier = getOtaHtmlClassifier(configuration, sdk);
    String ipaClassifier = getIpaClassifier(configuration, sdk);
    PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(otaHtmlFile), "UTF-8"));
    try {
      Parameters parameters = new Parameters(miosOtaServiceUrl, productName, bundleIdentifier, bundleVersion,
            ipaClassifier, otaClassifier);
      OtaBuildHtmlGenerator.getInstance().generate(printWriter, parameters);
      printWriter.flush();
    }
    finally {
      printWriter.flush();
      printWriter.close();
    }
    log.info("OTA HTML file '" + otaHtmlFile + "' created for configuration '" + configuration + "' and sdk '"
            + sdk + "'");

    projectHelper.attachArtifact(mavenProject, OTA_HTML_FILE_APPENDIX, otaClassifier, otaHtmlFile);

    log.info("OTA HTML file '" + otaHtmlFile + "' attached as additional artifact for configuration '"
          + configuration + "' and sdk '" + sdk + "'");
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


}
