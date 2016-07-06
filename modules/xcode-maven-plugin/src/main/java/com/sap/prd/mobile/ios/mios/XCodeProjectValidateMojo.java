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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Validates if the given project has a predefined structure. The convention expects that the name
 * of the Xcode project is the same as the artifact-Id.
 */
@Mojo(name="xcode-project-validate")
public class XCodeProjectValidateMojo extends AbstractXCodeMojo
{

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    try {
      XCodeProjectLayoutValidator.verifyXcodeFolder(new File(project.getBuild().getSourceDirectory()),
            project.getArtifactId());
      getLog().info("Project '" + project + "' successfully validated.");
    }
    catch (XCodeException e) {
      throw new MojoExecutionException("Project " + project.getArtifactId() + " (" + project.getBasedir()
            + ") is not a valid xcode project: " + e.getMessage() + ".", e);
    }
  }
}
