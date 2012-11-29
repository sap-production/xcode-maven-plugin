package com.sap.prd.mobile.ios.mios;

/*
 * #%L
 * Xcode Maven Plugin
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

class EffectiveBuildSettings
{
  static final String PRODUCT_NAME = "PRODUCT_NAME";
  static final String SRC_ROOT = "SRCROOT";
  static final String GCC_GENERATE_DEBUGGING_SYMBOLS = "GCC_GENERATE_DEBUGGING_SYMBOLS";
  static final String CODE_SIGN_IDENTITY = "CODE_SIGN_IDENTITY";
  static final String CODESIGNING_FOLDER_PATH = "CODESIGNING_FOLDER_PATH";
  static final String INFOPLIST_FILE = "INFOPLIST_FILE";
  static final String PUBLIC_HEADERS_FOLDER_PATH = "PUBLIC_HEADERS_FOLDER_PATH";
  static final String BUILT_PRODUCTS_DIR = "BUILT_PRODUCTS_DIR";
  
  private final static Map<Key, Properties> buildSettings = new HashMap<Key, Properties>();
  
  static String getBuildSetting(XCodeContext context, String configuration, String sdk, String key) throws XCodeException
  {
    return getBuildSettings(context, configuration, sdk).getProperty(key);
  }
  
  private static synchronized Properties getBuildSettings(final XCodeContext context, final String configuration, final String sdk) throws XCodeException {
    
    final Key key = new Key(context.getProjectRootDirectory(), configuration, sdk);
    Properties _buildSettings = buildSettings.get(key);
    
    if(_buildSettings == null) {
      _buildSettings = extractBuildSettings(context, configuration, sdk);
      buildSettings.put(key, _buildSettings);
    }
      
    return _buildSettings;
  }
  
  private static Properties extractBuildSettings(final XCodeContext context, final String configuration, final String sdk) throws  XCodeException
  { 
    final CommandLineBuilder cmdLineBuilder = new CommandLineBuilder(configuration, sdk, context);
    PrintStream out = null;
    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      out = new PrintStream(os);

      final int returnValue = Forker.forkProcess(out, context.getProjectRootDirectory(),
            cmdLineBuilder.createShowBuildSettingsCall());

      if (returnValue != 0) {
        throw new XCodeException("Could not execute xcodebuild -showBuildSettings command for configuration "
              + configuration + " and sdk " + sdk);
      }

      out.flush();
      Properties prop = new Properties();
      prop.load(new ByteArrayInputStream(os.toByteArray()));
      return prop;

    } catch(IOException ex) {
      throw new XCodeException("Cannot extract build properties: " + ex.getMessage(), ex);
    }
    finally {
      IOUtils.closeQuietly(out);
    }
  }
  
  private static class Key {
    
    private final File location;
    private final String configuration, sdk;
    
    Key(File location, String configuration, String sdk) {
      
      if(configuration == null || configuration.isEmpty())
        throw new IllegalArgumentException("Configuration was not provided.");
      
      if(sdk == null || sdk.isEmpty())
        throw new IllegalArgumentException("SDK was not provided.");
      
      if(location == null)
        throw new IllegalArgumentException("Location was not provided.");
        
      
      this.configuration = configuration;
      this.sdk = sdk;
      this.location = location;
    }

    @Override
    public String toString()
    {
      return "Key [location=" + location + ", configuration=" + configuration + ", sdk=" + sdk + "]";
    }

    @Override
    public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
      result = prime * result + ((location == null) ? 0 : location.hashCode());
      result = prime * result + ((sdk == null) ? 0 : sdk.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj)
    {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      Key other = (Key) obj;
      if (configuration == null) {
        if (other.configuration != null) return false;
      }
      else if (!configuration.equals(other.configuration)) return false;
      if (location != other.location) return false;
      if (sdk == null) {
        if (other.sdk != null) return false;
      }
      else if (!sdk.equals(other.sdk)) return false;
      return true;
    }

  
  
  }
}
