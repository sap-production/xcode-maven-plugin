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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

class XCodePackageManager
{

  private final static Logger LOGGER = LogManager.getLogManager().getLogger(XCodePluginLogger.getLoggerName());
  final ArchiverManager archiverManager;
  final MavenProjectHelper projectHelper;
  private static final String ZIPPED_BUNDLE_SUFFIX = "xcode-bundle-zip";

  XCodePackageManager(final ArchiverManager archiverManager, final MavenProjectHelper projectHelper)
  {

    this.archiverManager = archiverManager;
    this.projectHelper = projectHelper;
  }

  /**
   * Packages all the artifacts. The main artifact is set and all side artifacts are attached for
   * deployment.
   * 
   * @param bundles
   * 
   * @param buildDir
   */
  void packageArtifacts(final File compileDir, final MavenProject project, final Set<String> bundles)
        throws IOException, XCodeException
  {

    File mainArtifact = createMainArtifactFile(project);

    attachBundle(compileDir, project, bundles, mainArtifact);

    final File mainArtifactFile = archiveMainArtifact(project, mainArtifact);
    setMainArtifact(project, mainArtifactFile);

  }

  private void attachBundle(File compileDir, MavenProject project, Set<String> bundles, File mainArtifact)
        throws IOException
  {

    final Set<String> bundleNames = new HashSet<String>();

    for (String bundleName : bundles) {
      File bundleDirectory = XCodeBuildLayout.getBundleDirectory(compileDir, bundleName);

      if (!bundleDirectory.exists()) {
        LOGGER.info("Bundle directory '" + bundleDirectory + "' does not exist. Bundle will not be attached.");
        continue;
      }
      final File bundleFile = new File(new File(project.getBuild().getDirectory()), bundleName + ".bundle");

      try {

        archive("zip", bundleDirectory, bundleFile, new String[] { "**/*" }, null);
        LOGGER.info("Bundle zip file created (" + bundleFile + ")");
      }
      catch (XCodeException ex) {
        throw new RuntimeException("Could not archive header directory '" + bundleDirectory + "'", ex);
      }
      catch (NoSuchArchiverException ex) {
        throw new RuntimeException("Could not archive header directory '" + bundleDirectory + "'", ex);
      }

      String escapedBundleName = escapeBundleName(bundleName);
      prepareBundleFileForDeployment(project, bundleFile, escapedBundleName);
      bundleNames.add(getBundleReference(project, escapedBundleName));
    }

    addBundleInfoToMainArtifact(bundleNames, new File(mainArtifact, "bundles.txt"));
  }

  private String getBundleReference(MavenProject project, String escapedBundleName)
  {
    return GAVUtil.toColonNotation(project.getGroupId(), project.getArtifactId(), project.getVersion(),
          ZIPPED_BUNDLE_SUFFIX,
          escapedBundleName);
  }

  private File createMainArtifactFile(final MavenProject project) throws IOException
  {
    File mainArtifact = FolderLayout.getFolderForExtractedMainArtifact(project);

    if (mainArtifact.exists())
      com.sap.prd.mobile.ios.mios.FileUtils.deleteDirectory(mainArtifact);

    if (!mainArtifact.mkdirs())
      throw new IOException("Could not create directory '" + mainArtifact + "'.");

    FileUtils.writeStringToFile(new File(mainArtifact, "README.TXT"),
          "This zip file may contain additonal information about the depoyed artifacts. \n");
    return mainArtifact;
  }

  private File archiveMainArtifact(final MavenProject project, File mainArtifact) throws IOException
  {
    final File mainArtifactTarFile = new File(new File(project.getBuild().getDirectory()), "main.artifact.tar");

    try {

      archive("tar", mainArtifact, mainArtifactTarFile, new String[] { "**/*" }, null);
      LOGGER.info("header tar file created (" + mainArtifactTarFile + ")");
    }
    catch (XCodeException ex) {
      throw new RuntimeException("Could not archive main artifact directory '" + mainArtifact + "'", ex);
    }
    catch (NoSuchArchiverException ex) {
      throw new RuntimeException("Could not archive main artifact directory '" + mainArtifact + "'", ex);
    }
    return mainArtifactTarFile;
  }

  private void setMainArtifact(final MavenProject project, final File mainArtifactTarFile)
  {
    project.getArtifact().setFile(mainArtifactTarFile);
    LOGGER.info("Main artifact file '" + mainArtifactTarFile + "' attached for " + project.getArtifact());
  }

