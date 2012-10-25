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
package com.sap.prd.mobile.ios.mios.task;

import java.io.IOException;

import org.apache.maven.plugin.logging.Log;

import com.sap.prd.mobile.ios.mios.CommandLineBuilder;
import com.sap.prd.mobile.ios.mios.Forker;
import com.sap.prd.mobile.ios.mios.XCodeContext;
import com.sap.prd.mobile.ios.mios.XCodeException;

public class XcodeBuildTask
{

  private Log log;
  private XCodeContext ctx;
  private String configuration;
  private String sdk;

  public XcodeBuildTask setLog(Log log)
  {
    this.log = log;
    return this;
  }

  public XcodeBuildTask setCtx(XCodeContext ctx)
  {
    this.ctx = ctx;
    return this;
  }

  public XcodeBuildTask setConfiguration(String configuration)
  {
    this.configuration = configuration;
    return this;
  }

  public XcodeBuildTask setSdk(String sdk)
  {
    this.sdk = sdk;
    return this;
  }

  /**
   * Perform the xcodebuild call
   */
  public void execute() throws XCodeException
  {
    final CommandLineBuilder commandLineBuilder = new CommandLineBuilder(configuration, sdk, ctx);

    //
    //TODO The command line printed into the log is not 100% accurate. We have a problem with
    // quotation marks.
    log.info("Executing xcode command: '" + commandLineBuilder.toString() + "'.");

    try {
      final int returnValue = Forker.forkProcess(ctx.getOut(), ctx.getProjectRootDirectory(),
            commandLineBuilder.createBuildCall());
      if (returnValue != 0) {
        throw new XCodeException("Could not execute xcodebuild for configuration " + configuration);
      }
    }
    catch (IOException ex) {
      log.error("Could not call the xcodebuild command: " + ex.getMessage(), ex);
      throw new XCodeException("Could not call the xcodebuild command: " + ex.getMessage(), ex);
    }
  }

}
