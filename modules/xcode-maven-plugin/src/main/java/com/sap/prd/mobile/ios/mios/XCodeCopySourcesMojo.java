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

import static com.sap.prd.mobile.ios.mios.FileUtils.getCanonicalFile;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Copies sources to the checkout directory below the target directory.
 * 
 * @goal copy-sources
 */
public class XCodeCopySourcesMojo extends AbstractXCodeMojo
{

  /**
   * @parameter expression="${xcode.useSymbolicLinks}" default-value="false"
   */
  private boolean useSymbolicLinks;

  private boolean useSymbolicLinks()
  {
    return useSymbolicLinks;
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    final File baseDirectory = getCanonicalFile(project.getBasedir());
    final File checkoutDirectory = getCanonicalFile(getCheckoutDirectory());
    final String buildDirPath = getProjectBuildDirectory();

    getLog().info("Base directory: " + baseDirectory);
    getLog().info("Checkout directory: " + checkoutDirectory);
    getLog().info("BuildDirPath: " + buildDirPath);

    final File originalLibDir = getCanonicalFile(new File(project.getBuild()
      .getDirectory(), FolderLayout.LIBS_DIR_NAME));
    final File copyOfLibDir = getCanonicalFile(new File(checkoutDirectory,
          buildDirPath + "/" + FolderLayout.LIBS_DIR_NAME));

    final File originalHeadersDir = getCanonicalFile(new File(project.getBuild()
      .getDirectory(), FolderLayout.HEADERS_DIR_NAME));
    final File copyOfHeadersDir = getCanonicalFile(new File(checkoutDirectory,
          buildDirPath + "/" + FolderLayout.HEADERS_DIR_NAME));

    final File originalXcodeDepsDir = getCanonicalFile(new File(project
      .getBuild().getDirectory(), FolderLayout.XCODE_DEPS_TARGET_FOLDER));
    final File copyOfXcodeDepsDir = getCanonicalFile(new File(checkoutDirectory,
          buildDirPath + "/" + FolderLayout.XCODE_DEPS_TARGET_FOLDER));

    try {

      if (checkoutDirectory.exists())
        com.sap.prd.mobile.ios.mios.FileUtils.deleteDirectory(checkoutDirectory);

      copy(baseDirectory, checkoutDirectory, new FileFilter() {

        @Override
        public boolean accept(final File pathname)
        {
          final File canonicalPathName = getCanonicalFile(pathname);

          return !(checkoutDirectory.equals(canonicalPathName) ||
                originalLibDir.equals(canonicalPathName) ||
                originalHeadersDir.equals(canonicalPathName) ||
                originalXcodeDepsDir.equals(canonicalPathName));
        }

      });

      if (originalLibDir.exists()) {
        if (useSymbolicLinks()) {
          com.sap.prd.mobile.ios.mios.FileUtils.createSymbolicLink(originalLibDir, copyOfLibDir);
        }
        else {
          FileUtils.copyDirectory(originalLibDir, copyOfLibDir);
        }
      }

      if (originalHeadersDir.exists()) {
        if (useSymbolicLinks) {
          com.sap.prd.mobile.ios.mios.FileUtils.createSymbolicLink(originalHeadersDir, copyOfHeadersDir);
        }
        else {
          FileUtils.copyDirectory(originalHeadersDir, copyOfHeadersDir);
        }
      }

      if (originalXcodeDepsDir.exists()) {
        if (useSymbolicLinks) {
          com.sap.prd.mobile.ios.mios.FileUtils.createSymbolicLink(originalXcodeDepsDir, copyOfXcodeDepsDir);
        }
        else {
          FileUtils.copyDirectory(originalXcodeDepsDir, copyOfXcodeDepsDir);
        }
      }

    }
    catch (IOException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  /**
   * // Return the part of the path between project base directory and project build directory. //
   * Assumption is: project build directory is located below project base directory.
   **/
  private String getProjectBuildDirectory()
  {
    return com.sap.prd.mobile.ios.mios.FileUtils.getDelta(project.getBasedir(), new File(project.getBuild()
      .getDirectory()));
  }

  private void copy(final File source, final File targetDirectory, final FileFilter excludes) throws IOException
  {

    for (final File sourceFile : source.listFiles()) {
      final File destFile = new File(targetDirectory, sourceFile.getName());
      if (sourceFile.isDirectory()) {

        if (excludes.accept(sourceFile)) {
          copy(sourceFile, destFile, excludes);
        }
        else {
          getLog().info("File '" + sourceFile + "' ommited.");
        }
      }
      else {
        FileUtils.copyFile(sourceFile, destFile);
        if (sourceFile.canExecute()) {
          destFile.setExecutable(true);
        }
        getLog().debug(
              (destFile.canExecute() ? "Executable" : "File '") + sourceFile + "' copied to '" + destFile + "'.");
      }
    }
  }
}
