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

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public class PListAccessor
{
  public static final String KEY_BUNDLE_IDENTIFIER = "CFBundleIdentifier";
  public static final String KEY_BUNDLE_VERSION = "CFBundleVersion";
  public static final String KEY_BUNDLE_SHORT_VERSION_STRING = "CFBundleShortVersionString";
  public static final String KEY_WK_COMPANION_APP_BUNDLE_IDENTIFIER ="WKCompanionAppBundleIdentifier";
  public static final String KEY_WK_APP_BUNDLE_IDENTIFIER ="NSExtension:NSExtensionAttributes:WKAppBundleIdentifier";

  private final File plist;

  public PListAccessor(File file)
  {
    plist = file;
  }

  public File getPlistFile()
  {
    return plist;
  }

  public String getStringValue(String key) throws IOException
  {
    if (!plist.exists())
    {
      throw new FileNotFoundException("The Plist " + plist.getAbsolutePath() + " does not exist.");
    }

    try
    {
      String command = "/usr/libexec/PlistBuddy -c \"Print :" + key + "\" \"" + plist.getAbsolutePath() + "\"";

      System.out.println("[INFO] PlistBuddy Print command is: '" + command + "'.");

      String[] args = new String[] { "bash", "-c", command };
      Process p = Runtime.getRuntime().exec(args);
      p.waitFor();

      int exitValue = p.exitValue();

      if (exitValue == 0)
      {
        InputStream is = p.getInputStream();
        try {
          return new Scanner(is, Charset.defaultCharset().name()).useDelimiter("\\Z").next();
        }
        finally {
          closeQuietly(is);
        }
      }

      String errorMessage = "<n/a>";

      try {
        errorMessage = new Scanner(p.getErrorStream(), Charset.defaultCharset().name()).useDelimiter("\\Z").next();
      }
      catch (Exception ex) {
        System.out.println("[ERROR] Exception caught during retrieving error message of command '" + command + "': "
              + ex);
      }

      if (errorMessage.contains(":" + key + "\", Does Not Exist")) {
        // ugly string parsing above, but no other known way ...
        return null;
      }

      throw new IllegalStateException("Execution of \"" + StringUtils.join(args, " ")
            + "\" command failed. Error message is: " + errorMessage + ". Return code was: '" + exitValue + "'.");
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }

  public void updateStringValue(String key, String value) throws IOException
  {
    if (!plist.exists())
    {
      throw new FileNotFoundException("Plist file '" + plist + "' not found.");
    }

    try
    {
      String command = "/usr/libexec/PlistBuddy -x -c \"Set :" + key + " " + value + "\" \"" + plist.getAbsolutePath()
            + "\"";
      System.out.println("[INFO] PlistBuddy Set command is: '" + command + "'.");
      String[] args = new String[] { "bash", "-c", command };
      Process p = Runtime.getRuntime().exec(args);
      p.waitFor();
      int exitValue = p.exitValue();
      if (exitValue != 0)
      {
        String errorMessage = "n/a";
        try {
          errorMessage = new Scanner(p.getErrorStream(), Charset.defaultCharset().name()).useDelimiter("\\Z").next();
        }
        catch (Exception ex) {
          System.out.println("[ERROR] Exception caught during retrieving error message of command '" + command + "': "
                + ex);
        }
        throw new IllegalStateException("Execution of \"" + StringUtils.join(args, " ") + "\" command failed: "
              + errorMessage + ". Exit code was: " + exitValue);
      }
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }

  public void addStringValue(String key, String value) throws IOException
  {
    if (!plist.exists())
    {
      throw new FileNotFoundException("Plist file '" + plist + "' not found.");
    }

    try
    {
      String command = "/usr/libexec/PlistBuddy -x -c \"Add :" + key + " string " + value + "\" \""
            + plist.getAbsolutePath() + "\"";
      System.out.println("[INFO] PlistBuddy Add command is: '" + command + "'.");
      String[] args = new String[] { "bash", "-c", command };
      Process p = Runtime.getRuntime().exec(args);
      p.waitFor();
      int exitValue = p.exitValue();
      if (exitValue != 0)
      {
        String errorMessage = "n/a";
        try {
          errorMessage = new Scanner(p.getErrorStream(), Charset.defaultCharset().name()).useDelimiter("\\Z").next();
        }
        catch (Exception ex) {
          System.out.println("[ERROR] Exception caught during retrieving error message of command '" + command + "': "
                + ex);
        }
        throw new IllegalStateException("Execution of \"" + StringUtils.join(args, " ") + "\" command failed: "
              + errorMessage + ". Exit code was: " + exitValue);
      }
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }

  public void addStringValueToDict(String key, String value, String dictKey) throws IOException
  {
    if (!plist.exists())
    {
      throw new FileNotFoundException("Plist file '" + plist + "' not found.");
    }

    try
    {
      String command = "/usr/libexec/PlistBuddy -x -c \"Add :" + dictKey + ":" + key + " string " + value + "\" \""
            + plist.getAbsolutePath() + "\"";
      System.out.println("[INFO] PlistBuddy Add command is: '" + command + "'.");
      String[] args = new String[] { "bash", "-c", command };
      Process p = Runtime.getRuntime().exec(args);
      p.waitFor();
      int exitValue = p.exitValue();
      if (exitValue != 0)
      {
        String errorMessage = "n/a";
        try {
          errorMessage = new Scanner(p.getErrorStream(), Charset.defaultCharset().name()).useDelimiter("\\Z").next();
        }
        catch (Exception ex) {
          System.out.println("[ERROR] Exception caught during retrieving error message of command '" + command + "': "
                + ex);
        }
        throw new IllegalStateException("Execution of \"" + StringUtils.join(args, " ") + "\" command failed: "
              + errorMessage + ". Exit code was: " + exitValue);
      }
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }

  public void addElement(String key, String type) throws IOException
  {
    if (!plist.exists())
    {
      throw new FileNotFoundException("Plist file '" + plist + "' not found.");
    }
    try
    {
      String command = "/usr/libexec/PlistBuddy -x -c \"Add :" + key + " " + type + "  " + "\" \""
            + plist.getAbsolutePath() + "\"";
      System.out.println("[INFO] PlistBuddy Add command is: '" + command + "'.");
      String[] args = new String[] { "bash", "-c", command };
      Process p = Runtime.getRuntime().exec(args);
      p.waitFor();
      int exitValue = p.exitValue();
      if (exitValue != 0)
      {
        String errorMessage = "n/a";
        try {
          errorMessage = new Scanner(p.getErrorStream(), Charset.defaultCharset().name()).useDelimiter("\\Z").next();
        }
        catch (Exception ex) {
          System.out.println("[ERROR] Exception caught during retrieving error message of command '" + command + "': "
                + ex);
        }
        throw new IllegalStateException("Execution of \"" + StringUtils.join(args, " ") + "\" command failed: "
              + errorMessage + ". Exit code was: " + exitValue);
      }
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }

  public void addDictToArray(String dict, String array) throws IOException
  {
    if (!plist.exists())
    {
      throw new FileNotFoundException("Plist file '" + plist + "' not found.");
    }

    try
    {
      String command = "/usr/libexec/PlistBuddy -x -c \"Add :" + array + ":" + dict + " dict " + "\" \""
            + plist.getAbsolutePath() + "\"";
      System.out.println("[INFO] PlistBuddy Add command is: '" + command + "'.");
      String[] args = new String[] { "bash", "-c", command };
      Process p = Runtime.getRuntime().exec(args);
      p.waitFor();
      int exitValue = p.exitValue();
      if (exitValue != 0)
      {
        String errorMessage = "n/a";
        try {
          errorMessage = new Scanner(p.getErrorStream(), Charset.defaultCharset().name()).useDelimiter("\\Z").next();
        }
        catch (Exception ex) {
          System.out.println("[ERROR] Exception caught during retrieving error message of command '" + command + "': "
                + ex);
        }
        throw new IllegalStateException("Execution of \"" + StringUtils.join(args, " ") + "\" command failed: "
              + errorMessage + ". Exit code was: " + exitValue);
      }
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }

  public void createPlist() throws IOException
  {

    try
    {
      String command = "/usr/libexec/PlistBuddy -x -c \"Save \" \"" + plist.getAbsolutePath() + "\"";
      System.out.println("[INFO] PlistBuddy Add command is: '" + command + "'.");
      String[] args = new String[] { "bash", "-c", command };

      Process p = Runtime.getRuntime().exec(args);

      p.waitFor();

      int exitValue = p.exitValue();

      if (exitValue != 0)
      {
        String errorMessage = "n/a";
        try {
          errorMessage = new Scanner(p.getErrorStream(), Charset.defaultCharset().name()).useDelimiter("\\Z").next();
        }
        catch (Exception ex) {
          System.out.println("[ERROR] Exception caught during retrieving error message of command '" + command + "': "
                + ex);
        }
        throw new IllegalStateException("Execution of \"" + StringUtils.join(args, " ") + "\" command failed: "
              + errorMessage + ". Exit code was: " + exitValue);
      }
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }

  public String printValue(String key) throws IOException
  {

    try
    {
      String command = "/usr/libexec/PlistBuddy -c \"Print :" + key + "\" \"" + plist.getAbsolutePath() + "\"";
      System.out.println("[INFO] PlistBuddy Add command is: '" + command + "'.");
      String[] args = new String[] { "bash", "-c", command };

      Process p = Runtime.getRuntime().exec(args);

      InputStream is = p.getInputStream();
      p.waitFor();
      int exitValue = p.exitValue();

      if (exitValue != 0)
      {
        String errorMessage = "n/a";
        try {
          errorMessage = new Scanner(p.getErrorStream(), Charset.defaultCharset().name()).useDelimiter("\\Z").next();
        }
        catch (Exception ex) {
          System.out.println("[ERROR] Exception caught during retrieving error message of command '" + command + "': "
                + ex);
        }
        throw new IllegalStateException("Execution of \"" + StringUtils.join(args, " ") + "\" command failed: "
              + errorMessage + ". Exit code was: " + exitValue);
      }

      byte[] buff = new byte[64];
      StringBuilder sb = new StringBuilder();
      for (int i = 0; (i = is.read(buff)) != -1;) {
        sb.append(new String(buff, 0, i, Charset.defaultCharset().name()));
      }
      BufferedReader reader = new BufferedReader(new StringReader(sb.toString()));

      try {
        return reader.readLine();
      }
      finally {
        IOUtils.closeQuietly(reader);
      }
    }

    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }

}
