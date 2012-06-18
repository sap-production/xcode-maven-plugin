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
package com.sap.prd.mobile.ios.mios.versioninfo.v_0_0_0;

public class SCM
{

  private String repository;
  private String path;
  private String snapshotId;

  public String getRepository()
  {
    return repository;
  }

  public void setRepository(String repository)
  {
    this.repository = repository;
  }

  public String getPath()
  {
    return path;
  }

  public void setPath(String path)
  {
    this.path = path;
  }

  public String getSnapshotId()
  {
    return snapshotId;
  }

  public void setSnapshotId(String snapshotId)
  {
    this.snapshotId = snapshotId;
  }

  @Override
  public String toString()
  {
    return "SCM [repository=" + repository + ", path=" + path + ", snapshotId=" + snapshotId + "]";
  }

}
