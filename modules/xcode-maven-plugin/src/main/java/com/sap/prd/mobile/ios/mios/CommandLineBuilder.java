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
  private static String SDK = "sdk";

  private String projectName;
  private String configuration;
  private String sdk;
  private String codeSignIdentity;
  private String provisioningProfile;
  private List<String> buildActions;
  
  CommandLineBuilder setCodeSignIdentity(String codeSignIdentity)
  {
    if (codeSignIdentity != null && codeSignIdentity.trim().isEmpty())
    {
      throw new IllegalArgumentException(
            "CodeSignIdentity must not be an empty String. '"
                  + codeSignIdentity
                  + "'.");
    }

    this.codeSignIdentity = codeSignIdentity;
    return this;
  }



  CommandLineBuilder setProjectName(String projectName)
  {
    this.projectName = projectName;
    return this;
  }

  CommandLineBuilder setConfiguration(String configuration)
  {
    this.configuration = configuration;
    return this;
  }

  CommandLineBuilder setSdk(String sdk)
  {
    this.sdk = sdk;
    return this;
  }

  CommandLineBuilder setBuildActions(List<String> buildActions)
  {
    this.buildActions = buildActions;
    return this;
  }
  
  CommandLineBuilder setProvisioningProfile(String provisioningProfile)
  {
    this.provisioningProfile = provisioningProfile;
    return this;
  }

  String[] createCommandline()
  {

    List<String> result = new ArrayList<String>();

    result.add(XCODEBUILD);
    appendOption(result, PROJECT_NAME, projectName + XCodeConstants.XCODE_PROJECT_EXTENTION);
    appendOption(result, CONFIGURATION, configuration);
    appendOption(result, SDK, sdk);

    if (codeSignIdentity != null && !codeSignIdentity.isEmpty()) {
      appendEnv(result, CODE_SIGN_IDENTITY, codeSignIdentity);
    }
    
    if (provisioningProfile != null) {
      appendEnv(result, PROVISIONING_PROFILE, provisioningProfile);
    }
    
    for (String buildAction : buildActions) {
      appendOption(result, buildAction);
    }

    return result.toArray(new String[result.size()]);
  }

  private static void appendEnv(List<String> result, String key, String value)
  {
    result.add(key + "=" + value);
  }

  private static void appendOption(List<String> result, String key, String value)
  {

    check("option", key);
    appendOption(result, "-" + key);
    appendOption(result, value);
  }

  private static void appendOption(List<String> result, String value)
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

    for (String part : createCommandline()) {

      if (!first)
        sb.append(" ");
      else
        first = false;

      sb.append(part);
    }

    return sb.toString();
  }
}
