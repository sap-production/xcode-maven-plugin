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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

public class PListAccessor
{
  private File plist;

  public PListAccessor(File file)
  {
    plist = file;
  }

  public String getStringValue(String key) throws IOException
  {
    if (!plist.exists())
    {
      throw new FileNotFoundException();
    }

    try
    {
      String infoPList = FilenameUtils.removeExtension(plist.getAbsolutePath());
      String command = "/usr/bin/defaults read \"" + infoPList + "\" \"" + key +"\"";
      String[] args = new String[] { "bash", "-c", command };
      Process p = Runtime.getRuntime().exec(args);
      p.waitFor();

      if (p.exitValue() == 0)
      {
        return new Scanner(p.getInputStream()).useDelimiter("\\Z").next();
      }
      throw new IllegalStateException("Execution of \""+ StringUtils.join(args, " ") +"\" command failed");
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }    
  }

  public void setStringValue(String key, String value) throws IOException
  {
    if (!plist.exists())
    {
      throw new FileNotFoundException();
    }

    try
    {
      String infoPList = FilenameUtils.removeExtension(plist.getAbsolutePath());
      String command = "/usr/bin/defaults write \"" + infoPList + "\" \"" + key + "\" \"" + value + "\"";
      String[] args = new String[] { "bash", "-c", command };
      Process p = Runtime.getRuntime().exec(args);
      p.waitFor();
      if (p.exitValue() != 0)
      {
        throw new IllegalStateException("Execution of \""+ StringUtils.join(args, " ") +"\" command failed");
      }
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }
}
