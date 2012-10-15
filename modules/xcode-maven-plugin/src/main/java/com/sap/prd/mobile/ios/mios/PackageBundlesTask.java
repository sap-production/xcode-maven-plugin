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

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;

class PackageBundlesTask
{

  private static final String ZIPPED_BUNDLE_SUFFIX = "xcode-bundle-zip";

  private Log log;
  private ArchiverManager archiverManager;
  private MavenProjectHelper projectHelper;
  private File compileDir;
  private Set<String> bundleNames;
  private MavenProject mavenProject;

  public PackageBundlesTask setLog(Log log)
  {
    this.log = log;
    return this;
  }

  public PackageBundlesTask setMavenProject(MavenProject mavenProject)
  {
    this.mavenProject = mavenProject;
    return this;
  }

  public PackageBundlesTask setArchiverManager(ArchiverManager archiverManager)
  {
    this.archiverManager = archiverManager;
    return this;
  }

  public PackageBundlesTask setProjectHelper(MavenProjectHelper projectHelper)
  {
    this.projectHelper = projectHelper;
    return this;
  }

  public PackageBundlesTask setCompileDir(File compileDir)
  {
    this.compileDir = compileDir;
    return this;
  }

  public PackageBundlesTask setBundleNames(Set<String> bundleNames)
  {
    this.bundleNames = bundleNames;
    return this;
  }

  /**
   * Packages all the artifacts. The main artifact is set and all side artifacts are attached for
   * deployment.
   * 
   * @param bundles
   * 
   * @param buildDir
   */
  public void execute() throws XCodeException
  {

    try {

      File mainArtifactDir = createMainArtifactDir();

      final Set<String> finalBundleNames = new HashSet<String>();

      for (String bundleName : bundleNames) {
        File bundleDirectory = XCodeBuildLayout.getBundleDirectory(compileDir, bundleName);

        if (!bundleDirectory.exists()) {
          log.info("Bundle directory '" + bundleDirectory + "' does not exist. Bundle will not be attached.");
          return;
        }
        final File bundleFile = new File(new File(mavenProject.getBuild().getDirectory()), bundleName + ".bundle");

        archive("zip", bundleDirectory, bundleFile, new String[] { "**/*" }, null);
        log.info("Bundle zip file created (" + bundleFile + ")");

        String escapedBundleName = escapeBundleName(bundleName);
        projectHelper.attachArtifact(mavenProject, ZIPPED_BUNDLE_SUFFIX, escapedBundleName, bundleFile);

        finalBundleNames.add(getBundleReference(mavenProject, escapedBundleName));
      }

      addBundleInfoToMainArtifact(finalBundleNames, new File(mainArtifactDir, "bundles.txt"));

    }
    catch (IOException ioe) {
      throw new XCodeException("Could not package the library", ioe);
    }

  }

  private String getBundleReference(MavenProject project, String escapedBundleName)
  {
    return GAVUtil.toColonNotation(project.getGroupId(), project.getArtifactId(), project.getVersion(),
          ZIPPED_BUNDLE_SUFFIX,
          escapedBundleName);
  }

  private File createMainArtifactDir() throws IOException
  {
    File mainArtifactDir = MavenBuildFolderLayout.getFolderForExtractedMainArtifact(mavenProject);

    if (mainArtifactDir.exists())
      FileUtils.deleteDirectory(mainArtifactDir);

    if (!mainArtifactDir.mkdirs())
      throw new IOException("Could not create directory '" + mainArtifactDir + "'.");

    FileUtils.writeStringToFile(new File(mainArtifactDir, "README.TXT"),
          "This zip file may contain additonal information about the depoyed artifacts. \n");
    return mainArtifactDir;
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

  private final void archive(final String archiverType, final File rootDir, final File archive,
        final String[] includes, final String[] excludes) throws XCodeException
  {
    try {
      final Archiver archiver = archiverManager.getArchiver(archiverType);
      archiver.addDirectory(rootDir, includes, excludes);
      archiver.setDestFile(archive);
      archiver.createArchive();
    }
    catch (Exception ex) {
      throw new XCodeException("Could not archive folder '" + rootDir + "' into '" + archive + "': " + ex.getMessage(),
            ex);
    }
  }

}
