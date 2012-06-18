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
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

public class XCodeContextTest
{

  private static File projectDirectory;

  private final static Set<String> configurations = new HashSet<String>(Arrays.asList("Release"));
  private final static Set<String> sdks = new HashSet<String>(Arrays.asList("iphoneos"));

  @BeforeClass
  public static void setup()
  {
    projectDirectory = new File(new File("..").getAbsoluteFile(), "test-projects/straight-forward/MyLibrary");
  }

  @Test
  public void testStraightForward()
  {

    final String projectName = "MyLibrary";
    final Set<String> sdks = new HashSet<String>(Arrays.asList(new String[] {}));

    final XCodeContext xCodeContext = new XCodeContext(projectName, configurations, sdks, Arrays.asList("clean",
          "build"), projectDirectory, "MyCodeSignIdentity", System.out);
    xCodeContext.setProvisioningProfile("MyProvisioningProfile");

    assertEquals(configurations, xCodeContext.getConfigurations());
    assertEquals(projectName, xCodeContext.getProjectName());
    assertArrayEquals(new String[] { "clean", "build" }, xCodeContext.getBuildActions());
    assertEquals("MyCodeSignIdentity", xCodeContext.getCodeSignIdentity());
    assertEquals("MyProvisioningProfile", xCodeContext.getProvisioningProfile());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyBuildActions()
  {
    new XCodeContext("MyLibrary", new HashSet<String>(Arrays.asList(new String[] { "Release" })), new HashSet<String>(
          Arrays.asList("iphoneos")),
          new ArrayList<String>(), projectDirectory, null, System.out);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuildActionWithEmptyEntry()
  {
    new XCodeContext("MyLibrary", configurations, sdks, Arrays.asList(
          "clean", "", "build"), projectDirectory, null, System.out);
  }

  @Test(expected = IllegalArgumentException.class)
  public void TestBuildActionEntryWithBlank()
  {
    new XCodeContext("MyLibrary", configurations, sdks, Arrays.asList(
          "clean", "build foo"), projectDirectory, null, System.out);

  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuildActionWithNullElement()
  {
    new XCodeContext("MyLibrary", configurations, sdks, Arrays.asList(
          "clean", null, "build"), projectDirectory, null, System.out);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testXCodeContextWithEmptyProjectName()
  {
    new XCodeContext("", configurations, sdks, Arrays.asList("clean", "build"),
          projectDirectory, null, System.out);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testXCodeContextWithoutProjectName()
  {
    new XCodeContext(null, configurations, sdks, Arrays.asList("clean", "build"),
          projectDirectory, null, System.out);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testXCodeContextWithEmptyConfiguration()
  {
    new XCodeContext("MyLibrary", new HashSet<String>(Arrays.asList(new String[] {})), sdks, Arrays.asList("clean",
          "build"), projectDirectory, null, System.out);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testXCodeContextWithoutConfiguration()
  {
    new XCodeContext("MyLibrary", null, sdks, Arrays.asList("clean", "build"), projectDirectory,
          null, System.out);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testXCodeContextWithoutPrintStream()
  {
    new XCodeContext("MyLibrary", configurations, sdks, Arrays.asList("clean", "build"),
          projectDirectory, null, null);
  }

  @Test
  public void testCodeSignIdentityIsNull() throws Exception
  {
    XCodeContext xCodeContext = new XCodeContext("MyLibrary", configurations, sdks, Arrays.asList("clean", "build"),
          projectDirectory, null, System.out);

    Assert.assertNull(xCodeContext.getCodeSignIdentity());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCodeSignIdentityIsEmpty() throws Exception
  {
    new XCodeContext("MyLibrary", configurations, sdks, Arrays.asList("clean", "build"),
          projectDirectory, "", System.out);
  }
  
  @Test
  public void testProvisioningProfileIsNull() throws Exception
  {
    XCodeContext xCodeContext = new XCodeContext("MyLibrary", configurations, sdks, Arrays.asList("clean", "build"),
          projectDirectory, null, System.out);

    Assert.assertNull(xCodeContext.getProvisioningProfile());
  }
}
