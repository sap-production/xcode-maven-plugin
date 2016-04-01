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
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Appends a suffix to the appId. No actions are taken if the suffix is not specified or the suffix
 * has zero length.
 * 
 * @goal change-app-id
 * 
 */
public class XCodeChangeAppIDMojo extends BuildContextAwareMojo
{

	private final static Logger LOGGER = LogManager.getLogManager().getLogger(XCodePluginLogger.getLoggerName());
	/**
	* This suffix gets appended to the appId as '.&lt;appIdSuffix>' in the <code>Info.plist</code>
	* before the signing takes place.
	*
	* @parameter expression="${xcode.appIdSuffix}"
	* @since 1.2.0
	*/
	private static String appIdSuffix;

	/**
	* appType helps to inject the appIdSuffix in to appropriate location of appId in the <code>Info.plist</code>
	* before the signing takes place.
	* Without type appId will be suffixed with the given value, with type we are injecting value at appropriate location
	* @parameter expression="${xcode.appType}"
	*/
	private String appType;

	/**
	* watchkitAppPlist helps to know the location of watchkitApp plist file
	* appIdSuffix will be injected to this plist file
	*  @parameter expression="${xcode.watchkitAppPlist}"
	*/
	private static String watchkitAppPlist;

	/**
	* watchkitExtensionPlist helps to know the location of watchkitExtension plist file
	* appIdSuffix will be injected to this plist file
	*  @parameter expression="${xcode.watchkitExtentionPlist}"
	*/
	private static String watchkitExtentionPlist;

	/**
	* This is a variable in plist file of Watchkitapp
	* This variable reference to the Bundle Identifier of the iphone app
	*/
	private static String wkCompanionAppBundleIdentifier;


	/**
	* This is a variable in plist file of watchkitextention
	* This variable reference to the Bundle Identifier of the Watchkitapp
	*/
	private static String wkAppBundleIdentifier;

	/**
	* This is common variable in all the plist files, we need to make sure values are same in all plist files
	*/
	private static String cfBundleShortVersionString;

	/**
	* This is common variable in all the plist files, we need to make sure values are same in all plist files
	*/
	private static String cfBundleVersion;

	/**
	* This is a watchkit apps modified bundle identifier value, this needs to be extended for the watchkitextension bundle identifier
	* Requirement for the watchos2.0 applications, without with build fails
	*/

	private static String watchkitAppBundleIdentifier;
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


	@Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    if (appIdSuffix == null || "".equals(appIdSuffix.trim())) {
      return;
    }

    LOGGER.info("appIdSuffix=" + appIdSuffix);

	final Collection<File> alreadyUpdatedPlists = new HashSet<File>();

