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

import static com.sap.prd.mobile.ios.mios.FileUtils.mkdirs;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;

class XCodePrepareBuildManager
{
  private final static Logger LOGGER = LogManager.getLogManager().getLogger(XCodePluginLogger.getLoggerName());
  
  private final static String TYPE_HEADERS = "headers.tar", TYPE_ARCHIVE = "a";

  private final ArchiverManager archiverManager;
  private final XCodeDownloadManager downloadManager;
  private final boolean useSymbolicLinks;
  private Map<String, String> additionalPackagingTypes;

  private boolean preferFatLibs;

  XCodePrepareBuildManager(final ArchiverManager archiverManager,
        final RepositorySystemSession repoSystemSession, final RepositorySystem repoSystem,
        final List<RemoteRepository> projectRepos, final boolean useSymbolicLinks,
        final Map<String, String> additionalPackagingTypes)
  {
    this.archiverManager = archiverManager;
    this.downloadManager = new XCodeDownloadManager(projectRepos, repoSystem, repoSystemSession);
    this.useSymbolicLinks = useSymbolicLinks;

    if (additionalPackagingTypes == null) {
      this.additionalPackagingTypes = Collections.emptyMap();
    }
    else {
      this.additionalPackagingTypes = additionalPackagingTypes;

    }

  }

  public XCodePrepareBuildManager setPreferFalLibs(boolean preferFatLibs)
  {
    this.preferFatLibs = preferFatLibs;
    return this;
  }

  void prepareBuild(final MavenProject project, Set<String> configurations,
        final Set<String> sdks) throws MojoExecutionException, XCodeException, IOException
  {

    prepareRootFolders(project, configurations, sdks);

    final Iterator<Artifact> dependentArtifacts = project.getArtifacts().iterator();

    if (!dependentArtifacts.hasNext()) {
      LOGGER.info("No dependencies found.");
    }

    while (dependentArtifacts.hasNext()) {

      final Artifact mainArtifact = (Artifact) dependentArtifacts.next();

      LOGGER.info("Preparing dependency: " + mainArtifact.getId());

      if (PackagingType.LIB.getMavenPackaging().equals(mainArtifact.getType())) {
        prepareLibrary(project, configurations, sdks, mainArtifact);
      }
      else if (PackagingType.FRAMEWORK.getMavenPackaging().equals(mainArtifact.getType())) {
        prepareFramework(project, mainArtifact, configurations);
      }
      else if (additionalPackagingTypes.keySet().contains(mainArtifact.getType())) {

        final PackagingTypeAction packagingTypeAction = PackagingTypeAction.valueOf(additionalPackagingTypes
          .get(mainArtifact.getType()));
        LOGGER.info("Packaging type '" + mainArtifact.getType() + "' found in pom. Action: " + packagingTypeAction);
        packagingTypeAction.perform(archiverManager, project, mainArtifact);
      }
      else {

        LOGGER.warning("Unknown dependency type detected: '" + mainArtifact.getType() + "'. The corresponding dependency '"
              + mainArtifact.getGroupId() + ":" + mainArtifact.getArtifactId() + ":" + mainArtifact.getVersion()
              + "' will be ignored.");
      }
    }
  }

