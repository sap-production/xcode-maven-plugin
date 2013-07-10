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

import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal setup-logging
 */
public class LoggerMojo extends AbstractXCodeMojo
{
  static {

     if(null == LogManager.getLogManager().getLogger(XCodePluginLogger.getLoggerName())) {

       final XCodePluginLogger logger = new XCodePluginLogger();
       LogManager.getLogManager().addLogger(logger);
       setLogger(logger);
       logger.finest(String.format("XCode plugin logger has been created: %s", logger.getName()));
     }
   }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    final String loggerName = XCodePluginLogger.getLoggerName();
    
    Logger logger = LogManager.getLogManager().getLogger(loggerName);

    if (logger == null) {
      getLog().error(
            "Cannot setup logging infrastructure. Logger '" + loggerName
                  + "' was null.");
    }
    else if (logger instanceof XCodePluginLogger) {
      ((XCodePluginLogger) logger).setLog(getLog());
      getLog().debug("Logging infrastructure has been setup.");
    }
    else {
      getLog().error(
            "Cannot setup logging infrastructure. Logger '" + loggerName
                  + "' is not an instance of '" + XCodePluginLogger.class.getName() + "' It was found to be a '"
                  + logger.getClass().getName() + "'.");
    }
  }
}
