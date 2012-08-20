package com.sap.prd.mobile.ios.mios;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * 
 * @goal clean
 * 
 */
public class CleanMojo extends AbstractMojo
{

  /**
   * The folder used for hudson archiving
   * 
   * @parameter expression="${archive.dir}" default-value="${project.build.directory}"
   * @readonly
   */
  private File archiveDirectory;

  /**
   * @parameter default-value="${project.build.directory}"
   * @readonly
   */
  private File buildDirectory;

  /**
   * @parameter expression="${basedir}"
   * @readonly
   */
  private File baseDirectory;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {

    try {

      if (archiveDirectory.getCanonicalFile().equals(buildDirectory.getCanonicalFile()))
      {
        getLog().info(
              "Archive directory '" + archiveDirectory + "' equals build directory '" + buildDirectory
                    + ". This directory will be cleaned by the standard maven clean plugin. Nothing todo.");
        return;
      }

      if (archiveDirectory.getCanonicalFile().equals(baseDirectory.getCanonicalFile()))
      {
        getLog().error(
              "Archive directory '" + archiveDirectory + "' equals base directory '" + baseDirectory
                    + ". This directory will not be removed.");
        return;
      }

      getLog().info("Delete '" + archiveDirectory + "'.");
      FileUtils.deleteDirectory(archiveDirectory);
    }
    catch (IOException e) {
      throw new MojoFailureException(e.getMessage(), e);
    }
  }

}
