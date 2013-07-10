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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class XCodeManagerTest extends XCodeTest
{

  private static File projectDirectory;
  
  //
  // keep the reference to the logger. Otherwise the logger
  // might be garbage collected.
  //
  private static Logger logger;

  @BeforeClass
  public static void setup()
  {
    projectDirectory = new File(new File(".").getAbsoluteFile(), "target/tests/"
          + XCodeManagerTest.class.getName());
    logger = new XCodePluginLogger();
    LogManager.getLogManager().addLogger(logger);
  }

  @Before
  public void ensureCleanProjectDirectoryAndFilterPom() throws Exception
  {
    ensureCleanProjectDirectoryAndFilterPom(projectDirectory);
  }

  @After
  public void cleanupProjectDirectory() throws Exception
  {
    MacFileUtil.deleteDirectory(projectDirectory);
  }

  @Test
  public void straightForwardTestBuildLibWithoutPredecessors() throws Exception
  {
    Map<String, String> managedOptions = new HashMap<String, String>();
    managedOptions.put(Options.ManagedOption.PROJECT.getOptionName(), "MyLibrary.xcodeproj");
    managedOptions.put(Options.ManagedOption.CONFIGURATION.getOptionName(), "Release");
    managedOptions.put(Options.ManagedOption.SDK.getOptionName(), "iphoneos");
    Options options = new Options(null, managedOptions);
    final XCodeContext context = new XCodeContext(Arrays.asList("clean", "build"), new File(projectDirectory,
          "MyLibrary/src/xcode"), System.out, null, options);

    MavenProject mavenProject = EasyMock.createMock(MavenProject.class);
    Build build = EasyMock.createMock(Build.class);

    EasyMock.expect(mavenProject.getBuild()).andStubReturn(build);
    EasyMock.expect(build.getDirectory()).andStubReturn("");

    EasyMock.expect(mavenProject.getCompileArtifacts()).andStubReturn(Arrays.asList((Artifact) null));

    EasyMock.replay(build, mavenProject);

    // The null values below does only work since we do not have any
    // dependency resolution. We build here a project without any
    // predecessor.

    new XCodeManager().callXcodeBuild(context);
  }

  //
  // TODO How should we handle invalid configurations. We must be able
  // to detect this and to abort the build.
  //
  @Ignore
  // Default configuration is used when a invalid configuration is provided.
  @Test
  public void invalidConfigurationTest() throws Exception
  {
    MavenProject mavenProject = EasyMock.createMock(MavenProject.class);
    Build build = EasyMock.createMock(Build.class);

    EasyMock.expect(mavenProject.getBuild()).andStubReturn(build);
    EasyMock.expect(build.getDirectory()).andStubReturn("");

    EasyMock.expect(mavenProject.getCompileArtifacts()).andStubReturn(Arrays.asList((Artifact) null));

    EasyMock.replay(build, mavenProject);

    Map<String, String> managedOptions = new HashMap<String, String>();
    managedOptions.put(Options.ManagedOption.CONFIGURATION.getOptionName(), "NON-EXISTNG_CONFIGURATION");
    managedOptions.put(Options.ManagedOption.SDK.getOptionName(), "iphoneos");

    Options options = new Options(null, managedOptions);
    final XCodeContext context = new XCodeContext(Arrays.asList("clean", "build"), projectDirectory, System.out, null,
          options);
    new XCodeManager().callXcodeBuild(context);

  }

  @Test(expected = IOException.class)
  public void damagedPrintStreamProvidedTest() throws Exception
  {
    MavenProject mavenProject = EasyMock.createMock(MavenProject.class);
    Build build = EasyMock.createMock(Build.class);

    EasyMock.expect(mavenProject.getBuild()).andStubReturn(build);
    EasyMock.expect(build.getDirectory()).andStubReturn("");

    EasyMock.expect(mavenProject.getCompileArtifacts()).andStubReturn(Arrays.asList((Artifact) null));

    EasyMock.replay(build, mavenProject);

    Map<String, String> managedOptions = new HashMap<String, String>();
    managedOptions.put(Options.ManagedOption.CONFIGURATION.getOptionName(), "Release");
    managedOptions.put(Options.ManagedOption.SDK.getOptionName(), "iphoneos");
    managedOptions.put(Options.ManagedOption.PROJECT.getOptionName(), "MyLibrary.xcodeproj");

    Options options = new Options(null, managedOptions);
    final XCodeContext context = new XCodeContext(Arrays.asList("clean", "build"), projectDirectory, new PrintStream(
          new ByteArrayOutputStream()) {

      @Override
      public void write(byte[] buf, int off, int len)
      {
        setError();
      }
    }, null, options);
    new XCodeManager().callXcodeBuild(context);

  }
}
