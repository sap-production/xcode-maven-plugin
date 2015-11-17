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
  private String appIdSuffix;
  
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
   * These are the various parameters we need to change for watchkit apps
   */
  private static String WKCompanionAppBundleIdentifier;

  private static String WKAppBundleIdentifier;

  private static String CFBundleShortVersionString;

  private static String CFBundleVersion;

  
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
				 * Currently no strict check for the appType TODO: Can be extended !! MIOS: Bangalore team;
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
				    LOGGER.info("appType: " + appType + " value provided, needs to consider additional plist files for appIdSuffix injection");

					try {
						srcRoot = getProjectRootDirectory(XCodeContext.SourceCodeLocation.WORKING_COPY, configuration,sdk);
						
						if(watchkitAppPlist != null || "".equals(watchkitAppPlist.trim())){
							LOGGER.info("File path of watchkitApp Plist file \n watchkitAppPlist: "+watchkitAppPlist);
							infoPlistFile = new File(srcRoot, watchkitAppPlist);
							infoPlistAccessor = new PListAccessor(infoPlistFile);
							changeAppId(infoPlistAccessor, appIdSuffix , appType);
							try {
								changeCompanionBundleId(infoPlistAccessor,getWKCompanionAppBundleIdentifier());
								changeWatchAppBundleShortVersionString(infoPlistAccessor, getCFBundleShortVersionString());
								changeWatchAppBundleVersion(infoPlistAccessor, getCFBundleVersion());

							} catch (IOException e) {
								throw new MojoExecutionException(e.getMessage(), e);
							}

							setWKAppBundleIdentifier(infoPlistAccessor.getStringValue(PListAccessor.KEY_BUNDLE_IDENTIFIER));

							alreadyUpdatedPlists.add(infoPlistFile);
						}
						
						if(watchkitExtentionPlist != null || "".equals(watchkitExtentionPlist.trim())){
							LOGGER.info("File path of watchkitAppExtention Plist file \n watchkitExtentionPlist: "+watchkitExtentionPlist);
							infoPlistFile = new File(srcRoot, watchkitExtentionPlist);
							infoPlistAccessor = new PListAccessor(infoPlistFile);
							changeAppId(infoPlistAccessor, appIdSuffix , appType);
							changeWKAppBundleIdentifier(infoPlistAccessor, getWKAppBundleIdentifier());
							alreadyUpdatedPlists.add(infoPlistFile);
						}
		
					} catch (XCodeException e) {
						e.printStackTrace();
					} catch (IOException e) {
						throw new MojoExecutionException(e.getMessage(), e);
					}
				}
      }
    }
  }

  static void changeAppId(PListAccessor infoPlistAccessor, String appIdSuffix, String appType) throws MojoExecutionException
  {
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


  private static void appendAppIdSuffix(PListAccessor infoPlistAccessor, String appIdSuffix, String appType)
        throws IOException
  {
	
    String newAppId;
    if (appType == null || "".equals(appType.trim())) {
    	String originalAppId = infoPlistAccessor.getStringValue(PListAccessor.KEY_BUNDLE_IDENTIFIER);
    	newAppId = originalAppId + "." + appIdSuffix;
    	LOGGER.info("Original AppId value : "+ originalAppId);
        LOGGER.info("New upated AppID value: "+ newAppId);
        
        setWKCompanionAppBundleIdentifier(newAppId);

        //TODO: MIOS: Bangalore team; We can also plan to draw the plist files dynamically using CFBundleName: Future work
    	String CFBundleName = infoPlistAccessor.getStringValue("CFBundleName");
    	LOGGER.info("CFBundleName : "+ CFBundleName);    	
      }else{
    	  newAppId = injectAppId(appIdSuffix, infoPlistAccessor);
      }
   
    infoPlistAccessor.updateStringValue(PListAccessor.KEY_BUNDLE_IDENTIFIER, newAppId);
    LOGGER.info("PList file '" + infoPlistAccessor.getPlistFile() + "' updated: Set AppId to '" + newAppId + "'.");
  }


  private static String injectAppId(String appIdSuffix, PListAccessor infoPlistAccessor) throws IOException {
	  
	String originalAppId = infoPlistAccessor.getStringValue(PListAccessor.KEY_BUNDLE_IDENTIFIER);
	String firstPart= originalAppId.substring(0,originalAppId.lastIndexOf("."));
    String lastPart = originalAppId.substring(originalAppId.lastIndexOf(".")+ 1);    
    String newAppId = firstPart + "." + appIdSuffix + "." + lastPart;
    LOGGER.info("Original AppId value : "+ originalAppId);
    LOGGER.info("New upated AppID value: "+ newAppId);
	return newAppId;
  }

	private void changeWatchAppBundleVersion(PListAccessor infoPlistAccessor, String cfBundleVersion2)
			throws IOException, MojoExecutionException {
		ensurePListFileIsWritable(infoPlistAccessor.getPlistFile());
		infoPlistAccessor.updateStringValue(PListAccessor.KEY_BUNDLE_VERSION, cfBundleVersion2);
		LOGGER.info("PList file '" + infoPlistAccessor.getPlistFile() + "' updated: Set "
				+ PListAccessor.KEY_BUNDLE_VERSION + "cfBundleVersion '" + cfBundleVersion2 + "'.");

	}

	private void changeWatchAppBundleShortVersionString(PListAccessor infoPlistAccessor,
			String keyBundleShortVersionString) throws IOException, MojoExecutionException {
		ensurePListFileIsWritable(infoPlistAccessor.getPlistFile());
		infoPlistAccessor.updateStringValue(PListAccessor.KEY_BUNDLE_SHORT_VERSION_STRING, keyBundleShortVersionString);
		LOGGER.info("PList file '" + infoPlistAccessor.getPlistFile() + "' updated: Set "
				+ PListAccessor.KEY_BUNDLE_SHORT_VERSION_STRING + "keyBundleShortVersionString '"
				+ keyBundleShortVersionString + "'.");

	}

	private void changeWKAppBundleIdentifier(PListAccessor infoPlistAccessor, String wkAppBundleIdentifier2)
			throws IOException, MojoExecutionException {
		ensurePListFileIsWritable(infoPlistAccessor.getPlistFile());
		infoPlistAccessor.updateStringValue(PListAccessor.KEY_WK_APP_BUNDLE_IDENTIFIER, wkAppBundleIdentifier2);
		LOGGER.info("PList file '" + infoPlistAccessor.getPlistFile() + "' updated: Set "
				+ PListAccessor.KEY_WK_APP_BUNDLE_IDENTIFIER + "AppId to '" + wkAppBundleIdentifier2 + "'.");
	}

	private void changeCompanionBundleId(PListAccessor infoPlistAccessor, String wkCompanionAppBundleIdentifier2)
			throws MojoExecutionException, IOException {
		ensurePListFileIsWritable(infoPlistAccessor.getPlistFile());
		infoPlistAccessor.updateStringValue(PListAccessor.KEY_WK_COMPANION_APP_BUNDLE_IDENTIFIER,
				wkCompanionAppBundleIdentifier2);
		LOGGER.info("PList file '" + infoPlistAccessor.getPlistFile() + "' updated: Set "
				+ PListAccessor.KEY_WK_COMPANION_APP_BUNDLE_IDENTIFIER + "AppId to '" + wkCompanionAppBundleIdentifier2
				+ "'.");
	}

	public static String getWKCompanionAppBundleIdentifier() {
		return WKCompanionAppBundleIdentifier;
	}

	public static void setWKCompanionAppBundleIdentifier(String wKCompanionAppBundleIdentifier) {
		WKCompanionAppBundleIdentifier = wKCompanionAppBundleIdentifier;
	}

	public static String getWKAppBundleIdentifier() {
		return WKAppBundleIdentifier;
	}

	public static void setWKAppBundleIdentifier(String wKAppBundleIdentifier) {
		WKAppBundleIdentifier = wKAppBundleIdentifier;
	}

	public static String getCFBundleShortVersionString() {
		return CFBundleShortVersionString;
	}

	public static void setCFBundleShortVersionString(String cFBundleShortVersionString) {
		CFBundleShortVersionString = cFBundleShortVersionString;
	}

	public static String getCFBundleVersion() {
		return CFBundleVersion;
	}

	public static void setCFBundleVersion(String cFBundleVersion) {
		CFBundleVersion = cFBundleVersion;
	}

}
