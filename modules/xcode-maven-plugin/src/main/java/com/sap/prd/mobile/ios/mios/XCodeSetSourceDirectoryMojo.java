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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**.
 * Sets the maven source directory according to the needs for xcode based projects.
 * 
 * @goal set-source-directory
 */
public class XCodeSetSourceDirectoryMojo extends AbstractMojo
{

  private static final String DEFAULT_MAVEN_SOURCE_DIRECTORY = "src/main/java";
  private static final String DEFAULT_XCODE_SOURCE_DIRECTORY = "src/xcode";

  /**
   * @parameter expression="${project}"
   * @required
   */
  protected MavenProject project;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {

    try {
      setupSourceDirectory();
    } catch(final IOException ex) {
      throw new MojoExecutionException(ex.getMessage(), ex);
    }

    getLog().info("Summary:");
    getLog().info("${project.build.sourceDirectory}: " + project.getBuild().getSourceDirectory());
  }

  private void setupSourceDirectory() throws IOException
  {
    final File defaultMavenSourceDirectory = new File(project.getBasedir(), DEFAULT_MAVEN_SOURCE_DIRECTORY).getCanonicalFile();
    final File defaultXcodeSourceDirectory = new File(project.getBasedir(), DEFAULT_XCODE_SOURCE_DIRECTORY).getCanonicalFile();
    
    if (new File(project.getBuild().getSourceDirectory()).getCanonicalFile().equals(defaultMavenSourceDirectory)) {

      project.getBuild().setSourceDirectory(defaultXcodeSourceDirectory.getPath());

      getLog().info(
            "Build source directory was found to be '" + project.getBuild().getSourceDirectory()
                  + "' which is the maven default. Set to xcode default '" + defaultXcodeSourceDirectory + "'.");

    }
    else {
      getLog().info(
            "Build source directory was found to be '" + project.getBuild().getSourceDirectory()
                  + "' which is not the default value in maven. This value is not modified.");
    }
  }
}
