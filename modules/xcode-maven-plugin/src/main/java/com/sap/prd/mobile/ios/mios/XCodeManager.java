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
import java.util.logging.LogManager;
import java.util.logging.Logger;

class XCodeManager
{
  
  private static final Logger LOGGER = LogManager.getLogManager().getLogger(XCodePluginLogger.getLoggerName());
  /**
   * Trigger xcodebuild. The configuration is provided by <code>context</code>
   * 
   * @param context
   * @throws IOException
   * @throws {@link XCodeException}
   */
  void callXcodeBuild(XCodeContext ctx) throws IOException,
        XCodeException
  {
    final CommandLineBuilder commandLineBuilder = new CommandLineBuilder(ctx);

    //
    //TODO The command line printed into the log is not 100% accurate. We have a problem with
    // quotation marks.
    LOGGER.info("Executing xcode command: '" + commandLineBuilder.toString() + "'.");

    final int returnValue = Forker.forkProcess(ctx.getOut(), ctx.getProjectRootDirectory(),
          commandLineBuilder.createBuildCall());
    if (returnValue != 0) {
      throw new XCodeException("Could not execute xcodebuild for configuration "
            + ctx.getOptions().getAllOptions().get(Options.ManagedOption.CONFIGURATION.getOptionName()));
    }
  }
}
