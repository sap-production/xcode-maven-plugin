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
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class CodeSignManager
{
  public static void verify(File appFolder, boolean noStrictVerify) throws IOException
  {
    if (noStrictVerify) {
      checkExitValue(exec(new String[] { "/usr/bin/codesign", "--verify --no-strict -v",
          "\"" + appFolder.getAbsolutePath() + "\"" }));
    }
    else {
      checkExitValue(exec(new String[] { "/usr/bin/codesign", "--verify -v", "\"" + appFolder.getAbsolutePath() + "\"" }));
    }
  }

  public static void sign(String codeSignIdentity, File appFolder, boolean force) throws IOException
  {
    String[] cmd = new String[] { "/usr/bin/codesign", "--preserve-metadata", "-s", "\"" + codeSignIdentity + "\"",
        "\"" + appFolder.getAbsolutePath() + "\"" };
    if (force)
    {
      cmd = (String[]) ArrayUtils.add(cmd, "-f");
    }
    ExecResult exec = exec(cmd);
    checkExitValue(exec);
  }

  public static ExecResult getCodesignEntitlementsInformation(File appFolder) throws IOException
  {
    ExecResult result = exec(new String[] { "/usr/bin/codesign", "-d --entitlements -",
        "\"" + appFolder.getAbsolutePath() + "\"" });
    checkExitValue(result);
    return result;
  }

  public static ExecResult getSecurityCMSInformation(File appFolder) throws IOException
  {
    ExecResult result = exec(new String[] { "/usr/bin/security", "cms", "-D -i",
        "\"" + appFolder.getAbsolutePath() + "/embedded.mobileprovision\"" });
    checkExitValue(result);
    return result;
  }

  public static void verify(ExecResult result1, ExecResult result2) throws ExecutionResultVerificationException
  {
    if (!result2.equals(result1)) {
      throw new ExecutionResultVerificationException(result1, result2);
    }
  }

  private static ExecResult exec(String[] cmd) throws IOException
  {
    String cmdStr = StringUtils.join(cmd, " ");
    System.out.println("Invoking " + cmdStr);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos, true, "UTF-8");
    try {
      int exitValue = Forker.forkProcess(ps, null, "bash", "-c", cmdStr);
      return new ExecResult(cmdStr, baos.toString("UTF-8"), exitValue);
    }
    finally {
      ps.close();
    }
  }

  private static void checkExitValue(ExecResult exec)
  {
    if (exec.exitCode != 0)
    {
      throw new IllegalStateException("Command failed, check log for details (" +
            "exit code = " + exec.exitCode +
            ", command = '" + exec.command +
            "', result = '" + exec.result + "')");
    }
  }

  static class ExecResult
  {
    public ExecResult(String command, String result, int exitCode)
    {
      this.command = command;
      this.result = result;
      this.exitCode = exitCode;
    }

    public final String command;
    public final String result;
    public final int exitCode;

    @Override
    public String toString()
    {
      return "ExecResult [command=" + command + ", result=" + result
            + ", exitCode=" + exitCode + "]";
    }

    @Override
    public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((command == null) ? 0 : command.hashCode());
      result = prime * result + exitCode;
      result = prime * result
            + ((this.result == null) ? 0 : this.result.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj)
    {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      ExecResult other = (ExecResult) obj;
      if (command == null) {
        if (other.command != null)
          return false;
      }
      else if (!command.equals(other.command))
        return false;
      if (exitCode != other.exitCode)
        return false;
      if (result == null) {
        if (other.result != null)
          return false;
      }
      else if (!result.equals(other.result))
        return false;
      return true;
    }
  }

  static class ExecutionResultVerificationException extends Exception
  {
    private static final long serialVersionUID = 1L;
    private final ExecResult result1;
    private final ExecResult result2;

    public ExecutionResultVerificationException(ExecResult result1, ExecResult result2)
    {
      super();
      this.result1 = result1;
      this.result2 = result2;
    }
    
    @Override
    public String getMessage()
    {
      return String.format("Verification failed, results differ. Result 1: '%s', Result 2: '%s'", result1, result2);
    }
  }
}
