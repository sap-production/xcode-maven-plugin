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
import java.io.PrintStream;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

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

    final boolean[] troubleWithOutputStream = {false};

    final StreamConsumer streamConsumer = new StreamConsumer() {

      
      @Override
      public void consumeLine(String line)
      {
        if (out.checkError())
            troubleWithOutputStream[0] = true;

        if(!troubleWithOutputStream[0])
            out.println(line);

        if (out.checkError())
          troubleWithOutputStream[0] = true;
      }
    };

    try {

        final String command = StringUtils.join(args, " ");

        System.out.println("Executing command: " + command);
        
        final Commandline cl = new Commandline(command);

        if(executionDirectory != null) {
            cl.setWorkingDirectory(executionDirectory);
        }

        final int returnValue = CommandLineUtils.executeCommandLine(cl, streamConsumer, streamConsumer);

        if(troubleWithOutputStream[0])
          throw new IOException("Cannot handle log output. PrintStream that should be used for log handling is damaged.");

        out.flush();
        
        return returnValue;
    
    } catch(CommandLineException ex) {
      throw new IOException(ex);
    }
  }
}
