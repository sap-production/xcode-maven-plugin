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

  public static PackagingType getByMavenType(String mavenPackaging) throws UnknownPackagingTypeException
  {
    if (mavenPackaging == null)
      throw new IllegalArgumentException("No packaging provided.");

    for (PackagingType type : PackagingType.values())
    {
      if (type.getMavenPackaging().equals(mavenPackaging))
      {
        return type;
      }
    }
    throw new UnknownPackagingTypeException("Packaging type '" + mavenPackaging + "' is not handled by xcode-maven-plugin.");
  }
  
  static class UnknownPackagingTypeException extends XCodeException{

    private static final long serialVersionUID = -4039520479565138503L;

    UnknownPackagingTypeException(String message)
    {
      super(message);
    }
  }
}
