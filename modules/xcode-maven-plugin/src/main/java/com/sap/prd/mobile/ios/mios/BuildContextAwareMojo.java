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

import static com.sap.prd.mobile.ios.mios.XCodeVersionUtil.checkVersions;
import static com.sap.prd.mobile.ios.mios.XCodeVersionUtil.getVersion;
import static com.sap.prd.mobile.ios.mios.XCodeVersionUtil.getXCodeVersionString;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Contains all parameters and methods that are needed for mojos that invoke the 'xcodebuild'
 * command.
 * 
 */
public abstract class BuildContextAwareMojo extends AbstractXCodeMojo
{

  private static final String PREFIX_XCODE_OPTIONS = "xcode.options.";
  private static final String PREFIX_XCODE_SETTINGS = "xcode.settings.";
  public final static String MIN_XCODE_VERSION = "8.0";


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
   * The code signing required is used to disable code signing when no
   * developer provisioning certificate is available (e.g.
   * <code>NO</code>, <code>YES</code>).
   * 
   * @parameter expression="${xcode.codeSigningRequired}" default-value = "true"
   * @since 1.14.1
   */
  protected boolean codeSigningRequired;
  
  /**
   * Can be used to override the provisioning profile defined in the Xcode project target. You can
   * set it to an empty String if you want to use the default provisioning profile.
   * 
   * @parameter expression="${xcode.provisioningProfile}"
   * @since 1.2.1
   */
  protected String provisioningProfile;

  /**
   * The Xcode target to be built. If not specified, the default target (the first target) will be
   * built.
   * 
   * @parameter expression="${xcode.target}"
   * @since 1.4.1
   */
  protected String target;

  /**
   * @parameter expression="${product.name}"
   */
  private String productName;

  /**
   * Settings to pass to XCode - if any are explicitly defined here, this plugin will not provide
   * default settings to XCode.
   * 
   * @parameter
   * @since 1.6.2
   */
  private Map<String, String> settings;

  /**
   * Options to pass to XCode - if any are explicitly defined here, this plugin will not provide
   * default options to XCode.
   * 
   * @parameter
   * @since 1.6.2
   */
  private Map<String, String> options;

  /**
   * @parameter expression="${session}"
   * @required
   * @readonly
   */
  private MavenSession session;

  /**
   * @parameter expression="${xcode.watchapp}"
   * For watchos2.0 we should not send the sdk value to xcodebuild, To differentiate between regular app and watch2.0 (Now it's specific)
   * expecting this property from developer in pom.xml, on demand this will be considered.
   *
   * pom.xml entry:
   *
   * <pre>
   * {@code
   * <properties>
   *  <xcode.watchapp>watchos2.0</xcode.watchapp>
   * </properties>
   * }
   * </pre>
   *
   * @since 1.14.3
   */
  private String watchapp;

  /**
   * Allowed developers to override the SYMROOT settings
   *
   * @parameter expression="${xcode.symroot}" default-value = "build"
   * @since 1.14.4
   */
  private String symRootDir;
  /**
   * Allowed developers to override the xcconfig settings through pom.xml
   * Signing methodology has been changed with xcode8 onwards, to enable this we are expecting dev to give singing related parameters
   * This is only for the Apps with Entitlements like Push notification, Wallet etc...
   * @parameter expression="${xcode.xcconfig.enterprise}"
   *
   * pom.xml entry:
   *
   * <pre>
   * {@code
   * <properties>
   *  <xcode.xcconfig.enterprise>$ENTERPRISE_XCCONFG_FILE_PATH</xcode.xcconfig.enterprise>
   * </properties>
   * }
   * </pre>
   * where ENTERPRISE_XCCONFG_FILE_PATH = Relative path to enterprise xcconfig file, ideally we expect developer to keep in xcode project level
   *
   * @since 1.14.7
   */
  private String enterpriseXcconfig;

  /**
   * Allowed developers to override the xcconfig settings through pom.xml for the release builds
   * This has to be provided for the every release builds, because generic profiles can't used for building release projects
   * @parameter expression="${xcode.xcconfig.naas}"
   *
   * pom.xml entry:
   *
   * <pre>
   * {@code
   * <properties>
   *  <xcode.xcconfig.naas>$NAAS_XCCONFG_FILE_PATH</xcode.xcconfig.naas>
   * </properties>
   * }
   * </pre>
   * where NAAS_XCCONFG_FILE_PATH = Relative path to release xcconfig file, ideally we expect developer to keep in xcode project level
   *
   * @since 1.14.7
   */
  private String naasXcconfig;

  /**
   * Allowed developers to override the xcconfig settings through pom.xml for the local builds
   * This has to be provided for the every release builds, because generic profiles can't used for building release projects
   * @parameter expression="${xcode.xcconfig.local}"
   *
   * pom.xml entry:
   *
   * <pre>
   * {@code
   * <properties>
   *  <xcode.xcconfig.local>$LOCAL_XCCONFG_FILE_PATH</xcode.xcconfig.local>
   * </properties>
   * }
   * </pre>
   * where LOCAL_XCCONFG_FILE_PATH = Relative path to development xcconfig file, ideally we expect developer to keep in xcode project level
   *
   * @since 1.14.7
   */
  private String localXcconfig;

