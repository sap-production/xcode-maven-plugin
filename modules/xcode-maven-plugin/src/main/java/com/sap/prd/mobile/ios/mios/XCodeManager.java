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
  void callXcodeBuild(XCodeContext ctx, String configuration, final String sdk, boolean test) throws IOException,
        XCodeException
  {
    final CommandLineBuilder commandLineBuilder = new CommandLineBuilder(configuration, sdk, ctx);
    final String[] commandLine;
    if (test) {
    	commandLine = commandLineBuilder.createTestCall();
    } else {
    	commandLine = commandLineBuilder.createBuildCall();
    }

    //
    //TODO The command line printed into the log is not 100% accurate. We have a problem with
    // quotation marks.
    log.info("Executing xcode command: '" + join(" ", commandLine) + "'.");

    final int returnValue = Forker.forkProcess(ctx.getOut(), ctx.getProjectRootDirectory(),
          commandLine);
    if (returnValue != 0) {
      throw new XCodeException("Could not execute xcodebuild for configuration " + configuration);
    }
  }
  
  private String join(String glue, String[] arg)
  {
	boolean first = true;
	StringBuilder result = new StringBuilder();
	
	for (String item : arg) {
	  if (first) {
		result.append(glue);
	  }
	  result.append(item);
	}
	return result.toString();
  }
}