		for (final String configuration : getConfigurations()) {
			for (final String sdk : getSDKs()) {

				File infoPlistFile = null;
				File srcRoot = null;
				PListAccessor infoPlistAccessor = null;

				/*
				 * Add appIdSuffix to the info.plist of Application
				 */
				try {
					infoPlistFile = getPListFile(XCodeContext.SourceCodeLocation.WORKING_COPY, configuration, sdk);
				} catch (XCodeException e) {
					throw new MojoExecutionException(e.getMessage(), e);
				}

				infoPlistAccessor = new PListAccessor(infoPlistFile);
				if (alreadyUpdatedPlists.contains(infoPlistFile)) {
					LOGGER.finer("PList file '" + infoPlistFile.getName()
							+ "' was already updated for another configuration. This file will be skipped.");
				} else {
					changeAppId(infoPlistAccessor, appIdSuffix, null);
					try {
						setCFBundleShortVersionString(infoPlistAccessor.getStringValue(PListAccessor.KEY_BUNDLE_SHORT_VERSION_STRING));
						setCFBundleVersion(infoPlistAccessor.getStringValue(PListAccessor.KEY_BUNDLE_VERSION));

					} catch (IOException e) {
						throw new MojoExecutionException(e.getMessage(), e);
					}

					alreadyUpdatedPlists.add(infoPlistFile);
				}

				/**
				 * If appType provided explicitly; User also needs to specify the additional plist files for appId injection
				 * Currently no strict check for the appType which can be extended for specific checks in future.
				 * For watchkit Application support: User has to pass parameters as below in pom.xml
				 *
				 * <pre>
				 * {@code
				 * <properties>
				 *  <xcode.appType>watchKit</xcode.appType>
				 *  <xcode.watchkitAppPlist>${watchkitApp-plist-file-path}</xcode.watchkitAppPlist>
				 *  <xcode.watchkitExtentionPlist>${watchkitExtention-plist-file-path}</xcode.watchkitExtentionPlist>
				 * </properties>
				 * }
				 * </pre>
				 *
				 * Sample:
				 *
				 * Provide the relative paths of the plist files: Project_Root_Dir\{plist-file-path}
				 * Ex:
				 * AppName: 						"TodoList"
				 * WatchKit App plist file: 		"TodoList WatchKit App\Info.plist"
				 * WatchKit Extension plist file: 	"TodoList WatchKit Extension\Info.plist"
				 * pom.xml entry should look like,
				 *
				 * <pre>
				 * {@code
				 * <properties>
				 *  <xcode.appType>watchKit</xcode.appType>
				 *  <xcode.watchkitAppPlist>TodoList WatchKit App\Info.plist</xcode.watchkitAppPlist>
				 *  <xcode.watchkitExtentionPlist>TodoList WatchKit Extension\Info.plist</xcode.watchkitExtentionPlist>
				 * </properties>
				 * }
				 * </pre>
				 *
				 */
				if (!(appType == null || "".equals(appType.trim()))) {
					LOGGER.info("appType: " + appType
							+ " value provided, needs to consider additional plist files for appIdSuffix injection");

					try {
						srcRoot = getProjectRootDirectory(XCodeContext.SourceCodeLocation.WORKING_COPY, configuration,
								sdk);

						if (watchkitAppPlist != null || "".equals(watchkitAppPlist.trim())) {
							LOGGER.info("location of watchkitAppPlist: " + watchkitAppPlist);
							infoPlistFile = new File(srcRoot, watchkitAppPlist);
							infoPlistAccessor = new PListAccessor(infoPlistFile);
							if (alreadyUpdatedPlists.contains(infoPlistFile)) {
								LOGGER.finer("PList file '" + infoPlistFile.getName()
										+ "' was already updated for another configuration. This file will be skipped.");
							} else {
								try {
									changeAppId(infoPlistAccessor, appIdSuffix, appType);

									changePlistKeyValue(infoPlistFile,PListAccessor.KEY_WK_COMPANION_APP_BUNDLE_IDENTIFIER,getWKCompanionAppBundleIdentifier());
									changePlistKeyValue(infoPlistFile,PListAccessor.KEY_BUNDLE_SHORT_VERSION_STRING,getCFBundleShortVersionString());
									changePlistKeyValue(infoPlistFile,PListAccessor.KEY_BUNDLE_VERSION,getCFBundleVersion());
								} catch (IOException e) {
									throw new MojoExecutionException(e.getMessage(), e);
								}
							}
							setWKAppBundleIdentifier(infoPlistAccessor.getStringValue(PListAccessor.KEY_BUNDLE_IDENTIFIER));
							alreadyUpdatedPlists.add(infoPlistFile);
						}

						if (watchkitExtentionPlist != null || "".equals(watchkitExtentionPlist.trim())) {
							LOGGER.info("location of watchkitExtentionPlist: "
									+ watchkitExtentionPlist);
							infoPlistFile = new File(srcRoot, watchkitExtentionPlist);
							infoPlistAccessor = new PListAccessor(infoPlistFile);
							if (alreadyUpdatedPlists.contains(infoPlistFile)) {
								LOGGER.finer("PList file '" + infoPlistFile.getName()
										+ "' was already updated for another configuration. This file will be skipped.");
							} else {
								changeAppIdForExtension(infoPlistFile, appIdSuffix, appType);

								changePlistKeyValue(infoPlistFile,PListAccessor.KEY_WK_APP_BUNDLE_IDENTIFIER,getWKAppBundleIdentifier());
								changePlistKeyValue(infoPlistFile,PListAccessor.KEY_BUNDLE_SHORT_VERSION_STRING,getCFBundleShortVersionString());
								changePlistKeyValue(infoPlistFile,PListAccessor.KEY_BUNDLE_VERSION,getCFBundleVersion());
								alreadyUpdatedPlists.add(infoPlistFile);
							}
						}

					} catch (XCodeException e) {
						throw new MojoExecutionException(e.getMessage(), e);
					} catch (IOException e) {
						throw new MojoExecutionException(e.getMessage(), e);
					}
				}
			}
		}
	}

	static void changeAppIdForExtension(File plist, String appIdSuffix, String appType) throws MojoExecutionException {
		PListAccessor infoPlistAccessor = new PListAccessor(plist);
		ensurePListFileIsWritable(infoPlistAccessor.getPlistFile());
		try {
			LOGGER.info("Watchkit extension bundle ID is combination of WatchkitApp BI and extension string");
			LOGGER.info("WatchApp Bundle Identifier Value: "+getWKAppBundleIdentifier());
			//String watchkitExtensionBI= getWKAppBundleIdentifier()+"."+appIdSuffix+".watchkitextension";
			String watchkitExtensionBI= getWKAppBundleIdentifier()+".watchkitextension";
			LOGGER.info("So watchkitExtention BI: "+watchkitExtensionBI);
			infoPlistAccessor.updateStringValue(PListAccessor.KEY_BUNDLE_IDENTIFIER, watchkitExtensionBI);
		}
		catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	static void changeAppId(PListAccessor infoPlistAccessor, String appIdSuffix, String appType) throws MojoExecutionException{
		ensurePListFileIsWritable(infoPlistAccessor.getPlistFile());
		try {
			appendAppIdSuffix(infoPlistAccessor, appIdSuffix, appType);
		}
		catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

  private static void ensurePListFileIsWritable(File pListFile) throws MojoExecutionException
	{
    if (!pListFile.canWrite()) {
    	if (!pListFile.setWritable(true, true))
    	throw new MojoExecutionException("Could not make plist file '" + pListFile + "' writable.");
		LOGGER.info("Made PList file '" + pListFile + "' writable.");
	}
	}

  private static void appendAppIdSuffix(PListAccessor infoPlistAccessor, String appIdSuffix, String appType) throws IOException
	{
	String newAppId;
	if (appType == null || "".equals(appType.trim())) {
		String originalAppId = infoPlistAccessor.getStringValue(PListAccessor.KEY_BUNDLE_IDENTIFIER);
		newAppId = originalAppId + "." + appIdSuffix;
		LOGGER.info("Original AppId value : "+ originalAppId);
		LOGGER.info("New upated AppID value: "+ newAppId);
		setWKCompanionAppBundleIdentifier(newAppId);

		String CFBundleName = infoPlistAccessor.getStringValue("CFBundleName");
		LOGGER.info("CFBundleName : "+ CFBundleName);
	}else{
			newAppId = injectAppIdSuffix(appIdSuffix, infoPlistAccessor);
		}

	infoPlistAccessor.updateStringValue(PListAccessor.KEY_BUNDLE_IDENTIFIER, newAppId);
	LOGGER.info("PList file '" + infoPlistAccessor.getPlistFile() + "' updated: Set AppId to '" + newAppId + "'.");
	}


  private static String injectAppIdSuffix(String appIdSuffix, PListAccessor infoPlistAccessor) throws IOException {
	String originalAppId = infoPlistAccessor.getStringValue(PListAccessor.KEY_BUNDLE_IDENTIFIER);
	String firstPart= originalAppId.substring(0,originalAppId.lastIndexOf("."));
	String lastPart = originalAppId.substring(originalAppId.lastIndexOf(".")+ 1);
	String newAppId = firstPart + "." + appIdSuffix + "." + lastPart;
	LOGGER.info("Original AppId value : "+ originalAppId);
	LOGGER.info("New updated AppID value: "+ newAppId);
	return newAppId;
	}

  private static void changePlistKeyValue(File plist, String key, String value) throws MojoExecutionException, IOException{
	  PListAccessor infoPlistAccessor = new PListAccessor(plist);
	  ensurePListFileIsWritable(infoPlistAccessor.getPlistFile());
	  infoPlistAccessor.updateStringValue(key, value);
	  LOGGER.info("PList file '" + plist.getAbsolutePath() + "' updated: Set "
			  + "Key " + key  + "and Value " + value);
    }

	public static String getWKCompanionAppBundleIdentifier() {
		return wkCompanionAppBundleIdentifier;
	}

	public static void setWKCompanionAppBundleIdentifier(String wKCompanionAppBundleIdentifier) {
		wkCompanionAppBundleIdentifier = wKCompanionAppBundleIdentifier;
	}

	public static String getWKAppBundleIdentifier() {
		return wkAppBundleIdentifier;
	}

	public static void setWKAppBundleIdentifier(String wKAppBundleIdentifier) {
		wkAppBundleIdentifier = wKAppBundleIdentifier;
	}

	public static String getCFBundleShortVersionString() {
		return cfBundleShortVersionString;
	}

	public static void setCFBundleShortVersionString(String cFBundleShortVersionString) {
		cfBundleShortVersionString = cFBundleShortVersionString;
	}

	public static String getCFBundleVersion() {
		return cfBundleVersion;
	}

	public static void setCFBundleVersion(String cFBundleVersion) {
		cfBundleVersion = cFBundleVersion;
	}

	 public static String getWatchkitAppBundleIdentifier() {
			return watchkitAppBundleIdentifier;
	}

	public static void setWatchkitAppBundleIdentifier(String watchkitAppBundleIdentifier) {
			XCodeChangeAppIDMojo.watchkitAppBundleIdentifier = watchkitAppBundleIdentifier;
	}


}
