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

public enum PackagingType
{
  APP("xcode-app"),
  LIB("xcode-lib"),
  FRAMEWORK("xcode-framework");

  private final String packagingName;

  private PackagingType(String mavenPackaging)
  {
    this.packagingName = mavenPackaging;
  }

  public String getMavenPackaging()
  {
    return packagingName;
  }

  public static PackagingType getByMavenType(String mavenPackaging)
  {
    for (PackagingType type : PackagingType.values())
    {
      if (type.getMavenPackaging().equals(mavenPackaging))
      {
        return type;
      }
    }
    throw new IllegalArgumentException("Maven Packaging type '" + mavenPackaging + "' does not exist");
  }
}
