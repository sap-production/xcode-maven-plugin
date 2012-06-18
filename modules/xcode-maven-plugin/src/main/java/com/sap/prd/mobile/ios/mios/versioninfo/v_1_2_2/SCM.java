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
package com.sap.prd.mobile.ios.mios.versioninfo.v_1_2_2;

public class SCM
{

  private String connection;
  private String revision;

  public String getConnection()
  {
    return connection;
  }

  public void setConnection(String connection)
  {
    this.connection = connection;
  }

  public String getRevision()
  {
    return revision;
  }

  public void setRevision(String revision)
  {
    this.revision = revision;
  }

  @Override
  public String toString()
  {
    return "SCM [connection=" + connection + ", revision=" + revision + "]";
  }

}
