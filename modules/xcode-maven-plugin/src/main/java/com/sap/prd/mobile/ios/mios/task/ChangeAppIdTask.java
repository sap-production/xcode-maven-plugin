package com.sap.prd.mobile.ios.mios.task;

/*
 * #%L
 * Xcode Maven Plugin
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

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.maven.plugin.logging.Log;

import com.sap.prd.mobile.ios.mios.EffectiveBuildSettings;
import com.sap.prd.mobile.ios.mios.PListAccessor;
import com.sap.prd.mobile.ios.mios.XCodeException;

public class ChangeAppIdTask
{
  private Log log;
  private String appIdSuffix, configuration, sdk;
  private Collection<File> alreadyUpdatedPlists;
  private File xCodeCompileDirectory, buildDirectory;


  public ChangeAppIdTask setLog(Log log)
  {
    this.log = log;
    return this;
  }

  public ChangeAppIdTask setBuildDirectory(File buildDirectory)
  {
    this.buildDirectory = buildDirectory;
    return this;
  }

  public ChangeAppIdTask setConfiguration(String configuration)
  {
    this.configuration = configuration;
    return this;
  }

  public ChangeAppIdTask setSdk(String sdk)
  {
    this.sdk = sdk;
    return this;
  }

  public ChangeAppIdTask  setxCodeCompileDirectory(File xCodeCompileDirectory)
  {
    this.xCodeCompileDirectory = xCodeCompileDirectory;
    return this;
  }

  public ChangeAppIdTask setAppIdSuffix(String appIdSuffix)
  {
    this.appIdSuffix = appIdSuffix;
    return this;
  }

  public ChangeAppIdTask setAlreadyUpdatedPlists(Collection<File> alreadyUpdatedPlists)
  {
    this.alreadyUpdatedPlists = alreadyUpdatedPlists;
    return this;
  }
  
  public void execute() throws XCodeException {
    
    if (appIdSuffix == null || "".equals(appIdSuffix.trim())) {
      return;
    }
    // log is needed in an integration test to determine the appId suffix settings of the machine the test is running on
    log.info("appIdSuffix=" + appIdSuffix);

    PListAccessor infoPlistAccessor = getInfoPListAccessor(xCodeCompileDirectory, configuration, sdk);
    
    File infoPlistFile = infoPlistAccessor.getPlistFile();
    
    if (alreadyUpdatedPlists.contains(infoPlistFile)) {
      log.debug("PList file '" + infoPlistFile.getName()
            + "' was already updated for another configuration. This file will be skipped.");
    } else {
      changeAppId(infoPlistAccessor, appIdSuffix, log);
      alreadyUpdatedPlists.add(infoPlistFile);
    }
  }
  
  public static void changeAppId(PListAccessor infoPlistAccessor, String appIdSuffix, Log log) throws XCodeException
  {
    ensurePListFileIsWritable(infoPlistAccessor.getPlistFile(), log);
    try {
      appendAppIdSuffix(infoPlistAccessor, appIdSuffix, log);
    }
    catch (IOException e) {
      throw new XCodeException(e.getMessage(), e);
    }
  }

  private static void ensurePListFileIsWritable(File pListFile, Log log) throws XCodeException
  {
    if (!pListFile.canWrite()) {
      if (!pListFile.setWritable(true, true))
        throw new XCodeException("Could not make plist file '" + pListFile + "' writable.");

      log.info("Made PList file '" + pListFile + "' writable.");
    }
  }
  
  private static void appendAppIdSuffix(PListAccessor infoPlistAccessor, String appIdSuffix, Log log) throws IOException
  {
    String newAppId = infoPlistAccessor.getStringValue(PListAccessor.KEY_BUNDLE_IDENTIFIER) + "." + appIdSuffix;
    infoPlistAccessor.updateStringValue(PListAccessor.KEY_BUNDLE_IDENTIFIER, newAppId);
    log.info("PList file '" + infoPlistAccessor.getPlistFile() + "' updated: Set AppId to '" + newAppId + "'.");
  }
  
  /**
   * Retrieves the Info Plist out of the effective Xcode project settings and returns the accessor
   * to it.
   * 
   * @param xcodeProjectDirectory
   *          the directory where the Xcode project is located. If you want to access the unmodified
   *          Plist (i.e. AppID not appended) use the {@link #getXCodeSourceDirectory()} method, if
   *          you want to access the modified plist, use the {@link #getXCodeCompileDirectory()}
   *          method.
   * @throws XCodeException 
   */
  protected PListAccessor getInfoPListAccessor(File xcodeProjectDirectory, String configuration, String sdk) throws XCodeException
  {
    String plistFileName = new EffectiveBuildSettings(buildDirectory, configuration, sdk)
      .getBuildSetting(EffectiveBuildSettings.INFOPLIST_FILE);
    File plistFile = new File(xcodeProjectDirectory, plistFileName);
    if (!plistFile.isFile()) {
      throw new XCodeException("The Xcode project refers to the Info.plist file '" + plistFileName
            + "' that does not exist.");
    }
    return new PListAccessor(plistFile);
  }
  
}