  private void prepareLibrary(final MavenProject project,
        Set<String> configurations, final Set<String> sdks,
        final Artifact mainArtifact) throws MojoExecutionException,
        IOException, XCodeException
  {
    for (final String xcodeConfiguration : configurations) {

      Map<String, File> thinLibs = new HashMap<String, File>();

      for (final String sdk : sdks) {

        thinLibs.put(sdk, resolveThinLib(project, xcodeConfiguration, sdk, mainArtifact));

        try {
          prepareHeaders(project, xcodeConfiguration, sdk, mainArtifact);
        }
        catch (SideArtifactNotFoundException e) {
          LOGGER.info("Headers not found for: '" + mainArtifact.getGroupId() + ":" + mainArtifact.getArtifactId()
                + ":"
                + mainArtifact.getVersion() + ":" + mainArtifact.getType() + "'.");
        }
      }

      File fatLib = resolveFatLib(project, xcodeConfiguration, mainArtifact);

      if (thinLibs.values().contains(null)) {

        if (fatLib != null) {
          provideFatLib(fatLib, project, xcodeConfiguration, mainArtifact, sdks);
        }
        else {
          throw new XCodeException("Neither all thin libs nor fat lib available for " + mainArtifact.getId() + ".");
        }
      }
      else {

        if (preferFatLibs && fatLib != null)
          provideFatLib(fatLib, project, xcodeConfiguration, mainArtifact, sdks);
        else
          provideThinLibs(thinLibs, xcodeConfiguration, mainArtifact, project);
      }
    }

    try {
      prepareBundles(project, mainArtifact);
    }
    catch (SideArtifactNotFoundException e) {
      LOGGER.info("Bundle not found for: '" + mainArtifact.getGroupId() + ":" + mainArtifact.getArtifactId() + ":"
            + mainArtifact.getVersion() + ":" + mainArtifact.getType() + "'.");
    }
  }

  private static void prepareRootFolders(MavenProject project, Set<String> configurations, Set<String> sdks)
        throws IOException
  {
    mkdirs(FolderLayout.getFolderForExtractedMainArtifact(project));
    mkdirs(FolderLayout.getFolderForExtractedBundles(project));
    mkdirs(FolderLayout.getFolderForExtractedFrameworks(project));

    for (String configuration : configurations) {

      mkdirs(FolderLayout.getFolderForExtractedFatLibs(project, configuration));

      for (String sdk : sdks) {
        mkdirs(FolderLayout.getFolderForExtractedHeaders(project, configuration, sdk));
        mkdirs(FolderLayout.getFolderForExtractedLibs(project, configuration, sdk));
      }
    }
  }

  private File resolveThinLib(MavenProject project, final String xcodeConfiguration, final String sdk,
        final Artifact primaryArtifact)
  {
    try {
      return downloadManager.resolveSideArtifact(primaryArtifact,
            xcodeConfiguration + "-" + sdk, TYPE_ARCHIVE).getFile();
    }
    catch (SideArtifactNotFoundException ex) {
      LOGGER.info("Library not found for: " + primaryArtifact.getGroupId() + ":"
            + primaryArtifact.getArtifactId() + ":" + primaryArtifact.getVersion() + ":"
            + primaryArtifact.getClassifier()
            + ":" + primaryArtifact.getType());
      return null;
    }
  }

  void provideThinLibs(Map<String, File> thinLibs, String xcodeConfiguration, Artifact mainArtifact,
        MavenProject project) throws IOException
  {
    for (Map.Entry<String, File> e : thinLibs.entrySet()) {
      provideThinLib(e.getValue(), project, xcodeConfiguration, e.getKey(), mainArtifact);
    }
  }

  private void prepareBundles(MavenProject project, final Artifact primaryArtifact) throws MojoExecutionException,
        SideArtifactNotFoundException, IOException
  {
    List<String> bundles = readBundleInformation(project, primaryArtifact);
    if (bundles == null)
      return;

    for (String coords : bundles) {
      prepareBundle(project, primaryArtifact, coords);
    }
  }

  private void prepareBundle(MavenProject project, final Artifact primaryArtifact, String coords)
        throws SideArtifactNotFoundException, MojoExecutionException
  {
    Artifact bundleArtifact = GAVUtil.getArtifact(coords);

    final org.sonatype.aether.artifact.Artifact artifact = downloadManager.resolveSideArtifact(bundleArtifact);

    if (artifact != null) {
      final File source = artifact.getFile();

      final File target = new File(FolderLayout.getFolderForExtractedBundlesWithGA(project,
            primaryArtifact.getGroupId(), primaryArtifact.getArtifactId()), bundleArtifact.getClassifier().replaceAll(
            "~", File.separator)
            + ".bundle");

      createDirectory(target);
      com.sap.prd.mobile.ios.mios.FileUtils.unarchive(archiverManager, "zip", source, target);

      LOGGER.info("Bundle unarchived from " + source + " to " + target);

    }
  }

