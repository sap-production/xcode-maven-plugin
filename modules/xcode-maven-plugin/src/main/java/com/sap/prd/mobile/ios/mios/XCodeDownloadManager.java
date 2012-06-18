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
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

class XCodeDownloadManager
{

  private final List<RemoteRepository> projectRepos;
  private final RepositorySystem repoSystem;
  private final RepositorySystemSession repoSession;

  XCodeDownloadManager(final List<RemoteRepository> projectRepos,
        final RepositorySystem repoSystem, final RepositorySystemSession repoSession)
  {
    this.projectRepos = projectRepos;
    this.repoSystem = repoSystem;
    this.repoSession = repoSession;
  }

  /**
   * 
   * @return The requested artifact or <code>null</code> if the requested artifact does not exist
   *         inside the remote repositories
   * @throws SideArtifactNotFoundException
   */
  org.sonatype.aether.artifact.Artifact resolveSideArtifact(final Artifact mainArtifact, final String classifier,
        final String type)
        throws SideArtifactNotFoundException
  {
    final DefaultArtifact sideArtifact = getSideArtifact(mainArtifact, classifier, type);

    final ArtifactRequest artifactRequest = new ArtifactRequest();
    artifactRequest.setRepositories(projectRepos);
    artifactRequest.setArtifact(sideArtifact);

    try {

      final ArtifactResult result = repoSystem.resolveArtifact(repoSession, artifactRequest);

      return getResolvedSideArtifact(sideArtifact, result);

    }
    catch (ArtifactResolutionException ex) {
      throw new SideArtifactNotFoundException("Side artifact" + sideArtifact != null ? sideArtifact.getGroupId()
            : "<n/a>" + ":" + sideArtifact != null ? sideArtifact.getArtifactId()
                  : "<n/a>" + ":" + sideArtifact != null ? sideArtifact.getVersion()
                        : "<n/a>" + ":" + sideArtifact != null ? sideArtifact.getClassifier() : "<n/a>" + ":"
                              + " could not be resolved.", sideArtifact);
    }
  }

  /**
   * 
   * @return The requested artifact or <code>null</code> if the requested artifact does not exist
   *         inside the remote repositories
   * @throws SideArtifactNotFoundException
   */
  org.sonatype.aether.artifact.Artifact resolveSideArtifact(final Artifact artifact)
        throws SideArtifactNotFoundException
  {
    return resolveSideArtifact(artifact, artifact.getClassifier(), artifact.getType());
  }

  private org.sonatype.aether.artifact.Artifact getResolvedSideArtifact(final DefaultArtifact sideArtifact,
        final ArtifactResult result) throws SideArtifactNotFoundException
  {

    org.sonatype.aether.artifact.Artifact resolvedArtifact = result.getArtifact();

    final File artifactFile = resolvedArtifact.getFile();

    if (artifactFile == null || !artifactFile.exists())
      throw new SideArtifactNotFoundException("Problems with resolving artifact \"" + sideArtifact
            + "\". The file that corresponds to that artifact (" + artifactFile
            + ") should be present in local repo but could not be found.", sideArtifact);

    return resolvedArtifact;
  }

  private DefaultArtifact getSideArtifact(final Artifact mainArtifact, final String classifier,
        String type)
  {
    return new DefaultArtifact(mainArtifact.getGroupId(), mainArtifact.getArtifactId(), classifier,
          type, mainArtifact.getVersion());
  }
}
