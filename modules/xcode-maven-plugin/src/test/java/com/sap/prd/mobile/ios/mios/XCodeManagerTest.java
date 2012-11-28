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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.logging.Log;
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

  @BeforeClass
  public static void setupProjectDirectory()
  {

    projectDirectory = new File(new File(".").getAbsoluteFile(), "target/tests/"
          + XCodeManagerTest.class.getName());
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

    final XCodeContext context = new XCodeContext();
    context.setProjectName("MyLibrary");
    context.setBuildActions(Arrays.asList("clean", "build"));
    context.setProjectRootDirectory(new File(projectDirectory, "MyLibrary/src/xcode"));
    context.setOut(System.out);

    Log log = EasyMock.createMock(Log.class);
    MavenProject mavenProject = EasyMock.createMock(MavenProject.class);
    Build build = EasyMock.createMock(Build.class);

    EasyMock.expect(mavenProject.getBuild()).andStubReturn(build);
    EasyMock.expect(build.getDirectory()).andStubReturn("");

    EasyMock.expect(mavenProject.getCompileArtifacts()).andStubReturn(Arrays.asList((Artifact)null));

    EasyMock.replay(build, mavenProject);

    // The null values below does only work since we do not have any
    // dependency resolution. We build here a project without any
    // predecessor.

    new XCodeManager(log).callXcodeBuild(context, "Release", "iphoneos");
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

    Log log = EasyMock.createMock(Log.class);
    MavenProject mavenProject = EasyMock.createMock(MavenProject.class);
    Build build = EasyMock.createMock(Build.class);

    EasyMock.expect(mavenProject.getBuild()).andStubReturn(build);
    EasyMock.expect(build.getDirectory()).andStubReturn("");

    EasyMock.expect(mavenProject.getCompileArtifacts()).andStubReturn(Arrays.asList((Artifact)null));

    EasyMock.replay(build, mavenProject);

    final XCodeContext context = new XCodeContext();
    context.setProjectName("MyLibrary");
    context.setBuildActions(Arrays.asList("clean", "build"));
    context.setProjectRootDirectory(projectDirectory);
    context.setOut(System.out);

    new XCodeManager(log). callXcodeBuild(context, "NON-EXISTNG_CONFIGURATION", "iphoneos");

  }

  @Test(expected = IOException.class)
  public void damagedPrintStreamProvidedTest() throws Exception
  {

    Log log = EasyMock.createMock(Log.class);
    MavenProject mavenProject = EasyMock.createMock(MavenProject.class);
    Build build = EasyMock.createMock(Build.class);

    EasyMock.expect(mavenProject.getBuild()).andStubReturn(build);
    EasyMock.expect(build.getDirectory()).andStubReturn("");

    EasyMock.expect(mavenProject.getCompileArtifacts()).andStubReturn(Arrays.asList((Artifact)null));

    EasyMock.replay(build, mavenProject);

    
    final XCodeContext context = new XCodeContext();
    context.setProjectName("MyLibrary");
    context.setBuildActions(Arrays.asList("clean", "build"));
    context.setProjectRootDirectory(projectDirectory);
    context.setOut(new PrintStream(
          new ByteArrayOutputStream()) {

      @Override
      public void write(byte[] buf, int off, int len)
      {
        setError();
      }
    });
    new XCodeManager(log).callXcodeBuild(context, "Release", "iphoneos");

  }
}
