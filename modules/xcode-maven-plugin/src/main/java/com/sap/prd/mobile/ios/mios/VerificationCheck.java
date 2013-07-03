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

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

public abstract class VerificationCheck
{
  private XCodeContext context;
  private Log log;
  private MavenProject mavenProject;

  protected MavenProject getMavenProject()
  {
    return mavenProject;
  }
  void setMavenProject(MavenProject mavenProject)
  {
    this.mavenProject = mavenProject;
  }
  final void setXcodeContext(XCodeContext context) {
    this.context = context;
  }
  final void setLog(Log log) {
    this.log = log;
  }

  protected Log getLog() {
    return this.log;
  }

  protected XCodeContext getXcodeContext() {
    return this.context;
  }

  public abstract void check() throws VerificationException;
}
