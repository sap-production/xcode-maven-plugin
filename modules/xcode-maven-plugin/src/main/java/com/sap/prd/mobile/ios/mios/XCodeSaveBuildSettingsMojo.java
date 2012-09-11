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

import java.io.PrintStream;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Appends a build phase to the project that dumps the all environment variables to a file.
 * 
 * @goal save-build-settings
 * 
 */
public class XCodeSaveBuildSettingsMojo extends AbstractXCodeBuildMojo
{
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    try {
      for (String configuration : getConfigurations()) {
        for (String sdk : getSDKs()) {
          XCodeContext ctx = getXCodeContext();
          CommandLineBuilder cmdLineBuilder = new CommandLineBuilder(configuration, sdk, ctx);
          PrintStream out = null;
          try {
            out = new PrintStream(EffectiveBuildSettings.getBuildSettingsFile(this.project, configuration, sdk));
            final int returnValue = Forker.forkProcess(out, ctx.getProjectRootDirectory(), cmdLineBuilder.createShowBuildSettingsCall());
            if (returnValue != 0) {
              throw new XCodeException("Could not execute xcodebuild -showBuildSettings command for configuration " + configuration);
            }
          }
          finally {
            IOUtils.closeQuietly(out);
          }
        }
      }
    }
    catch (Exception e) {
      throw new MojoExecutionException("Could not save build settings", e);
    }
  }
}
