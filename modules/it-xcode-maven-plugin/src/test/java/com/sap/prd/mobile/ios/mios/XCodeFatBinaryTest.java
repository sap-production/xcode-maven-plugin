/*
 * #%L
 * it-xcode-maven-plugin
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

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class XCodeFatBinaryTest extends XCodeTest
{

  @Test
  public void testFatBinary() throws Exception
  {

    final String testName = Thread.currentThread().getStackTrace()[1].getMethodName();

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass()
      .getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    test(testName, new File(getTestRootDirectory(), "fat-binary/MyLibrary"),
          "pom.xml", "deploy",
          THE_EMPTY_LIST, THE_EMPTY_MAP, remoteRepositoryDirectory);

    assertTrue(new File(remoteRepositoryDirectory,
          "com/sap/ondevice/production/ios/tests/MyLibrary/1.0.1/MyLibrary-1.0.1-"
                + "Debug" + "-fat-binary.a").exists());

    assertTrue(new File(remoteRepositoryDirectory,
          "com/sap/ondevice/production/ios/tests/MyLibrary/1.0.1/MyLibrary-1.0.1-"
                + "Release" + "-fat-binary.a").exists());
  }
}
