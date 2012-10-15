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
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.manager.ArchiverManager;

/**
 * Packages all the artifacts for Xcode libraries and prepares the generated artifacts for
 * deployment.
 * 
 * @goal xcode-package
 * 
 */
public class XCodePackageMojo extends AbstractXCodeMojo
{

  /**
   * @component role="org.codehaus.plexus.archiver.manager.ArchiverManager"
   * @required
   */
  private ArchiverManager archiverManager;

  /**
   * @component
   */
  private MavenProjectHelper projectHelper;

  /**
   * @parameter
   */
  private Set<String> bundles;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {

    try {

      for (String configuration : getConfigurations()) {
        for (String sdk : getSDKs()) {
          PackageHeadersTask packageHeadersTask = new PackageHeadersTask();
          packageHeadersTask.setMavenProject(project).setArchiverManager(archiverManager)
            .setCompileDir(getXCodeCompileDirectory()).setLog(getLog()).setProjectHelper(projectHelper)
            .setConfiguration(configuration).setSdk(sdk);
          packageHeadersTask.execute();

          File xcodeBuildDir = XCodeBuildLayout.getBuildDir(getXCodeCompileDirectory());
          AttachLibArtifactTask attachLibTask = new AttachLibArtifactTask();
          attachLibTask.setLog(getLog()).setMavenProject(project).setProjectHelper(projectHelper)
            .setXcodeBuildDir(xcodeBuildDir).setConfiguration(configuration).setSdk(sdk);
          attachLibTask.execute();
        }
      }

      if (bundles == null) {
        bundles = new HashSet<String>();
      }
      bundles.add(project.getArtifactId());
      PackageBundlesTask packageBundlesTask = new PackageBundlesTask();
      packageBundlesTask.setMavenProject(project).setArchiverManager(archiverManager).setBundleNames(bundles)
        .setCompileDir(getXCodeCompileDirectory()).setLog(getLog()).setProjectHelper(projectHelper);
      packageBundlesTask.execute();

      PackageLibMainArtifactTask packageLibMainArtifactTask = new PackageLibMainArtifactTask().setLog(getLog()).setArchiverManager(archiverManager)
        .setMavenProject(project);
      packageLibMainArtifactTask.execute();
    }
    catch (XCodeException ex) {
      throw new MojoExecutionException("Failed to package the library with headers and bundles", ex);
    }
  }
}
