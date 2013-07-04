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

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FileUtilsTest
{

  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

  @Test
  public void testGetDelta() throws Exception
  {

    Assert.assertEquals("source/dir",
          FileUtils.getDelta(new File("/home/abc/def"), new File("/home/abc/def/source/dir")));
  }

  @Test(expected = IllegalStateException.class)
  public void testNoCommonPath() throws Exception
  {
    FileUtils.getDelta(new File("/home/abc"), new File("/home/def"));
  }

  @Test
  public void testIsChild() throws Exception
  {
    assertTrue(FileUtils.isChild(new File("/abc/def"), new File("/abc/def/ghi")));
  }

  public void testIsNotAChild() throws Exception
  {
    assertFalse(FileUtils.isChild(new File("/abc/def"), new File("/abc/ghi/def")));
  }

  @Test
  public void testCreateSymbolicLink() throws Exception
  {
    File source = prepareFile();
    File target = tmpFolder.newFile("target");

    FileUtils.createSymbolicLink(source, target);

    Assert.assertTrue(checkForSymbolicLink(target));
  }

  @Test
  public void testIsSymbolicLinkForNullFile() throws Exception
  {
    assertFalse(FileUtils.isSymbolicLink(null));
  }

  @Test
  public void testIsSymbolicLinkWithSymbolicLink() throws Exception
  {
    File source = prepareFile();
    File target = tmpFolder.newFile("target");
    FileUtils.createSymbolicLink(source, target);
    assertTrue(FileUtils.isSymbolicLink(target));
  }

  @Test
  public void testIsSymbolicLinkWithRealFile() throws Exception
  {
    File file = prepareFile();
    assertFalse(FileUtils.isSymbolicLink(file));
  }

  @Test
  public void testAddLeadingSlash()
  {
    assertEquals("/abc/def", FileUtils.ensureLeadingSlash("abc/def"));
  }

  @Test
  public void testNoDoubleLeadingSlash()
  {
    assertEquals("/abc/def", FileUtils.ensureLeadingSlash("/abc/def"));
  }

  @Test(expected = NullPointerException.class)
  public void testNullPointer()
  {
    FileUtils.ensureLeadingSlash(null);
  }

  @Test
  public void testFileAppendix()
  {
    assertEquals("zip", FileUtils.getAppendix(new File("MyFile.zip")));
  }

  @Test
  public void testNoFileAppendix()
  {
    assertNull(FileUtils.getAppendix(new File("MyFile")));
  }

  private static boolean checkForSymbolicLink(final File f) throws IOException
  {

    ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
    PrintStream stream = new PrintStream(byteOs);

    try {
      Forker.forkProcess(stream, null, "ls", "-l", f.getAbsolutePath());
      stream.flush();
      return byteOs.toString().startsWith("l");
    }
    finally {
      IOUtils.closeQuietly(stream);
    }
  }

  private File prepareFile() throws IOException
  {
    File file = tmpFolder.newFile("source");
    org.apache.commons.io.FileUtils.writeStringToFile(file, "abc");
    return file;
  }
}
