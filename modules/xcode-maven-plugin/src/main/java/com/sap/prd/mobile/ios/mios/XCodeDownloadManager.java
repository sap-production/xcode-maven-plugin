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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.artifact.DefaultArtifact;

class XCodeDownloadManager
{

  private final static Logger LOGGER = LogManager.getLogManager().getLogger(XCodePluginLogger.getLoggerName());

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

  org.eclipse.aether.artifact.Artifact resolveArtifact(final org.eclipse.aether.artifact.Artifact artifact)
        throws SideArtifactNotFoundException
  {
    return resolveSideArtifact(artifact, null, artifact.getExtension());
  }

  org.eclipse.aether.artifact.Artifact resolveSideArtifact(final org.apache.maven.artifact.Artifact mainArtifact,
        final String classifier,
        final String type) throws SideArtifactNotFoundException
  {
    return resolveSideArtifact(new DefaultArtifact(mainArtifact.getGroupId(), mainArtifact.getArtifactId(), null, null,
          mainArtifact.getVersion()), classifier, type);
  }

  public Set<org.eclipse.aether.artifact.Artifact> resolveArtifactWithTransitveDependencies(
        final Dependency dependency, final Set<String> scopes, final Set<org.eclipse.aether.artifact.Artifact> omits)
        throws DependencyCollectionException, SideArtifactNotFoundException
  {

    CollectRequest request = new CollectRequest();

    request.setRoot(dependency);
    CollectResult collectedDependencies = repoSystem.collectDependencies(repoSession, request);

    final DependencyNode root = collectedDependencies.getRoot();

    final Set<org.eclipse.aether.artifact.Artifact> artifacts = new HashSet<org.eclipse.aether.artifact.Artifact>();

    final Set<org.eclipse.aether.artifact.Artifact> _omits = new TreeSet<org.eclipse.aether.artifact.Artifact>(
          new Comparator<org.eclipse.aether.artifact.Artifact>() {

            @Override
            public int compare(org.eclipse.aether.artifact.Artifact a1, org.eclipse.aether.artifact.Artifact a2)
            {
              if (a1.getGroupId().compareTo(a2.getGroupId()) != 0) {
                return a1.getGroupId().compareTo(a2.getGroupId());
              }
              if (a1.getArtifactId().compareTo(a2.getArtifactId()) != 0) {
                return a1.getArtifactId().compareTo(a2.getArtifactId());
              }
              if (a1.getVersion().compareTo(a2.getVersion()) != 0) {
                return a1.getVersion().compareTo(a2.getVersion());
              }

              return 0;
            }
          });

    _omits.addAll(omits);

    root.accept(new DependencyVisitor() {

      @Override
      public boolean visitLeave(DependencyNode node)
      {
        return true;
      }

      @Override
      public boolean visitEnter(DependencyNode node)
      {
        if (scopes.contains(node.getDependency().getScope()) && (!_omits.contains(node.getDependency().getArtifact())))
        {
          artifacts.add(node.getDependency().getArtifact());
          if (LOGGER.isLoggable(Level.FINER))
          {
            final Artifact depArtifact = node.getDependency().getArtifact();
            final Artifact rootArtifact = root.getDependency().getArtifact();;
            LOGGER.finer(String.format("Adding transitive dependency '%s:%s:%s' for artifact '%s:%s:%s'",
                  depArtifact.getGroupId(), depArtifact.getArtifactId(), depArtifact.getVersion(),
                  rootArtifact.getGroupId(), rootArtifact.getArtifactId(), rootArtifact.getVersion()));
          }
          return true;
        }
        if (LOGGER.isLoggable(Level.FINER))
        {
          final Artifact depArtifact = node.getDependency().getArtifact();
          final Artifact rootArtifact = root.getDependency().getArtifact();;
          LOGGER.finer(String.format(
                "Omitting transitive dependency '%s:%s:%s' and the transitive envelope for artifact '%s:%s:%s'",
                depArtifact.getGroupId(), depArtifact.getArtifactId(), depArtifact.getVersion(),
                rootArtifact.getGroupId(), rootArtifact.getArtifactId(), rootArtifact.getVersion()));
        }
        return false;
      }
    });

    final Set<org.eclipse.aether.artifact.Artifact> result = new HashSet<org.eclipse.aether.artifact.Artifact>();

    for (org.eclipse.aether.artifact.Artifact myArtifact : artifacts) {
      org.eclipse.aether.artifact.Artifact resolvedArtifact = resolveArtifact(myArtifact);
      result.add(resolvedArtifact);
    }
    return result;
  }

  /**
   * 
   * @return The requested artifact according to the <code>groupId</code>, <code>artifactId</code>
   *         and <code>version</code> of the main artifact and <code>classifier</code> and
   *         <code>type</code> as specified by the actual parameters.
   * @throws SideArtifactNotFoundException
   *           If the requested artifact does not exist inside the remote repositories
   */
  org.eclipse.aether.artifact.Artifact resolveSideArtifact(final org.eclipse.aether.artifact.Artifact mainArtifact,
        final String classifier,
        final String type)
        throws SideArtifactNotFoundException
  {
    final org.eclipse.aether.artifact.Artifact sideArtifact = getSideArtifact(mainArtifact, classifier, type);

    final ArtifactRequest artifactRequest = new ArtifactRequest();
    artifactRequest.setRepositories(projectRepos);
    artifactRequest.setArtifact(sideArtifact);

    try {

      final ArtifactResult result = repoSystem.resolveArtifact(repoSession, artifactRequest);

      return getResolvedSideArtifact(sideArtifact, result);

    }
    catch (ArtifactResolutionException ex) {
      
      final boolean sideArtifactAvailable = sideArtifact != null;
      throw new SideArtifactNotFoundException("Side artifact " +
            (sideArtifactAvailable ? sideArtifact.getGroupId() : "<n/a>") + ":" +
            (sideArtifactAvailable ? sideArtifact.getArtifactId() : "<n/a>") + ":" +
            (sideArtifactAvailable ? sideArtifact.getVersion() : "<n/a>") + ":" +
            (sideArtifactAvailable ? sideArtifact.getClassifier() : "<n/a>")
            + " could not be resolved.", sideArtifact, ex);
    }
  }

  /**
   * 
   * @return The requested artifact or <code>null</code> if the requested artifact does not exist
   *         inside the remote repositories
   * @throws SideArtifactNotFoundException
   */
  org.eclipse.aether.artifact.Artifact resolveSideArtifact(final org.apache.maven.artifact.Artifact artifact)
        throws SideArtifactNotFoundException
  {
    return resolveSideArtifact(artifact, artifact.getClassifier(), artifact.getType());
  }

  private org.eclipse.aether.artifact.Artifact getResolvedSideArtifact(
        final org.eclipse.aether.artifact.Artifact sideArtifact,
        final ArtifactResult result) throws SideArtifactNotFoundException
  {

    org.eclipse.aether.artifact.Artifact resolvedArtifact = result.getArtifact();

    final File artifactFile = resolvedArtifact.getFile();

    if (artifactFile == null || !artifactFile.exists())
      throw new SideArtifactNotFoundException("Problems with resolving artifact \"" + sideArtifact
            + "\". The file that corresponds to that artifact (" + artifactFile
            + ") should be present in local repo but could not be found.", sideArtifact);

    return resolvedArtifact;
  }

  private DefaultArtifact getSideArtifact(final org.eclipse.aether.artifact.Artifact mainArtifact,
        final String classifier,
        String type)
  {
    return new DefaultArtifact(mainArtifact.getGroupId(), mainArtifact.getArtifactId(), classifier,
          type, mainArtifact.getVersion());
  }
}
