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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class CodeSignManager
{
  public static void verify(File appFolder) throws IOException
  {
    exec(new String[] { "/usr/bin/codesign", "--verify -v", "\"" + appFolder.getAbsolutePath() + "\"" });
  }

  public static void sign(String codeSignIdentity, String appId, File appFolder, boolean force) throws IOException
  {
    String[] cmd = new String[] { "/usr/bin/codesign", "-s", "\"" + codeSignIdentity + "\"", "-i", "\"" + appId + "\"",
        "\"" + appFolder.getAbsolutePath() + "\"" };
    if (force)
    {
      cmd = (String[]) ArrayUtils.add(cmd, "-f");
    }
    exec(cmd);
  }

  private static void exec(String[] cmd) throws IOException
  {
    String cmdStr = StringUtils.join(cmd, " ");
    System.out.println("Invoking " + cmdStr);
    int exitValue = Forker.forkProcess(System.out, null, "bash", "-c", cmdStr);
    if (exitValue != 0)
    {
      throw new IllegalStateException(cmd[0] + " command failed (exit code = " + exitValue + ", command = "
            + cmdStr + " check log for details");
    }
  }
}
