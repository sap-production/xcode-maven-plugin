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

import java.util.List;
import java.util.ArrayList;
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

      /**
       * Initially, FileUtils was used to copy files over.
       * But this was causing issues with some of the builds as the copy didn't
       * deal with permissions/execution bit flags/symlinks/etc.
       * Executing an rsync operations works 100% of the time and is a
       * better mechanism than the FileUtils.
       * Thanks to Tracy Keeling for finding the fix.
       *
       */
      copyWithRsync(baseDirectory, checkoutDirectory, 
          checkoutDirectory, originalLibDir, originalHeadersDir, originalXcodeDepsDir);

      if (originalLibDir.exists()) {
        if (useSymbolicLinks()) {
          if (copyOfLibDir.exists()) com.sap.prd.mobile.ios.mios.FileUtils.deleteDirectory(copyOfLibDir);
          com.sap.prd.mobile.ios.mios.FileUtils.createSymbolicLink(originalLibDir, copyOfLibDir);
        }
        else {
          FileUtils.copyDirectory(originalLibDir, copyOfLibDir);
        }
      }

      if (originalHeadersDir.exists()) {
        if (useSymbolicLinks) {
          if (copyOfHeadersDir.exists()) com.sap.prd.mobile.ios.mios.FileUtils.deleteDirectory(copyOfHeadersDir);
          com.sap.prd.mobile.ios.mios.FileUtils.createSymbolicLink(originalHeadersDir, copyOfHeadersDir);
        }
        else {
          FileUtils.copyDirectory(originalHeadersDir, copyOfHeadersDir);
        }
      }

      if (originalXcodeDepsDir.exists()) {
        if (useSymbolicLinks) {
          if (copyOfXcodeDepsDir.exists()) com.sap.prd.mobile.ios.mios.FileUtils.deleteDirectory(copyOfXcodeDepsDir);
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

  private void copyWithRsync(final File baseDirectory,
      final File checkoutDirectory, final File... excludes) throws IOException {
    List<String> rsyncArgs = new ArrayList<String>();

    rsyncArgs.add("rsync");
    rsyncArgs.add("--recursive");
    rsyncArgs.add("--perms");
    rsyncArgs.add("--executability");
    
    // The "checkout" directory by intention shall not contain symlinks but only copies.
    // We have to ensure the original sources are not modified - that's why we copy them to "checkout" 
    rsyncArgs.add("--copy-links");
    //rsyncArgs.add("--links");
    //rsyncArgs.add("--safe-links");

    // TODO: We simply need to exclude buildDirPath, but doing
    // all this to maintain functionality with previous versions

    for(File exclude : excludes) {
      rsyncArgs.add("--exclude");
      rsyncArgs.add(getRelativePath(baseDirectory, exclude));
    }

    rsyncArgs.add(baseDirectory.getAbsolutePath() + "/");
    rsyncArgs.add(checkoutDirectory.getAbsolutePath());

    int returnValue = Forker.forkProcess(System.out, null,
        rsyncArgs.toArray(new String[rsyncArgs.size()]));

    if (returnValue != 0) {
      throw new RuntimeException("Could not copy '" + baseDirectory + "' to '"
          + checkoutDirectory + "'. Return value:" + returnValue);
    }
  }

  private String getRelativePath(File baseDirectory, File childDir) throws IOException {
    String base = baseDirectory.getAbsolutePath();
    String child = childDir.getAbsolutePath();
    if(!child.startsWith(base)) throw new IOException("base dir no parent of child dir. base: '"+ base + "', child: '" + child + "'");
    String relative = child.substring(base.length());
    if(relative.startsWith("/")) relative = relative.substring(1);
	return relative;
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

}
