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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * 
 * @goal update-version-in-pom
 * 
 */
public class UpdateVersionInPomMojo extends BuildContextAwareMojo
{

  /**
   * @parameter expression="${appendSnapshot}" default-value="true"
   * @since 1.6.1
   */
  private boolean appendSnapshot;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    try {

      String version = getXCodeVersion();

      if (version == null || version.isEmpty())
        throw new MojoExecutionException("No xcode version found. Could not update version in pom.");

      if (appendSnapshot)
        version += "-SNAPSHOT";

      final File pom = new File("pom.xml").getAbsoluteFile();

      if (!pom.canWrite())
        throw new MojoExecutionException("Pom file '" + pom + "' is readonly. Cannot update version in this file.");

      Model model = readPom(pom);

      String oldVersion = model.getVersion();
      
      if (oldVersion != null && oldVersion.equals(version))
      {
        getLog().info("! XCode version matches pom version. No update needed.");
        return;
      }
      model.setVersion(version);

      writePom(pom, model);

      getLog().info("! Version in pom file (" + pom + ") updated from '" + oldVersion + "' to " + version + ".");
    }
    catch (IOException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
    catch (XmlPullParserException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
    catch (XCodeException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  Model readPom(File pom) throws IOException, XmlPullParserException
  {

    Reader r = null;

    try {
      r = new FileReader(pom);

      Model model = new MavenXpp3Reader().read(r);
      r.close();
      return model;
    }
    finally {
      IOUtils.closeQuietly(r);
    }
  }

  private void writePom(File pom, Model model) throws IOException
  {
    Writer w = null;

    try {

      w = new FileWriter(pom);
      new MavenXpp3Writer().write(w, model);

    }
    finally {
      IOUtils.closeQuietly(w);
    }
  }

  private String getXCodeVersion() throws XCodeException
  {
    String version = null;

    for (String configuration : getConfigurations()) {
      for (String sdk : getSDKs()) {

        String _version = null;

        String infoPListFileName = EffectiveBuildSettings.getBuildSetting(getXCodeContext(XCodeContext.SourceCodeLocation.WORKING_COPY, configuration, sdk), getLog(), EffectiveBuildSettings.INFOPLIST_FILE);
        if(infoPListFileName == null || infoPListFileName.isEmpty())
          throw new XCodeException("Cannot retrieve info plist file from Build settings.");
        
        final File sourceDir = getXCodeSourceDirectory();
        getLog().info("Xcode source directory is: '" + sourceDir + "'.");
        
        File infoPList = new File(sourceDir, infoPListFileName);
        getLog().info("InfoPList file is: '" + infoPList + "'.");

        if (!infoPList.canRead())
          throw new XCodeException("InfoPList file '" + infoPList + "' is not available.");

        _version = getVersionFromInfoPList(infoPList);

        if (_version == null)
          throw new XCodeException("Version not found inside plist file '" + infoPList + "'.");

        if (version != null && !_version.equals(version))
          throw new XCodeException("Version differs between different combinations of configuration/sdk (" + _version
                + "/" + version + ")");

        version = _version;
      }
    }

    return version;
  }

  private String getVersionFromInfoPList(File infoPList) throws XCodeException
  {
    if (!infoPList.exists())
      throw new XCodeException("InfoPlist file '" + infoPList + "' does not exist.");

    try {
      return new PListAccessor(infoPList).getStringValue(PListAccessor.KEY_BUNDLE_VERSION);
    }
    catch (IOException ex) {
      throw new XCodeException("Cannot retrieve version from plist file '" + infoPList + "'.");
    }
  }
}
