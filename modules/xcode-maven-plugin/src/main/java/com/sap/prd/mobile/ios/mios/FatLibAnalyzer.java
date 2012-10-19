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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;

class FatLibAnalyzer
{
  private File fatLib;

  private Set<String> architectures = null;

  FatLibAnalyzer(final File fatLib)
  {
    if (fatLib == null)
      throw new NullPointerException();

    if (!fatLib.canRead())
      throw new IllegalArgumentException("Cannot access " + fatLib + ".");

    this.fatLib = fatLib;
  }
  
  File getFatLibrary() {
    return fatLib;
  }

  Set<String> getArchitectures() throws IOException
  {
    if (architectures == null) {

      BufferedReader br = null;
      
      try {

        final String detailedLipoInfo = getDetailedLipoInfo();
        
        br = new BufferedReader(new StringReader(getDetailedLipoInfo()));

        Set<String> architectures = new HashSet<String>();

        for(String line; (line = br.readLine()) != null; ) {

          if (!line.trim().startsWith("architecture"))
            continue;

          String[] strings = line.split(" ");

          if (strings.length != 2)
            throw new IllegalStateException("Lipo output cannot be parsed: " + detailedLipoInfo);

          architectures.add(strings[1]);
        }

        this.architectures = architectures;
      }
      finally {
        IOUtils.closeQuietly(br);
      }
    }

    return this.architectures;
  }

  boolean containsI386() throws IOException
  {
    return getArchitectures().contains("i386");
  }

  boolean containsArmv() throws IOException
  {
    for (String architecture : getArchitectures()) {
      if (architecture.startsWith("armv")) {
        return true;
      }
    }
    return false;
  }
  
  private String getDetailedLipoInfo() throws IOException {
    
    ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(byteOs);
    try {
      Forker.forkProcess(ps, null, "lipo", "-detailed_info", fatLib.getAbsolutePath());

      return new String(byteOs.toByteArray());
    } finally {
      IOUtils.closeQuietly(ps);
    }
  }
}
