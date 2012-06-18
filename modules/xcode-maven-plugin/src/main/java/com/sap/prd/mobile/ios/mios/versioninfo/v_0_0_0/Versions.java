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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "coordinates", "scm", "dependencies" })
public class Versions
{

  private Coordinates coordinates;
  private SCM scm;

  @XmlElementWrapper(name = "dependencies")
  @XmlElement(name = "dependency")
  private List<Dependency> dependencies = null;

  public Coordinates getCoordinates()
  {
    return coordinates;
  }

  public void setCoordinates(Coordinates coordinates)
  {
    this.coordinates = coordinates;
  }

  public SCM getScm()
  {
    return scm;
  }

  public void setScm(SCM scm)
  {
    this.scm = scm;
  }

  public void addDependency(Dependency dependency)
  {
    if (dependencies == null)
      dependencies = new ArrayList<Dependency>();

    dependencies.add(dependency);
  }

  public List<Dependency> getDependencies()
  {
    return dependencies != null ? dependencies : Collections.unmodifiableList(new ArrayList<Dependency>());
  }

  @Override
  public String toString()
  {
    return "Versions [coordinates=" + coordinates + ", scm=" + scm + ", dependencies=" + dependencies + "]";
  }
}
