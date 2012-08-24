package com.sap.prd.mobile.ios.mios;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

public class EffectiveBuildSettings
{
  public static final String PRODUCT_NAME = "PRODUCT_NAME";
  
  private Properties properties;
  
  public static String getProductName(AbstractXCodeMojo mojo, String configuration, String platform)
  {
    EffectiveBuildSettings settings = new EffectiveBuildSettings(mojo, configuration, platform);
    return settings.getBuildSetting(PRODUCT_NAME);
  }
  
  /**
   * @param configuration
   *          e.g. "Release"
   * @param platform
   *          e.g. "iphoneos"
   */
  public EffectiveBuildSettings(AbstractXCodeMojo mojo, String configuration, String platform)
  {
    this(mojo.project.getBuild().getDirectory(), configuration, platform);
  }
  
  /**
   * @param directory
   *          the project directory
   * @param configuration
   *          e.g. "Release"
   * @param platform
   *          e.g. "iphoneos"
   */
  public EffectiveBuildSettings(String directory, String configuration, String platform)
  {
      File file = XCodeAppendBuildPhaseMojo.getBuildEnvironmentFile(directory, configuration, platform);
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
        if (fis != null) {
          IOUtils.closeQuietly(fis);
        }
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
