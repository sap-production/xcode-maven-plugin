package com.sap.prd.mobile.ios.mios;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;

public class XCodePackageManagerTest
{

  @Test
  public void testGetPublicHeaderFolderPathNoAlternate() throws XCodeException {
    
    assertEquals(new File("build/Release-iphoneos/include/MyApp"), XCodePackageManager.getPublicHeaderFolderPath(new SystemStreamLog(), "build/Release-iphoneos", "include/MyApp", null));
  }
  
  @Test
  public void testGetPublicHeaderFolderPathAlternateWithoutLeadingSlash() throws XCodeException {
    
    assertEquals(new File("build/Release-iphoneos/include"), XCodePackageManager.getPublicHeaderFolderPath(new SystemStreamLog(), "build/Release-iphoneos", "include/MyApp", "include"));
  }

  @Test
  public void testGetPublicHeaderFolderPathAlternateWithLeadingSlash() throws XCodeException {
    
    assertEquals(new File("build/Release-iphoneos/include"), XCodePackageManager.getPublicHeaderFolderPath(new SystemStreamLog(), "build/Release-iphoneos", "include/MyApp", "/include"));
  }
  @Test
  public void testGetPublicHeaderFolderPathPropertyInXcodeWithLeadingSlashAlternateWithoutSlash() throws XCodeException {
    
    assertEquals(new File("build/Release-iphoneos/include"), XCodePackageManager.getPublicHeaderFolderPath(new SystemStreamLog(), "build/Release-iphoneos", "/include/MyApp", "include"));
  }

  @Test
  public void testGetPublicHeaderFolderPathPropertyInXcodeWithLeadingSlashAlternateAlsoWithSlash() throws XCodeException {
    
    assertEquals(new File("build/Release-iphoneos/include"), XCodePackageManager.getPublicHeaderFolderPath(new SystemStreamLog(), "build/Release-iphoneos", "/include/MyApp", "/include"));
  }

  
  @Test
  public void testGetPublicHeaderFolderPathAlternateWithTrailingSlash() throws XCodeException {
    
    assertEquals(new File("build/Release-iphoneos/include"), XCodePackageManager.getPublicHeaderFolderPath(new SystemStreamLog(), "build/Release-iphoneos", "include/MyApp", "/include/"));
  }

  @Test(expected=InvalidAlternatePublicHeaderPathException.class)
  public void testGetPublicHeaderFolderPathAlternateWithoutParentChildRelationship() throws XCodeException {
    
    XCodePackageManager.getPublicHeaderFolderPath(new SystemStreamLog(), "build/Release-iphoneos", "include/MyApp", "/include123/");
  }

}
