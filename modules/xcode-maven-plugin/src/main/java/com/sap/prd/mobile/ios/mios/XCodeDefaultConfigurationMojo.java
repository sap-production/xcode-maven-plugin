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

import org.apache.maven.execution.MavenSession;
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

  static final String XCODE_CHECKOUT_DIR = "xcode.checkoutDirectory";
  private static final String XCODE_COMPILE_DIR = "xcode.compileDirectory";
  static final String XCODE_SOURCE_DIRECTORY = "xcode.sourceDirectory";

  /**
   * @parameter expression="${project}"
   * @required
   */
  protected MavenProject project;
  
  /**
   * @parameter expression="${session}"
   * @required
   * @readonly 
   */
  protected MavenSession session;

  /**
   * The original Xcode sources located in the <code>src/xcode</code> directory stay untouched
   * during the whole Maven build. However, as we might have to modify the info.plist or the project
   * itself we copy the whole Xcode source directory during the build into another "checkout"
   * directory that by default named <code>checkout</code> and located below the Maven build (
   * <code>target</code>) directory. Expected is a directory name. This directory name is
   * interpreted relative to the maven build directory.
   * 
   * @parameter expression="${xcode.checkoutDirectory}";
   */
  private File checkoutDirectory;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {

    final Properties projectProperties = project.getProperties();

    if (!propertyAvailable(XCODE_SOURCE_DIRECTORY)) {

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
            getCanonicalPath(new File(project.getBasedir(), projectProperties.getProperty(XCODE_SOURCE_DIRECTORY))));
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

    String sourceDirectory = getCurrentSourceDirectory(getCanonicalFile(project.getBasedir()),
          getCanonicalFile(new File(project.getBuild().getSourceDirectory())));

    if (sourceDirectory.equals(DEFAULT_MAVEN_SOURCE_DIRECTORY)) {

      project.getBuild().setSourceDirectory(
            getCanonicalPath(new File(project.getBasedir(), DEFAULT_XCODE_SOURCE_DIRECTORY)));

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

    File checkoutDirectory = getCanonicalFile(new File(new File(project.getBuild().getDirectory()),
          DEFAULT_FOLDER_NAME_CHECKOUT_DIRECTORY));

    if (!propertyAvailable(XCODE_CHECKOUT_DIR)) {

      projectProperties.setProperty(XCODE_CHECKOUT_DIR, getCanonicalPath(checkoutDirectory));

      getLog().info(
            "Property ${" + XCODE_CHECKOUT_DIR + "} was not set. ${" + XCODE_CHECKOUT_DIR + "} set to '"
                  + getCanonicalPath(checkoutDirectory) + "'.");
    }
    else {

      getLog().info(
            "Property ${" + XCODE_CHECKOUT_DIR + "} found with value '"
                  + projectProperties.getProperty(XCODE_CHECKOUT_DIR) + "'. This value will not be modified.");

      checkoutDirectory = getCanonicalFile(new File(project.getBuild().getDirectory(), getProperty(XCODE_CHECKOUT_DIR)));
      projectProperties.put(XCODE_CHECKOUT_DIR, getCanonicalPath(checkoutDirectory));

    }

    final File compileDirectory = getCanonicalFile(new File(checkoutDirectory, sourceDirectory));

    if (!propertyAvailable(XCODE_COMPILE_DIR)) {

      projectProperties.setProperty(XCODE_COMPILE_DIR, getCanonicalPath(compileDirectory));

      getLog().info(
            "Property ${" + XCODE_COMPILE_DIR + "} was not set. ${" + XCODE_COMPILE_DIR + "} set to "
                  + getCanonicalPath(compileDirectory));

    }
    else if (!compileDirectory.equals(getCanonicalFile(new File(getProperty(XCODE_COMPILE_DIR))))) {

      getLog()
        .warn("Property ${"
              + XCODE_COMPILE_DIR
              + "} was found to be '"
              + getProperty(XCODE_COMPILE_DIR)
              + "' but should be '"
              + compileDirectory
              + "'. That property will be updated accordingly. Fix this issue in your pom file e.g by removing property ${"
              + XCODE_COMPILE_DIR + "}.");

      projectProperties.setProperty(XCODE_COMPILE_DIR, getCanonicalPath(compileDirectory));
    }
    else {
      getLog().info(
            "Property ${" + XCODE_COMPILE_DIR + "} was found with value '" + compileDirectory
                  + "' which is the expended value. This value is not modified.");
    }

    getLog().info("Summary:");
    getLog().info("${project.build.sourceDirectory}: " + project.getBuild().getSourceDirectory());
    getLog().info("${" + XCODE_CHECKOUT_DIR + "} : " + project.getProperties().getProperty(XCODE_CHECKOUT_DIR));
    getLog().debug("Checkout directory handed over from outside was: '" + this.checkoutDirectory + "'");
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

  
  private boolean propertyAvailable(String name) {
    return session.getUserProperties().keySet().contains(name) || project.getProperties().keySet().contains(name);
  }
  private String getProperty(String name) {
    String value = session.getUserProperties().getProperty(name);

    if(value == null)
      value = project.getProperties().getProperty(name);

    return value;
  }
  private static String getCanonicalPath(File f) throws MojoExecutionException
  {
    try {
      return f.getCanonicalPath();
    }
    catch (final IOException ex) {
      throw new MojoExecutionException(ex.getMessage(), ex);
    }
  }

  private static File getCanonicalFile(File f) throws MojoExecutionException
  {
    try {
      return f.getCanonicalFile();
    }
    catch (final IOException ex) {
      throw new MojoExecutionException(ex.getMessage(), ex);
    }
  }
}
