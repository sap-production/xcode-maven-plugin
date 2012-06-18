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
import java.util.Collection;
import java.util.HashSet;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Appends a suffix to the appId. No actions are taken if the suffix is not specified or the suffix has zero length.
 * 
 * @goal change-app-id
 * 
 */
public class XCodeChangeAppIDMojo extends AbstractXCodeMojo
{

  /**
   * This suffix gets appended to the appId as '.&lt;appIdSuffix>' in the <code>Info.plist</code>
   * before the signing takes place.
   * 
   * @parameter expression="${xcode.appIdSuffix}"
   * @since 1.2.0
   */
  private String appIdSuffix;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    if (appIdSuffix == null || "".equals(appIdSuffix.trim())) {
      return;
    }

    getLog().info("appIdSuffix=" + appIdSuffix);

    final Collection<File> alreadyUpdatedPlists = new HashSet<File>();

    final File xcodeProjectRootDirectory;

    xcodeProjectRootDirectory = getXCodeCompileDirectory();

    for (final String configuration : getConfigurations()) {
      new PlistManager(getLog(), xcodeProjectRootDirectory, project.getArtifactId()).changeAppId(appIdSuffix,
            alreadyUpdatedPlists, getTargetBuildConfiguration(configuration));
    }
  }
}
