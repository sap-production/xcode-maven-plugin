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
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

public class EffectiveBuildSettings implements IEffectiveBuildSettings
{

  private final static Logger LOGGER = LogManager.getLogManager().getLogger(XCodePluginLogger.getLoggerName());

  private final static Map<IXCodeContext, Properties> buildSettings = new HashMap<IXCodeContext, Properties>();

  @Override
  public String getBuildSettingByKey(IXCodeContext context, String key)
  {
    try {
      return getBuildSetting(context, key);
    }
    catch (XCodeException e) {
      throw new IllegalStateException("Cannot obtain build setting for key '" + key + "' for configuration '"
            + context.getConfiguration() + "' and sdk '" + context.getSDK() + "'.", e);
    }
  }

  public static String getBuildSetting(IXCodeContext context, String key) throws XCodeException
  {
    String buildSetting = getBuildSettings(context).getProperty(key);
    LOGGER.finer("Build settings for context '" + context + "'. Key: '" + key + "' resolved to: " + buildSetting);
    return buildSetting;
  }

  private static synchronized Properties getBuildSettings(final IXCodeContext context)
        throws XCodeException
  {

    Properties _buildSettings = buildSettings.get(context);

    if (_buildSettings == null) {
      _buildSettings = extractBuildSettings(context);
      buildSettings.put(context, _buildSettings);
      LOGGER.info("Build settings for context: " + context + " loaded:" + toString(_buildSettings));
    }
    else {
      LOGGER.finer("Build settings for key: '" + context + " found in cache.");
    }

    return _buildSettings;
  }

  private static String toString(Properties buildSettings)
  {
    String ls = System.getProperty("line.separator");
    StringBuilder sb = new StringBuilder(ls);

    for (Map.Entry<?, ?> e : buildSettings.entrySet()) {
      sb.append(e.getKey()).append("=").append(e.getValue()).append(ls);
    }
    return sb.toString();
  }

  private static Properties extractBuildSettings(final IXCodeContext context) throws XCodeException
  {
    List<String> buildActions = Collections.emptyList();
    IOptions options = context.getOptions();
    Map<String, String> managedOptions = new HashMap<String, String>(options.getManagedOptions());
    managedOptions.put(Options.ManagedOption.SHOWBUILDSETTINGS.getOptionName(), null);

    XCodeContext showBuildSettingsContext = new XCodeContext(buildActions, context.getProjectRootDirectory(),
          context.getOut(), new Settings(context.getSettings().getUserSettings(), context.getSettings()
            .getManagedSettings()), new Options(options.getUserOptions(), managedOptions));

    final CommandLineBuilder cmdLineBuilder = new CommandLineBuilder(showBuildSettingsContext);
    PrintStream out = null;
    ByteArrayOutputStream os = null;
    try {
      os = new ByteArrayOutputStream();
      out = new PrintStream(os, true, Charset.defaultCharset().name());

      final int returnValue = Forker.forkProcess(out, context.getProjectRootDirectory(),
            cmdLineBuilder.createBuildCall());

      if (returnValue != 0) {
        if (out != null)
          out.flush();
        throw new XCodeException("Could not execute xcodebuild -showBuildSettings command for configuration "
              + context.getConfiguration() + " and sdk " + context.getSDK() + ": " + new String(os.toByteArray(), Charset.defaultCharset().name()));
      }

      out.flush();
      Properties prop = new Properties();
      prop.load(new ByteArrayInputStream(os.toByteArray()));
      return prop;

    }
    catch (IOException ex) {
      throw new XCodeException("Cannot extract build properties: " + ex.getMessage(), ex);
    }
    finally {
      IOUtils.closeQuietly(out);
    }
  }
}
