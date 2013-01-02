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

  @Test
  public void testCommandlineBuilderStraightForward() throws Exception
  {
    CommandLineBuilder commandLineBuilder = new CommandLineBuilder("Release", "mysdk", new XCodeContext("MyLib", Arrays.asList("clean", "build"), projectDirectory, System.out));
    assertArrayEquals(new String[] { "xcodebuild", "-project", "MyLib.xcodeproj", "-configuration", "Release", "-sdk",
        "mysdk", "DSTROOT=build", "SYMROOT=build", "SHARED_PRECOMPS_DIR=build", "OBJROOT=build", "clean", "build" }, commandLineBuilder.createBuildCall());
  }

  @Test
  public void testCommandlineBuilderTestCommand() throws Exception
  {
	  CommandLineBuilder commandLineBuilder = new CommandLineBuilder("Release", "mysdk", new XCodeContext("MyLib", Arrays.asList("clean", "build"), projectDirectory, System.out));
	  assertArrayEquals(new String[] { "xcodebuild", "-project", "MyLib.xcodeproj", "-configuration", "Release", "-sdk",
			  "mysdk", "DSTROOT=build", "SYMROOT=build", "SHARED_PRECOMPS_DIR=build", "OBJROOT=build", "build", "TEST_AFTER_BUILD=YES" }, commandLineBuilder.createTestCall());
  }
  
  @Test
  public void testCodeSignIdentity() throws Exception
  {
    XCodeContext context = new XCodeContext("MyLib", Arrays.asList("clean", "build"), projectDirectory, System.out, "MyCodeSignIdentity", null, null);
    CommandLineBuilder commandLineBuilder = new CommandLineBuilder("Release", "mysdk", context);
    assertTrue(Arrays.asList(commandLineBuilder.createBuildCall()).contains("CODE_SIGN_IDENTITY=MyCodeSignIdentity"));
  }

  @Test
  public void testCodeSignIdentityIsNull() throws Exception
  {
    XCodeContext context = new XCodeContext("MyLib", Arrays.asList("clean", "build"), projectDirectory, System.out);
    CommandLineBuilder commandLineBuilder = new CommandLineBuilder("Release", "mysdk", context);

    for (String param : commandLineBuilder.createBuildCall()) {
      assertFalse("The command line must not contain a parameter 'CODE_SIGN_IDENTITY='",
            param.contains("CODE_SIGN_IDENTITY="));
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCodeSignIdentityIsEmpty() throws Exception
  {
    XCodeContext context = new XCodeContext("MyLib", Arrays.asList("clean", "build"), null, System.out, "", null, null);
    new CommandLineBuilder("Release", "mysdk", context);
  }

  
  @Test
  public void testProvisioningProfile() throws Exception
  {
    XCodeContext context = new XCodeContext("MyLib", Arrays.asList("clean", "build"), projectDirectory, System.out, null, "MyProvisioningProfile", null);
    CommandLineBuilder commandLineBuilder = new CommandLineBuilder("Release", "mysdk", context);
    assertTrue(Arrays.asList(commandLineBuilder.createBuildCall()).contains("PROVISIONING_PROFILE=MyProvisioningProfile"));
  }

  @Test
  public void testProvisioningProfileIsNull() throws Exception
  {
    XCodeContext context = new XCodeContext("MyLib", Arrays.asList("clean", "build"), projectDirectory, System.out, null, null, null);
    CommandLineBuilder commandLineBuilder = new CommandLineBuilder("Release", "mysdk", context);
    for (String param : commandLineBuilder.createBuildCall()) {
      assertFalse("The command line must not contain a parameter 'PROVISIONING_PROFILE='",
            param.contains("PROVISIONING_PROFILE="));
    }
  }
}
