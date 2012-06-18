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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;

public class XCodeTest
{
  protected void ensureCleanProjectDirectoryAndFilterPom(final File projectDirectory) throws Exception
  {
    File source = new File(new File(".").getAbsoluteFile(), "src/test/projects");

    MacFileUtil.deleteDirectory(projectDirectory);
    FileUtils.copyDirectory(source, projectDirectory);
    filterPoms(projectDirectory);
  }

  private static void filterPoms(File projectDirectory) throws IOException
  {
    Set<File> pomFiles = new HashSet<File>();
    getPomFiles(projectDirectory, pomFiles);

    for (File pomFile : pomFiles) {
      String pom = FileUtils.readFileToString(pomFile);
      pom = pom.replaceAll("\\$\\{xcode.maven.plugin.version\\}", getMavenXcodePluginVersion());
      FileUtils.writeStringToFile(pomFile, pom, "UTF-8");
    }
  }

  private static void getPomFiles(File root, final Set<File> pomFiles)
  {
    if (root.isFile())
      return;

    pomFiles.addAll(Arrays.asList(root.listFiles(new FileFilter() {

      @Override
      public boolean accept(File f)
      {
        // here we have the implicit assumtion that a pom file is named "pom.xml".
        // In case we introduce any test with a diffent name for a pom file we have
        // to revisit that.
        return f.isFile() && f.getName().equals("pom.xml");
      }

    })));

    for (File f : Arrays.asList(root.listFiles(new FileFilter() {

      @Override
      public boolean accept(File f)
      {
        return f.isDirectory();
      }
    })))
      getPomFiles(f, pomFiles);
  }

  private static String getMavenXcodePluginVersion() throws IOException
  {
    Properties properties = new Properties();
    properties.load(XCodeManagerTest.class.getResourceAsStream("/misc/project.properties"));
    final String xcodePluginVersion = properties.getProperty("xcode-plugin-version");

    if (xcodePluginVersion.equals("${project.version}"))
      throw new IllegalStateException(
            "Variable ${project.version} was not replaced. May be running \"mvn clean install\" beforehand might solve this issue.");
    return xcodePluginVersion;
  }

}
