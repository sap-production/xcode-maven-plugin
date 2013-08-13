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

import static com.sap.prd.mobile.ios.mios.versioninfo.Coordinates.ARTIFACT_ID;
import static com.sap.prd.mobile.ios.mios.versioninfo.Coordinates.GROUP_ID;
import static junit.framework.Assert.assertEquals;

import java.io.File;
import java.util.Properties;

import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.easymock.EasyMock;
import org.junit.Test;

public class FolderLayoutTest
{

  @Test
  public void testLibraryFolder() throws Exception
  {

    final File f = new File(".").getAbsoluteFile();
    assertEquals(new File(f, "libs/Release-iphoneos/groupId/artifactId"),
          (FolderLayout.getFolderForExtractedLibsWithGA(getProject(f), "Release", "iphoneos", GROUP_ID, ARTIFACT_ID)));
  }

  @Test
  public void testHeadersFolder() throws Exception
  {

    final File f = new File(".").getAbsoluteFile();
    assertEquals(new File(f, "headers/Release-iphoneos/groupId/artifactId"),
          FolderLayout
            .getFolderForExtractedHeadersWithGA(getProject(f), "Release", "iphoneos", GROUP_ID, ARTIFACT_ID));
  }

  @Test
  public void testDefaultSourceFolder()
  {
    final File f = new File(".").getAbsoluteFile();
    final MavenProject project = EasyMock.createMock(MavenProject.class);

    Properties props = new Properties();
    EasyMock.expect(project.getProperties()).andReturn(props);
    EasyMock.expect(project.getBasedir()).andReturn(f);
    EasyMock.replay(project);

    assertEquals(new File(f, "src/xcode"), FolderLayout.getSourceFolder(project));
  }

  @Test
  public void testModifiedSourceFolder()
  {
    final File f = new File(".").getAbsoluteFile();
    final MavenProject project = EasyMock.createMock(MavenProject.class);

    Properties props = new Properties();
    props.setProperty(XCodeDefaultConfigurationMojo.XCODE_SOURCE_DIRECTORY, "src/myxcode");
    EasyMock.expect(project.getProperties()).andReturn(props);
    EasyMock.expect(project.getBasedir()).andReturn(f);
    EasyMock.replay(project);

    assertEquals(new File(f, "src/myxcode"), FolderLayout.getSourceFolder(project));
  }

  private static MavenProject getProject(final File f)
  {

    final MavenProject project = EasyMock.createMock(MavenProject.class);
    final Build build = EasyMock.createMock(Build.class);

    EasyMock.expect(project.getBuild()).andReturn(build);
    EasyMock.expect(build.getDirectory()).andReturn(f.getAbsolutePath());
    EasyMock.replay(project, build);

    return project;
  }
}
