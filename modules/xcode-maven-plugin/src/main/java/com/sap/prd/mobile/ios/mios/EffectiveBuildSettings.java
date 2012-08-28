package com.sap.prd.mobile.ios.mios;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.maven.project.MavenProject;

public class EffectiveBuildSettings
{
  public static final String PRODUCT_NAME = "PRODUCT_NAME";
  public static final String GCC_GENERATE_DEBUGGING_SYMBOLS = "GCC_GENERATE_DEBUGGING_SYMBOLS";
  public static final String CODE_SIGN_IDENTITY = "CODE_SIGN_IDENTITY";
  public static final String CODESIGNING_FOLDER_PATH = "CODESIGNING_FOLDER_PATH";
  
  private Properties properties;
  
  public static String getProductName(MavenProject project, String configuration, String sdk)
  {
    EffectiveBuildSettings settings = new EffectiveBuildSettings(project, configuration, sdk);
    return settings.getBuildSetting(PRODUCT_NAME);
  }
  
  /**
   * @param configuration
   *          e.g. "Release"
   * @param sdk
   *          e.g. "iphoneos"
   */
  public EffectiveBuildSettings(MavenProject project, String configuration, String sdk)
  {
    this(project.getBuild().getDirectory(), configuration, sdk);
  }
  
  /**
   * @param directory
   *          the project directory
   * @param configuration
   *          e.g. "Release"
   * @param sdk
   *          e.g. "iphoneos"
   */
  public EffectiveBuildSettings(String directory, String configuration, String sdk)
  {
      File file = XCodeSaveBuildSettingsMojo.getBuildEnvironmentFile(directory, configuration, sdk);
      Properties p = new Properties();
      FileInputStream fis = null;
      try {
        fis = new FileInputStream(file);
        p.load(fis);
        properties = p;
      }
      catch (IOException e) {
        throw new IllegalStateException("Could not read build properties file", e);
      }
      finally {
        IOUtils.closeQuietly(fis);
      }
  }
  
  public String getBuildSetting(String key)
  {
    return properties.getProperty(key);
  }
  
  public String getBuildSetting(String key, String defaultValue)
  {
    return properties.getProperty(key, defaultValue);
  }
}
