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

import java.io.IOException;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

public class SCMUtilTest
{

  @Test
  public void testPerforceShowConfidentialInformation() throws Exception {
    
    Properties versionInfo = new Properties();
    versionInfo.setProperty("port", "scm.example.com:1234");
    versionInfo.setProperty("depotpath", "//root/TEST_Project/dev/");
    
    String connectionString = SCMUtil.getConnectionString(versionInfo, false);
    
    Assert.assertEquals("scm:perforce:scm.example.com:1234://root/TEST_Project/dev/", connectionString);
  }

  @Test
  public void testPerforceHideConfidentialInformation() throws Exception {
    
    Properties versionInfo = new Properties();
    versionInfo.setProperty("port", "scm.example.com:1234");
    versionInfo.setProperty("depotpath", "//root/TEST_Project/dev/");
    
    String connectionString = SCMUtil.getConnectionString(versionInfo, true);
    
    Assert.assertEquals("1234//root/TEST_Project/dev/", connectionString);
  }

  @Test
  public void testGitShowConfidentialInformation() throws Exception {
    
    Properties versionInfo = new Properties();
    versionInfo.setProperty("type", "git");
    versionInfo.setProperty("repo", "ssh://scm.example.com:1234/MyProject");
    
    String connectionString = SCMUtil.getConnectionString(versionInfo, false);
    
    Assert.assertEquals("scm:git:ssh://scm.example.com:1234/MyProject", connectionString);
  }

  @Test
  public void testGitHideConfidentialInformationPortNotSpecified() throws Exception {
    
    Properties versionInfo = new Properties();
    versionInfo.setProperty("type", "git");
    versionInfo.setProperty("repo", "ssh://scm.example.com/MyProject");
    
    String connectionString = SCMUtil.getConnectionString(versionInfo, true);
    
    Assert.assertEquals("29418/MyProject", connectionString); // git default ssh port.
  }

  
  @Test
  public void testGitHideConfidentialInformation() throws Exception {
    
    Properties versionInfo = new Properties();
    versionInfo.setProperty("type", "git");
    versionInfo.setProperty("repo", "ssh://scm.example.com:1234/MyProject");
    
    String connectionString = SCMUtil.getConnectionString(versionInfo, true);
    
    Assert.assertEquals("1234/MyProject", connectionString);
  }

  @Test
  public void testGitHideConfidentialInformationWithoutPortProtocolSSH() throws Exception {
    
    Properties versionInfo = new Properties();
    versionInfo.setProperty("type", "git");
    versionInfo.setProperty("repo", "ssh://scm.example.com/MyProject");
    
    String connectionString = SCMUtil.getConnectionString(versionInfo, true);
    
    Assert.assertEquals("29418/MyProject", connectionString);
  }

  @Test
  public void testGitHideConfidentialInformationWithoutPortProtocolHTTP() throws Exception {
    
    Properties versionInfo = new Properties();
    versionInfo.setProperty("type", "git");
    versionInfo.setProperty("repo", "http://scm.example.com/MyProject");
    
    String connectionString = SCMUtil.getConnectionString(versionInfo, true);
    
    Assert.assertEquals("80/MyProject", connectionString);
  }

  @Test
  public void testGitHideConfidentialInformationWithoutPortProtocolHTTPS() throws Exception {
    
    Properties versionInfo = new Properties();
    versionInfo.setProperty("type", "git");
    versionInfo.setProperty("repo", "https://scm.example.com/MyProject");
    
    String connectionString = SCMUtil.getConnectionString(versionInfo, true);
    
    Assert.assertEquals("443/MyProject", connectionString);
  }

  @Test
  public void testGitHideConfidentialInformationWithoutPortProtocolGIT() throws Exception {
    
    Properties versionInfo = new Properties();
    versionInfo.setProperty("type", "git");
    versionInfo.setProperty("repo", "git://scm.example.com/MyProject");
    
    String connectionString = SCMUtil.getConnectionString(versionInfo, true);
    
    Assert.assertEquals("9418/MyProject", connectionString);
  }

  @Test
  public void testGitHideConfidentialInformationWithoutPortAndWithoutProtocol() throws Exception {
    
    Properties versionInfo = new Properties();
    versionInfo.setProperty("type", "git");
    versionInfo.setProperty("repo", "//scm.example.com/MyProject");
    
    String connectionString = SCMUtil.getConnectionString(versionInfo, true);
    
    Assert.assertEquals("-1/MyProject", connectionString);
  }

  
  @Test
  public void testGitHideConfidentialInformationWithoutHostAndPortAndWithoutProtocol() throws Exception {
    
    Properties versionInfo = new Properties();
    versionInfo.setProperty("type", "git");
    versionInfo.setProperty("repo", "scm.example.com/MyProject");
    
    String connectionString = SCMUtil.getConnectionString(versionInfo, true);
    
    Assert.assertEquals("-1", connectionString);
  }

  @Test(expected=IllegalStateException.class)
  public void testPerforceNoPort() throws Exception {
    Properties versionInfo = new Properties();
    SCMUtil.getConnectionString(versionInfo, true);    
  }
  
  @Test(expected=IllegalStateException.class)
  public void testGitNoPort() throws Exception {
    Properties versionInfo = new Properties();
    versionInfo.setProperty("type", "git"); 
    SCMUtil.getConnectionString(versionInfo, true);    
  }
  
  @Test(expected=IllegalStateException.class)
  public void testPerforceShowConfidentialInformationDepotPathMissing() throws Exception {
    
    Properties versionInfo = new Properties();
    versionInfo.setProperty("port", "scm.example.com:1234");    
    SCMUtil.getConnectionString(versionInfo, false);
  }

  @Test(expected=IllegalStateException.class)
  public void testGitMultipleRepositories() throws Exception {

    Properties versionInfo = new Properties();
    versionInfo.setProperty("type", "git");
    versionInfo.setProperty("repo_1", "scm.example1.com:1234");    
    versionInfo.setProperty("repo_2", "scm.example2.com:1234");    
    
    SCMUtil.getConnectionString(versionInfo, false);
  }
  
}
