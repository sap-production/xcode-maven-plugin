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

import org.junit.Test;

public class CommandLineBuilderTest
{

  @Test
  public void testCommandlineBuilderStraightForward() throws Exception
  {
    CommandLineBuilder commandLineBuilder = new CommandLineBuilder();
    commandLineBuilder.setProjectName("MyLib").setConfiguration("Release").setSdk("mysdk")
      .setBuildActions(Arrays.asList("clean", "build"));

    assertArrayEquals(new String[] { "xcodebuild", "-project", "MyLib.xcodeproj", "-configuration", "Release", "-sdk",
        "mysdk", "clean", "build" }, commandLineBuilder.createCommandline());
  }

  @Test
  public void testCodeSignIdentity() throws Exception
  {
    CommandLineBuilder commandLineBuilder = new CommandLineBuilder();
    commandLineBuilder.setProjectName("MyLib").setConfiguration("Release").setSdk("mysdk")
      .setBuildActions(Arrays.asList("clean", "build")).setCodeSignIdentity("MyCodeSignIdentity");

    assertTrue(Arrays.asList(commandLineBuilder.createCommandline()).contains("CODE_SIGN_IDENTITY=MyCodeSignIdentity"));
  }

  @Test
  public void testCodeSignIdentityIsNull() throws Exception
  {
    CommandLineBuilder commandLineBuilder = new CommandLineBuilder();
    commandLineBuilder.setProjectName("MyLib").setConfiguration("Release").setSdk("mysdk")
      .setBuildActions(Arrays.asList("clean", "build")).setCodeSignIdentity(null);

    for (String param : commandLineBuilder.createCommandline()) {
      assertFalse("The command line must not contain a parameter 'CODE_SIGN_IDENTITY='",
            param.contains("CODE_SIGN_IDENTITY="));
    }

  }

  @Test(expected = IllegalArgumentException.class)
  public void testCodeSignIdentityIsEmpty() throws Exception
  {
    CommandLineBuilder commandLineBuilder = new CommandLineBuilder();
    commandLineBuilder.setProjectName("MyLib").setConfiguration("Release").setSdk("mysdk")
      .setBuildActions(Arrays.asList("clean", "build")).setCodeSignIdentity("");

  }

  @Test(expected = IllegalStateException.class)
  public void testValueNotSet() throws Exception
  {
    CommandLineBuilder commandLineBuilder = new CommandLineBuilder();
    commandLineBuilder.setProjectName("MyLib").setConfiguration("Release")
      .setBuildActions(Arrays.asList("clean", "build"));
    commandLineBuilder.createCommandline();
  }
  
  
  @Test
  public void testProvisioningProfile() throws Exception
  {
    CommandLineBuilder commandLineBuilder = new CommandLineBuilder();
    commandLineBuilder.setProjectName("MyLib").setConfiguration("Release").setSdk("mysdk")
      .setBuildActions(Arrays.asList("clean", "build")).setProvisioningProfile("MyProvisioningProfile");

    assertTrue(Arrays.asList(commandLineBuilder.createCommandline()).contains("PROVISIONING_PROFILE=MyProvisioningProfile"));
  }

  @Test
  public void testProvisioningProfileIsNull() throws Exception
  {
    CommandLineBuilder commandLineBuilder = new CommandLineBuilder();
    commandLineBuilder.setProjectName("MyLib").setConfiguration("Release").setSdk("mysdk")
       .setBuildActions(Arrays.asList("clean", "build")).setCodeSignIdentity(null);


    for (String param : commandLineBuilder.createCommandline()) {
      assertFalse("The command line must not contain a parameter 'PROVISIONING_PROFILE='",
            param.contains("PROVISIONING_PROFILE="));
    }

  }
  
}
