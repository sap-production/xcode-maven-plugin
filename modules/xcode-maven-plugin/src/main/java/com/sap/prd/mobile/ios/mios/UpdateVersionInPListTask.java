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

import org.apache.maven.plugin.logging.Log;

abstract class UpdateVersionInPListTask
{
  protected File plistFile;
  protected String version;
  protected Log log;
  
  final UpdateVersionInPListTask setPListFile(File plistFile)
  {
    this.plistFile = plistFile;
    return this;
  }

  final UpdateVersionInPListTask setVersion(String version)
  {
    this.version = version;
    return this;
  }

  final UpdateVersionInPListTask setLog(Log log)
  {
    this.log = log;
    return this;
  }
  
  void execute() throws XCodeException {

    if(version == null || version.isEmpty()) {
      throw new IllegalArgumentException("No version provided: '" + version + "'.");
    }
    
    if(plistFile == null) {
      throw new IllegalArgumentException("No PlistFile provided.");
    }
    
    if(!plistFile.exists())
      throw new IllegalArgumentException("PlistFile '" + plistFile + "' does not exist.");
    
    if(!plistFile.isFile()) {
      throw new IllegalArgumentException("PlistFile '" + plistFile + "' is not a file.");
    }
  }
  
  protected void updateProperty(PListAccessor infoPlistAccessor, String key, String newValue) throws XCodeException {

    try {
      
      final String oldValue = infoPlistAccessor.getStringValue(key);

      if(oldValue == null) {
      
        infoPlistAccessor.addStringValue(key, newValue);
        log.info(key + " was not present in PList '" + infoPlistAccessor.getPlistFile() + ". Entry has been added with value '" + newValue + "'.");
        return;
        
      } else if(oldValue.equals(newValue)) {
        log.info(key + " in PList '" + infoPlistAccessor.getPlistFile() + "' file is already up-to-date (" + oldValue + "). No update needed.");
        return;
      }
             
      infoPlistAccessor.updateStringValue(key, newValue);
      log.info("PList file '" + infoPlistAccessor.getPlistFile() + "' updated: Set " + key + " to '" + version + "'.");
    }
    catch (IOException e) {
      throw new XCodeException(e.getMessage(), e);
    }
  }
}
