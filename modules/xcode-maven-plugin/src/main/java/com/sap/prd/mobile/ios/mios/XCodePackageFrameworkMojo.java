package com.sap.prd.mobile.ios.mios;

/*
 * #%L
 * Xcode Maven Plugin
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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Packages the framework built by Xcode and prepares the generated artifact for deployment.
 * 
 * @goal package-framework
 * 
 */
public class XCodePackageFrameworkMojo extends AbstractXCodeMojo
{

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {

    PackageFrameworkTask task = new PackageFrameworkTask();
    task.setLog(getLog()).setMavenProject(project).setPrimaryFmwkConfiguration(getPrimaryFmwkConfiguration());
    try {
      task.execute();
    }
    catch (XCodeException ex) {
      throw new MojoExecutionException("Could not package the framework", ex);
    }
  }



}
