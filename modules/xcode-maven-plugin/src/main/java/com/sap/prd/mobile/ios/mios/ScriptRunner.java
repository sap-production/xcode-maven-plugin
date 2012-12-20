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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class ScriptRunner
{
  /**
   * 
   * @param script
   *          The script file that should be executed. Must be located inside the class path.
   * @throws IOException
   */
  public static int copyAndExecuteScript(PrintStream out, String script, File workingDirectory, String... args)
        throws IOException
  {
    File scriptFile = copyScript(script, workingDirectory);
    return execute(out, scriptFile, args);
  }

  private static File copyScript(String script, File workingDirectory) throws IOException
  {
    if (!workingDirectory.exists())
      if (!workingDirectory.mkdirs())
        throw new IOException("Cannot create directory '" + workingDirectory + "'.");

    final File scriptFile = new File(workingDirectory, getScriptFileName(script));
    scriptFile.deleteOnExit();

    OutputStream os = null;
    InputStream is = null;

    try
    {
      is = ScriptRunner.class.getResourceAsStream(script);

      if (is == null)
        throw new FileNotFoundException(script + " not found.");

      os = FileUtils.openOutputStream(scriptFile);

      IOUtils.copy(is, os);
    }
    finally
    {
      IOUtils.closeQuietly(is);
      IOUtils.closeQuietly(os);
    }

    Forker.forkProcess(System.out, null, "chmod", "755", scriptFile.getCanonicalPath());
    return scriptFile;
  }

  private static String getScriptFileName(String script)
  {
    String[] split = script.split("/");
    return split[split.length - 1];
  }

  private static int execute(PrintStream out, File scriptFile, String... args) throws IOException
  {
    String[] _args = new String[args.length + 1];
    System.arraycopy(args, 0, _args, 1, args.length);
    _args[0] = scriptFile.getCanonicalPath();

    return Forker.forkProcess(out, null, _args);
  }
}
