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
import java.util.Collections;
import java.util.List;

/**
 * Contains all parameters and methods that are needed for mojos that invoke the 'xcodebuild'
 * command.
 * 
 */
public abstract class AbstractXCodeBuildMojo extends AbstractXCodeMojo
{

  protected final static List<String> DEFAULT_BUILD_ACTIONS = Collections.unmodifiableList(Arrays.asList("clean",
        "build"));

  /**
   * The Xcode build action to to execute (e.g. clean, build, install). By default
   * <code>clean</code> and <code>build</code> are executed.
   * 
   * @parameter
   */
  protected List<String> buildActions;

  /**
   * The code sign identity is used to select the provisioning profile (e.g.
   * <code>iPhone Distribution</code>, <code>iPhone Developer</code>).
   * 
   * @parameter expression="${xcode.codeSignIdentity}"
   * @since 1.2.0
   */
  protected String codeSignIdentity;

  /**
   * Can be used to override the provisioning profile defined in the Xcode project target. You can
   * set it to an empty String if you want to use the default provisioning profile.
   * 
   * @parameter expression="${xcode.provisioningProfile}"
   * @since 1.2.1
   */
  protected String provisioningProfile;
  
  /**
   * The Xcode target to be built. If not specified, the default target (the first target) will be built.
   * @parameter expression="${xcode.target}"
   * @since 1.4.1
   */
  protected String target;

  protected XCodeContext getXCodeContext()
  {
    final String projectName = project.getArtifactId();
    final File projectDirectory = getXCodeCompileDirectory();

    final XCodeContext context = new XCodeContext();
    context.setProjectName(projectName);
    context.setConfigurations(getConfigurations());
    context.setSDKs(getSDKs());
    context.setBuildActions(getBuildActions());
    context.setProjectRootDirectory(projectDirectory);
    context.setCodeSignIdentity(codeSignIdentity);
    context.setOut(System.out);
    context.setProvisioningProfile(provisioningProfile);
    context.setTarget(target);

    return context;
  }

  protected List<String> getBuildActions()
  {
    return (buildActions == null || buildActions.isEmpty()) ? DEFAULT_BUILD_ACTIONS : Collections
      .unmodifiableList(buildActions);
  }
}
