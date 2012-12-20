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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sap.prd.mobile.ios.mios.XCodeException;
import com.sap.prd.mobile.ios.mios.XCodeProjectLayoutValidator;

public class XCodeProjectLayoutValidatorTest extends XCodeTest
{

  private static File projectDirectory;

  @BeforeClass
  public static void setupProjectDirectory()
  {
    projectDirectory = new File(new File(".").getAbsoluteFile(), "target/tests/"
          + XCodeProjectLayoutValidator.class.getName());
  }

  @Before
  public void ensureCleanProjectDirectoryAndFilterPom() throws Exception
  {
    ensureCleanProjectDirectoryAndFilterPom(projectDirectory);
  }

  @Test
  public void verifyXCodeFolderStraightForwardTest() throws XCodeException
  {
    XCodeProjectLayoutValidator.verifyXcodeFolder(new File(new File(projectDirectory, "MyLibrary"), "src/xcode"),
          "MyLibrary");
  }

  @Test(expected = XCodeRootFolderDoesNotExistException.class)
  public void xCodeRootFolderDoesNotExist() throws XCodeException, IOException
  {
    final File projectDirectoryMyLibrary = new File(new File(projectDirectory, "MyLibrary"), "src/xcode");
    FileUtils.deleteDirectory(projectDirectoryMyLibrary);
    XCodeProjectLayoutValidator.verifyXcodeFolder(projectDirectoryMyLibrary, "MyLibrary");
  }

  @Test(expected = XCodeProjectNotFoundException.class)
  public void xCodeFolderDoesNotExist() throws XCodeException, IOException
  {
    final File projectDirectoryMyLibrary = new File(new File(projectDirectory, "MyLibrary"), "src/xcode");
    FileUtils.deleteDirectory(new File(projectDirectoryMyLibrary, "MyLibrary.xcodeproj"));
    XCodeProjectLayoutValidator.verifyXcodeFolder(projectDirectoryMyLibrary, "MyLibrary");
  }

  @Test(expected = XCodeProjectFileDoesNotExistException.class)
  public void xCodeProjectFileDoesNotExist() throws XCodeException
  {
    final File projectDirectoryMyLibrary = new File(new File(projectDirectory, "MyLibrary"), "src/xcode");
    final File pbxprojFile = new File(new File(projectDirectoryMyLibrary, "MyLibrary.xcodeproj"), "project.pbxproj");
    if (!pbxprojFile.delete())
      throw new IllegalStateException("Could not delete " + pbxprojFile);
    XCodeProjectLayoutValidator.verifyXcodeFolder(projectDirectoryMyLibrary, "MyLibrary");
  }

}
