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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Updates the properties CFBundleVersion and CFBundleShortVersionString inside the plist file(s) that is/are denoted for
 * the given configurations and sdks. The version is updated with a version derived from the maven project version.
 * For CFBundleVersion all version parts containing only numbers are retained. The leading numbers of the first
 * version part containing characters are also retained. Any subsequent version part is ommited.
 * 
 * For CFBundleShortVersion the same strategy as described for CFBundleVersion applies. Additionally the version is truncated
 * so that it consists of three numbers separated by two dots.
 * 
 * @goal change-versions-in-plist
 * @since 1.6.2
 */
public class XCodeChangeVersionInPListMojo extends AbstractXCodeMojo
{
  /**
   * If this parameter is set to <code>true</code> no version will be transferred into the Xcode project.
   * @parameter expression="${xcode.skipVersionUpdate}" default-value="false"
   */
  private boolean skipVersionUpdate;
  
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    if(skipVersionUpdate) {
      getLog().info("Update of versions in info plist will be skipped.");
      return;
    }
    
    final Collection<File> alreadyUpdatedPlists = new HashSet<File>();

    Collection<UpdateVersionInPListTask> updateTasks = new HashSet<UpdateVersionInPListTask>(Arrays.asList(
          new UpdateCFBundleShortVersionStringInPListTask(),
          new UpdateCFBundleVersionInPListTask()));

    for (final String configuration : getConfigurations()) {
      for (final String sdk : getSDKs()) {
        File infoPlistFile = getPListFile(configuration, sdk);
        if (alreadyUpdatedPlists.contains(infoPlistFile)) {
          getLog().debug("Version in PList file '" + infoPlistFile.getName()
                + "' was already updated for another configuration. This file will be skipped.");
        } else {
          
          try { 
            
            ensurePListFileIsWritable(infoPlistFile);
            
            for(UpdateVersionInPListTask updateVersionInPListTask : updateTasks) {
              updateVersionInPListTask.setPListFile(infoPlistFile).setVersion(project.getVersion()).setLog(getLog()).execute();
            }
            
            alreadyUpdatedPlists.add(infoPlistFile);

          } catch(XCodeException ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
          }
        }
      }
    }
  }

  private void ensurePListFileIsWritable(File pListFile) throws XCodeException
  {    
    if (!pListFile.canWrite()) {
      if (!pListFile.setWritable(true, true))
        throw new XCodeException("Could not make plist file '" + pListFile + "' writable.");

      getLog().info("Made PList file '" + pListFile + "' writable.");
    }
  }
}
