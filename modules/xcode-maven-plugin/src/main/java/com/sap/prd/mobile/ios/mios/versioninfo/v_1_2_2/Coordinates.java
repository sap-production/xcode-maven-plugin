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

import static com.sap.prd.mobile.ios.mios.versioninfo.Coordinates.ARTIFACT_ID;
import static com.sap.prd.mobile.ios.mios.versioninfo.Coordinates.GROUP_ID;
import static com.sap.prd.mobile.ios.mios.versioninfo.Coordinates.VERSION;
import static java.lang.String.format;

import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = { GROUP_ID, ARTIFACT_ID, VERSION })
public class Coordinates
{

  private String groupId;
  private String artifactId;
  private String version;

  public String getGroupId()
  {
    return groupId;
  }

  public void setGroupId(String groupId)
  {
    this.groupId = groupId;
  }

  public String getArtifactId()
  {
    return artifactId;
  }

  public void setArtifactId(String artifactId)
  {
    this.artifactId = artifactId;
  }

  public String getVersion()
  {
    return version;
  }

  public void setVersion(String version)
  {
    this.version = version;
  }

  @Override
  public String toString()
  {
    return format("Coordinates [%s=%s, %s=%s, %s=%s]", GROUP_ID, groupId, ARTIFACT_ID, artifactId, VERSION, version);
  }

}
