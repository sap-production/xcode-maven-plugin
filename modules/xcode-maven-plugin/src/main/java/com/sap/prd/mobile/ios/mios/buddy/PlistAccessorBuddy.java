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
package com.sap.prd.mobile.ios.mios.buddy;

import java.io.File;

import org.apache.maven.project.MavenProject;

import com.sap.prd.mobile.ios.mios.EffectiveBuildSettings;
import com.sap.prd.mobile.ios.mios.PListAccessor;
import com.sap.prd.mobile.ios.mios.XCodeException;

public class PlistAccessorBuddy
{

  /**
   * Retrieves the Info Plist out of the effective Xcode project settings and returns the accessor
   * to it.
   * 
   * @param xcodeProjectDirectory
   *          the directory where the Xcode project is located. If you want to access the unmodified
   *          Plist (i.e. AppID not appended) use the {@link #getXCodeSourceDirectory()} method, if
   *          you want to access the modified plist, use the {@link #getXCodeCompileDirectory()}
   *          method.
   */
  public static PListAccessor getInfoPListAccessor(MavenProject project, File xcodeProjectDirectory,
        String configuration, String sdk) throws XCodeException
  {
    String plistFileName = new EffectiveBuildSettings(project, configuration, sdk)
      .getBuildSetting(EffectiveBuildSettings.INFOPLIST_FILE);
    File plistFile = new File(xcodeProjectDirectory, plistFileName);
    if (!plistFile.isFile()) {
      throw new XCodeException("The Xcode project refers to the Info.plist file '" + plistFileName
            + "' that does not exist.");
    }
    return new PListAccessor(plistFile);
  }

}
