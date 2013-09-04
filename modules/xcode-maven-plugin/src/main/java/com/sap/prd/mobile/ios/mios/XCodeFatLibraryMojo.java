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
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

/**
 * Calls the lipo tool for creating universal (multi-architecture) libraries and prepares the
 * generated artifacts for deployment.
 * 
 * @goal package-fat-lib
 */
public class XCodeFatLibraryMojo extends AbstractXCodeMojo
{

  public final static String FAT_LIBRARY_CLASSIFIER_SUFFIX = "-fat-binary";

  /**
   * @component
   */
  private MavenProjectHelper projectHelper;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    if (!project.getPackaging().equals(PackagingType.LIB.getMavenPackaging()))
      return;

    for (String config : getConfigurations()) {
      try {
        createFatLibrary(config);
      }
      catch (IOException e) {
        throw new MojoExecutionException(e.getMessage());
      }
    }
  }

  private void createFatLibrary(String configuration) throws IOException, MojoExecutionException
  {

    List<String> lipoCommand = new ArrayList<String>();

    lipoCommand.add("lipo");
    lipoCommand.add("-create");

    for (String sdk : getSDKs()) {
      lipoCommand.add(XCodeBuildLayout.getBinary(XCodeBuildLayout.getBuildDir(getXCodeCompileDirectory()),
            configuration, sdk, project.getArtifactId()).getAbsolutePath());
    }
    final File fatBinaryDestDirectory = new File(new File(project.getBuild().getDirectory()), configuration);
    
    FileUtils.mkdirs(fatBinaryDestDirectory);

    final File fatLibrary = new File(fatBinaryDestDirectory, "lib" + project.getArtifactId() + ".a");

    lipoCommand.add("-output");
    lipoCommand.add(fatLibrary.getAbsolutePath());

    final int result = Forker.forkProcess(System.out, null, lipoCommand.toArray(new String[lipoCommand.size()]));

    if (result != 0)
      throw new MojoExecutionException("Lipo tool invocation failed. Check log file for details.");

    getLog().info("Fat binary '" + fatLibrary + "' created for configuration '" + configuration + "'.");

    prepareFatLibraryForDeployment(project, configuration, fatLibrary);
  }

  private void prepareFatLibraryForDeployment(final MavenProject mavenProject, final String configuration,
        final File fatLibrary)
  {

    projectHelper.attachArtifact(mavenProject, "a", configuration + FAT_LIBRARY_CLASSIFIER_SUFFIX, fatLibrary);
    getLog().info(
          "Fat binary '" + fatLibrary + "' attached as additional artifact with classifier '" + configuration + ".");
  }

}
