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
import java.io.OutputStream;
import java.io.PrintStream;

import junit.framework.Assert;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;

import com.sap.prd.mobile.ios.mios.Forker;

public class ForkerTest
{

  @Test
  public void testStraightForward() throws Exception
  {

    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final PrintStream log = new PrintStream(out, true);

    try {

      Forker.forkProcess(log, new File(".").getAbsoluteFile(), "echo", "Hello World");

    }
    finally {
      log.close();
    }

    Assert.assertEquals("Hello World\n", new String(out.toByteArray()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingPrintStreamForLogging() throws Exception
  {
    Forker.forkProcess(null, new File(".").getAbsoluteFile(), "echo", "Hello World");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingArguments_1() throws Exception
  {

    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final PrintStream log = new PrintStream(out, true);

    try {

      Forker.forkProcess(log, new File(".").getAbsoluteFile(), (String[]) null);

    }
    finally {
      log.close();
    }

  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingArguments_2() throws Exception
  {

    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final PrintStream log = new PrintStream(out, true);

    try {

      Forker.forkProcess(log, new File(".").getAbsoluteFile(), new String[0]);

    }
    finally {
      log.close();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingArguments_3() throws Exception
  {

    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final PrintStream log = new PrintStream(out, true);

    try {

      Forker.forkProcess(log, new File(".").getAbsoluteFile(), "echo", "", "Hello World");

    }
    finally {
      log.close();
    }
  }

  @Test(expected = IOException.class)
  public void damagedPrintStream() throws Exception
  {

    final ByteArrayOutputStream out = new ByteArrayOutputStream();

    class DamagedPrintStream extends PrintStream
    {
      public DamagedPrintStream(OutputStream out)
      {
        super(out);
        setError();
      }
    }
    final PrintStream log = new DamagedPrintStream(out);

    try {

      Forker.forkProcess(log, new File(".").getAbsoluteFile(), "echo", "Hello World");

    }
    finally {
      log.close();
    }
  }

}
