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
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Appends a suffix to the appId. No actions are taken if the suffix is not specified or the suffix
 * has zero length.
 */
@Mojo(name="change-app-id")
public class XCodeChangeAppIDMojo extends BuildContextAwareMojo
{

  private final static Logger LOGGER = LogManager.getLogManager().getLogger(XCodePluginLogger.getLoggerName());
  /**
   * This suffix gets appended to the appId as '.&lt;appIdSuffix>' in the <code>Info.plist</code>
   * before the signing takes place.
   * 
   * @since 1.2.0
   */
  @Parameter(property="xcode.appIdSuffix")
  private String appIdSuffix;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    if (appIdSuffix == null || "".equals(appIdSuffix.trim())) {
      return;
    }

    LOGGER.info("appIdSuffix=" + appIdSuffix);

    final Collection<File> alreadyUpdatedPlists = new HashSet<File>();

    for (final String configuration : getConfigurations()) {
      for (final String sdk : getSDKs()) {
        File infoPlistFile = null;
        try {
          infoPlistFile = getPListFile(XCodeContext.SourceCodeLocation.WORKING_COPY, configuration, sdk);
        }
        catch (XCodeException e) {
          throw new MojoExecutionException(e.getMessage(), e);
        }
        PListAccessor infoPlistAccessor = new PListAccessor(infoPlistFile);
        if (alreadyUpdatedPlists.contains(infoPlistFile)) {
          LOGGER.finer("PList file '" + infoPlistFile.getName()
                + "' was already updated for another configuration. This file will be skipped.");
        }
        else {
          changeAppId(infoPlistAccessor, appIdSuffix);
          alreadyUpdatedPlists.add(infoPlistFile);
        }
      }
    }
  }

  static void changeAppId(PListAccessor infoPlistAccessor, String appIdSuffix) throws MojoExecutionException
  {
    ensurePListFileIsWritable(infoPlistAccessor.getPlistFile());
    try {
      appendAppIdSuffix(infoPlistAccessor, appIdSuffix);
    }
    catch (IOException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  private static void ensurePListFileIsWritable(File pListFile) throws MojoExecutionException
  {
    if (!pListFile.canWrite()) {
      if (!pListFile.setWritable(true, true))
        throw new MojoExecutionException("Could not make plist file '" + pListFile + "' writable.");

      LOGGER.info("Made PList file '" + pListFile + "' writable.");
    }
  }

  private static void appendAppIdSuffix(PListAccessor infoPlistAccessor, String appIdSuffix)
        throws IOException
  {
    String newAppId = infoPlistAccessor.getStringValue(PListAccessor.KEY_BUNDLE_IDENTIFIER) + "." + appIdSuffix;
    infoPlistAccessor.updateStringValue(PListAccessor.KEY_BUNDLE_IDENTIFIER, newAppId);
    LOGGER.info("PList file '" + infoPlistAccessor.getPlistFile() + "' updated: Set AppId to '" + newAppId + "'.");
  }

}
