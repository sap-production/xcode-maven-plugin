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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;

public class PListAccessor
{
  public static final String KEY_BUNDLE_IDENTIFIER = "CFBundleIdentifier";
  public static final String KEY_BUNDLE_VERSION = "CFBundleVersion";
  public static final String KEY_BUNDLE_SHORT_VERSION_STRING = "CFBundleShortVersionString";
  
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
        return new Scanner(p.getInputStream()).useDelimiter("\\Z").next();
      }
      
      String errorMessage = "<n/a>";
      
      try {
        errorMessage = new Scanner(p.getErrorStream()).useDelimiter("\\Z").next();
      } catch(Exception ex) {
        System.out.println("[ERROR] Exception caught during retrieving error message of command '" + command + "': " + ex);
      }
      
      if(errorMessage.contains(":" + key + "\", Does Not Exist")) {
        // ugly string parsing above, but no other known way ...
        return null;
      }
      
      throw new IllegalStateException("Execution of \""+ StringUtils.join(args, " ") +"\" command failed. Error message is: " + errorMessage + ". Return code was: '" + exitValue + "'.");
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
      String command = "/usr/libexec/PlistBuddy -x -c \"Set :" + key + " " + value + "\" \"" + plist.getAbsolutePath() + "\"";
      System.out.println("[INFO] PlistBuddy Set command is: '" + command + "'.");
      String[] args = new String[] { "bash", "-c", command };
      Process p = Runtime.getRuntime().exec(args);
      p.waitFor();
      int exitValue = p.exitValue(); 
      if (exitValue != 0)
      {
        String errorMessage = "n/a";
        try {
          errorMessage = new Scanner(p.getErrorStream()).useDelimiter("\\Z").next();
        } catch(Exception ex) {
          System.out.println("[ERROR] Exception caught during retrieving error message of command '" + command + "': " + ex);
        }
        throw new IllegalStateException("Execution of \""+ StringUtils.join(args, " ") +"\" command failed: " + errorMessage + ". Exit code was: " + exitValue);
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
      String command = "/usr/libexec/PlistBuddy -x -c \"Add :" + key + " string " + value + "\" \"" + plist.getAbsolutePath() + "\"";
      System.out.println("[INFO] PlistBuddy Add command is: '" + command + "'.");
      String[] args = new String[] { "bash", "-c", command };
      Process p = Runtime.getRuntime().exec(args);
      p.waitFor();
      int exitValue = p.exitValue(); 
      if (exitValue != 0)
      {
        String errorMessage = "n/a";
        try {
          errorMessage = new Scanner(p.getErrorStream()).useDelimiter("\\Z").next();
        } catch(Exception ex) {
          System.out.println("[ERROR] Exception caught during retrieving error message of command '" + command + "': " + ex);
        }
        throw new IllegalStateException("Execution of \""+ StringUtils.join(args, " ") +"\" command failed: " + errorMessage + ". Exit code was: " + exitValue);
      }
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }
}
