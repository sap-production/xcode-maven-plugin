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
package com.sap.prd.mobile.ios.mios.buddy;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import com.sap.prd.mobile.ios.mios.EffectiveBuildSettings;

public class ProductNameBuddy
{

  /**
   * Returns the product name. If the pomProvidedName is not <code>null</code> this one is taken,
   * otherwise the product name gets extracted from the Xcode build settings.
   * 
   * @param log
   *          The logger to write log messages to
   * @param pomProvidedName
   *          the product name that has been explicitly provided in the pom. Can be
   *          <code>null</code>
   * @param mavenProject
   *          the Maven project
   * @param sdk
   *          the Xcode sdk (iphoneos, iphonesimulator)
   * @param configuration
   *          The Xcode configuration (e.g. Debug or Release)
   * @return the product name
   */
  public static String getProductName(Log log, String pomProvidedName, MavenProject mavenProject, String sdk,
        String configuration)
  {
    String productName;

    if (pomProvidedName != null) {
      if (pomProvidedName.trim().isEmpty()) {
        throw new IllegalStateException("The productName provided in the POM or via a system property is empty. " +
              "Either you do not provide any product name to use the one provided in your Xcode project or " +
              "you override it with a non empty name.");
      }
      productName = pomProvidedName;
      log.debug("Product name obtained from pom file: " + productName);
    }
    else {
      productName = EffectiveBuildSettings.getProductName(mavenProject, configuration, sdk);
      log.debug("Product name obtained from effective build settings file: " + productName);
    }

    String strippedProductName = productName.trim().replaceAll(" ", "");
    log.debug("Using product name '" + productName + "' (stripped product name '" + strippedProductName + "')"
          + "' for configuration '" + configuration + "' and sdk '" + sdk + "'.");
    return strippedProductName;
  }

  public static String stripProductName(String productName)
  {
    return productName.trim().replaceAll(" ", "");
  }

}
