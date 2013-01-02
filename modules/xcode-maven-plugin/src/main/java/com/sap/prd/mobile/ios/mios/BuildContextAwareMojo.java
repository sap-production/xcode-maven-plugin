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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Contains all parameters and methods that are needed for mojos that invoke the 'xcodebuild'
 * command.
 * 
 */
public abstract class BuildContextAwareMojo extends AbstractXCodeMojo
{

  protected final static List<String> DEFAULT_BUILD_ACTIONS = Collections.unmodifiableList(Arrays.asList("clean",
        "build"));

  /**
   * The Xcode build action to to execute (e.g. clean, build, install). By default
   * <code>clean</code> and <code>build</code> are executed.
   * 
   * @parameter
   */
  protected List<String> buildActions;

  /**
   * The code sign identity is used to select the provisioning profile (e.g.
   * <code>iPhone Distribution</code>, <code>iPhone Developer</code>).
   * 
   * @parameter expression="${xcode.codeSignIdentity}"
   * @since 1.2.0
   */
  protected String codeSignIdentity;

  /**
   * Can be used to override the provisioning profile defined in the Xcode project target. You can
   * set it to an empty String if you want to use the default provisioning profile.
   * 
   * @parameter expression="${xcode.provisioningProfile}"
   * @since 1.2.1
   */
  protected String provisioningProfile;
  
  /**
   * The Xcode target to be built. If not specified, the default target (the first target) will be built.
   * @parameter expression="${xcode.target}"
   * @since 1.4.1
   */
  protected String target;

  /**
   * @parameter expression="${product.name}"
   */
  private String productName;
  
  /**
   * The name of the project file to be used. The default is the POM artifact-id.
   * @parameter projectName expression="${xcode.projectName}"
   * @since 1.7.1
   */
  protected String xcodeProjectName;

  protected XCodeContext getXCodeContext(final XCodeContext.SourceCodeLocation sourceCodeLocation)
  {
    File projectDirectory = null;
    String projectName = xcodeProjectName;
    
    if (null == projectName) {
    	projectName = project.getArtifactId();
    }
    getLog().info("Setting projectName = <" + projectName + ">");

    if(sourceCodeLocation == XCodeContext.SourceCodeLocation.WORKING_COPY) {
      projectDirectory = getXCodeCompileDirectory();
    } else if(sourceCodeLocation == XCodeContext.SourceCodeLocation.ORIGINAL) {
      projectDirectory = getXCodeSourceDirectory();
    } else {
      throw new IllegalStateException("Invalid source code location: '" + sourceCodeLocation + "'");
    }

    return new XCodeContext(projectName, getBuildActions(), projectDirectory, System.out, codeSignIdentity,
          provisioningProfile, target);
  }

  protected List<String> getBuildActions()
  {
    return (buildActions == null || buildActions.isEmpty()) ? DEFAULT_BUILD_ACTIONS : Collections
      .unmodifiableList(buildActions);
  }
  
  /**
   * Retrieves the Info Plist out of the effective Xcode project settings and returns the accessor
   * to it.
   */
  protected PListAccessor getInfoPListAccessor(XCodeContext.SourceCodeLocation location, String configuration, String sdk)
        throws MojoExecutionException, XCodeException
  {
    File plistFile = getPListFile(location, configuration, sdk);
    if (!plistFile.isFile()) {
      throw new MojoExecutionException("The Xcode project refers to the Info.plist file '" + plistFile
            + "' that does not exist.");
    }
    return new PListAccessor(plistFile);
  }
  
  protected File getPListFile(XCodeContext.SourceCodeLocation location, String configuration, String sdk) throws XCodeException {

    
    XCodeContext context = getXCodeContext(location);
    
    String plistFileName = EffectiveBuildSettings.getBuildSetting(context, getLog(), configuration, sdk, EffectiveBuildSettings.INFOPLIST_FILE);
    File srcRoot = new File(EffectiveBuildSettings.getBuildSetting(context, getLog(), configuration, sdk, EffectiveBuildSettings.SRC_ROOT));

    final File plistFile = new File(plistFileName);


    if(! plistFile.isAbsolute()) {
      return new File(srcRoot, plistFileName);
    }
    

    if(FileUtils.isChild(srcRoot, plistFile))
      return plistFile;
    
    throw new IllegalStateException("Plist file " + plistFile + " is not located inside the xcode project " + srcRoot +  ".");
    
  }
  
  protected String getProductName(final String configuration, final String sdk) throws MojoExecutionException {
    
    final String productName;

    if (this.productName != null) {
      productName = this.productName;
      getLog().info("Production name obtained from pom file");
    }
    else {
      
      try {
        productName = EffectiveBuildSettings.getBuildSetting(getXCodeContext(XCodeContext.SourceCodeLocation.WORKING_COPY), getLog(), configuration, sdk, EffectiveBuildSettings.PRODUCT_NAME);
        getLog().info("Product name obtained from effective build settings file");
        
      } catch(final XCodeException ex) {
        throw new MojoExecutionException("Cannot get product name: " + ex.getMessage(), ex);
      }
    }

    if(productName == null || productName.trim().length() == 0)
      throw new MojoExecutionException("Invalid product name. Was null or empty.");

    return productName;
  }
}
