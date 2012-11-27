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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class CommandLineBuilderTest
{

  private XCodeContext context;

  @Before
  public void before()
  {
    context = new XCodeContext();
    context.setProjectName("MyLib");
    context.setBuildActions(Arrays.asList("clean", "build"));
  }
  
    private String appendStrings(String[] stringArray) {
       StringBuilder builder = new StringBuilder();
       for (String string : stringArray) {
          builder.append(string).append(" ");
       }
       return builder.toString();
    }

    private void expect(String ... expected) {
        CommandLineBuilder commandLineBuilder = new CommandLineBuilder("Release", "mysdk", context);
        String[] actual = commandLineBuilder.createBuildCall();
        assertArrayEquals("\r\nExpected: "+appendStrings(expected)+"\r\nActual:   "+appendStrings(actual), expected, actual);
    }

  @Test
  public void testCommandlineBuilderStraightForward() throws Exception
  {
    expect("xcodebuild", "-project", "MyLib.xcodeproj", "-configuration", "Release", "-sdk",
            "mysdk", "DSTROOT=build", "SYMROOT=build", "SHARED_PRECOMPS_DIR=build", "OBJROOT=build", "clean", "build");
  }

    @Test
    public void testCommandlineBuilderStraightForwardSettings() throws Exception
    {
      Map<String, String> settings = new LinkedHashMap<String, String>();
      settings.put("VALID_ARCHS", "i386");
      settings.put("CONFIGURATION_BUILD_DIR", "/Users/me/projects/myapp/target/xcode/src/main/xcode/build");
      context.setSettings(settings);
      expect("xcodebuild", "-project", "MyLib.xcodeproj", "-configuration", "Release", "-sdk",
              "mysdk", "VALID_ARCHS=i386",
              "CONFIGURATION_BUILD_DIR=/Users/me/projects/myapp/target/xcode/src/main/xcode/build", "DSTROOT=build",
              "SYMROOT=build", "SHARED_PRECOMPS_DIR=build", "OBJROOT=build", "clean", "build");
      context.setSettings(null);
    }

    private void assertIllegalSettings(String name, String value) throws IllegalArgumentException {
        Map<String, String> settings = new LinkedHashMap<String, String>();
        settings.put(name, value);
        context.setSettings(settings);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCommandlineBuilderStraightForwardSetting_DSTROOT() {
        assertIllegalSettings(CommandLineBuilder.Settings.DSTROOT, "build");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCommandlineBuilderStraightForwardSetting_SYMROOT() {
        assertIllegalSettings(CommandLineBuilder.Settings.SYMROOT, "build");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCommandlineBuilderStraightForwardSetting_SHARED_PRECOMPS_DIR() {
        assertIllegalSettings(CommandLineBuilder.Settings.SHARED_PRECOMPS_DIR, "build");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCommandlineBuilderStraightForwardSetting_OBJROOT() {
        assertIllegalSettings(CommandLineBuilder.Settings.OBJROOT, "build");
    }

    @Test
    public void testCommandlineBuilderStraightForwardOptions() throws Exception
    {
      Map<String, String> options = new LinkedHashMap<String, String>();
      options.put("arch", "i386");
      context.setOptions(options);
      expect("xcodebuild", "-arch", "i386", "-project", "MyLib.xcodeproj", "-configuration", "Release", "-sdk",
              "mysdk", "DSTROOT=build", "SYMROOT=build", "SHARED_PRECOMPS_DIR=build", "OBJROOT=build", "clean", "build");
      context.setOptions(null);
    }

    private void assertIllegalOption(String name, String value) throws IllegalArgumentException {
        Map<String, String> options = new LinkedHashMap<String, String>();
        options.put(name, value);
        context.setOptions(options);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCommandlineBuilderStraightForwardOption_project() {
        assertIllegalOption(CommandLineBuilder.Options.PROJECT_NAME, "MyLib.xcodeproj");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCommandlineBuilderStraightForwardOption_configuration() {
        assertIllegalOption(CommandLineBuilder.Options.CONFIGURATION, "Release");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCommandlineBuilderStraightForwardOption_sdk() {
        assertIllegalOption(CommandLineBuilder.Options.SDK, "mysdk");
    }

  @Test
  public void testCodeSignIdentity() throws Exception
  {
    context.setCodeSignIdentity("MyCodeSignIdentity");
    CommandLineBuilder commandLineBuilder = new CommandLineBuilder("Release", "mysdk", context);
    assertTrue(Arrays.asList(commandLineBuilder.createBuildCall()).contains("CODE_SIGN_IDENTITY=MyCodeSignIdentity"));
  }

  @Test
  public void testCodeSignIdentityIsNull() throws Exception
  {
    context.setCodeSignIdentity(null);
    CommandLineBuilder commandLineBuilder = new CommandLineBuilder("Release", "mysdk", context);

    for (String param : commandLineBuilder.createBuildCall()) {
      assertFalse("The command line must not contain a parameter 'CODE_SIGN_IDENTITY='",
            param.contains("CODE_SIGN_IDENTITY="));
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCodeSignIdentityIsEmpty() throws Exception
  {
    context.setCodeSignIdentity("");
    new CommandLineBuilder("Release", "mysdk", context);
  }

  
  @Test
  public void testProvisioningProfile() throws Exception
  {
    context.setProvisioningProfile("MyProvisioningProfile");
    CommandLineBuilder commandLineBuilder = new CommandLineBuilder("Release", "mysdk", context);
    assertTrue(Arrays.asList(commandLineBuilder.createBuildCall()).contains("PROVISIONING_PROFILE=MyProvisioningProfile"));
  }

  @Test
  public void testProvisioningProfileIsNull() throws Exception
  {
    context.setProvisioningProfile(null);
    CommandLineBuilder commandLineBuilder = new CommandLineBuilder("Release", "mysdk", context);
    for (String param : commandLineBuilder.createBuildCall()) {
      assertFalse("The command line must not contain a parameter 'PROVISIONING_PROFILE='",
            param.contains("PROVISIONING_PROFILE="));
    }
  }
}
