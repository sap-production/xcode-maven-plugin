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
import java.util.Arrays;

import org.apache.maven.plugin.logging.Log;

class XCodeManager
{

  private final Log log;

  public XCodeManager(Log log)
  {
    this.log = log;
  }

  /**
   * Trigger xcodebuild. The configuration is provided by <code>context</code>
   * 
   * @param context
   * @throws IOException
   * @throws {@link XCodeException}
   */
  void build(final XCodeContext context) throws IOException, XCodeException
  {

    if (context == null)
      throw new IllegalArgumentException("No XCodeContext available.");

    for (String configuration : context.getConfigurations()) {

      for (final String sdk : context.getSdks()) {

        final CommandLineBuilder commandLineBuilder = new CommandLineBuilder();

        commandLineBuilder.setConfiguration(configuration);
        commandLineBuilder.setProjectName(context.getProjectName());
        commandLineBuilder.setSdk(sdk);
        commandLineBuilder.setBuildActions(Arrays.asList(context.getBuildActions()));
        commandLineBuilder.setCodeSignIdentity(context.getCodeSignIdentity());
        commandLineBuilder.setProvisioningProfile(context.getProvisioningProfile());

        //
        //TODO The command line printed into the log is not 100% accurate. We have a problem with
        // quotation marks.
        log.info("Executing xcode command: '" + commandLineBuilder.toString() + "'.");

        final int returnValue = Forker.forkProcess(context.getOut(), context.getProjectRootDirectory(),
              commandLineBuilder.createCommandline());
        if (returnValue != 0)
          throw new XCodeException("Could not execute xcode build for configuration " + configuration);
      }
    }
  }
}
