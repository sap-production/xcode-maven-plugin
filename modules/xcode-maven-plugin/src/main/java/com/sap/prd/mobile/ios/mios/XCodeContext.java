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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Context object for Xcode build to hold relevant data: * projectName * Set of configurations * Set
 * of SDKs * List of buildActions projectRootDirectory codeSignIdentity output stream xcode options
 * xcode settings
 */
public class XCodeContext implements IXCodeContext
{
  enum SourceCodeLocation
  {
    ORIGINAL, WORKING_COPY
  };

  private final static String ls = System.getProperty("line.separator");

  private final List<String> buildActions;

  private final File projectRootDirectory;

  private PrintStream out;

  private final Options options;

  private final Settings settings;

  public XCodeContext(List<String> buildActions,
        File projectRootDirectory, PrintStream out, Settings settings, Options options)
  {
    super();

    raiseExceptionIfBuildActionsAreInvalid("buildActions", buildActions);

    if (projectRootDirectory == null || !projectRootDirectory.canRead())
      throw new IllegalArgumentException("ProjectRootDirectory '" + projectRootDirectory
            + "' is null or cannot be read.");

    this.buildActions = Collections.unmodifiableList(new ArrayList<String>(buildActions));
    this.projectRootDirectory = new File(projectRootDirectory, "");
    setOut(out);

    if (settings == null) {
      Map<String, String> userSettings = new HashMap<String, String>(), managedSettings = new HashMap<String, String>();
      this.settings = new Settings(userSettings, managedSettings);
    }
    else {
      this.settings = settings;
    }

    if (options == null) {
      Map<String, String> userOptions = new HashMap<String, String>(), managedOptions = new HashMap<String, String>();
      this.options = new Options(userOptions, managedOptions);
    }
    else {
      this.options = options;
    }
  }

  public String getProjectName()
  {
    return options.getAllOptions().get(Options.ManagedOption.PROJECT.getOptionName());
  }

  public List<String> getBuildActions()
  {
    return buildActions;
  }

  public String getCodeSignIdentity()
  {
    return settings.getAllSettings().get(Settings.ManagedSetting.CODE_SIGN_IDENTITY.name());
  }

  public File getProjectRootDirectory()
  {
    return projectRootDirectory;
  }

  public PrintStream getOut()
  {
    return out;
  }

  public final void setOut(PrintStream out)
  {
    if (out == null)
      throw new IllegalArgumentException("PrintStream for log handling is not available.");
    this.out = out;
  }

  /* (non-Javadoc)
   * @see com.sap.prd.mobile.ios.mios.IXCodeContext#getSDK()
   */
  @Override
  public String getSDK()
  {
    return getOptions().getAllOptions().get(Options.ManagedOption.SDK.getOptionName());
  }

  @Override
  public String getConfiguration()
  {
    return getOptions().getAllOptions().get(Options.ManagedOption.CONFIGURATION.getOptionName());
  }

  public String getProvisioningProfile()
  {
    return getSettings().getAllSettings().get(Settings.ManagedSetting.PROVISIONING_PROFILE.name());
  }

  public String getTarget()
  {
    return getOptions().getAllOptions().get(Options.ManagedOption.TARGET.getOptionName());
  }

  public Options getOptions()
  {
    return options;
  }

  /* (non-Javadoc)
   * @see com.sap.prd.mobile.ios.mios.IXCodeContext#getSettings()
   */
  public Settings getSettings()
  {
    return settings;
  }

  @Override
  public String toString()
  {
    final StringBuilder sb = new StringBuilder();
    sb.append(ls).append(super.toString()).append(ls);
    sb.append("ProjectRootDirectory: ").append(getProjectRootDirectory()).append(ls);
    sb.append("BuildActions: ").append(buildActions).append(ls).append(ls);
    sb.append("Options:").append(ls);
    sb.append(options).append(ls).append(ls);
    sb.append("Settings:").append(ls);
    sb.append(settings).append(ls);
    return sb.toString();
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((buildActions == null) ? 0 : buildActions.hashCode());
    result = prime * result + ((options == null) ? 0 : options.hashCode());
    result = prime * result + ((projectRootDirectory == null) ? 0 : projectRootDirectory.hashCode());
    result = prime * result + ((settings == null) ? 0 : settings.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    XCodeContext other = (XCodeContext) obj;
    if (buildActions == null) {
      if (other.buildActions != null) return false;
    }
    else if (!buildActions.equals(other.buildActions)) return false;
    if (options == null) {
      if (other.options != null) return false;
    }
    else if (!options.equals(other.options)) return false;
    if (projectRootDirectory == null) {
      if (other.projectRootDirectory != null) return false;
    }
    else if (!projectRootDirectory.equals(other.projectRootDirectory)) return false;
    if (settings == null) {
      if (other.settings != null) return false;
    }
    else if (!settings.equals(other.settings)) return false;
    return true;
  }

  private static void raiseExceptionIfBuildActionsAreInvalid(final String key, final Collection<String> buildActions)
  {

    for (final String buildAction : buildActions) {

      if (buildAction == null || buildAction.length() == 0)
        throw new InvalidBuildActionException("Build action array contained a null element or an empty element.");

      if (!buildAction.matches("[A-Za-z0-9_]+"))
        throw new InvalidBuildActionException("Build action array contains an invalid element (" + buildAction + ").");
    }
  }

  static class InvalidBuildActionException extends IllegalArgumentException
  {

    private static final long serialVersionUID = 6635006296438188082L;

    InvalidBuildActionException(String message)
    {
      super(message);
    }
  }
}
