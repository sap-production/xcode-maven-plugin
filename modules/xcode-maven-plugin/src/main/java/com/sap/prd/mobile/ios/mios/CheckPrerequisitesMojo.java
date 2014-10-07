package com.sap.prd.mobile.ios.mios;

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

import static com.sap.prd.mobile.ios.mios.XCodeVersionUtil.checkVersions;
import static com.sap.prd.mobile.ios.mios.XCodeVersionUtil.getBuildVersion;
import static com.sap.prd.mobile.ios.mios.XCodeVersionUtil.getVersion;
import static com.sap.prd.mobile.ios.mios.XCodeVersionUtil.getXCodeVersionString;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Assures that the following required prerequisites are fulfilled:
 * <ul>
 * <li>Xcode version {@value #MIN_XCODE_VERSION} or higher is installed</li>
 * </ul>
 * 
 * @goal check-prerequisites
 * 
 */
public class CheckPrerequisitesMojo extends AbstractXCodeMojo
{
  public final static String MIN_XCODE_VERSION = "4.4";

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    try {
      String xCodeVersionString = getXCodeVersionString();
      DefaultArtifactVersion version = getVersion(xCodeVersionString);
      String buildVersion = getBuildVersion(xCodeVersionString);
      getLog().info("Using Xcode " + version + " " + buildVersion);
      if(!checkVersions(version, MIN_XCODE_VERSION)) {
        throw new MojoExecutionException("Xcode " + MIN_XCODE_VERSION + " (or higher) is required (installed: " + version
              + " " + buildVersion + ")");
      }
    }
    catch (XCodeException e) {
      throw new MojoExecutionException("Could not get xcodebuild version", e);
    }
  }

}
