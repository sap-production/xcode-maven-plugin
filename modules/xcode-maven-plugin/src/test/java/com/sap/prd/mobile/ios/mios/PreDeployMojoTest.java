package com.sap.prd.mobile.ios.mios;

import static com.sap.prd.mobile.ios.mios.PreDeployMojo.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class PreDeployMojoTest
{

  @Test
  public void testGetNthIndexFromBack_OtaHtm() {
    assertEquals(29, getNthIndexFromBack(
          "MyApp-1.0.0-20120821.132955-1-Release-iphoneos-ota.htm", "-", 3));    
  }

  @Test
  public void testGetNthIndexFromBack_MultiCharSearch() {
    assertEquals(4, getNthIndexFromBack("A:-B:-C:-D", ":-", 2));    
  }

  @Test(expected = NullPointerException.class)
  public void testGetNthIndexFromBack_Null() {
    getNthIndexFromBack(null, "-", 1);    
  }

  @Test
  public void testGetNthIndexFromBack_Empty() {
    assertEquals(-1, getNthIndexFromBack("", "-", 1));    
  }

  @Test
  public void testGetNthIndexFromBack_nEquals0() {
    assertEquals(-1, getNthIndexFromBack("a-b-c", "-", 0));    
  }

  @Test
  public void testGetRedirectHtmlFilename_Unknown() {
    assertEquals("MyApp-MyApp-1.0.0-20120821.132955-1-Unknown.zip.htm", getRedirectHtmlFilename("MyApp-1.0.0-20120821.132955-1-Unknown.zip", "MyApp"));
  }

  @Test
  public void testGetRedirectHtmlFilename_UnknownForFinalArtifact() {
    assertEquals("MyApp-MyApp-1.0.0-Unknown.zip.htm", getRedirectHtmlFilename("MyApp-1.0.0-Unknown.zip", "MyApp"));
  }

  
  @Test
  public void testGetRedirectHtmlFilename_AppStoreMetaData() {
    assertEquals("MyApp-AppStoreMetaData.zip.htm", getRedirectHtmlFilename("MyApp-1.0.0-20120821.132955-1-AppStoreMetaData.zip", "MyApp"));
  }

  @Test
  public void testGetRedirectHtmlFilename_AppStoreMetaDataForFinalArtifact() {
    assertEquals("MyApp-AppStoreMetaData.zip.htm", getRedirectHtmlFilename("MyApp-1.0.0-AppStoreMetaData.zip", "MyApp"));
  }

  
  @Test
  public void testGetRedirectHtmlFilename_OtaHtm() {
    assertEquals("MyApp-Release-iphoneos-ota.htm", getRedirectHtmlFilename("MyApp-1.0.0-20120821.132955-1-Release-iphoneos-ota.htm", "MyApp"));
  }

  @Test
  public void testGetRedirectHtmlFilename_OtaHtmForFinalArtifact() {
    assertEquals("MyApp-Release-iphoneos-ota.htm", getRedirectHtmlFilename("MyApp-1.0.0-Release-iphoneos-ota.htm", "MyApp"));
  }

  
  @Test
  public void testGetRedirectHtmlFilename_dSym() {
    assertEquals("MyApp-Release-iphoneos-app.dSYM.zip.htm", getRedirectHtmlFilename("MyApp-1.0.0-20120821.132955-1-Release-iphoneos-app.dSYM.zip", "MyApp"));
  }

  @Test
  public void testGetRedirectHtmlFilename_dSymForFinalArtifact() {
    assertEquals("MyApp-Release-iphoneos-app.dSYM.zip.htm", getRedirectHtmlFilename("MyApp-1.0.0-Release-iphoneos-app.dSYM.zip", "MyApp"));
  }

  
  @Test
  public void testGetRedirectHtmlFilename_appZip() {
    assertEquals("MyApp-Release-iphoneos-app.zip.htm", getRedirectHtmlFilename("MyApp-1.0.0-20120821.132955-1-Release-iphoneos-app.zip", "MyApp"));
  }

  @Test
  public void testGetRedirectHtmlFilename_appZipForFinalArtifact() {
    assertEquals("MyApp-Release-iphoneos-app.zip.htm", getRedirectHtmlFilename("MyApp-1.0.0-Release-iphoneos-app.zip", "MyApp"));
  }

  
  @Test
  public void testGetRedirectHtmlFilename_Ipa() {
    assertEquals("MyApp-Release-iphoneos.ipa.htm", getRedirectHtmlFilename("MyApp-1.0.0-20120821.132955-1-Release-iphoneos.ipa", "MyApp"));
  }

  @Test
  public void testGetRedirectHtmlFilename_IpaForFinalArtifact() {
    assertEquals("MyApp-Release-iphoneos.ipa.htm", getRedirectHtmlFilename("MyApp-1.0.0-Release-iphoneos.ipa", "MyApp"));
  }

  
  @Test
  public void testGetRedirectHtmlFilename_versionsXml() {
    assertEquals("MyApp-versions.xml.htm", getRedirectHtmlFilename("MyApp-1.0.0-20120821.132955-1-versions.xml", "MyApp"));
  }

  @Test
  public void testGetRedirectHtmlFilename_versionsXmlForFinalArtifact() {
    assertEquals("MyApp-versions.xml.htm", getRedirectHtmlFilename("MyApp-1.0.0-versions.xml", "MyApp"));
  }

  
  @Test
  public void testGetRedirectHtmlFilename_Pom() {
    assertEquals("MyApp.pom.htm", getRedirectHtmlFilename("MyApp-1.0.0-20120821.132955-1.pom", "MyApp"));
  }

  @Test
  public void testGetRedirectHtmlFilenameForFinalArtifact_Pom() {
    assertEquals("MyApp.pom.htm", getRedirectHtmlFilename("MyApp-1.0.0.pom", "MyApp"));
  }

  
  @Test
  public void testGetRedirectHtmlFilename_mavenMetadata() {
    assertEquals("MyApp-maven-metadata.xml.htm", getRedirectHtmlFilename("maven-metadata.xml", "MyApp"));
  }

}
