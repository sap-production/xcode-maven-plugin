/*
 * #%L
 * it-xcode-maven-plugin
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

import static java.util.Arrays.asList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.it.Verifier;

public class XCodeTestParameters
{
  public final ProjectModifier modifier;
  public final Map<String, String> pomReplacements;
  public final List<String> additionalCommandLineOptions;
  public final List<String> targets;
  public final File projectDirectory;
  public final String testName;
  public final Verifier _verifier;
  public final Map<String, String> additionalSystemProperties;

  public XCodeTestParameters(final String testName, final File projectDirectory, String target,
        List<String> additionalCommandLineOptions, Map<String, String> pomReplacements, ProjectModifier modifier)
  {
    this(null, testName, projectDirectory, Arrays.asList(new String[] { target }), additionalCommandLineOptions,
          new HashMap<String, String>(), pomReplacements, modifier);
  }

  public XCodeTestParameters(final String testName, final File projectDirectory, List<String> targets,
        List<String> additionalCommandLineOptions, Map<String, String> pomReplacements, ProjectModifier modifier)
  {
    this(null, testName, projectDirectory, targets, additionalCommandLineOptions, new HashMap<String, String>(),
          pomReplacements, modifier);
  }

  public XCodeTestParameters(final Verifier _verifier, final String testName, final File projectDirectory,
        List<String> targets, List<String> additionalCommandLineOptions,
        Map<String, String> additionalSystemProperties, Map<String, String> pomReplacements, ProjectModifier modifier)
  {
    this.testName = testName;
    this.projectDirectory = projectDirectory;
    this.targets = targets == null ? Collections.<String> emptyList() :
          Collections.unmodifiableList(new ArrayList<String>(targets));
    this.additionalCommandLineOptions = additionalCommandLineOptions == null ? Collections.<String> emptyList() :
          Collections.unmodifiableList(new ArrayList<String>(additionalCommandLineOptions));
    this.pomReplacements = pomReplacements == null ? Collections.<String, String> emptyMap() :
          Collections.unmodifiableMap(new HashMap<String, String>(pomReplacements));
    this.modifier = modifier;
    this._verifier = _verifier;
    this.additionalSystemProperties = additionalSystemProperties == null ? Collections.<String, String> emptyMap() :
          Collections.unmodifiableMap(new HashMap<String, String>(additionalSystemProperties));
  }

  @Override
  public String toString()
  {
    return String
      .format(
            "XCodeTestParameters [modifier=%s, pomReplacements=%s, additionalCommandLineOptions=%s, targets=%s, projectDirectory=%s, testName=%s, _verifier=%s, additionalSystemProperties=%s]",
            modifier, pomReplacements, additionalCommandLineOptions, targets, projectDirectory, testName, _verifier,
            additionalSystemProperties);
  }

  public static class Builder
  {

    private ProjectModifier modifier;
    private Map<String, String> pomReplacements = new HashMap<String, String>();
    private List<String> additionalCommandLineOptions = new ArrayList<String>();
    private List<String> targets = new ArrayList<String>();
    private File projectDirectory;
    private String testName;
    private Verifier verifier = null;
    private Map<String, String> additionalSystemProperties = new HashMap<String, String>();

    public XCodeTestParameters create()
    {
      return new XCodeTestParameters(verifier, testName, projectDirectory, targets, additionalCommandLineOptions,
            additionalSystemProperties, pomReplacements, modifier);
    }

    public Builder setModifier(ProjectModifier modifier)
    {
      this.modifier = modifier;
      return this;
    }

    public Builder addPomReplacements(Map<String, String> pomReplacements)
    {
      this.pomReplacements.putAll(pomReplacements);
      return this;
    }

    public Builder addPomReplacements(String key, String value)
    {
      this.pomReplacements.put(key, value);
      return this;
    }

    public Builder addAdditionalCommandLineOptions(List<String> additionalCommandLineOptions)
    {
      this.additionalCommandLineOptions.addAll(additionalCommandLineOptions);
      return this;
    }

    public Builder addAdditionalCommandLineOptions(String... additionalCommandLineOptions)
    {
      this.additionalCommandLineOptions.addAll(asList(additionalCommandLineOptions));
      return this;
    }

    public Builder addTargets(String... targets)
    {
      this.targets.addAll(asList(targets));
      return this;
    }

    public Builder setProjectDirectory(File projectDirectory)
    {
      this.projectDirectory = projectDirectory;
      return this;
    }

    public Builder setTestName(String testName)
    {
      this.testName = testName;
      return this;
    }

    public Builder set_verifier(Verifier _verifier)
    {
      this.verifier = _verifier;
      return this;
    }

    public Builder addAdditionalSystemProperties(Map<String, String> additionalSystemProperties)
    {
      this.additionalSystemProperties.putAll(additionalSystemProperties);
      return this;
    }

    public Builder addAdditionalSystemProperties(String key, String value)
    {
      this.additionalSystemProperties.put(key, value);
      return this;
    }

  }

}