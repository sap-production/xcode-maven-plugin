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
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;

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
  
  private final static Map<XCodeContext, Properties> buildSettings = new HashMap<XCodeContext, Properties>();
  
  static String getBuildSetting(XCodeContext context, Log log, String key) throws XCodeException
  {
    String buildSetting = getBuildSettings(context, log).getProperty(key);
    debug(log, "Build settings for context '" + context + "'. Key: '" + key + "' resolved to: " + buildSetting);
    return buildSetting;
  }
  
  private static synchronized Properties getBuildSettings(final XCodeContext context, final Log log) throws XCodeException {
    
    Properties _buildSettings = buildSettings.get(context);
    
    if(_buildSettings == null) {
      _buildSettings = extractBuildSettings(context);
      buildSettings.put(context, _buildSettings);
      log.info("Build settings for context: " + context + " loaded:" + toString(_buildSettings));
    }else{
      debug(log, "Build settings for key: '" + context + " found in cache.");
    }
      
    return _buildSettings;
  }
  
  private static String toString(Properties buildSettings)
  {
    String ls = System.getProperty("line.separator");
    StringBuilder sb = new StringBuilder(ls);
    
    for(Map.Entry<?, ?> e : buildSettings.entrySet()) {
      sb.append(e.getKey()).append("=").append(e.getValue()).append(ls);
    }
    return sb.toString();
  }

  private static Properties extractBuildSettings(final XCodeContext context) throws  XCodeException
  { 
    List<String> buildActions = Collections.emptyList();
    Options options = context.getOptions();
    Map<String, String> managedOptions = new HashMap<String, String>(options.getManagedOptions());
    managedOptions.put(Options.ManagedOption.SHOWBUILDSETTINGS.getOptionName(), null);

    XCodeContext showBuildSettingsContext = new XCodeContext(buildActions, context.getProjectRootDirectory(), context.getOut(), context.getSettings(), new Options(options.getUserOptions(), managedOptions));
    
    final CommandLineBuilder cmdLineBuilder = new CommandLineBuilder(showBuildSettingsContext);
    PrintStream out = null;
    ByteArrayOutputStream os = null;
    try {
      os = new ByteArrayOutputStream();
      out = new PrintStream(os);

      final int returnValue = Forker.forkProcess(out, context.getProjectRootDirectory(),
            cmdLineBuilder.createBuildCall());

      if (returnValue != 0) {
        if(out != null)
          out.flush();
        throw new XCodeException("Could not execute xcodebuild -showBuildSettings command for configuration "
              + context.getConfiguration() + " and sdk " + context.getSDK() + ": " + new String(os.toByteArray()));
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
  
  private static void debug(Log log, String message) {
    log.debug(EffectiveBuildSettings.class.getName() + ": " + message);
  }
}
