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

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

public class CommandLineBuilderTest
{
  private static File projectDirectory;

  @BeforeClass
  public static void setup()
  {
    projectDirectory = new File(new File(".").getAbsoluteFile(), "src/test/projects/MyLibrary");
  }

  private String appendStrings(String[] stringArray)
  {
    StringBuffer buffer = new StringBuffer();
    for (String string : stringArray) {
      buffer.append(string).append(" ");
    }
    return buffer.toString();
  }

  private void expect(XCodeContext context, String... expected)
  {

    CommandLineBuilder commandLineBuilder = new CommandLineBuilder(context);
    String[] actual = commandLineBuilder.createBuildCall();
    assertArrayEquals("\r\nExpected: " + appendStrings(expected) + "\r\nActual:   " + appendStrings(actual), expected,
          actual);
  }

  @Test
  public void testCommandlineBuilderStraightForward() throws Exception
  {
    Map<String, String> managedOptions = new HashMap<String, String>();
    managedOptions.put(Options.ManagedOption.CONFIGURATION.getOptionName(), "Release");
    managedOptions.put(Options.ManagedOption.SDK.getOptionName(), "mysdk");
    managedOptions.put(Options.ManagedOption.PROJECT.getOptionName(), "MyLib.xcodeproj");

    Options options = new Options(null, managedOptions);

    XCodeContext context = new XCodeContext(Arrays.asList("clean", "build"), projectDirectory, System.out, null,
          options);
    expect(context, "xcodebuild", "-project", "MyLib.xcodeproj", "-sdk",
          "mysdk", "-configuration", "Release", "clean", "build", "OBJROOT=build", "SHARED_PRECOMPS_DIR=build", "DSTROOT=build");
  }

  @Test
  public void testCommandlineBuilderStraightForwardSettings() throws Exception
  {
    Map<String, String> managedOptions = new HashMap<String, String>();
    managedOptions.put(Options.ManagedOption.CONFIGURATION.getOptionName(), "Release");
    managedOptions.put(Options.ManagedOption.SDK.getOptionName(), "mysdk");
    managedOptions.put(Options.ManagedOption.PROJECT.getOptionName(), "MyLib.xcodeproj");

    Options options = new Options(null, managedOptions);

    Map<String, String> userSettings = new LinkedHashMap<String, String>();
    userSettings.put("VALID_ARCHS", "i386");
    userSettings.put("CONFIGURATION_BUILD_DIR", "/Users/me/projects/myapp/target/xcode/src/main/xcode/build");
    Settings settings = new Settings(userSettings, null);

    XCodeContext context = new XCodeContext(Arrays.asList("clean", "build"), projectDirectory, System.out, settings,
          options);
    expect(context, "xcodebuild", "-project", "MyLib.xcodeproj", "-sdk",
          "mysdk", "-configuration", "Release", "clean", "build", "OBJROOT=build", "DSTROOT=build",
          "CONFIGURATION_BUILD_DIR=/Users/me/projects/myapp/target/xcode/src/main/xcode/build",
          "SHARED_PRECOMPS_DIR=build", "VALID_ARCHS=i386");
  }

  @Test
  public void testCommandlineBuilderStraightForwardOptions() throws Exception
  {

    Map<String, String> managedOptions = new HashMap<String, String>();
    managedOptions.put(Options.ManagedOption.CONFIGURATION.getOptionName(), "Release");
    managedOptions.put(Options.ManagedOption.SDK.getOptionName(), "mysdk");
    managedOptions.put(Options.ManagedOption.PROJECT.getOptionName(), "MyLib.xcodeproj");

    Map<String, String> userOptions = new LinkedHashMap<String, String>();
    userOptions.put("arch", "i386");

    Options options = new Options(userOptions, managedOptions);

    XCodeContext context = new XCodeContext(Arrays.asList("clean", "build"), projectDirectory, System.out, null,
          options);
    expect(context, "xcodebuild", "-project", "MyLib.xcodeproj", "-arch", "i386", "-sdk",
          "mysdk", "-configuration", "Release", "clean", "build", "OBJROOT=build", "SHARED_PRECOMPS_DIR=build","DSTROOT=build"
          );
  }

