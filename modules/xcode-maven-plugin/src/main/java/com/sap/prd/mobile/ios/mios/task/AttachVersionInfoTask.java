package com.sap.prd.mobile.ios.mios.task;

/*
 * #%L
 * Xcode Maven Plugin
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.xml.sax.SAXException;

import com.sap.prd.mobile.ios.mios.CodeSignManager;
import com.sap.prd.mobile.ios.mios.EffectiveBuildSettings;
import com.sap.prd.mobile.ios.mios.PackagingType;
import com.sap.prd.mobile.ios.mios.SideArtifactNotFoundException;
import com.sap.prd.mobile.ios.mios.VersionInfoManager;
import com.sap.prd.mobile.ios.mios.XCodeBuildLayout;
import com.sap.prd.mobile.ios.mios.XCodeDownloadManager;
import com.sap.prd.mobile.ios.mios.XCodeException;
import com.sap.prd.mobile.ios.mios.CodeSignManager.ExecResult;
import com.sap.prd.mobile.ios.mios.CodeSignManager.ExecutionResultVerificationException;
import com.sap.prd.mobile.ios.mios.versioninfo.v_1_2_2.Dependency;

public class AttachVersionInfoTask
{

  private Log log;
  private MavenSession mavenSession;
  private MavenProject project;
  private String syncInfo;
  private boolean failOnMissingSyncInfo;
  private PackagingType packagingType;
  private Set<String> configurations;
  private Set<String> sdks;
  private File xcodeCompileDirectory;
  private MavenProjectHelper projectHelper;
  private List<RemoteRepository> projectRepos;
  private RepositorySystem repoSystem;
  private RepositorySystemSession repoSession;
  
  public AttachVersionInfoTask setLog(Log log)
  {
    this.log = log;
    return this;
  }

  public AttachVersionInfoTask setMavenSession(MavenSession mavenSession)
  {
    this.mavenSession = mavenSession;
    return this;
  }

  public AttachVersionInfoTask setProject(MavenProject project)
  {
    this.project = project;
    return this;
  }

  public AttachVersionInfoTask setSyncInfo(String syncInfo)
  {
    this.syncInfo = syncInfo;
    return this;
  }

  public AttachVersionInfoTask setFailOnMissingSyncInfo(boolean failOnMissingSyncInfo)
  {
    this.failOnMissingSyncInfo = failOnMissingSyncInfo;
    return this;
  }

  public AttachVersionInfoTask setPackagingType(PackagingType packagingType)
  {
    this.packagingType = packagingType;
    return this;
  }

  public AttachVersionInfoTask setConfigurations(Set<String> configurations)
  {
    this.configurations = configurations;
    return this;
  }

  public AttachVersionInfoTask setSdks(Set<String> sdks)
  {
    this.sdks = sdks;
    return this;
  }

  public AttachVersionInfoTask setXcodeCompileDirectory(File xcodeCompileDirectory)
  {
    this.xcodeCompileDirectory = xcodeCompileDirectory;
    return this;
  }

  public AttachVersionInfoTask setProjectHelper(MavenProjectHelper projectHelper)
  {
    this.projectHelper = projectHelper;
    return this;
  }

  public AttachVersionInfoTask setProjectRepos(List<RemoteRepository> projectRepos)
  {
    this.projectRepos = projectRepos;
    return this;
  }

  public AttachVersionInfoTask setRepoSystem(RepositorySystem repoSystem)
  {
    this.repoSystem = repoSystem;
    return this;
  }

  public AttachVersionInfoTask setRepoSession(RepositorySystemSession repoSession)
  {
    this.repoSession = repoSession;
    return this;
  }

  public void exectue() throws XCodeException {
    final File syncInfoFile = new File(mavenSession.getExecutionRootDirectory(), syncInfo);

    if (!syncInfoFile.exists()) {

      if (failOnMissingSyncInfo)
      {
        throw new XCodeException("Sync info file '" + syncInfoFile.getAbsolutePath()
              + "' not found. Please configure your SCM plugin accordingly.");
      }

      log.info("The optional sync info file '" + syncInfoFile.getAbsolutePath()
            + "' not found. Cannot attach versions.xml to build results.");
      return;
    }

    log.info("Sync info file found: '" + syncInfoFile.getAbsolutePath() + "'. Creating versions.xml file.");

    final File versionsFile = new File(project.getBuild().getDirectory(), "versions.xml");

    FileOutputStream os = null;

    try {
      os = new FileOutputStream(versionsFile);

      new VersionInfoManager().createVersionInfoFile(project.getGroupId(), project.getArtifactId(),
            project.getVersion(), syncInfoFile, getDependencies(), os);

    }
    catch (JAXBException e) {
      throw new XCodeException(e.getMessage(), e);
    }
    catch (SAXException e) {
      throw new XCodeException(e.getMessage(), e);
    }
    catch (IOException e) {
      throw new XCodeException(e.getMessage(), e);
    }
    finally {
      IOUtils.closeQuietly(os);
    }

    if (packagingType == PackagingType.APP)
    {
      try
      {
        copyVersionsXmlAndSign();
      }
      catch (IOException e) {
        throw new XCodeException(e.getMessage(), e);
      }
      catch (ExecutionResultVerificationException e) {
        throw new XCodeException(e.getMessage(), e);
      }
    }

    projectHelper.attachArtifact(project, "xml", "versions", versionsFile);

    log.info("versions.xml '" + versionsFile + " attached as additional artifact.");

  }
  
  private void copyVersionsXmlAndSign() throws IOException, ExecutionResultVerificationException
  {
    for (final String configuration : configurations)
    {
      for (final String sdk : sdks)
      {
        if (sdk.startsWith("iphoneos"))
        {
          File versionsXmlInBuild = new File(project.getBuild().getDirectory(), "versions.xml");
          File rootDir = XCodeBuildLayout.getAppFolder(xcodeCompileDirectory, configuration, sdk);
          String productName = EffectiveBuildSettings.getProductName(this.project, configuration, sdk);
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

  private void sign(File rootDir, String configuration, String sdk) throws IOException
  {
    EffectiveBuildSettings settings = new EffectiveBuildSettings(this.project, configuration, sdk);
    String csi = settings.getBuildSetting(EffectiveBuildSettings.CODE_SIGN_IDENTITY);
    File appFolder = new File(settings.getBuildSetting(EffectiveBuildSettings.CODESIGNING_FOLDER_PATH));
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
        log.info("Version information retrieved for artifact: " + mainArtifact);

        result.add(VersionInfoManager.parseDependency(sideArtifact.getFile()));
      }
      catch (SideArtifactNotFoundException e) {
        log.info("Could not retrieve version information for artifact:" + mainArtifact);
      }
    }
    return result;
  }
}
