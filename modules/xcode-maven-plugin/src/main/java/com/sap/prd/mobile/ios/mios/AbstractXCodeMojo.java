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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.LogManager;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Base class for Xcode specific mojos
 * 
 */
public abstract class AbstractXCodeMojo extends AbstractMojo
{
  //
  // This variable here ensures that a reference to the logger is kept.
  // According to the API doc of the java.util.logging framework it is required
  // to keep a reference. Otherwise it might happen that the logger is garbage
  // collected. This in turn must not happen since the logger keeps a
  // reference to the maven logger that could not be restored. This variable
  // should not be accessed. Instead use the LogManager to get the instance.
  //
  private static XCodePluginLogger logger = null;
  
  static {

    if(null == LogManager.getLogManager().getLogger(XCodePluginLogger.getLoggerName())) {

      logger = new XCodePluginLogger();
      LogManager.getLogManager().addLogger(logger);
      logger.finest(String.format("XCode plugin logger has been created: %s", logger.getName()));
    }
  }
  
  /**
   * The original Xcode sources located in the <code>src/xcode</code> directory stay untouched
   * during the whole Maven build. However, as we might have to modify the info.plist or the project
   * itself we copy the whole Xcode source directory during the build into another "checkout"
   * directory that by default named <code>checkout</code> and located below the Maven build (
   * <code>target</code>) directory.
   * 
   * @parameter expression="${xcode.checkoutDirectory}";
   */
  private File checkoutDirectory;

  /**
   * The xcode directory of the copied sources below the checkout directory.
   * 
   * @parameter expression="${xcode.compileDirectory}"
   */
  private File xcodeCompileDirectory;

  /**
   * @parameter expression="${project}"
   * @readonly
   * @required
   */
  protected MavenProject project;

  /**
   * The Xcode configurations that shall be built (e.g. Debug and Release).
   * 
   * If no configuration is provided in the plugin's <code>configuration</code> section of the
   * <code>pom.xml</code> it defaults to the values provided in the
   * <code>defaultAppConfigurations</code> or <code>defaultLibConfigurations</code> parameters
   * 
   * @parameter
   */
  private Set<String> configurations;

  /**
   * @parameter expression="${project.packaging}"
   * @readonly
   * @required
   */
  protected String packaging;

  /**
   * Explicit lists of sdks (iphoneos,iphonesimulator) the Xcode project shall be built for.
   * 
   * If no configuration is provided in the plugin's <code>configuration</code> section of the
   * <code>pom.xml</code> it defaults to the values provided in the <code>defaultAppSdks</code> or
   * <code>defaultLibSdks</code> parameters
   * 
   * @parameter
   */
  private Set<String> sdks;

  /**
   * Comma separated list of the default Xcode build configurations that should be built for apps
   * (in contrast to libraries). These values only apply if no "configurations" are explicitly
   * provided in the POM.
   * 
   * @parameter expression="${xcode.app.defaultConfigurations}" default-value="Release,Debug"
   * @since 1.2.0
   * 
   */
  private String defaultAppConfigurations;

  /**
   * Comma separated list of the default Xcode build configurations that should be built for
   * libraries (in contrast to apps). These values only apply if no "configurations" are explicitly
   * provided in the POM.
   * 
   * @parameter expression="${xcode.lib.defaultConfigurations}" default-value="Release,Debug"
   * @since 1.2.0
   * 
   */
  private String defaultLibConfigurations;

  /**
   * Comma separated list of the default Xcode SDKs that should be used for apps (in contrast to
   * libs). These values only apply if no "sdks" are explicitly provided in the POM.
   * 
   * @parameter expression="${xcode.app.defaultSdks}" default-value="iphoneos,iphonesimulator"
   * @since 1.2.0
   * 
   */
  private String defaultAppSdks;

  /**
   * Comma separated list of the default Xcode SDKs that should be used for libraries (in contrast
   * to apps). These values only apply if no "sdks" are explicitly provided in the POM.
   * 
   * @parameter expression="${xcode.lib.defaultSdks}" default-value="iphoneos,iphonesimulator"
   * @since 1.2.0
   * 
   */
  private String defaultLibSdks;

