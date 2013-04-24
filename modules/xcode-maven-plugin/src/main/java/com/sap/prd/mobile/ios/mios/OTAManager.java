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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;

import com.sap.prd.mobile.ios.ota.lib.OtaBuildHtmlGenerator;
import com.sap.prd.mobile.ios.ota.lib.OtaBuildHtmlGenerator.Parameters;

class OTAManager
{

  private final URL miosOtaServiceUrl;
  private final String title;
  private final String bundleIdentifier;
  private final String bundleVersion;
  private final String ipaClassifier;
  private final String otaClassifier;
  private final String buildHtmltemplate;
  private final Map<String, String> properties;
  private Log log = null;

  public OTAManager(URL miosOtaServiceUrl, String title, String bundleIdentifier,
        String bundleVersion, String ipaClassifier, String otaClassifier, String buildHtmltemplate,
        Map<String, String> properties)
  {
    super();
    this.miosOtaServiceUrl = miosOtaServiceUrl;
    this.title = title;
    this.bundleIdentifier = bundleIdentifier;
    this.bundleVersion = bundleVersion;
    this.ipaClassifier = ipaClassifier;
    this.otaClassifier = otaClassifier;
    this.buildHtmltemplate = buildHtmltemplate;
    this.properties = properties;
  }

  boolean generateOtaHTML()
  {
    return miosOtaServiceUrl != null;
  }

  void writeOtaHtml(PrintWriter printWriter) throws IOException
  {
    if (!generateOtaHTML())
      return;

    try {
      Parameters parameters = new Parameters(miosOtaServiceUrl, title, bundleIdentifier, bundleVersion,
            ipaClassifier, otaClassifier, properties);
      OtaBuildHtmlGenerator generator = OtaBuildHtmlGenerator.getInstance(buildHtmltemplate);
      log("Using OTA build HTML template "+generator.getTemplateName()+" (requested: "+buildHtmltemplate+")");
      generator.generate(printWriter, parameters);
      printWriter.flush();
    }
    finally {
      printWriter.flush();
    }
  }

  private void log(String message)
  {
    if(log != null) log.info(message);
  }

  public void setLogger(Log log)
  {
    this.log  = log;
  
  }
}
