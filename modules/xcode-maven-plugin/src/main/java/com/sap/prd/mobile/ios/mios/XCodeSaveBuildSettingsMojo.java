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
    for (String configuration : getConfigurations()) {
      for (String sdk : getSDKs()) {

        try {
          new ExtractBuildSettingsTask().setBuildDirectory(new File(project.getBuild().getDirectory()))
            .setConfiguration(configuration)
            .setSdk(sdk).setCtx(getXCodeContext()).execute();
        }
        catch (XCodeException e) {
          throw new MojoExecutionException(e.getMessage(), e);
        }

      }
    }
  }
}
