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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class Options implements IOptions
{

  enum ManagedOption
  {
    PROJECT(false, false), CONFIGURATION(false, false), SDK(false, false), TARGET(false, false), SHOWBUILDSETTINGS(
          "showBuildSettings", false, true),
    DRY_RUN("dry-run", false, true), SHOWSDKS(false, true), VERSION(false, true), LIST(false, true), USAGE(false, true), HELP(
          false, true), LICENSE(false, true);

    static ManagedOption forName(String name)
    {
      for (ManagedOption value : values()) {
        if (value.name().equals(name)) {
          return value;
        }
      }
      return null;
    }

    private String name;
    private final boolean required;
    private final boolean emptyValue;

    ManagedOption(boolean required, boolean emptyValue)
    {
      this(null, required, emptyValue);
    }

    ManagedOption(String name, boolean required, boolean emptyValue)
    {
      this.name = name;
      this.required = required;
      this.emptyValue = emptyValue;
    }

    boolean isRequired()
    {
      return required;
    }

    String getOptionName()
    {
      return name == null ? name().toLowerCase() : name;
    }

    boolean hasEmptyValue()
    {
      return emptyValue;
    }
  }

  private final Map<String, String> userOptions, managedOptions;

  Options(Map<String, String> userOptions, Map<String, String> managedOptions)
  {

    if (userOptions == null)
      this.userOptions = Collections.emptyMap();
    else
      this.userOptions = Collections.unmodifiableMap(new HashMap<String, String>(userOptions));

    if (managedOptions == null)
      this.managedOptions = Collections.emptyMap();
    else
      this.managedOptions = Collections.unmodifiableMap(new HashMap<String, String>(managedOptions));

    validateManagedOptions(this.managedOptions);

    validateUserOptions(this.userOptions);

    if(null == this.userOptions.get("scheme") && this.managedOptions.get(ManagedOption.PROJECT.getOptionName() )== null){
	throw new IllegalOptionException(ManagedOption.PROJECT,"managed option \"project\" or user option \"scheme\" is not available");
    }
  }

  public Map<String, String> getUserOptions()
  {
    return userOptions;
  }

  public Map<String, String> getManagedOptions()
  {
    return managedOptions;
  }

  /* (non-Javadoc)
   * @see com.sap.prd.mobile.ios.mios.IOptions#getAllOptions()
   */
  @Override
  public Map<String, String> getAllOptions()
  {
    final Map<String, String> result = new HashMap<String, String>();

    result.putAll(getUserOptions());
    result.putAll(getManagedOptions());

    return result;
  }

  /**
   * @param userOptions
   *          to be validated.
   * @return the passed in userOptions if validation passed without exception
   * @throws IllegalArgumentException
   *           if the userOptions contain a key of an XCode option that is managed by the plugin.
   */
  private final static Map<String, String> validateUserOptions(Map<String, String> userOptions)
  {

    for (ManagedOption option : ManagedOption.values()) {
      if (userOptions.keySet().contains(option.getOptionName()))
        throw new IllegalOptionException(option, "XCode Option '" + option.getOptionName()
              + "' is managed by the plugin and cannot be modified by the user.");
    }

    return userOptions;
  }

  private final static Map<String, String> validateManagedOptions(Map<String, String> managedOptions)
  {

    for (ManagedOption option : ManagedOption.values()) {

      if (option.isRequired() && !managedOptions.containsKey(option.getOptionName()))
        throw new IllegalOptionException(option, "Required option '" + option.getOptionName()
              + "' was not available inside the managed options.");

      if (!managedOptions.containsKey(option.getOptionName()))
        continue;

      final String value = managedOptions.get(option.getOptionName());

      if (!option.hasEmptyValue() && (value == null || value.trim().isEmpty()))
        throw new IllegalOptionException(option, "Invalid option: " + option.getOptionName()
              + " must be provided with a value.");
      if (option.hasEmptyValue() && (value != null && value.trim().isEmpty()))
        throw new IllegalOptionException(option, "Invalid option: " + option.getOptionName()
              + " must not be provided with a value.");

    }

    for (String key : managedOptions.keySet()) {
      if (ManagedOption.forName(key.toUpperCase()) == null)
        throw new IllegalArgumentException("Option '" + key
              + "' is not managed by the plugin. This option must not be provided as managed option.");
    }

    return managedOptions;
  }

  @Override
  public String toString()
  {
    final String ls = System.getProperty("line.separator");
    StringBuffer buffer = new StringBuffer();
    for (Map.Entry<String, String> entry : getAllOptions().entrySet()) {
      buffer.append(" -").append(entry.getKey()).append(" ").append(entry.getValue() == null ? "" : entry.getValue())
        .append(ls);
    }
    return buffer.toString();
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((managedOptions == null) ? 0 : managedOptions.hashCode());
    result = prime * result + ((userOptions == null) ? 0 : userOptions.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Options other = (Options) obj;
    if (managedOptions == null) {
      if (other.managedOptions != null) return false;
    }
    else if (!managedOptions.equals(other.managedOptions)) return false;
    if (userOptions == null) {
      if (other.userOptions != null) return false;
    }
    else if (!userOptions.equals(other.userOptions)) return false;
    return true;
  }

  static class IllegalOptionException extends IllegalArgumentException
  {

    private static final long serialVersionUID = -3298815948503432790L;

    private ManagedOption violated;

    IllegalOptionException(ManagedOption vialated, String message)
    {
      super(message);
      this.violated = vialated;
    }

    ManagedOption getViolated()
    {
      return violated;
    }
  }
}
