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
package com.sap.prd.mobile.ios.mios.task;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;

import com.sap.prd.mobile.ios.mios.Forker;
import com.sap.prd.mobile.ios.mios.GAVUtil;
import com.sap.prd.mobile.ios.mios.MavenBuildFolderLayout;
import com.sap.prd.mobile.ios.mios.PackagingType;
import com.sap.prd.mobile.ios.mios.ScriptRunner;
import com.sap.prd.mobile.ios.mios.SideArtifactNotFoundException;
import com.sap.prd.mobile.ios.mios.XCodeDownloadManager;
import com.sap.prd.mobile.ios.mios.XCodeException;

public class XCodePrepareBuildTask
{

  private final static String TYPE_HEADERS = "headers.tar", TYPE_ARCHIVE = "a";

  private Log log;
  private ArchiverManager archiverManager;
  private RepositorySystem repoSystem;
  private RepositorySystemSession repoSystemSession;
  private List<RemoteRepository> projectRepos;
  
  private MavenProject project;
  private String configuration, sdk;
  
  private XCodeDownloadManager downloadManager;

  public XCodePrepareBuildTask setConfiguration(String configuration)
  {
    this.configuration = configuration;
    return this;
  }
  
  public XCodePrepareBuildTask setSdk(String sdk)
  {
    this.sdk = sdk;
    return this;
  }
  
  public XCodePrepareBuildTask setLog(Log log)
  {
    this.log = log;
    return this;
  }
  
  public XCodePrepareBuildTask setMavenProject(MavenProject project)
  {
    this.project = project;
    return this;
  }
  
  public XCodePrepareBuildTask setProjectRepos(List<RemoteRepository> projectRepos)
  {
    this.projectRepos = projectRepos;
    return this;
  }
  
  public XCodePrepareBuildTask setRepositorySystemSession(RepositorySystemSession repoSystemSession)
  {
    this.repoSystemSession = repoSystemSession;
    return this;
  }
  
  public XCodePrepareBuildTask setRepositorySystem(RepositorySystem repoSystem)
  {
    this.repoSystem = repoSystem;
    return this;
  }
  
  
  public XCodePrepareBuildTask setArchiverManager(ArchiverManager archiverManager)
  {
    this.archiverManager = archiverManager;
    return this;
  }
  
  public void execute() throws XCodeException
  {
    
    this.downloadManager = new XCodeDownloadManager(projectRepos, repoSystem, repoSystemSession);
    
    for (@SuppressWarnings("rawtypes")
    final Iterator it = project.getCompileArtifacts().iterator(); it.hasNext();) {

      final Artifact mainArtifact = (Artifact) it.next();

      if (PackagingType.LIB.getMavenPackaging().equals(mainArtifact.getType())) {

            try {
              prepareHeaders(project, configuration, sdk, mainArtifact);
            }
            catch (SideArtifactNotFoundException e) {
              log.info("Headers not found for: '" + mainArtifact.getGroupId() + ":" + mainArtifact.getArtifactId()
                    + ":"
                    + mainArtifact.getVersion() + ":" + mainArtifact.getType() + "'.");
            }

            try {
              prepareLibrary(project, configuration, sdk, mainArtifact);
            }
            catch (SideArtifactNotFoundException e) {
              throw new XCodeException("Library not found for: " + mainArtifact.getGroupId() + ":"
                    + mainArtifact.getArtifactId() + ":" + mainArtifact.getVersion() + ":"
                    + mainArtifact.getClassifier()
                    + ":" + mainArtifact.getType(), e);

        }

        try {
          prepareBundles(project, mainArtifact);
        }
        catch (SideArtifactNotFoundException e) {
          log.info("Bundle not found for: '" + mainArtifact.getGroupId() + ":" + mainArtifact.getArtifactId() + ":"
                + mainArtifact.getVersion() + ":" + mainArtifact.getType() + "'.");
        }
      }
      else if ("xcode-framework".equals(mainArtifact.getType())) {
        prepareFramework(project, mainArtifact);
      }
      else
        continue;
    }
  }

  private void prepareBundles(MavenProject project, final Artifact primaryArtifact) throws XCodeException
  {
    List<String> bundles = readBundleInformation(project, primaryArtifact);
    if (bundles == null)
      return;

    for (String coords : bundles) {
      prepareBundle(project, primaryArtifact, coords);
    }
  }

  private void prepareBundle(MavenProject project, final Artifact primaryArtifact, String coords)
        throws SideArtifactNotFoundException, XCodeException
  {
    Artifact bundleArtifact = GAVUtil.getArtifact(coords);

    final org.sonatype.aether.artifact.Artifact artifact = downloadManager.resolveSideArtifact(bundleArtifact);

    if (artifact != null) {
      final File source = artifact.getFile();

      final File target = new File(MavenBuildFolderLayout.getFolderForExtractedBundlesWithGA(project,
            primaryArtifact.getGroupId(), primaryArtifact.getArtifactId()), bundleArtifact.getClassifier().replaceAll(
            "~", File.separator)
            + ".bundle");

      createDirectory(target);
      unarchive("zip", source, target);

      log.info("Bundle unarchived from " + source + " to " + target);

    }
  }

