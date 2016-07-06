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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Contains all parameters and methods that are needed for mojos that invoke the 'xcodebuild'
 * command.
 */
public abstract class BuildContextAwareMojo extends AbstractXCodeMojo
{

  private static final String PREFIX_XCODE_OPTIONS = "xcode.options.";
  private static final String PREFIX_XCODE_SETTINGS = "xcode.settings.";

  protected final static List<String> DEFAULT_BUILD_ACTIONS = Collections.unmodifiableList(Arrays.asList("clean",
        "build"));

  /**
   * The Xcode build action to to execute (e.g. clean, build, install). By default
   * <code>clean</code> and <code>build</code> are executed.
   */
  @Parameter
  protected List<String> buildActions;

  /**
   * The code sign identity is used to select the provisioning profile (e.g.
   * <code>iPhone Distribution</code>, <code>iPhone Developer</code>).
   * 
   * @since 1.2.0
   */
  @Parameter(property="xcode.codeSignIdentity")
  protected String codeSignIdentity;

  /**
   * The code signing required is used to disable code signing when no
   * developer provisioning certificate is available (e.g.
   * <code>NO</code>, <code>YES</code>).
   * 
   * @since 1.14.1
   */
  @Parameter(property="xcode.codeSigningRequired", defaultValue = "true")
  protected boolean codeSigningRequired;
  
  /**
   * Can be used to override the provisioning profile defined in the Xcode project target. You can
   * set it to an empty String if you want to use the default provisioning profile.
   * 
   * @since 1.2.1
   */
  @Parameter(property="xcode.provisioningProfile")
  protected String provisioningProfile;

  /**
   * The Xcode target to be built. If not specified, the default target (the first target) will be
   * built.
   * 
   * @since 1.4.1
   */
  @Parameter(property="xcode.target")
  protected String target;

  @Parameter(property="product.name")
  private String productName;

  /**
   * Indicates whenever plugin should build workspace instead of project.
   * Allows building projects which are using CocoaPods to resolve theirs dependencies.
   * Please also see <code>installPods</code> property setting to read more about CocaPods integration.
   */
  @Parameter(property="xcode.buildWorkspace")
  protected boolean buildWorkspace;
  /**
   * Settings to pass to XCode - if any are explicitly defined here, this plugin will not provide
   * default settings to XCode.
   * 
   * @since 1.6.2
   */
  @Parameter
  private Map<String, String> settings;

  /**
   * Options to pass to XCode - if any are explicitly defined here, this plugin will not provide
   * default options to XCode.
   * 
   * @since 1.6.2
   */
  @Parameter
  private Map<String, String> options;

  @Parameter(property="session", required=true, readonly=true)
  private MavenSession session;