  @SuppressWarnings("unchecked")
  private List<String> readBundleInformation(MavenProject project, Artifact primaryArtifact) throws IOException
  {

    final File mainArtifactExtracted = FolderLayout.getFolderForExtractedPrimaryArtifact(project,
          primaryArtifact);

    if (mainArtifactExtracted.exists())
      com.sap.prd.mobile.ios.mios.FileUtils.deleteDirectory(mainArtifactExtracted);

    if (!mainArtifactExtracted.mkdirs())
      throw new IOException("Cannot create directory for expanded mainartefact of " + primaryArtifact.getGroupId()
            + ":" + primaryArtifact.getArtifactId() + " (" + mainArtifactExtracted + ").");

    com.sap.prd.mobile.ios.mios.FileUtils.unarchive(archiverManager, "tar", primaryArtifact.getFile(),
          mainArtifactExtracted);

    LOGGER.info("Main artifact extracted to '" + mainArtifactExtracted + "'.");

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

  private void provideThinLib(final File source, MavenProject project, final String xcodeConfiguration,
        final String sdk, final Artifact primaryArtifact) throws IOException
  {

    final File target = new File(FolderLayout.getFolderForExtractedLibsWithGA(project, xcodeConfiguration,
          sdk,
          primaryArtifact.getGroupId(), primaryArtifact.getArtifactId()), getArchiveFileName(primaryArtifact));

    if (ArtifactUtils.isSnapshot(primaryArtifact.getVersion()) || !useSymbolicLinks()) {
      FileUtils.copyFile(source, target);
    }
    else {
      com.sap.prd.mobile.ios.mios.FileUtils.createSymbolicLink(source, target);
    }
  }

  private File resolveFatLib(MavenProject project, final String xcodeConfiguration, final Artifact primaryArtifact)
  {
    try {
      return downloadManager.resolveSideArtifact(primaryArtifact,
            xcodeConfiguration + XCodeFatLibraryMojo.FAT_LIBRARY_CLASSIFIER_SUFFIX,
            TYPE_ARCHIVE).getFile();
    }
    catch (SideArtifactNotFoundException ex) {
      LOGGER.info("There does not exist a fat library for the artifact " + primaryArtifact.getId());
      return null;
    }
  }

  private void provideFatLib(File source, MavenProject project, final String xcodeConfiguration,
        final Artifact primaryArtifact, Set<String> sdks) throws IOException
  {

    final File target = new File(FolderLayout.getFolderForExtractedFatLibsWithGA(project, xcodeConfiguration,
          primaryArtifact.getGroupId(), primaryArtifact.getArtifactId()), getArchiveFileName(primaryArtifact));

    if (ArtifactUtils.isSnapshot(primaryArtifact.getVersion()) || !useSymbolicLinks()) {
      FileUtils.copyFile(source, target);
    }
    else {
      com.sap.prd.mobile.ios.mios.FileUtils.createSymbolicLink(source, target);
    }

    final FatLibAnalyzer lipoHelper = new FatLibAnalyzer(target);

    //
    // TODO improve hard coded strings for iphoneos and iphonesimulator below
    //
    if (sdks.contains("iphoneos")) {
      if (!lipoHelper.containsArmv())
        LOGGER.warning("Fat library '" + lipoHelper.getFatLibrary() + "' does not contain a library for armv*.");
      else
        LOGGER.info("Fat library '" + lipoHelper.getFatLibrary() + "'contains a library for armv*.");
    }
    else if (sdks.contains("iphonesimulator")) {
      if (!lipoHelper.containsI386())
        LOGGER.warning("Fat library '" + lipoHelper.getFatLibrary() + "' does not contain a library for i386.");
      else
        LOGGER.info("Fat library '" + lipoHelper.getFatLibrary() + "'contains a library for i386.");
    }
  }

  private boolean useSymbolicLinks()
  {
    return this.useSymbolicLinks;
  }

  private void prepareHeaders(MavenProject project, String xcodeConfiguration,
        final String sdk, final Artifact primaryArtifact) throws MojoExecutionException, SideArtifactNotFoundException
  {

    final org.sonatype.aether.artifact.Artifact headersArtifact = downloadManager.resolveSideArtifact(primaryArtifact,
          xcodeConfiguration + "-" + sdk, TYPE_HEADERS);

    if (headersArtifact != null) {

      final File headersDirectory = FolderLayout.getFolderForExtractedHeadersWithGA(project,
            xcodeConfiguration, sdk,
            primaryArtifact.getGroupId(), primaryArtifact.getArtifactId());

      createDirectory(headersDirectory);

      extractHeaders(headersDirectory, headersArtifact.getFile());
    }
  }

  private void prepareFramework(MavenProject project, final Artifact primaryArtifact, Collection<String> configurations)
        throws MojoExecutionException
  {

    for (String configuration : configurations) {
      try {
        org.sonatype.aether.artifact.Artifact frameworkArtifact = downloadManager.resolveSideArtifact(primaryArtifact,
              configuration, Types.FRAMEWORK);
        extractFramework(project, primaryArtifact, configuration, frameworkArtifact.getFile());
      }
      catch (SideArtifactNotFoundException e) {
        LOGGER
          .warning("Framework '"
                + primaryArtifact
                + "' does not contain configuration specific variant. Will download the generic framework for configuration '"
                + configuration + "'.");
        handlePrimaryArtifact(project, primaryArtifact, configurations);
      }
    }
  }

  private void extractFramework(MavenProject project, final Artifact primaryArtifact, String configuration,
        File frameworkArtifact) throws MojoExecutionException
  {
    File target = FolderLayout.getFolderForExtractedFrameworkswithGA(project, primaryArtifact.getGroupId(),
          primaryArtifact.getArtifactId(), configuration);
    createDirectory(target);
    try {
      extractFileWithShellScript(frameworkArtifact, target, new File(project.getBuild().getDirectory()));
    }
    catch (IOException ioe) {
      throw new MojoExecutionException("Cannot unarchive framework from " + frameworkArtifact + " to "
            + target);
    }
  }

  private void handlePrimaryArtifact(MavenProject project, final Artifact primaryArtifact,
        Collection<String> configurations)
        throws MojoExecutionException
  {
    if (primaryArtifact != null) {
      final File source = primaryArtifact.getFile();
      for (String configuration : configurations) {
        final File target = FolderLayout.getFolderForExtractedFrameworkswithGA(project,
              primaryArtifact.getGroupId(), primaryArtifact.getArtifactId());
        extractFramework(project, primaryArtifact, configuration, source);

        try {
          extractFileWithShellScript(source, target, new File(project.getBuild().getDirectory()));
        }
        catch (IOException ioe) {
          throw new MojoExecutionException("Cannot unarchive framework from " + source + " to " + target);
        }

        LOGGER.info("Framework unarchived from " + source + " to " + target);
      }
    }
  }

  private static String getArchiveFileName(final Artifact primaryArtifact)
  {
    return "lib" + primaryArtifact.getArtifactId() + ".a";
  }

  private void extractHeaders(final File headersDirectory, final File headers)
  {
    com.sap.prd.mobile.ios.mios.FileUtils.unarchive(archiverManager, "tar", headers, headersDirectory);
  }

  /**
   * Creates a directory. If the directory already exists the directory is deleted beforehand.
   * 
   * @param directory
   * @throws MojoExecutionException
   */
  private static void createDirectory(final File directory) throws MojoExecutionException
  {

    try {
      com.sap.prd.mobile.ios.mios.FileUtils.deleteDirectory(directory);
    }
    catch (IOException ex) {
      throw new MojoExecutionException("", ex);
    }

    if (!directory.mkdirs())
      throw new MojoExecutionException("Cannot create directory (" + directory + ")");
  }

  private void extractFileWithShellScript(File sourceFile, File destinationFolder, File tmpFolder) throws IOException
  {
    File workingDirectory = new File(tmpFolder, "scriptWorkingDir");
    workingDirectory.deleteOnExit();
    ScriptRunner.copyAndExecuteScript(System.out, "/com/sap/prd/mobile/ios/mios/unzip.sh", workingDirectory,
          sourceFile.getCanonicalPath(),
          destinationFolder.getCanonicalPath());
  }

}
