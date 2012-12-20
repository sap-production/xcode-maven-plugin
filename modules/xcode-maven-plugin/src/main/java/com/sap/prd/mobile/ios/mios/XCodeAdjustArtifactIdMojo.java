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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Appends a suffix to the artifactId. No actions are taken if the suffix is not specified or has
 * zero length.
 * 
 * @goal change-artifact-id
 * 
 */
public class XCodeAdjustArtifactIdMojo extends AbstractXCodeMojo
{

  /**
   * If not empty the artifactId gets appended by '_&lt;artifactIdSuffix&gt;'
   * 
   * @parameter expression="${xcode.artifactIdSuffix}"
   * @since 1.2.0
   */
  private String artifactIdSuffix;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {

    getLog().info("artifactIdSuffix is: " + artifactIdSuffix);

    if (artifactIdSuffix != null && !"".equals(artifactIdSuffix.trim())) {
      final String originalArtifactId = project.getArtifactId();
      project.getArtifact().setArtifactId(originalArtifactId + "_" + artifactIdSuffix);
      getLog().info(
            "ArtifactId has been updated from '" + originalArtifactId + "' to '"
                  + project.getArtifact().getArtifactId() + "'.");
    }
    else {
      getLog().info("ArtifactId has not been modified.");
    }
  }
}
