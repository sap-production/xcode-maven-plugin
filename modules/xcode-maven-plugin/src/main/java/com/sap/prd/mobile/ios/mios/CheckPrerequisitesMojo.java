package com.sap.prd.mobile.ios.mios;

/*
 * #%L
 * Xcode Maven Plugin
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Assures that the following required prerequisites are fulfilled:
 * <ul>
 * <li>Xcode version {@value #MIN_XCODE_VERSION} or higher is installed</li>
 * </ul>
 */
@Mojo(name="check-prerequisites")
public class CheckPrerequisitesMojo extends AbstractXCodeMojo
{
  public final static String MIN_XCODE_VERSION = "4.4";

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      PrintStream out = new PrintStream(bos, true, Charset.defaultCharset().name());
      int exitCode = Forker.forkProcess(out, new File("."), new String[] { "xcodebuild", "-version" });
      if (exitCode == 0) {
        String output = bos.toString(Charset.defaultCharset().name());
        DefaultArtifactVersion version = getVersion(output);
        String buildVersion = getBuildVersion(output);
        getLog().info("Using Xcode " + version + " " + buildVersion);
        checkVersions(version, buildVersion);
      }
      else {
        throw new MojoExecutionException("Could not get xcodebuild version (exit code = " + exitCode + ")");
      }
    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not get xcodebuild version", e);
    }
  }

  private DefaultArtifactVersion getVersion(String output) throws MojoExecutionException
  {
    Pattern versionPattern = Pattern.compile("Xcode (\\d+(\\.\\d+)+)", Pattern.CASE_INSENSITIVE);
    Matcher versionMatcher = versionPattern.matcher(output);
    if (versionMatcher.find()) {
      return new DefaultArtifactVersion(versionMatcher.group(1));
    }
    throw new MojoExecutionException("Could not get xcodebuild version");
  }

  private String getBuildVersion(String output) throws MojoExecutionException
  {
    Pattern buildPattern = Pattern.compile("Build version (\\w+)", Pattern.CASE_INSENSITIVE);
    Matcher buildMatcher = buildPattern.matcher(output);
    if (buildMatcher.find()) {
      return buildMatcher.group(1);
    }
    throw new MojoExecutionException("Could not get xcodebuild build version");
  }

  private void checkVersions(DefaultArtifactVersion version, String buildVersion) throws MojoExecutionException
  {
    DefaultArtifactVersion minXcodeVersion = new DefaultArtifactVersion(MIN_XCODE_VERSION);
    if (version.compareTo(minXcodeVersion) < 0) {
      throw new MojoExecutionException("Xcode " + MIN_XCODE_VERSION + " (or higher) is required (installed: " + version
            + " " + buildVersion + ")");
    }
  }
}
