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

import java.util.ArrayList;
import java.util.List;

class CommandLineBuilder
{

  private final static String XCODEBUILD = "xcodebuild";
  private final static String PROJECT_NAME = "project";
  private final static String CONFIGURATION = "configuration";
  private final static String CODE_SIGN_IDENTITY = "CODE_SIGN_IDENTITY";
  private final static String PROVISIONING_PROFILE = "PROVISIONING_PROFILE";
  private final static String SDK = "sdk";

  private String configuration;
  private String sdk;
  private XCodeContext xcodeContext;
  
  public CommandLineBuilder(String configuration, String sdk, XCodeContext ctx)
  {
    this.configuration = configuration;
    this.sdk = sdk;
    this.xcodeContext = ctx;
  }

  String[] createBuildCall()
  {
    
    List<String> result = createBaseCall();
    for (String buildAction : xcodeContext.getBuildActions()) {
      appendValue(result, buildAction);
    }

    return result.toArray(new String[result.size()]);
  }
  
  String[] createShowBuildSettingsCall()
  {
    List<String> result = createBaseCall();
    appendKey(result, "showBuildSettings");
    return result.toArray(new String[result.size()]);
  }
  
  private List<String> createBaseCall()
  {
    List<String> result = new ArrayList<String>();

    result.add(XCODEBUILD);
    appendOption(result, PROJECT_NAME, xcodeContext.getProjectName() + XCodeConstants.XCODE_PROJECT_EXTENTION);
    appendOption(result, CONFIGURATION, configuration);
    appendOption(result, SDK, sdk);

    if (xcodeContext.getCodeSignIdentity() != null && !xcodeContext.getCodeSignIdentity().isEmpty()) {
      appendEnv(result, CODE_SIGN_IDENTITY, xcodeContext.getCodeSignIdentity());
    }
    
    if (xcodeContext.getProvisioningProfile() != null) {
      appendEnv(result, PROVISIONING_PROFILE, xcodeContext.getProvisioningProfile());
    }
    
    // Output directories should be specified (recommended by Apple - http://developer.apple.com/devcenter/download.action?path=/wwdc_2012/wwdc_2012_session_pdfs/session_404__building_from_the_command_line_with_xcode.pdf)
    appendEnv(result, "DSTROOT", "build");
    appendEnv(result, "SYMROOT", "build");
    appendEnv(result, "SHARED_PRECOMPS_DIR", "build");
    appendEnv(result, "OBJROOT", "build");
    return result;
  }

  private static void appendEnv(List<String> result, String key, String value)
  {
    result.add(key + "=" + value);
  }

  private static void appendOption(List<String> result, String key, String value)
  {

    check("option", key);
    appendKey(result, key);
    appendValue(result, value);
  }

  private static void appendKey(List<String> result, String key)
  {
    check("key", key);
    result.add("-" + key);
  }
  
  private static void appendValue(List<String> result, String value)
  {
    check("value", value);
    result.add(value);
  }

  private static void check(final String name, final String forCheck)
  {

    if (forCheck == null || forCheck.isEmpty())
      throw new IllegalStateException("Invalid " + name + ": " + forCheck + "'. Was null or empty.");
  }

  @Override
  public String toString()
  {

    final StringBuilder sb = new StringBuilder(256);

    boolean first = true;

    for (String part : createBuildCall()) {

      if (!first)
        sb.append(" ");
      else
        first = false;

      sb.append(part);
    }

    return sb.toString();
  }
}