  protected Set<String> getSDKs()
  {
    if (sdks == null || sdks.isEmpty()) {

      try {

        PackagingType packagingType = PackagingType.getByMavenType(packaging);

        if (packagingType == PackagingType.APP) {

          getLog().info(
                "No SDKs in POM set. Using default configurations for applications: " + defaultAppSdks);
          return commaSeparatedStringToSet(defaultAppSdks);
        }
        else if (packagingType == PackagingType.LIB || packagingType == PackagingType.FRAMEWORK) {
          getLog().info(
                "No SDKs in POM set. Using default configurations for libraries: " + defaultLibSdks);
          return commaSeparatedStringToSet(defaultLibSdks);
        }
      }
      catch (PackagingType.UnknownPackagingTypeException e) {
        getLog().info("Unknown PackagingType found.", e);
        return Collections.emptySet();
      }
    }
    getLog().info("SDKs have been explicitly set in POM: " + sdks);
    return sdks;
  }

  protected Set<String> getConfigurations()
  {
    if (configurations == null || configurations.isEmpty()) {

      try {
        PackagingType packagingType = PackagingType.getByMavenType(packaging);

        if (packagingType == PackagingType.APP) {
          getLog().info(
                "No configurations in POM set. Using default configurations for applications: "
                      + defaultAppConfigurations);
          return commaSeparatedStringToSet(defaultAppConfigurations);
        }
        else if (packagingType == PackagingType.LIB || packagingType == PackagingType.FRAMEWORK) {
          getLog()
            .info(
                  "No configurations in POM set. Using default configurations for libraries: "
                        + defaultLibConfigurations);
          return commaSeparatedStringToSet(defaultLibConfigurations);
        }
      }
      catch (PackagingType.UnknownPackagingTypeException e) {
        getLog().info("Unknown PackagingType found.", e);
        return Collections.emptySet();
      }
    }
    getLog().info("Configurations have been explicitly set in POM: " + configurations);
    return configurations;
  }

  private Set<String> commaSeparatedStringToSet(String commaSeparetedValues)
  {
    Set<String> values = new HashSet<String>();
    String[] valueArray = commaSeparetedValues.split(",");
    for (String value : valueArray) {
      value = value.trim();
      if (!value.isEmpty()) {
        values.add(value);
      }
    }
    return Collections.unmodifiableSet(values);
  }

  protected File getCheckoutDirectory()
  {
    return checkoutDirectory;
  }

  /**
   * The xcode directory of the copied sources below the checkout directory.
   */
  protected File getXCodeCompileDirectory()
  {
    return xcodeCompileDirectory;
  }

  /**
   * 
   * @return the <code>src/xcode</code> directory
   */
  protected File getXCodeSourceDirectory()
  {
    return new File(project.getBuild().getSourceDirectory());
  }

  protected String getFixedProductName(final String productName)
  {
    return productName.trim().replaceAll(" ", "");
  }

  protected File getXCodeProjectFile()
  {
    return new File(getXCodeCompileDirectory(), project.getArtifactId() + ".xcodeproj/project.pbxproj");
  }

  /**
   * Calls a shell script in order to zip a folder. We have to call a shell script as Java cannot
   * zip symbolic links.
   * 
   * @param rootDir
   *          the directory where the zip command shall be executed
   * @param zipSubFolder
   *          the subfolder to be zipped
   * @param zipFileName
   *          the name of the zipFile (will be located in the rootDir)
   * @param archiveFolder
   *          an optional folder name if the zipSubFolder folder shall be placed inside the zip into
   *          a parent folder
   * @return the zip file
   * @throws MojoExecutionException
   */
  protected File zipSubfolder(File rootDir, String zipSubFolder, String zipFileName, String archiveFolder)
        throws MojoExecutionException
  {
    int resultCode = 0;

    try {

      File scriptDirectory = new File(project.getBuild().getDirectory(), "scripts").getCanonicalFile();
      scriptDirectory.deleteOnExit();

      if (archiveFolder != null)
      {
        resultCode = ScriptRunner.copyAndExecuteScript(System.out, "/com/sap/prd/mobile/ios/mios/zip-subfolder.sh",
              scriptDirectory, rootDir.getCanonicalPath(), zipSubFolder, zipFileName, archiveFolder);
      }
      else
      {
        resultCode = ScriptRunner.copyAndExecuteScript(System.out, "/com/sap/prd/mobile/ios/mios/zip-subfolder.sh",
              scriptDirectory, rootDir.getCanonicalPath(), zipSubFolder, zipFileName);
      }
    }
    catch (Exception ex) {
      throw new MojoExecutionException("Cannot create zip file " + zipFileName + ". Check log for details.", ex);
    }
    if (resultCode != 0) {
      throw new MojoExecutionException("Cannot create zip file " + zipFileName + ". Check log for details.");
    }
    getLog().info("Zip file '" + zipFileName + "' created.");
    return new File(rootDir, zipFileName);
  }
}
