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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

class Forker
{

  static int forkProcess(final PrintStream out, final File executionDirectory, final String... args) throws IOException
  {

    if (out == null)
      throw new IllegalArgumentException("Print stream for log handling was not provided.");

    if (args == null || args.length == 0)
      throw new IllegalArgumentException("No arguments has been provided.");

    for (final String arg : args)
      if (arg == null || arg.isEmpty())
        throw new IllegalArgumentException("Invalid argument '" + arg + "' provided with arguments '"
              + Arrays.asList(args) + "'.");

    final ProcessBuilder builder = new ProcessBuilder(args);

    if (executionDirectory != null)
      builder.directory(executionDirectory);

    builder.redirectErrorStream(true);

    InputStream is = null;

    //
    // TODO: check if there is any support for forking processes in
    // maven/plexus
    //

    try {

      final Process process = builder.start();

      is = process.getInputStream();

      handleLog(is, out);

      return process.waitFor();

    }
    catch (InterruptedException e) {
      throw new RuntimeException(
            e.getClass().getName()
                  + " caught during while waiting for a forked process. This exception is not expected to be caught at that time.",
            e);
    }
    finally {
      //
      // Exception raised during close operation below are not reported.
      // That is actually bad.
      // We do not have any logging facility here and we cannot throw the
      // exception since this would swallow any
      // other exception raised in the try block.
      // May be we should revisit that ...
      //
      IOUtils.closeQuietly(is);
    }
  }

  private static void handleLog(final InputStream is, final PrintStream out) throws IOException
  {

    if (out.checkError())
      throw new IOException("Cannot handle log output. PrintStream that should be used for log handling is damaged.");

    byte[] buff = new byte[1024];
    for (int i; (i = is.read(buff)) != -1;) {
      out.write(buff, 0, i);
      if (out.checkError())
        throw new IOException("Cannot handle log output from xcodebuild. Underlying PrintStream indicates problems.");
    }
  }
}
