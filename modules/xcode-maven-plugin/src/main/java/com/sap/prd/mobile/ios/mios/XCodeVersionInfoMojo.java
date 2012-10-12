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

import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProjectHelper;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;

/**
 * Generates a &lt;artifact-id&gt;-&lt;version&gt;-version.xml for reproducibility reasons. This
 * versions.xml contains information about the scm location and revision of the built project and
 * all its dependencies. Expects a sync.info file in the root folder of the project as input.
 * 
 * 
 * The sync.info file is a property file and must contain the following entries: <code>
 * <ul>
 *   <li> port=PORT
 *   <li> depotpath=DEPOT_PATH
 *   <li> changelist=CHANGELIST
 * </ul>
 * </code>
 * 
 * 
 * 
 * PORT entry is the SCM server where the project is located. <br>
 * DEPOT_PATH is the path to the project on the SCM server. For Perforce projects, DEPOT_PATH has
 * the following format //DEPOT_PATH/... <br>
 * CHANGELIST is the revision synced.
 * 
 * 
 * @goal attach-version-info
 * @requiresDependencyResolution
 */
public class XCodeVersionInfoMojo extends AbstractXCodeMojo
{

  /**
   * 
   * @parameter default-value="${session}"
   * @required
   * @readonly
   */
  protected MavenSession mavenSession;

  /**
   * The entry point to Aether, i.e. the component doing all the work.
   * 
   * @component
   */
  protected RepositorySystem repoSystem;

  /**
   * The current repository/network configuration of Maven.
   * 
   * @parameter default-value="${repositorySystemSession}"
   * @readonly
   */
  protected RepositorySystemSession repoSession;

  /**
   * The project's remote repositories to use for the resolution of project dependencies.
   * 
   * @parameter default-value="${project.remoteProjectRepositories}"
   * @readonly
   */
  protected List<RemoteRepository> projectRepos;

  /**
   * @component
   */
  private MavenProjectHelper projectHelper;

  /**
   * @parameter expression="${sync.info.file}" default-value="sync.info"
   */
  private String syncInfo;

  /**
   * If <code>true</code> the build fails if it does not find a sync.info file in the root directory
   * 
   * @parameter expression="${xcode.failOnMissingSyncInfo}" default-value="false"
   */
  private boolean failOnMissingSyncInfo;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {

    try {
      new AttachVersionInfoTask().setConfigurations(getConfigurations()).setFailOnMissingSyncInfo(failOnMissingSyncInfo)
        .setLog(getLog()).setMavenSession(mavenSession).setPackagingType(getPackagingType()).setProject(project)
        .setProjectHelper(projectHelper).setProjectRepos(projectRepos).setRepoSession(repoSession)
        .setRepoSystem(repoSystem).setSdks(getSDKs()).setSyncInfo(syncInfo)
        .setXcodeCompileDirectory(getXCodeCompileDirectory()).exectue();
    }
    catch (XCodeException ex) {
      throw new MojoExecutionException(ex.getMessage(), ex);
    }
  }
}
