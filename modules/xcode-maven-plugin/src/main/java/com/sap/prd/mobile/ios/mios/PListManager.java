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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.sap.prd.mobile.ios.mios.xcodeprojreader.BuildConfiguration;

class PlistManager
{

  private final File projectDir;

  private final Log log;

  private String projectName;

  PlistManager(final Log log, final File projectDir, final String projectName)
  {
    this.log = log;
    this.projectDir = projectDir;
    this.projectName = projectName;
  }

  enum BundleKey
  {
    CFBundleIdentifier,
    CFBundleVersion,
    CFBundleName
  }

  void changeAppId(final String appIdSuffix, final Collection<File> alreadyUpdatedPlists, final BuildConfiguration configuration)
        throws MojoExecutionException
  {

    File pListFile = null;

    pListFile = getPListFile(configuration);

    if (alreadyUpdatedPlists.contains(pListFile)) {
      this.log.info("PList file '" + pListFile
            + "' was already updated for another configuration. This file will be skipped.");

      return;
    }

    ensurePListFileIsWritable(pListFile);

    try {

      updatePListFile(pListFile, appIdSuffix);
      alreadyUpdatedPlists.add(pListFile);

    }
    catch (IOException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  File getPListFile(final BuildConfiguration buildConfig) throws MojoExecutionException
  {

    final File xcodeProjectFile = getXCodeProjectFile();
    
    final String plistFile = buildConfig.getBuildSettings().getDict().getString("INFOPLIST_FILE");

    if (plistFile == null || plistFile.isEmpty())
      throw new MojoExecutionException("No PList file extracted from xcode project file '" + xcodeProjectFile + "'.");

    return new File(projectDir, plistFile);
  }

  String getValueFromPlist(File plistFile, BundleKey key)
        throws IOException
  {
    return new PListAccessor(plistFile).getStringValue(key.name());
  }

  File getXCodeProjectFile()
  {
    return new File(new File(projectDir, projectName + ".xcodeproj"), "project.pbxproj");
  }

  private void ensurePListFileIsWritable(File pListFile) throws MojoExecutionException
  {

    if (!pListFile.canWrite()) {
      if (!pListFile.setWritable(true, true))
        throw new MojoExecutionException("Could not make plist file '" + pListFile + "' writable.");

      this.log.info("PList file '" + pListFile + "' set to writable.");
    }
  }

  private void updatePListFile(final File pListFile, final String appIdSuffix) throws IOException
  {
    this.log.info("Updating plist file '" + pListFile + "'.");
    PListAccessor plist = new PListAccessor(pListFile);
    plist.setStringValue(BundleKey.CFBundleIdentifier.name(), plist.getStringValue(BundleKey.CFBundleIdentifier.name())
          + "." + appIdSuffix);
    this.log.info("PList file '" + pListFile + "' updated.");
  }
}