  @Test
  public void testCodeSignIdentity() throws Exception
  {
    Map<String, String> userSettings = null, managedSettings = new HashMap<String, String>();
    managedSettings.put("CODE_SIGN_IDENTITY", "MyCodeSignIdentity");
    Settings settings = new Settings(userSettings, managedSettings);

    Map<String, String> managedOptions = new HashMap<String, String>();
    managedOptions.put(Options.ManagedOption.CONFIGURATION.getOptionName(), "Release");
    managedOptions.put(Options.ManagedOption.SDK.getOptionName(), "mysdk");
    managedOptions.put(Options.ManagedOption.PROJECT.getOptionName(), "MyLib.xcodeproj");
    Options options = new Options(null, managedOptions);

    XCodeContext context = new XCodeContext(Arrays.asList("clean", "build"), projectDirectory, System.out, settings,
          options);
    CommandLineBuilder commandLineBuilder = new CommandLineBuilder(context);
    assertTrue(Arrays.asList(commandLineBuilder.createBuildCall()).contains("CODE_SIGN_IDENTITY=MyCodeSignIdentity"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCodeSignIdentityIsEmpty() throws Exception
  {
    Map<String, String> managedOptions = new HashMap<String, String>();
    managedOptions.put(Options.ManagedOption.CONFIGURATION.getOptionName(), "Release");
    managedOptions.put(Options.ManagedOption.SDK.getOptionName(), "mysdk");
    managedOptions.put(Options.ManagedOption.PROJECT.getOptionName(), "MyLib.xcodeproj");
    Options options = new Options(null, managedOptions);

    HashMap<String, String> managedSettings = new HashMap<String, String>();
    managedSettings.put(Settings.ManagedSetting.CODE_SIGN_IDENTITY.name(), "");
    Settings settings = new Settings(null, managedSettings);
    XCodeContext context = new XCodeContext(Arrays.asList("clean", "build"), null, System.out, settings, options);
    new CommandLineBuilder(context);
  }

  @Test
  public void testProvisioningProfile() throws Exception
  {
    Map<String, String> userSettings = null, managedSettings = new HashMap<String, String>();
    managedSettings.put("PROVISIONING_PROFILE", "MyProvisioningProfile");
    Settings settings = new Settings(userSettings, managedSettings);

    Map<String, String> managedOptions = new HashMap<String, String>();
    managedOptions.put(Options.ManagedOption.CONFIGURATION.getOptionName(), "Release");
    managedOptions.put(Options.ManagedOption.SDK.getOptionName(), "mysdk");
    managedOptions.put(Options.ManagedOption.PROJECT.getOptionName(), "MyLib.xcodeproj");
    Options options = new Options(null, managedOptions);

    XCodeContext context = new XCodeContext(Arrays.asList("clean", "build"), projectDirectory, System.out, settings,
          options);
    CommandLineBuilder commandLineBuilder = new CommandLineBuilder(context);
    assertTrue(Arrays.asList(commandLineBuilder.createBuildCall()).contains(
          "PROVISIONING_PROFILE=MyProvisioningProfile"));
  }

  @Test
  public void testProvisioningProfileIsNull() throws Exception
  {
    Map<String, String> managedOptions = new HashMap<String, String>();
    managedOptions.put(Options.ManagedOption.CONFIGURATION.getOptionName(), "Release");
    managedOptions.put(Options.ManagedOption.SDK.getOptionName(), "mysdk");
    managedOptions.put(Options.ManagedOption.PROJECT.getOptionName(), "MyLib.xcodeproj");
    Options options = new Options(null, managedOptions);

    XCodeContext context = new XCodeContext(Arrays.asList("clean", "build"), projectDirectory, System.out, null,
          options);
    CommandLineBuilder commandLineBuilder = new CommandLineBuilder(context);
    for (String param : commandLineBuilder.createBuildCall()) {
      assertFalse("The command line must not contain a parameter 'PROVISIONING_PROFILE='",
            param.contains("PROVISIONING_PROFILE="));
    }
  }

  @Test
  public void testLibraryCommandline() throws Exception
  {
    //
    // Should be similiar to
    //xcodebuild -project $PROJECT_NAME.xcodeproj -arch i386 -target $PROJECT_NAME -configuration Debug -sdk $SDK clean build VALID_ARCHS="i386" CONFIGURATION_BUILD_DIR="$PROJECT_PATH/build"
    //
    // PROJECT_NAME, PROJECT_PATH, SDK are environment variables in the shell and are replaced by concrete values.
    //
    Map<String, String> managedOptions = new HashMap<String, String>();
    managedOptions.put(Options.ManagedOption.CONFIGURATION.getOptionName(), "Debug");
    managedOptions.put(Options.ManagedOption.SDK.getOptionName(), "iphoneos");
    managedOptions.put(Options.ManagedOption.PROJECT.getOptionName(), "MyLib.xcodeproj");
    managedOptions.put(Options.ManagedOption.TARGET.getOptionName(), "MyLib");

    Map<String, String> userOptions = new HashMap<String, String>();
    userOptions.put("arch", "i386");

    Options options = new Options(userOptions, managedOptions);
    HashMap<String, String> userSettings = new HashMap<String, String>();
    userSettings.put("VALID_ARCHS", "i386");
    userSettings.put("CONFIGURATION_BUILD_DIR", "MyLib/build");
    Settings settings = new Settings(userSettings, null);
    XCodeContext context = new XCodeContext(Arrays.asList("clean", "build"), projectDirectory, System.out, settings,
          options);
    expect(context, "xcodebuild", "-project", "MyLib.xcodeproj", "-arch", "i386", "-target", "MyLib", "-sdk",
          "iphoneos", "-configuration", "Debug", "clean", "build", "OBJROOT=build", "DSTROOT=build",
          "CONFIGURATION_BUILD_DIR=MyLib/build", "SHARED_PRECOMPS_DIR=build", "VALID_ARCHS=i386");
  }
}