  @SuppressWarnings("unchecked")
  private List<String> readBundleInformation(MavenProject project, Artifact primaryArtifact) throws XCodeException
  {

    final File mainArtifactExtracted = MavenBuildFolderLayout.getFolderForExtractedPrimaryArtifact(project,
          primaryArtifact);

    if (! mainArtifactExtracted.exists() && !mainArtifactExtracted.mkdirs())
      throw new XCodeException("Cannot create directory for expanded mainartifact of " + primaryArtifact.getGroupId()
            + ":" + primaryArtifact.getArtifactId() + " (" + mainArtifactExtracted + ").");

    unarchive("tar", primaryArtifact.getFile(), mainArtifactExtracted);

    File bundleFile = new File(mainArtifactExtracted, "bundles.txt");
    if (!bundleFile.exists())
      return null;

    try {
      return FileUtils.readLines(bundleFile);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  private void prepareLibrary(MavenProject project, final String xcodeConfiguration,
        final String sdk, final Artifact primaryArtifact) throws SideArtifactNotFoundException
  {

    final File source = downloadManager.resolveSideArtifact(primaryArtifact, xcodeConfiguration + "-" + sdk,
          TYPE_ARCHIVE).getFile();

    final File target = new File(MavenBuildFolderLayout.getFolderForExtractedLibsWithGA(project, xcodeConfiguration,
          sdk,
          primaryArtifact.getGroupId(), primaryArtifact.getArtifactId()), getArchiveFileName(primaryArtifact));

    try {
      
      if (ArtifactUtils.isSnapshot(primaryArtifact.getVersion()))
      {
        FileUtils.copyFile(source, target);
      }
      else
      {
        com.sap.prd.mobile.ios.mios.FileUtils.createSymbolicLink(source, target);
      }

    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }    
  }

  private void prepareHeaders(MavenProject project, String xcodeConfiguration,
        final String sdk, final Artifact primaryArtifact) throws SideArtifactNotFoundException, XCodeException
  {

    final org.sonatype.aether.artifact.Artifact headersArtifact = downloadManager.resolveSideArtifact(primaryArtifact,
          xcodeConfiguration + "-" + sdk, TYPE_HEADERS);

    if (headersArtifact != null) {

      final File headersDirectory = MavenBuildFolderLayout.getFolderForExtractedHeadersWithGA(project,
            xcodeConfiguration, sdk,
            primaryArtifact.getGroupId(), primaryArtifact.getArtifactId());

      createDirectory(headersDirectory);

      extractHeaders(headersDirectory, headersArtifact.getFile());
    }
  }

  private void prepareFramework(MavenProject project, final Artifact primaryArtifact) throws XCodeException
  {
    if (primaryArtifact != null) {
      final File source = primaryArtifact.getFile();

      final File target = MavenBuildFolderLayout.getFolderForExtractedFrameworkswithGA(project,
            primaryArtifact.getGroupId(), primaryArtifact.getArtifactId());

      createDirectory(target);
      unarchive("zip", source, target);

      try {
        extractFileWithShellScript(source, target, new File(project.getBuild().getDirectory()));
      }
      catch (IOException ioe) {
        throw new XCodeException("Cannot unarchive framework from " + source + " to " + target);
      }

      log.info("Framework unarchived from " + source + " to " + target);

    }
  }

  private static String getArchiveFileName(final Artifact primaryArtifact)
  {
    return "lib" + primaryArtifact.getArtifactId() + ".a";
  }

  private void extractHeaders(final File headersDirectory, final File headers)
  {
    unarchive("tar", headers, headersDirectory);
  }

  /**
   * Creates a directory. If the directory already exists the directory is deleted beforehand.
   * 
   * @param directory
   * @throws XCodeException 
   * @throws MojoExecutionException
   */
  private static void createDirectory(final File directory) throws XCodeException
  {

      try {
        if (0 != Forker.forkProcess(System.out,null, "rm", "-rf", directory.getAbsolutePath())) {
          throw new XCodeException("Cannot delete directory '" + directory + "'.");
        }
      }
      catch (IOException ex) {
        throw new XCodeException("Cannot delete directory '" + directory + "'.", ex);
      }

    if (!directory.mkdirs())
      throw new XCodeException("Cannot create directory '" + directory + "'.");
  }

  private void unarchive(final String archiverType, final File source, final File destinationDirectory)
  {
    try {
      UnArchiver unarchiver = archiverManager.getUnArchiver(archiverType);
      unarchiver.setSourceFile(source);
      unarchiver.setDestDirectory(destinationDirectory);
      unarchiver.extract();
    }
    catch (NoSuchArchiverException e) {
      throw new RuntimeException(e);
    }
    catch (ArchiverException e) {
      throw new RuntimeException(e);
    }
  }

  private void extractFileWithShellScript(File sourceFile, File destinationFolder, File tmpFolder) throws IOException
  {
    File workingDirectory = new File(tmpFolder, "scriptWorkingDir");
    workingDirectory.deleteOnExit();
    ScriptRunner.copyAndExecuteScript(System.out, "/com/sap/prd/mobile/ios/mios/unzip.sh", workingDirectory, sourceFile.getCanonicalPath(),
          destinationFolder.getCanonicalPath());
  }
}
