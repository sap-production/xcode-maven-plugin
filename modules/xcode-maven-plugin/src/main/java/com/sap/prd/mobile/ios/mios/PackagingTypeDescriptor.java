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

public class PackagingTypeDescriptor
{

  private String packagingType;
  private String unarchiverId;
  private String action;

  @Override
  public String toString()
  {
    return String.format("PackagingTypeDescriptor [packagingType=%s, unarchiverId=%s, action=%s]", getPackagingType(),
          getUnarchiverId(), getAction());
  }
  public String getPackagingType()
  {
    return packagingType;
  }
  public PackagingTypeDescriptor setPackagingType(String packagingType)
  {
    this.packagingType = packagingType;
    return this;
  }
  public String getUnarchiverId()
  {
    return unarchiverId;
  }
  public PackagingTypeDescriptor setUnarchiverId(String unarchiverId)
  {
    this.unarchiverId = unarchiverId;
    return this;
  }
  public String getAction()
  {
    return action;
  }
  public PackagingTypeDescriptor setAction(String action)
  {
    this.action = action;
    return this;
  }
  
  
}
