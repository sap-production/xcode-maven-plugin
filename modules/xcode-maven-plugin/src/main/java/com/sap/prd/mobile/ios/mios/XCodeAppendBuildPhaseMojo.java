/*
 * #%L
 * maven-xcode-plugin
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
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.sap.prd.mobile.ios.mios.xcodeprojreader.ProjectFile;
import com.sap.prd.mobile.ios.mios.xcodeprojreader.ReferenceArray;
import com.sap.prd.mobile.ios.mios.xcodeprojreader.buildphases.BuildPhase;
import com.sap.prd.mobile.ios.mios.xcodeprojreader.buildphases.PBXShellScriptBuildPhase;
import com.sap.prd.mobile.ios.mios.xcodeprojreader.jaxb.JAXBPlistParser;

/**
 * Appends a build phase to the project that dumps the all environment variables to a file.
 * 
 * @goal append-build-phase
 * 
 */
public class XCodeAppendBuildPhaseMojo extends AbstractXCodeMojo
{
  public static File getBuildEnvironmentFile(AbstractXCodeMojo mojo, String configuration, String platform)
  {
    return getBuildEnvironmentFile(mojo.project.getBuild().getDirectory(), configuration, platform);
  }
  
  public static File getBuildEnvironmentFile(String directory, String configuration, String platform)
  {
    return new File(directory, getBuildEnvironmentFileName(configuration, platform));
  }
  
  public static String getBuildEnvironmentFileName(String configuration, String platform)
  {
    return "build-environment" + "-" + configuration + "-" + platform + ".properties";
  }
  
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    ProjectFile projectFile = getXcodeProject();
    ReferenceArray<BuildPhase> buildPhases = projectFile.getProject().getTargets().get(0).getBuildPhases();

    String script;
    try
    {
      script = "env > \"" + getBuildEnvironmentFile(this, "${CONFIGURATION}", "${PLATFORM_NAME}").getCanonicalPath() + "\"";
    }
    catch (IOException e)
    {
      throw new MojoExecutionException(e.getMessage(), e);
    }

    if (findShellScriptBuildPhase(buildPhases, script) == null)
    {
      addBuildPhase(projectFile, buildPhases, script);
    }
  }

  private PBXShellScriptBuildPhase findShellScriptBuildPhase(ReferenceArray<BuildPhase> buildPhases, String script)
  {
    for (BuildPhase phase : buildPhases)
    {
      if (phase instanceof PBXShellScriptBuildPhase)
      {
        PBXShellScriptBuildPhase shellPhase = (PBXShellScriptBuildPhase) phase;
        if (script.equals(shellPhase.getShellScript()))
        {
          return shellPhase;
        }
      }
    }
    return null;
  }

  private void addBuildPhase(ProjectFile projectFile, ReferenceArray<BuildPhase> buildPhases, String script)
        throws MojoExecutionException
  {
    PBXShellScriptBuildPhase phase = new PBXShellScriptBuildPhase(projectFile);
    phase.setDefaultValues();
    phase.setShellScript(script);
    buildPhases.add(phase);
    JAXBPlistParser parser = new JAXBPlistParser();
    try
    {
      parser.save(projectFile.getPlist(), getXCodeProjectFile());
    }
    catch (JAXBException e)
    {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }
}
