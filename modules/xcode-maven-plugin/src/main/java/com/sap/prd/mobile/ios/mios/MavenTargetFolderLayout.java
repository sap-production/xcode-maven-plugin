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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

/**
 * Helper methods for Xcode build to retrieve certain directories below the target folder.
 * 
 */
class MavenTargetFolderLayout
{
  private static String XCODE_DEPS_TARGET_FOLDER = "xcode-deps";

  private static File getFolderForExtractedHeaders(MavenProject project, final String configuration, final String sdk)
  {
    return new File(new File(new File(project.getBuild().getDirectory()), "headers"), configuration + "-" + sdk);
  }

  private static File getFolderForExtractedLibs(MavenProject project, final String configuration, final String sdk)
  {
    return new File(new File(new File(project.getBuild().getDirectory()), "libs"), configuration + "-" + sdk);
  }

  private static File getFolderForExtractedBundles(MavenProject project)
  {
    return new File(new File(project.getBuild().getDirectory()), "bundles");
  }

  private static File getFolderForExtractedFrameworks(MavenProject project)
  {
    return new File(new File(new File(project.getBuild().getDirectory()), XCODE_DEPS_TARGET_FOLDER), "frameworks");
  }

  static File getFolderForExtractedHeadersWithGA(MavenProject project, final String configuration, final String sdk,
        final String groupId, final String artifactId)
  {
    return new File(new File(getFolderForExtractedHeaders(project, configuration, sdk), groupId), artifactId);
  }

  static File getFolderForExtractedLibsWithGA(MavenProject project, final String configuration, final String sdk,
        final String groupId, final String artifactId)
  {
    return new File(new File(getFolderForExtractedLibs(project, configuration, sdk), groupId), artifactId);
  }

  static File getFolderForExtractedBundlesWithGA(MavenProject project, final String groupId, final String artifactId)
  {
    return new File(new File(getFolderForExtractedBundles(project), groupId), artifactId);
  }

  static File getFolderForExtractedFrameworkswithGA(MavenProject project, final String groupId, final String artifactId)
  {
    return new File(new File(getFolderForExtractedFrameworks(project), groupId), artifactId);
  }

  static File getFolderForExtractedMainArtifact(MavenProject project)
  {
    return new File(new File(project.getBuild().getDirectory()), "main.artifact");
  }

  static File getFolderForExtractedPrimaryArtifact(MavenProject project, Artifact primaryArtifact)
  {
    return new File(new File(new File(new File(project.getBuild().getDirectory()), "extractedPrimaryArtifacts"),
          primaryArtifact.getGroupId()), primaryArtifact.getArtifactId());
  }
}