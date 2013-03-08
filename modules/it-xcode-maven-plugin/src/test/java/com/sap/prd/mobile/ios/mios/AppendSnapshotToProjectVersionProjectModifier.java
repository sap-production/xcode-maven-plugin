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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

public class AppendSnapshotToProjectVersionProjectModifier extends ProjectModifier
{

  @Override
  public void execute() throws Exception
  {
    final File pom = new File(testExecutionDirectory, "pom.xml");
    FileInputStream fis = null;
    FileOutputStream fos = null;

    try {
      fis = new FileInputStream(pom);
      final Model model = new MavenXpp3Reader().read(fis);
      fis.close();
      fos = new FileOutputStream(pom);
      model.setVersion(model.getVersion() + "-SNAPSHOT");
      new MavenXpp3Writer().write(fos,  model);
    } finally {
      IOUtils.closeQuietly(fis);
      IOUtils.closeQuietly(fos);
    }
  }

}
