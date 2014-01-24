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

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

public class ConnectionStringProviderTest
{

  @Test
  public void testPerforceShowConfidentialInformation() throws Exception {
    
    Properties versionInfo = new Properties();
    versionInfo.setProperty("port", "scm.example.com:1234");
    versionInfo.setProperty("depotpath", "//root/TEST_Project/dev/");
    
    String connectionString = ConnectionStringProvider.getConnectionString(versionInfo, false);
    
    Assert.assertEquals("scm:perforce:scm.example.com:1234://root/TEST_Project/dev/", connectionString);
  }

  @Test
  public void testPerforceHideConfidentialInformation() throws Exception {
    
    Properties versionInfo = new Properties();
    versionInfo.setProperty("port", "scm.example.com:1234");
    versionInfo.setProperty("depotpath", "//root/TEST_Project/dev/");
    
    String connectionString = ConnectionStringProvider.getConnectionString(versionInfo, true);
    
    Assert.assertEquals("1234", connectionString);
  }

  @Test
  public void testGitShowConfidentialInformation() throws Exception {
    
    Properties versionInfo = new Properties();
    versionInfo.setProperty("type", "git");
    versionInfo.setProperty("port", "ssh://scm.example.com:1234/MyProject");
    
    String connectionString = ConnectionStringProvider.getConnectionString(versionInfo, false);
    
    Assert.assertEquals("scm:git:ssh://scm.example.com:1234/MyProject", connectionString);
  }

  @Test
  public void testGitHideConfidentialInformationPortNotSpecified() throws Exception {
    
    Properties versionInfo = new Properties();
    versionInfo.setProperty("type", "git");
    versionInfo.setProperty("port", "ssh://scm.example.com/MyProject");
    
    String connectionString = ConnectionStringProvider.getConnectionString(versionInfo, true);
    
    Assert.assertEquals("29418", connectionString); // git default ssh port.
  }

  
  @Test
  public void testGitHideConfidentialInformation() throws Exception {
    
    Properties versionInfo = new Properties();
    versionInfo.setProperty("type", "git");
    versionInfo.setProperty("port", "ssh://scm.example.com:1234/MyProject");
    
    String connectionString = ConnectionStringProvider.getConnectionString(versionInfo, true);
    
    Assert.assertEquals("1234", connectionString);
  }
  
  @Test(expected=IllegalStateException.class)
  public void testPerforceNoPort() throws Exception {
    Properties versionInfo = new Properties();
    ConnectionStringProvider.getConnectionString(versionInfo, true);    
  }
  
  @Test(expected=IllegalStateException.class)
  public void testGitNoPort() throws Exception {
    Properties versionInfo = new Properties();
    versionInfo.setProperty("type", "git"); 
    ConnectionStringProvider.getConnectionString(versionInfo, true);    
  }
  
  @Test(expected=IllegalStateException.class)
  public void testPerforceShowConfidentialInformationDepotPathMissing() throws Exception {
    
    Properties versionInfo = new Properties();
    versionInfo.setProperty("port", "scm.example.com:1234");    
    ConnectionStringProvider.getConnectionString(versionInfo, false);
  }
  
}
