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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

abstract class AbstractProjectModifier extends ProjectModifier
{

  protected Model getModel(File pom) throws IOException, XmlPullParserException{

    InputStream is = null;

    try {
      is = new FileInputStream(pom);
      return new MavenXpp3Reader().read(is);
    } finally {
      IOUtils.closeQuietly(is);
    }
  }
  
  protected void persistModel(File pom, Model model) throws IOException {

    OutputStream os = null;

    try {
      os = new FileOutputStream(pom);
      new MavenXpp3Writer().write(os,  model);
    } finally {
      IOUtils.closeQuietly(os);
    }
  }
}
