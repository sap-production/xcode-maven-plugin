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
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

public class XCodeVersionUtil
{

  public static String getXCodeVersionString() throws XCodeException
  {
    PrintStream out = null;
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      out = new PrintStream(bos, true, Charset.defaultCharset().name());
      int exitCode = Forker.forkProcess(out, new File("."), new String[] { "xcodebuild", "-version" });
      if (exitCode == 0) {
        return bos.toString(Charset.defaultCharset().name());
      }
      else {
        throw new XCodeException(
              "Could not get xcodebuild version (exit code = " + exitCode + ")");
      }
    }
    catch (Exception e) {
      throw new XCodeException(
            "Could not get xcodebuild version");
    }
    finally {
      IOUtils.closeQuietly(out);
    }
  }

  public static DefaultArtifactVersion getVersion(String xCodeVersionString) throws XCodeException
  {
    Pattern versionPattern = Pattern.compile("Xcode (\\d+(\\.\\d+)+)", Pattern.CASE_INSENSITIVE);
    Matcher versionMatcher = versionPattern.matcher(xCodeVersionString);
    if (versionMatcher.find()) {
      return new DefaultArtifactVersion(versionMatcher.group(1));
    }
    throw new XCodeException("Could not get xcodebuild version");
  }
  
  public static String getBuildVersion(String xCodeVersionString) throws XCodeException
  {
    Pattern buildPattern = Pattern.compile("Build version (\\w+)", Pattern.CASE_INSENSITIVE);
    Matcher buildMatcher = buildPattern.matcher(xCodeVersionString);
    if (buildMatcher.find()) {
      return buildMatcher.group(1);
    }
    throw new XCodeException("Could not get xcodebuild build version");
  }
  

  public static boolean checkVersions(DefaultArtifactVersion version, final String MIN_XCODE_VERSION)
  {
    DefaultArtifactVersion minXcodeVersion = new DefaultArtifactVersion(MIN_XCODE_VERSION);
    return version.compareTo(minXcodeVersion) >= 0;
  }
  
}