  protected XCodeContext getXCodeContext(final XCodeContext.SourceCodeLocation sourceCodeLocation,
        String configuration, String sdk)
  {
    final String projectName = project.getArtifactId();
    File projectDirectory = null;

    if (sourceCodeLocation == XCodeContext.SourceCodeLocation.WORKING_COPY) {
      projectDirectory = getXCodeCompileDirectory();
    }
    else if (sourceCodeLocation == XCodeContext.SourceCodeLocation.ORIGINAL) {
      projectDirectory = getXCodeSourceDirectory();
    }
    else {
      throw new IllegalStateException("Invalid source code location: '" + sourceCodeLocation + "'");
    }

    HashMap<String, String> managedSettings = new HashMap<String, String>();
    if (codeSignIdentity != null)
      managedSettings.put(Settings.ManagedSetting.CODE_SIGN_IDENTITY.name(), codeSignIdentity);

    if (!codeSigningRequired)
      managedSettings.put(Settings.ManagedSetting.CODE_SIGNING_REQUIRED.name(), "NO");

    if (provisioningProfile != null)
      managedSettings.put(Settings.ManagedSetting.PROVISIONING_PROFILE.name(), provisioningProfile);

    HashMap<String, String> managedOptions = new HashMap<String, String>();

    if (configuration != null && !configuration.trim().isEmpty())
      managedOptions.put(Options.ManagedOption.CONFIGURATION.getOptionName(), configuration);
    if (sdk != null && !sdk.trim().isEmpty())
      managedOptions.put(Options.ManagedOption.SDK.getOptionName(), sdk);
    if (target != null && !target.trim().isEmpty())
      managedOptions.put(Options.ManagedOption.TARGET.getOptionName(), target);

    Map<String, String> _settings = new HashMap<String, String>(settings == null ? new HashMap<String, String>()
          : settings);

    for (String key : getKeys(PREFIX_XCODE_SETTINGS)) {
      _settings.put(key.substring(PREFIX_XCODE_SETTINGS.length()), getProperty(key));
    }

    Map<String, String> _options = new HashMap<String, String>(options == null ? new HashMap<String, String>()
          : options);

    for (String key : getKeys(PREFIX_XCODE_OPTIONS)) {
      _options.put(key.substring(PREFIX_XCODE_OPTIONS.length()), getProperty(key));
    }

    if (_options.get("scheme") == null) {
      managedOptions.put(Options.ManagedOption.PROJECT.getOptionName(), projectName + ".xcodeproj");
    }

    if(buildWorkspace) {
      managedOptions.put("workspace", projectName + ".xcworkspace");
    }

    return new XCodeContext(getBuildActions(), projectDirectory, System.out, new Settings(_settings, managedSettings),
          new Options(_options, managedOptions));
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
  protected PListAccessor getInfoPListAccessor(XCodeContext.SourceCodeLocation location, String configuration,
        String sdk)
        throws MojoExecutionException, XCodeException
  {
    File plistFile = getPListFile(location, configuration, sdk);
    if (!plistFile.isFile()) {
      throw new MojoExecutionException("The Xcode project refers to the Info.plist file '" + plistFile
            + "' that does not exist.");
    }
    return new PListAccessor(plistFile);
  }

  protected File getPListFile(XCodeContext.SourceCodeLocation location, String configuration, String sdk)
        throws XCodeException
  {

    XCodeContext context = getXCodeContext(location, configuration, sdk);

    String plistFileName = EffectiveBuildSettings.getBuildSetting(context, EffectiveBuildSettings.INFOPLIST_FILE);
    File srcRoot = new File(EffectiveBuildSettings.getBuildSetting(context, EffectiveBuildSettings.SRC_ROOT));

    final File plistFile = new File(plistFileName);

    if (!plistFile.isAbsolute()) {
      return new File(srcRoot, plistFileName);
    }

    if (FileUtils.isChild(srcRoot, plistFile))
      return plistFile;

    throw new IllegalStateException("Plist file " + plistFile + " is not located inside the xcode project " + srcRoot
          + ".");

  }

  protected String getProductName(final String configuration, final String sdk) throws MojoExecutionException
  {

    final String productName;

    if (this.productName != null) {
      productName = this.productName;
      getLog().info("Production name obtained from pom file");
    }
    else {

      try {
        productName = EffectiveBuildSettings.getBuildSetting(
              getXCodeContext(XCodeContext.SourceCodeLocation.WORKING_COPY, configuration, sdk), EffectiveBuildSettings.PRODUCT_NAME);
        getLog().info("Product name obtained from effective build settings file");

      }
      catch (final XCodeException ex) {
        throw new MojoExecutionException("Cannot get product name: " + ex.getMessage(), ex);
      }
    }

    if (productName == null || productName.trim().length() == 0)
      throw new MojoExecutionException("Invalid product name. Was null or empty.");

    return productName;
  }

  /**
   * Returns all keys of project properties and user properties matching the <code>prefix</code>.
   * 
   * @param prefix
   *          all keys if null
   * @return
   */
  @SuppressWarnings("unchecked")
  protected Set<String> getKeys(String prefix)
  {

    Set<String> result = new HashSet<String>();

    @SuppressWarnings("rawtypes")
    final Set keys = new HashSet();
    keys.addAll(session.getUserProperties().keySet());
    keys.addAll(project.getProperties().keySet());

    if (prefix == null) return keys;

    for (Object key : keys) {
      if (((String) key).startsWith(prefix))
        result.add((String) key);
    }

    return result;
  }

  protected String getProperty(String key)
  {
    String value = session.getUserProperties().getProperty(key);

    if (value == null)
    {
      value = project.getProperties().getProperty(key);
    }

    return value;
  }

}
