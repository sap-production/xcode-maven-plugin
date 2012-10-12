package com.sap.prd.mobile.ios.mios;

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
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;

public class CopySourcesTask
{

  private Log log;
  private File baseDirectory, buildDirectory, checkoutDirectory;
  
  public CopySourcesTask setLog(Log log)
  {
    this.log = log;
    return this;
  }

  public CopySourcesTask setBaseDirectory(File baseDirectory)
  {
    this.baseDirectory = baseDirectory;
    return this;
  }

  public CopySourcesTask setBuildDirectory(File buildDirectory)
  {
    this.buildDirectory = buildDirectory;
    return this;
  }

  
  public CopySourcesTask setCheckoutDirectory(File checkoutDirectory)
  {
    this.checkoutDirectory = checkoutDirectory;
    return this;
  }

  public void execute() throws XCodeException {
    
    final String relativeBuildDirPath = getRelativePathFromBaseDirToBuildDir();
    
    log.info("Base directory: " + baseDirectory);
    log.info("Checkout directory: " + checkoutDirectory);
    log.info("RelativeBuildDirPath: " + relativeBuildDirPath);
    
    
    final File originalLibDir = new File(buildDirectory, MavenBuildFolderLayout.LIBS_DIR_NAME);
    final File copyOfLibDir = new File(checkoutDirectory, relativeBuildDirPath + "/" + MavenBuildFolderLayout.LIBS_DIR_NAME);
    
    final File originalHeadersDir = new File(buildDirectory, MavenBuildFolderLayout.HEADERS_DIR_NAME);
    final File copyOfHeadersDir = new File(checkoutDirectory, relativeBuildDirPath + "/" + MavenBuildFolderLayout.HEADERS_DIR_NAME);

    try {

      if (checkoutDirectory.exists())
        FileUtils.deleteDirectory(checkoutDirectory);

      copy(baseDirectory, checkoutDirectory, new FileFilter() {

        @Override
        public boolean accept(File pathname)
        {
          return ! (checkoutDirectory.getAbsoluteFile().equals(pathname.getAbsoluteFile()) || 
                    originalLibDir.getAbsoluteFile().equals(pathname.getAbsoluteFile()) ||
                    originalHeadersDir.getAbsoluteFile().equals(pathname.getAbsoluteFile()));
        }
        
      });

      if(originalLibDir.exists())
        com.sap.prd.mobile.ios.mios.FileUtils.createSymbolicLink(originalLibDir, copyOfLibDir);
      
      if(originalHeadersDir.exists())
        com.sap.prd.mobile.ios.mios.FileUtils.createSymbolicLink(originalHeadersDir, copyOfHeadersDir);
    }
    catch (IOException e) {
      throw new XCodeException(e.getMessage(), e);
    }    
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
          log.info("File '" + sourceFile + "' ommited.");
        }
      }
      else {
        FileUtils.copyFile(sourceFile, destFile);
        log.debug("File '" + sourceFile + "' copied to '" + destFile + "'.");
      }
    }
  }
  
  /**
  // Return the part of the path between project base directory and project build directory.
  // Assumption is: project build directory is located below project base directory. 
  **/
  private String getRelativePathFromBaseDirToBuildDir() {
    return com.sap.prd.mobile.ios.mios.FileUtils.getDelta(baseDirectory, buildDirectory);
  }
}
