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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Triggers the Xcode build.
 * 
 * @goal xcodebuild
 * @requiresDependencyResolution
 * 
 */
public class XCodeBuildMojo extends BuildContextAwareMojo
{
  
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    
    try {
      XCodeManager xcodeMgr = new XCodeManager(getLog());

      if (getPackagingType() == PackagingType.FRAMEWORK) {
        // we do not provide a sdk for frameworks as the target should assure that all required sdks are built
        XCodeContext ctx = getXCodeContext(XCodeContext.SourceCodeLocation.WORKING_COPY, getPrimaryFmwkConfiguration(), null);
        getLog().info(ctx.toString());
        xcodeMgr.callXcodeBuild(ctx);
      }
      else { 
        for (String configuration : getConfigurations()) {
          for (final String sdk : getSDKs()) {
            // we do not provide a sdk for frameworks as the target should assure that all required sdks are built
            XCodeContext ctx = getXCodeContext(XCodeContext.SourceCodeLocation.WORKING_COPY, configuration, sdk);
            getLog().info(ctx.toString());

            xcodeMgr.callXcodeBuild(ctx);
          }
        }
      }
    }
    catch (IOException ex) {
      throw new MojoExecutionException("XCodeBuild failed due to " + ex.getMessage(), ex);
    }
    catch (XCodeException ex) {
      throw new MojoExecutionException("XCodeBuild failed due to " + ex.getMessage(), ex);
    }
  }
}
