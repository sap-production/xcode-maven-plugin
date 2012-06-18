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
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Context object for Xcode build to hold relevant data:
 * * projectName
 * * Set of configurations
 * * Set of SDKs
 * * List of buildActions
 * projectRootDirectory
 * codeSignIdentity
 * output stream
 *
 */
class XCodeContext
{
  private final static String ls = System.getProperty("line.separator");

  private final String projectName;

  private final Set<String> configurations;

  private final Set<String> sdks;

  private final List<String> buildActions;

  private final String codeSignIdentity;

  private final File projectRootDirectory;

  private final PrintStream out;

  private String provisioningProfile;


  XCodeContext(final String projectName, final Set<String> configurations, final Set<String> sdks,
        final List<String> buildActions, final File projectRootDirectory, String codeSignIdentity, final PrintStream out)
  {

    raiseExceptionIfNullOrEmpty("projectName", projectName);
    this.projectName = projectName;

    raiseExceptionIfInvalid("configuration", configurations);
    this.configurations = configurations;

    this.sdks = sdks;

    raiseExceptionIfInvalid("buildActions", buildActions);
    this.buildActions = Collections.unmodifiableList(buildActions);

    this.projectRootDirectory = projectRootDirectory;

    if (out == null)
      throw new IllegalArgumentException("PrintStream for log handling is not available.");
    this.out = out;

    if (codeSignIdentity != null && codeSignIdentity.trim().isEmpty())
      throw new IllegalArgumentException("CodesignIdentity was empty: '" + codeSignIdentity
            + "'. If you want to use the code" +
            " sign identity defined in the xCode project configuration just do" +
            " not provide the 'codeSignIdentity' in your Maven settings.");

    this.codeSignIdentity = codeSignIdentity;
  }

  String getProjectName()
  {
    return this.projectName;
  }

  Set<String> getConfigurations()
  {
    return this.configurations;
  }

  String[] getBuildActions()
  {
    return buildActions.toArray(new String[buildActions.size()]);
  }

  File getProjectRootDirectory()
  {
    return this.projectRootDirectory;
  }

  PrintStream getOut()
  {
    return this.out;
  }
  
  public String getProvisioningProfile()
  {
    return provisioningProfile;
  }
  
  public void setProvisioningProfile(String provisioningProfile)
  {
    this.provisioningProfile = provisioningProfile;
  }
  
  

  @Override
  public String toString()
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("Instance            : ").append(super.toString()).append(ls);
    sb.append("ProjectRootDirectory: ").append(getProjectRootDirectory()).append(ls);
    sb.append("ProjectName         : ").append(getProjectName()).append(ls);
    sb.append("Configuration       : ").append(getConfigurations()).append(ls);
    sb.append("BuildActions        : ").append(buildActions);
    sb.append("CodeSignIdentity    : ").append(codeSignIdentity);
    sb.append("ProvisioningProfile : ").append(provisioningProfile);
    return sb.toString();
  }

  private static void raiseExceptionIfNullOrEmpty(final String key, final String value)
  {
    if (value == null || value.length() == 0)
      throw new IllegalArgumentException(String.format(Locale.ENGLISH, "No %s provided. Was null or empty.", key));
  }

  private static void raiseExceptionIfInvalid(final String key, final Collection<String> collection)
  {

    if (collection == null || collection.size() == 0)
      throw new IllegalArgumentException("No build actions has been provided (Was either null or empty).");

    for (final String buildAction : collection) {

      if (buildAction == null || buildAction.length() == 0)
        throw new IllegalArgumentException("Build action array contained a null element or an empty element.");

      //
      // TODO: According to xcodebuild man page build actions are limited
      // to build, installsrc, install, clean
      // Maybe we should check for these 4 values. I'm not shure if these
      // values are stable enough ...
      // The main reason here is to avoid build actions with blanks in it.
      //
      if (!buildAction.matches("[A-Za-z0-9_]+"))
        throw new IllegalArgumentException("Build action array contains an invalid element (" + buildAction + ").");
    }
  }

  Set<String> getSdks()
  {
    return this.sdks;
  }

  String getCodeSignIdentity()
  {
    return codeSignIdentity;
  }


}
