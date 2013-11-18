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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

/**
 * Prepares the local build environment. Copies and unpacks the artifacts of the referenced projects
 * into the target folder.
 */
@Mojo(name="prepare-xcode-build", requiresDependencyResolution=ResolutionScope.COMPILE)
public class XCodePrepareMojo extends AbstractXCodeMojo
{
  @Parameter(property="project", readonly=true, required=true)
  public MavenProject project;

  @Component(role=ArchiverManager.class)
  private ArchiverManager archiverManager;

  /**
   * The entry point to Aether, i.e. the component doing all the work.
   */
  @Component
  protected RepositorySystem repoSystem;

  /**
   * The current repository/network configuration of Maven.
   */
  @Parameter(defaultValue="${repositorySystemSession}", readonly=true)
  protected RepositorySystemSession repoSession;

  /**
   * The project's remote repositories to use for the resolution of project dependencies.
   */
  @Parameter(defaultValue="${project.remoteProjectRepositories}", readonly=true)
  protected List<RemoteRepository> projectRepos;

  /**
   * If set to <code>true</code> the dependency resolution will try to retrieve the fat libs instead
   * of the sdk specific ones. In all cases the lib resolution will try to fallback to the other
   * library type if the preferred type is not available.
   * 
   * @since 1.5.2
   */
  @Parameter(property="xcode.preferFatLibs", defaultValue="false")
  protected boolean preferFatLibs;

  @Parameter(property="xcode.useSymbolicLinks", defaultValue="false")
  private boolean useSymbolicLinks;

  @Parameter
  private Map<String, String> additionalPackagingTypes;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {

    try {
      new XCodePrepareBuildManager(archiverManager,
            repoSession, repoSystem, projectRepos, useSymbolicLinks,
            additionalPackagingTypes).setPreferFalLibs(preferFatLibs)
        .prepareBuild(project, getConfigurations(), getSDKs());
    }
    catch (XCodeException ex) {
      throw new MojoExecutionException(
            "Cannot prepare build environment", ex);
    }
    catch (IOException ex) {
      throw new MojoExecutionException(
            "Cannot prepare build environment", ex);
    }
  }
}
