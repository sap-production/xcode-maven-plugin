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

/**
 * Helper methods for Xcode build to retrieve certain directories
 * 
 */
class XCodeBuildLayout
{

  //
  // TODO cleanup, reuse the methods
  //
  // TODO: we should introduce a class that encapsultes all parameters that
  // are relevant for distiguishing build artifacts.
  //

  static File getBinary(final File buildDir, final String configuration, final String sdk, final String projectName)
  {

    return new File(buildDir, configuration + "-" + sdk + "/lib" + projectName + ".a");
  }

  static File getBundleDirectory(final File srcDir, final String bundleName)
  {

    return new File(srcDir, bundleName + ".bundle");
  }

  // TODO invent better method name
  static File getAppFolder(final File baseDirectory, final String configuration, final String sdk)
  {
    return new File(getBuildDir(baseDirectory), configuration + "-" + sdk);
  }

  static File getBuildDir(final File baseDirectory)
  {

    if (baseDirectory == null)
      // exception here is required since File constructor below
      // does not fail when called with null value for baseDirectory.
      // We would not be acting fail fast without throwing an exception here.
      throw new IllegalStateException("baseDirectory was null");
    return new File(baseDirectory, "build");
  }
}
