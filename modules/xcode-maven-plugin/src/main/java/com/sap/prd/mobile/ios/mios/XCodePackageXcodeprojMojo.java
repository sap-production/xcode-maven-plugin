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
import java.util.Collections;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProjectHelper;

/**
 * Packages the Xcode project with all its resolved binary dependencies. I.e. this archive can be
 * unzipped and directly opened in Xcode.
 * 
 * If called from command line you have to call <code>mvn initialize</code> before in order to make
 * sure that all binary dependencies have been retrieved from the command line.
 * 
 * Please note that this goal is not part of the default lifecycle for xcode-lib and xcode-app
 * projects.
 * 
 * @goal package-xcodeproj
 * 
 */
public class XCodePackageXcodeprojMojo extends AbstractXCodeMojo
{


  public static final String XCODEPROJ_WITH_DEPS_CLASSIFIER = "xcodeproj-with-deps";

  /**
   * @component
   */
  private MavenProjectHelper projectHelper;


  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {

    String xprojZipFileName = project.getArtifactId() + "-" + XCODEPROJ_WITH_DEPS_CLASSIFIER + ".zip";

    try {
      // we have to do this via zip command line call in order to be able to package symbolic links
      ArrayList<String> zipCmdCall = new ArrayList<String>();
      File targetFolder = new File(project.getBuild().getDirectory());
      if (!targetFolder.isDirectory()) {
        targetFolder.mkdirs();
      }
      String relativeTargetDirName = FileUtils.getRelativePath(project.getBuild().getDirectory(), project.getBasedir()
        .getAbsolutePath(), "/");
      String relativeSrcDirName = FileUtils.getRelativePath(FolderLayout.getSourceFolder(project).getAbsolutePath(),
            project.getBasedir().getAbsolutePath(), "/");

      Collections.addAll(zipCmdCall, "zip", "-r", "-y", "-q", relativeTargetDirName + "/" + xprojZipFileName);

      zipCmdCall.add(relativeSrcDirName); // src/xcode folder
      zipCmdCall.add("pom.xml");
      zipCmdCall.add("sync.info");
      zipCmdCall.add(relativeTargetDirName + "/bundles");
      zipCmdCall.add(relativeTargetDirName + "/headers");
      zipCmdCall.add(relativeTargetDirName + "/libs");
      zipCmdCall.add(relativeTargetDirName + "/xcode-deps");
      getLog().info("Packaging the Xcode project with all its dependencies into the zip file " + xprojZipFileName);
      int exitCode = Forker.forkProcess(System.out, project.getBasedir(), zipCmdCall.toArray(new String[] {}));
      if (exitCode != 0) {
        throw new MojoExecutionException(
              "Could not package the Xcode project with all its dependencies into a zip file.");
      }
    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not package the Xcode project with all its dependencies into a zip file.");
    }
    
    projectHelper.attachArtifact(project, "zip", XCODEPROJ_WITH_DEPS_CLASSIFIER, new File(project.getBuild()
      .getDirectory(), xprojZipFileName));
    

  }


}