  /**
   * For simple application central team manages the xcconfig for enterprise builds
   * This will be managed in settings.xml
   *
   * @parameter expression="${xcode.xcconfig.generic}"
   * @since 1.14.7
   */
  private String genericXcconfig;

  /**
   * To differentiate type of build in central build infrastructure
   * This will take LOCAL, ENTERPRISE, COMPANY Values
   * @parameter expression = "${build-profile}" default-value = "local"
   */

  private String buildProfile;

  /**
   * This is to add additional build phase to xcodebuild
   * xcodebuild -showBuildSettings : Apple induced error for -showBuildSettings, fails all our builds
   *
   * * <pre>
   * {@code
   * <properties>
   *  <xcode.coredata.framework>true</xcode.coredata.framework>
   * </properties>
   * }
   * </pre>
   *
   * @parameter expression="${xcode.coredata.framework}" default-value = "false"
   * @since 1.14.7
   */
  public static boolean isCoreDataFramework;

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

    if (symRootDir != null)
        managedSettings.put(Settings.ManagedSetting.SYMROOT.name(), symRootDir);

    if (!codeSigningRequired)
      managedSettings.put(Settings.ManagedSetting.CODE_SIGNING_REQUIRED.name(), "NO");

    if (provisioningProfile != null)
      managedSettings.put(Settings.ManagedSetting.PROVISIONING_PROFILE.name(), provisioningProfile);

    HashMap<String, String> managedOptions = new HashMap<String, String>();

    if (configuration != null && !configuration.trim().isEmpty())
      managedOptions.put(Options.ManagedOption.CONFIGURATION.getOptionName(), configuration);
		try {

			String xCodeVersionString = getXCodeVersionString();
			DefaultArtifactVersion version = getVersion(xCodeVersionString);
			if (checkVersions(version, MIN_XCODE_VERSION)) {

				if (buildProfile.equals("dev") | buildProfile.equals("ent")) {
					getLog().info("Enterprise Signing ....... ");
					if (isXcconfigAvailable(enterpriseXcconfig)){
						getLog().info("Enterprise build, xcconfig provided by developer xcode.xcconfig.enterprise: "+enterpriseXcconfig);
						managedOptions.put(Options.ManagedOption.XCCONFIG.getOptionName(), enterpriseXcconfig);
					}
					else if(isXcconfigAvailable(genericXcconfig)){
						getLog().info("Enterprise build, xcconfig maintained by central team xcode.xcconfig.generic: "+genericXcconfig);
						managedOptions.put(Options.ManagedOption.XCCONFIG.getOptionName(), genericXcconfig);
					}
					else
						getLog().info("Enterprise build expects the xcconfig from Dev source code or from Generic content in build server .....");
				}else if (buildProfile.equals("comp")){
					getLog().info("Company Singing ....... ");
					if (isXcconfigAvailable(naasXcconfig)){
						getLog().info("Company build, xcconfig provided by developer xcode.xcconfig.naas: "+naasXcconfig);
						managedOptions.put(Options.ManagedOption.XCCONFIG.getOptionName(), naasXcconfig);
					}
					else
						getLog().info("Company build expects the xcconfig entry in pom.xml for xcode8 builds, Please contact release team for the same");
				}else{
					getLog().info("Development Singing ....... ");
					if (isXcconfigAvailable(localXcconfig)){
						getLog().info("Developer build, xcconfig provided by developer xcode.xcconfig.local: "+localXcconfig);
						managedOptions.put(Options.ManagedOption.XCCONFIG.getOptionName(), localXcconfig);
					}
					else
						getLog().info("None of the xcconfig file or buildProfile provided, it's considered as the local build and Build settings will be considered for signing");
				}
			}
		} catch (XCodeException e) {
			throw new IllegalStateException("Could not get xcodebuild version", e);
		}

    /**
     * No specific check has been done here, If property specified then sdk entry will be ignored
     * This can be extended with the specific check. I kept this as Generic because watchosX.X should support with this check,
     * without confusing developers it will serve the purpose
     */
    if (sdk != null && !sdk.trim().isEmpty() && watchapp ==null)
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

    if (null == _options.get("scheme"))
    managedOptions.put(Options.ManagedOption.PROJECT.getOptionName(), projectName + ".xcodeproj");


    return new XCodeContext(getBuildActions(), projectDirectory, System.out, new Settings(_settings, managedSettings),
          new Options(_options, managedOptions));
  }

  private boolean isXcconfigAvailable(String configFilePath) {
	  getLog().info("Using xccconfig and the location is: " + configFilePath);
		if (null != configFilePath){
			return(new File(configFilePath).exists());
		}else{
			getLog().info("configFile value is null, please provide valid entry");
			return(false);
		}
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

  protected File getProjectRootDirectory(XCodeContext.SourceCodeLocation location, String configuration, String sdk)
		throws XCodeException {
	XCodeContext context = getXCodeContext(location, configuration, sdk);

	File srcRoot = new File(EffectiveBuildSettings.getBuildSetting(context, EffectiveBuildSettings.SRC_ROOT));
	return srcRoot;
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
