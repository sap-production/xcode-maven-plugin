package com.sap.prd.mobile.ios.mios;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Assures that the following required prerequisites are fulfilled:
 * <ul>
 * <li>Xcode version {@value #MIN_XCODE_VERSION} or higher is installed</li>
 * </ul>
 * 
 * @goal check-prerequisites
 * 
 */
public class CheckPrerequisitesMojo extends AbstractXCodeMojo
{
  
  public final static String MIN_XCODE_VERSION = "4.4";
  
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      PrintStream out = new PrintStream(bos);
      int exitCode = Forker.forkProcess(out, new File("."), new String[] { "xcodebuild", "-version" });
      if (exitCode == 0) {
        String output = bos.toString();
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
  
  private String getBuildVersion(String output) throws MojoExecutionException {
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
      throw new MojoExecutionException("Xcode " + MIN_XCODE_VERSION + " (or higher) is required (installed: " + version + " " + buildVersion + ")");
    }
  }
}
