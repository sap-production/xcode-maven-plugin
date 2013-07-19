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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.Test;

public class XCodePackageManagerTest
{
  private final static Logger logger = new XCodePluginLogger();

  static {
    LogManager.getLogManager().addLogger(logger);
  }
  
  @Test
  public void testGetPublicHeaderFolderPathNoAlternate() throws XCodeException
  {

    assertEquals(new File("build/Release-iphoneos/include/MyApp"), XCodePackageManager.getPublicHeaderFolderPath(
          "build/Release-iphoneos", "include/MyApp", null));
  }

  @Test
  public void testGetPublicHeaderFolderPathAlternateWithoutLeadingSlash() throws XCodeException
  {

    assertEquals(new File("build/Release-iphoneos/include"), XCodePackageManager.getPublicHeaderFolderPath(
          "build/Release-iphoneos", "include/MyApp", "include"));
  }

  @Test
  public void testGetPublicHeaderFolderPathAlternateWithLeadingSlash() throws XCodeException
  {

    assertEquals(new File("build/Release-iphoneos/include"), XCodePackageManager.getPublicHeaderFolderPath(
          "build/Release-iphoneos", "include/MyApp", "/include"));
  }

  @Test
  public void testGetPublicHeaderFolderPathPropertyInXcodeWithLeadingSlashAlternateWithoutSlash() throws XCodeException
  {

    assertEquals(new File("build/Release-iphoneos/include"), XCodePackageManager.getPublicHeaderFolderPath(
          "build/Release-iphoneos", "/include/MyApp", "include"));
  }

  @Test
  public void testGetPublicHeaderFolderPathPropertyInXcodeWithLeadingSlashAlternateAlsoWithSlash()
        throws XCodeException
  {

    assertEquals(new File("build/Release-iphoneos/include"), XCodePackageManager.getPublicHeaderFolderPath(
          "build/Release-iphoneos", "/include/MyApp", "/include"));
  }

  @Test
  public void testGetPublicHeaderFolderPathAlternateWithTrailingSlash() throws XCodeException
  {

    assertEquals(new File("build/Release-iphoneos/include"), XCodePackageManager.getPublicHeaderFolderPath(
          "build/Release-iphoneos", "include/MyApp", "/include/"));
  }

  @Test(expected = InvalidAlternatePublicHeaderPathException.class)
  public void testGetPublicHeaderFolderPathAlternateWithoutParentChildRelationship() throws XCodeException
  {

    XCodePackageManager.getPublicHeaderFolderPath("build/Release-iphoneos", "include/MyApp", "/include123/");
  }

}
