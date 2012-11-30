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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProjectHelper;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.xml.sax.SAXException;

import com.sap.prd.mobile.ios.mios.CodeSignManager.ExecResult;
import com.sap.prd.mobile.ios.mios.CodeSignManager.ExecutionResultVerificationException;
import com.sap.prd.mobile.ios.mios.versioninfo.v_1_2_2.Dependency;

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
public class XCodeVersionInfoMojo extends BuildContextAwareMojo
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

    final File syncInfoFile = new File(mavenSession.getExecutionRootDirectory(), syncInfo);

    if (!syncInfoFile.exists()) {

      if (failOnMissingSyncInfo)
      {
        throw new MojoExecutionException("Sync info file '" + syncInfoFile.getAbsolutePath()
              + "' not found. Please configure your SCM plugin accordingly.");
      }

      getLog().info("The optional sync info file '" + syncInfoFile.getAbsolutePath()
            + "' not found. Cannot attach versions.xml to build results.");
      return;
    }

    getLog().info("Sync info file found: '" + syncInfoFile.getAbsolutePath() + "'. Creating versions.xml file.");

    final File versionsFile = new File(project.getBuild().getDirectory(), "versions.xml");

    FileOutputStream os = null;

    try {
      os = new FileOutputStream(versionsFile);

      new VersionInfoManager().createVersionInfoFile(project.getGroupId(), project.getArtifactId(),
            project.getVersion(), syncInfoFile, getDependencies(), os);

    }
    catch (JAXBException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
    catch (SAXException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
    catch (IOException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
    finally {
      IOUtils.closeQuietly(os);
    }

    if (getPackagingType() == PackagingType.APP)
    {
      try
      {
        copyVersionsXmlAndSign();
      }
      catch (IOException e) {
        throw new MojoExecutionException(e.getMessage(), e);
      }
      catch (ExecutionResultVerificationException e) {
        throw new MojoExecutionException(e.getMessage(), e);
      }
      catch(XCodeException e) {
        throw new MojoExecutionException(e.getMessage(), e);
      }
    }

    projectHelper.attachArtifact(project, "xml", "versions", versionsFile);

    getLog().info("versions.xml '" + versionsFile + " attached as additional artifact.");
  }

  private void copyVersionsXmlAndSign() throws IOException, ExecutionResultVerificationException, XCodeException
  {
    for (final String configuration : getConfigurations())
    {
      for (final String sdk : getSDKs())
      {
        if (sdk.startsWith("iphoneos"))
        {
          File versionsXmlInBuild = new File(project.getBuild().getDirectory(), "versions.xml");
          File rootDir = XCodeBuildLayout.getAppFolder(getXCodeCompileDirectory(), configuration, sdk);
          
          String productName = EffectiveBuildSettings.getBuildSetting(getXCodeContext(XCodeContext.SourceCodeLocation.WORKING_COPY), getLog(), configuration, sdk, EffectiveBuildSettings.PRODUCT_NAME);
          File appFolder = new File(rootDir, productName + ".app");
          File versionsXmlInApp = new File(appFolder, "versions.xml");

          CodeSignManager.verify(appFolder);
          final ExecResult originalCodesignEntitlementsInfo = CodeSignManager
            .getCodesignEntitlementsInformation(appFolder);
          final ExecResult originalSecurityCMSMessageInfo = CodeSignManager.getSecurityCMSInformation(appFolder);

          FileUtils.copyFile(versionsXmlInBuild, versionsXmlInApp);

          sign(rootDir, configuration, sdk);

          final ExecResult resignedCodesignEntitlementsInfo = CodeSignManager
            .getCodesignEntitlementsInformation(appFolder);
          final ExecResult resignedSecurityCMSMessageInfo = CodeSignManager.getSecurityCMSInformation(appFolder);
          CodeSignManager.verify(appFolder);
          CodeSignManager.verify(originalCodesignEntitlementsInfo, resignedCodesignEntitlementsInfo);
          CodeSignManager.verify(originalSecurityCMSMessageInfo, resignedSecurityCMSMessageInfo);
        }
      }
    }
  }

  private void sign(File rootDir, String configuration, String sdk) throws IOException, XCodeException
  {
    String csi = EffectiveBuildSettings.getBuildSetting(getXCodeContext(XCodeContext.SourceCodeLocation.WORKING_COPY), getLog(), configuration, sdk, EffectiveBuildSettings.CODE_SIGN_IDENTITY);
    File appFolder = new File(EffectiveBuildSettings.getBuildSetting(getXCodeContext(XCodeContext.SourceCodeLocation.WORKING_COPY), getLog(), configuration, sdk, EffectiveBuildSettings.CODESIGNING_FOLDER_PATH));
    CodeSignManager.sign(csi, appFolder, true);
  }

  private List<Dependency> getDependencies() throws JAXBException, SAXException, IOException
  {

    List<Dependency> result = new ArrayList<Dependency>();

    for (@SuppressWarnings("rawtypes")
    final Iterator it = project.getDependencyArtifacts().iterator(); it.hasNext();) {

      final Artifact mainArtifact = (Artifact) it.next();

      try {
        org.sonatype.aether.artifact.Artifact sideArtifact = new XCodeDownloadManager(projectRepos, repoSystem,
              repoSession).resolveSideArtifact(mainArtifact, "versions", "xml");
        getLog().info("Version information retrieved for artifact: " + mainArtifact);

        result.add(VersionInfoManager.parseDependency(sideArtifact.getFile()));
      }
      catch (SideArtifactNotFoundException e) {
        getLog().info("Could not retrieve version information for artifact:" + mainArtifact);
      }
    }
    return result;
  }
}
