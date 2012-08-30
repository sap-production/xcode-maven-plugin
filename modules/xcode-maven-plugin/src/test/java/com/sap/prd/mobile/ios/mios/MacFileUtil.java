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
import java.util.Arrays;

class MacFileUtil
{

  final static void deleteDirectory(final File dir) throws Exception
  {

    //
    // FileUtils.deleteDirectory does not work with symbolic links
    //

    if (!dir.exists())
      return;

    if (dir.isFile())
      throw new IllegalStateException(dir + " is not a directory.");

    process("rm", "-rf", dir.getAbsolutePath());

    if (dir.exists()) {
      throw new IllegalStateException("The removal of the dir " + dir.getAbsolutePath()
            + " failed. The dir still exists.");
    }
  }

  final static void setWritableRecursive(final boolean writable, final File root) throws Exception
  {

    if (!root.exists())
      return;

    if (root.isDirectory()) {
      process("chmod", "755", root.getAbsolutePath());

      for (final File child : root.listFiles())
        setWritableRecursive(writable, child);

    }
    else {
      if (writable) {
        process("chmod", "644", root.getAbsolutePath());
      }
      else {
        process("chmod", "444", root.getAbsolutePath());
      }
    }
  }

  private static void process(final String... command) throws IOException, InterruptedException
  {

    Process process = new ProcessBuilder(command).start();

    if (process.waitFor() != 0)
      throw new IllegalStateException("Cannot execute '" + Arrays.asList(command) + "'.");
  }
}
