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
import java.util.Properties;
import java.util.logging.LogManager;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Sets default values needed during Xcode build.
 * 
 * @goal set-default-configuration
 */
public class XCodeDefaultConfigurationMojo extends AbstractMojo
{

  private static final String DEFAULT_MAVEN_SOURCE_DIRECTORY = "src/main/java";
  static final String DEFAULT_XCODE_SOURCE_DIRECTORY = "src/xcode";
  private static final String DEFAULT_FOLDER_NAME_CHECKOUT_DIRECTORY = "checkout";

  private static final String XCODE_CHECKOUT_DIR = "xcode.checkoutDirectory";
  private static final String XCODE_COMPILE_DIR = "xcode.compileDirectory";
  static final String XCODE_SOURCE_DIRECTORY = "xcode.sourceDirectory";

  /**
   * @parameter expression="${project}"
   * @required
   */
  protected MavenProject project;

  private static XCodePluginLogger logger = new XCodePluginLogger();
  static {
     if(null == LogManager.getLogManager().getLogger(XCodePluginLogger.getLoggerName())) {
       LogManager.getLogManager().addLogger(logger);
     }
   }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    final Properties projectProperties = project.getProperties();

    if (!projectProperties.containsKey(XCODE_SOURCE_DIRECTORY)) {

      projectProperties.setProperty(XCODE_SOURCE_DIRECTORY, DEFAULT_XCODE_SOURCE_DIRECTORY);

      getLog().info(
            "Property ${" + XCODE_SOURCE_DIRECTORY + "} was not set. ${" + XCODE_SOURCE_DIRECTORY + "} set to '"
                  + projectProperties.getProperty(XCODE_SOURCE_DIRECTORY) + "'.");
    }
    else {
      getLog().info(
            "Property ${" + XCODE_SOURCE_DIRECTORY + "} found with value '"
                  + projectProperties.getProperty(XCODE_SOURCE_DIRECTORY) + "' This value will no be modified");

      project.getBuild().setSourceDirectory(
            FileUtils.getCanonicalPath(new File(project.getBasedir(), projectProperties.getProperty(XCODE_SOURCE_DIRECTORY))));
    }

    if (project.getBuild().getSourceDirectory().contains("${" + XCODE_SOURCE_DIRECTORY + "}")) {
      project.getBuild().setSourceDirectory(
            project.getBuild().getSourceDirectory()
              .replace("${" + XCODE_SOURCE_DIRECTORY + "}", projectProperties.getProperty(XCODE_SOURCE_DIRECTORY)));
      getLog().info(
            "Project Build directory detected with unresolved property ${" + XCODE_SOURCE_DIRECTORY
                  + "}. This property is resolved to '"
                  + projectProperties.getProperty(XCODE_SOURCE_DIRECTORY) + "'.");
    }

    String sourceDirectory = getCurrentSourceDirectory(FileUtils.getCanonicalFile(project.getBasedir()),
          FileUtils.getCanonicalFile(new File(project.getBuild().getSourceDirectory())));

    if (sourceDirectory.equals(DEFAULT_MAVEN_SOURCE_DIRECTORY)) {

      project.getBuild().setSourceDirectory(
            FileUtils.getCanonicalPath(new File(project.getBasedir(), DEFAULT_XCODE_SOURCE_DIRECTORY)));

      getLog().info(
            "Build source directory was found to bet '" + DEFAULT_MAVEN_SOURCE_DIRECTORY
                  + "' which is the maven default. Set to xcode default '" + DEFAULT_XCODE_SOURCE_DIRECTORY + "'.");

      sourceDirectory = DEFAULT_XCODE_SOURCE_DIRECTORY;
    }
    else {
      getLog().info(
            "Build source directory was found to be '" + sourceDirectory
                  + "' which is not the default value in maven. This value is not modified.");
    }

    File checkoutDirectory = FileUtils.getCanonicalFile(new File(new File(project.getBuild().getDirectory()),
          DEFAULT_FOLDER_NAME_CHECKOUT_DIRECTORY));

    if (!projectProperties.containsKey(XCODE_CHECKOUT_DIR)) {

      projectProperties.setProperty(XCODE_CHECKOUT_DIR, FileUtils.getCanonicalPath(checkoutDirectory));

      getLog().info(
            "Property ${" + XCODE_CHECKOUT_DIR + "} was not set. ${" + XCODE_CHECKOUT_DIR + "} set to '"
                  + FileUtils.getCanonicalPath(checkoutDirectory) + "'.");
    }
    else {

      getLog().info(
            "Property ${" + XCODE_CHECKOUT_DIR + "} found with value '"
                  + projectProperties.getProperty(XCODE_CHECKOUT_DIR) + "'. This value will not be modified.");

      checkoutDirectory = FileUtils.getCanonicalFile(new File(new File(project.getBuild().getDirectory()),
            projectProperties.getProperty(XCODE_CHECKOUT_DIR)));

      projectProperties.setProperty(XCODE_CHECKOUT_DIR, FileUtils.getCanonicalPath(checkoutDirectory));
    }

    final File compileDirectory = FileUtils.getCanonicalFile(new File(checkoutDirectory, sourceDirectory));

    if (!projectProperties.containsKey(XCODE_COMPILE_DIR)) {

      projectProperties.setProperty(XCODE_COMPILE_DIR, FileUtils.getCanonicalPath(compileDirectory));

      getLog().info(
            "Property ${" + XCODE_COMPILE_DIR + "} was not set. ${" + XCODE_COMPILE_DIR + "} set to "
                  + FileUtils.getCanonicalPath(compileDirectory));

    }
    else if (!compileDirectory.equals(FileUtils.getCanonicalFile(new File(projectProperties.getProperty(XCODE_COMPILE_DIR))))) {

      getLog()
        .warn("Property ${"
              + XCODE_COMPILE_DIR
              + "} was found to be '"
              + projectProperties.getProperty(XCODE_COMPILE_DIR)
              + "' but should be '"
              + compileDirectory
              + "'. That property will be updated accordingly. Fix this issue in your pom file e.g by removing property ${"
              + XCODE_COMPILE_DIR + "}.");

      projectProperties.setProperty(XCODE_COMPILE_DIR, FileUtils.getCanonicalPath(compileDirectory));
    }
    else {
      getLog().info(
            "Property ${" + XCODE_COMPILE_DIR + "} was found with value '" + compileDirectory
                  + "' which is the expended value. This value is not modified.");
    }

    getLog().info("Summary:");
    getLog().info("${project.build.sourceDirectory}: " + project.getBuild().getSourceDirectory());
    getLog().info("${" + XCODE_CHECKOUT_DIR + "} : " + project.getProperties().getProperty(XCODE_CHECKOUT_DIR));
    getLog().info("${" + XCODE_COMPILE_DIR + "} : " + project.getProperties().getProperty(XCODE_COMPILE_DIR));
    getLog().info("${" + XCODE_SOURCE_DIRECTORY + "}: " + project.getProperties().getProperty(XCODE_SOURCE_DIRECTORY));
  }

  /**
   * 
   * @return The part of the path that is under $project.basedir that represents the
   *         sourceDirectory. Assumption is: sourceDirectory is located under the project base
   *         directory.
   */
  private String getCurrentSourceDirectory(final File baseDir, final File sourceDir)
  {
    return FileUtils.getDelta(baseDir, sourceDir);
  }
}