  void packageHeaders(final XCodeContext xcodeContext, MavenProject project,
        String relativeAlternatePublicHeaderFolderPath) throws IOException, XCodeException
  {
    final File publicHeaderFolderPath = getPublicHeaderFolderPath(EffectiveBuildSettings.getBuildSetting(xcodeContext, EffectiveBuildSettings.BUILT_PRODUCTS_DIR),
          EffectiveBuildSettings.getBuildSetting(xcodeContext, EffectiveBuildSettings.PUBLIC_HEADERS_FOLDER_PATH),
          relativeAlternatePublicHeaderFolderPath);

    if (!publicHeaderFolderPath.canRead()) {
      LOGGER.warning("Public header folder path '" + publicHeaderFolderPath + "' cannot be read. Unable to package headers.");
      return;
    }

    final File headersFile = new File(new File(new File(project.getBuild().getDirectory()),
          xcodeContext.getConfiguration() + "-" + xcodeContext.getSDK()),
          "headers.tar");

    try {

      archive("tar", publicHeaderFolderPath, headersFile, new String[] { "**/*.h" }, null);
      LOGGER.info("header tar file created (" + headersFile + ")");
    }
    catch (XCodeException ex) {
      throw new RuntimeException("Could not archive header directory '" + publicHeaderFolderPath + "'", ex);
    }
    catch (NoSuchArchiverException ex) {
      throw new RuntimeException("Could not archive header directory '" + publicHeaderFolderPath + "'", ex);
    }

    prepareHeaderFileForDeployment(project, xcodeContext.getConfiguration(), xcodeContext.getSDK(), headersFile);

  }

  private void prepareHeaderFileForDeployment(final MavenProject mavenProject, final String configuration,
        final String sdk, final File headersFile)
  {

    projectHelper.attachArtifact(mavenProject, "headers.tar", configuration + "-" + sdk, headersFile);
  }

  private void prepareBundleFileForDeployment(MavenProject mavenProject, File bundleFile, String escapedBundleName)
  {
    projectHelper.attachArtifact(mavenProject, ZIPPED_BUNDLE_SUFFIX, escapedBundleName, bundleFile);
  }

  String escapeBundleName(String bundleName)
  {

    return bundleName.replaceAll("/", "~");
  }

  private void addBundleInfoToMainArtifact(Set<String> bundleNames, File bundlesFile) throws IOException
  {
    final PrintWriter pw = new PrintWriter(new FileWriter(bundlesFile));

    try {

      for (final String bundleName : bundleNames) {
        pw.println(bundleName);

      }
    }
    finally {
      pw.close();
    }
  }

  static void attachLibrary(final XCodeContext xcodeContext, File buildDir,
        final MavenProject project, final MavenProjectHelper projectHelper)
  {

    final File fatBinary = XCodeBuildLayout.getBinary(buildDir, xcodeContext.getConfiguration(), xcodeContext.getSDK(),
          project.getArtifactId());

    if (!fatBinary.exists())
      throw new RuntimeException(fatBinary + " should be attached but does not exist.");

    final String classifier = xcodeContext.getConfiguration() + "-" + xcodeContext.getSDK();

    projectHelper.attachArtifact(project, "a", classifier, fatBinary);

    LOGGER.info("Archive file '" + fatBinary + "' attached as side artifact for '" + project.getArtifact()
          + "' with classifier '" + classifier + "'.");
  }

  private final void archive(final String archiverType, final File rootDir, final File archive,
        final String[] includes, final String[] excludes) throws NoSuchArchiverException,
        IOException, XCodeException
  {
    try {
      final Archiver archiver = archiverManager.getArchiver(archiverType);
      archiver.addDirectory(rootDir, includes, excludes);
      archiver.setDestFile(archive);
      archiver.createArchive();
    }
    catch (ArchiverException ex) {
      throw new XCodeException("Could not archive folder '" + rootDir + "' into '" + archive + "': " + ex.getMessage(),
            ex);
    }
  }

  static File getPublicHeaderFolderPath(String builtProductDirInsideXcodeProject, String publicHeaderFolderPathInsideXcodeProject,
        String relativeAlternatePublicHeaderFolderPath)
        throws XCodeException
  {

    final String relativePublicHeaderFolderPathInXcodeProject = publicHeaderFolderPathInsideXcodeProject;
    final String relativePublicHeaderFolderPath;

    if (StringUtils.isEmpty(relativeAlternatePublicHeaderFolderPath)) {

      relativePublicHeaderFolderPath = relativePublicHeaderFolderPathInXcodeProject;

      LOGGER.info(
        "Using public header folder path as it is configured in the xcode project: '"
              + relativePublicHeaderFolderPathInXcodeProject + "'.");

    }
    else {

      relativePublicHeaderFolderPath = relativeAlternatePublicHeaderFolderPath;

      if (!com.sap.prd.mobile.ios.mios.FileUtils.isChild(
            new File(
                  com.sap.prd.mobile.ios.mios.FileUtils.ensureLeadingSlash(relativeAlternatePublicHeaderFolderPath)),
            new File(
                  com.sap.prd.mobile.ios.mios.FileUtils
                    .ensureLeadingSlash(relativePublicHeaderFolderPathInXcodeProject))))
        throw new InvalidAlternatePublicHeaderPathException(
              "Public header folder path configured on the level of xcode-maven-plugin configuration ("
                    + relativeAlternatePublicHeaderFolderPath
                    + ") is not a parent folder of the public header path configured inside the xcode project ("
                    + relativePublicHeaderFolderPathInXcodeProject + ").");

      LOGGER.info(
        "Using public header folder path as it is defined inside the xcode-maven-plugin ("
              + relativeAlternatePublicHeaderFolderPath
              + "). In the xcode project '" + relativePublicHeaderFolderPathInXcodeProject
              + "' is configured as public header folder path.");
    }

    return new File(builtProductDirInsideXcodeProject,
          com.sap.prd.mobile.ios.mios.FileUtils.ensureLeadingSlash(relativePublicHeaderFolderPath));
  }
}
