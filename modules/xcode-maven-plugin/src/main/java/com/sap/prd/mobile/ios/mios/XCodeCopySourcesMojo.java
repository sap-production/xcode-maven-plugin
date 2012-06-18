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

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {

    final File checkoutDirectory = getCheckoutDirectory();

    final File baseDirectory = project.getBasedir();
    final File buildDirectory = checkoutDirectory;

    getLog().info("Base directory: " + baseDirectory);
    getLog().info("Checkout directory: " + checkoutDirectory);

    try {

      if (checkoutDirectory.exists())
        FileUtils.deleteDirectory(checkoutDirectory);

      copy(baseDirectory, checkoutDirectory, buildDirectory);

    }
    catch (IOException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  private void copy(final File source, final File targetDirectory, final File buildDirectory) throws IOException
  {

    for (final File sourceFile : source.listFiles()) {
      final File destFile = new File(targetDirectory, sourceFile.getName());
      if (sourceFile.isDirectory()) {
        if (!sourceFile.equals(buildDirectory)) {
          copy(sourceFile, destFile, buildDirectory);
        }
        else {
          getLog().info("BuildDirectory '" + buildDirectory + "' ommited.");
        }
      }
      else {
        FileUtils.copyFile(sourceFile, destFile);
        getLog().debug("File '" + sourceFile + "' copied to '" + destFile + "'.");
      }
    }
  }
}
