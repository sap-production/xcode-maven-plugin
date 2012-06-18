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
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * Triggers the Xcode build.
 * 
 * @goal xcodebuild
 * @requiresDependencyResolution
 * 
 */
public class XCodeBuildMojo extends AbstractXCodeMojo
{

  private final static List<String> DEFAULT_BUILD_ACTIONS = Collections.unmodifiableList(Arrays
    .asList("clean", "build"));

  /**
   * @parameter
   * @readonly
   */
  private List<String> buildActions;

  /**
   * The code sign identity is used to select the provisioning profile (e.g.
   * <code>iPhone Distribution</code>, <code>iPhone Developer</code>).
   * 
   * @parameter expression="${xcode.codeSignIdentity}"
   * @since 1.2.0
   */
  private String codeSignIdentity;

  
  /**
   * Can be used to override the provisioning profile defined in the Xcode project target. You can set 
   * it to an empty String if you want to use the default provisioning profile.
   * 
   * @parameter expression="${xcode.provisioningProfile}"
   * @since 1.2.1
   */
  private String provisioningProfile;
  
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {

    final Log log = getLog();

    final String projectName = project.getArtifactId();

    final File projectDirectory;

    projectDirectory = getXCodeCompileDirectory();

    log.info("xCodeProject: " + projectName);
    log.info("xCodeConfiguration: " + getConfigurations());
    log.info("xCodeSDKs: " + getSDKs());
    log.info("Original xcode code root directory:" + project.getBuild().getSourceDirectory());
    log.info("XCode root directory used for the build: " + projectDirectory);
    log.info("BuildActions: " + getBuildActions());
    log.info("codeSignIdentity: " + codeSignIdentity);
    log.info("provisioningProfile: " + provisioningProfile);

    final XCodeContext context = new XCodeContext(projectName, getConfigurations(), getSDKs(), getBuildActions(),
          projectDirectory,
          codeSignIdentity, System.out);
    context.setProvisioningProfile(provisioningProfile);

    try {

      new XCodeManager(log).build(context);

    }
    catch (IOException ex) {
      throw new MojoExecutionException("XCodeBuild failed due to " + ex.getMessage(), ex);
    }
    catch (XCodeException ex) {
      throw new MojoExecutionException("XCodeBuild failed due to " + ex.getMessage(), ex);
    }
  }

  private List<String> getBuildActions()
  {
    return (buildActions == null || buildActions.isEmpty()) ? DEFAULT_BUILD_ACTIONS : Collections
      .unmodifiableList(buildActions);
  }
}
