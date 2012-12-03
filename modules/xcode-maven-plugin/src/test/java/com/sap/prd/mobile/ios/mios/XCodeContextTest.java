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
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class XCodeContextTest
{

  private static File projectDirectory;


  @BeforeClass
  public static void setup()
  {
    projectDirectory = new File(new File(".").getAbsoluteFile(), "src/test/projects/MyLibrary");
  }

  @Test
  public void testStraightForward()
  {

    final String projectName = "MyLibrary.xcodeproj";

    Map<String, String> managedOptions = new HashMap<String, String>();
    managedOptions.put(Options.ManagedOption.CONFIGURATION.getOptionName(), "Release");
    managedOptions.put(Options.ManagedOption.SDK.getOptionName(), "mysdk");
    managedOptions.put(Options.ManagedOption.PROJECT.getOptionName(), projectName);
    Options options = new Options(null, managedOptions);

    HashMap<String, String> managedSettings = new HashMap<String, String>();
    managedSettings.put(Settings.ManagedSetting.CODE_SIGN_IDENTITY.name(), "MyCodeSignIdentity");
    managedSettings.put(Settings.ManagedSetting.PROVISIONING_PROFILE.name(), "MyProvisioningProfile");
    Settings settings = new Settings(null, managedSettings);

    final XCodeContext xCodeContext = new XCodeContext(Arrays.asList("clean",
          "build"), projectDirectory, System.out, settings, options);

    
    assertEquals(projectName, xCodeContext.getProjectName());
    assertArrayEquals(new String[] { "clean", "build" }, xCodeContext.getBuildActions().toArray());
    assertEquals("MyCodeSignIdentity", xCodeContext.getCodeSignIdentity());
    assertEquals("MyProvisioningProfile", xCodeContext.getProvisioningProfile());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyBuildActions()
  {
    new XCodeContext(new ArrayList<String>(), projectDirectory, System.out, null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuildActionWithEmptyEntry()
  {
    new XCodeContext(Arrays.asList("clean", "", "build"), projectDirectory, System.out, null, null);
  }

  @Test(expected = XCodeContext.InvalidBuildActionException.class)
  public void TestBuildActionEntryWithBlank()
  {
    new XCodeContext(Arrays.asList("clean", "build foo"), projectDirectory, System.out, null, null);
  }

  @Test(expected = XCodeContext.InvalidBuildActionException.class)
  public void testBuildActionWithNullElement()
  {
    new XCodeContext(Arrays.asList("clean", null, "build"), projectDirectory, System.out, null, null);
  }

  @Test(expected = Options.IllegalOptionException.class)
  public void testXCodeContextWithEmptyProjectName()
  {
    HashMap<String, String> managedOptions = new HashMap<String, String>();
    managedOptions.put(Options.ManagedOption.PROJECT.getOptionName(), "");
    Options options = new Options(null, managedOptions);
    try {
      new XCodeContext(Arrays.asList("clean", "build"), projectDirectory, System.out, null, options);
    } catch(Options.IllegalOptionException ex) {

      assertEquals(Options.ManagedOption.PROJECT, ex.getViolated());
      throw ex;
    }
  }

  @Test(expected = Options.IllegalOptionException.class)
  public void testXCodeContextWithoutProjectName()
  {
    try {
      new XCodeContext(Arrays.asList("clean", "build"), projectDirectory, System.out, null, null);
    } catch(Options.IllegalOptionException ex) {
      assertEquals(Options.ManagedOption.PROJECT, ex.getViolated());
      throw ex;
    }
   }


  @Test(expected = IllegalArgumentException.class)
  public void testXCodeContextWithoutPrintStream()
  {
    new XCodeContext(Arrays.asList("clean", "build"), projectDirectory, null, null, null);
  }

  @Test
  public void testCodeSignIdentityIsNull() throws Exception
  {
    Map<String, String> managedOptions = new HashMap<String, String>();
    managedOptions.put(Options.ManagedOption.CONFIGURATION.getOptionName(), "Release");
    managedOptions.put(Options.ManagedOption.SDK.getOptionName(), "mysdk");
    managedOptions.put(Options.ManagedOption.PROJECT.getOptionName(), "MyLibrary.xcodeproj");
    Options options = new Options(null, managedOptions);

    final XCodeContext xCodeContext = new XCodeContext(Arrays.asList("clean", "build"), projectDirectory,System.out, null, options);
    Assert.assertNull(xCodeContext.getCodeSignIdentity());
  }

  @Test
  public void testCodeSignIdentityIsEmpty() throws Exception
  {
    Map<String, String> managedOptions = new HashMap<String, String>();
    managedOptions.put(Options.ManagedOption.CONFIGURATION.getOptionName(), "Release");
    managedOptions.put(Options.ManagedOption.SDK.getOptionName(), "mysdk");
    managedOptions.put(Options.ManagedOption.PROJECT.getOptionName(), "MyLibrary.xcodeproj");
    Options options = new Options(null, managedOptions);

    HashMap<String, String> managedSettings = new HashMap<String, String>();
    managedSettings.put(Settings.ManagedSetting.CODE_SIGN_IDENTITY.name(), "");
    Settings settings = new Settings(null, managedSettings);
    
    XCodeContext context = new XCodeContext(Arrays.asList("clean", "build"), projectDirectory, System.out, settings, options);
    
    assertEquals("", context.getCodeSignIdentity());
    
  }
  
  @Test
  public void testProvisioningProfileIsNull() throws Exception
  {
    Map<String, String> managedOptions = new HashMap<String, String>();
    managedOptions.put(Options.ManagedOption.CONFIGURATION.getOptionName(), "Release");
    managedOptions.put(Options.ManagedOption.SDK.getOptionName(), "mysdk");
    managedOptions.put(Options.ManagedOption.PROJECT.getOptionName(), "MyLibrary.xcodeproj");
    Options options = new Options(null, managedOptions);

    final XCodeContext xCodeContext = new XCodeContext(Arrays.asList("clean", "build"), projectDirectory, System.out, null, options);
    Assert.assertNull(xCodeContext.getProvisioningProfile());
  }
  
  @Test
  public void testIsImmutable() throws Exception
  {
    Map<String, String> userOptions = new HashMap<String, String>();
    Map<String, String> managedOptions = new HashMap<String, String>();
    managedOptions.put(Options.ManagedOption.CONFIGURATION.getOptionName(), "Release");
    managedOptions.put(Options.ManagedOption.SDK.getOptionName(), "mysdk");
    managedOptions.put(Options.ManagedOption.PROJECT.getOptionName(), "MyLibrary.xcodeproj");
    Options options = new Options(userOptions, managedOptions);

    HashMap<String, String> userSettings = new HashMap<String, String>();
    HashMap<String, String> managedSettings = new HashMap<String, String>();
    managedSettings.put(Settings.ManagedSetting.CODE_SIGN_IDENTITY.name(), "");
    Settings settings = new Settings(userSettings, managedSettings);
    
    List<String> buildActions = new ArrayList<String>(Arrays.asList("clean", "build"));

    File _projectDir = new File(projectDirectory.getParentFile().getCanonicalFile(), "MyLib2");
    _projectDir.deleteOnExit();

    FileUtils.copyDirectory(projectDirectory, _projectDir);

    final XCodeContext xCodeContext = new XCodeContext(buildActions, _projectDir, System.out, settings, options);

    int hashCode = xCodeContext.hashCode();

    managedOptions.put("managedOptionAddedAfterwards", "xxxx");
    managedSettings.put("managedSettigAddedAfterwards", "yyyy");

    userOptions.put("userOptionAddedAfterwards", "1111");
    userSettings.put("userSettigAddedAfterwards", "2222");

    buildActions.add("addedAfterwards");

    _projectDir.renameTo(new File(projectDirectory.getParent(), "MyLib3"));

    assertEquals("Context is not immutable.", hashCode, xCodeContext.hashCode());
  }
}
