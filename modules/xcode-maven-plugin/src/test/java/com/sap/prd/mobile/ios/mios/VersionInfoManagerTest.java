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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.maven.it.util.IOUtil;
import org.junit.Test;

import com.sap.prd.mobile.ios.mios.versioninfo.v_1_2_2.Dependency;

public class VersionInfoManagerTest
{

  @Test
  public void testStraightForward() throws Exception
  {
    Dependency dependency = VersionInfoXmlManager.parseDependency(new File("src/test/resources/validXML.xml"));

    Assert.assertEquals("com.sap.ondevice.production.ios.tests", dependency.getCoordinates().getGroupId());
    Assert.assertEquals("MyApp", dependency.getCoordinates().getArtifactId());
    Assert.assertEquals("1.0.0", dependency.getCoordinates().getVersion());

    Assert.assertEquals("scm:perforce:PERFORCE_HOST:PORT://MY_DEPOT_PATH", dependency.getScm().getConnection());
    Assert.assertEquals("4711", dependency.getScm().getRevision());
    // To be continued

  }

  @Test
  public void testOldVersionInfoFormatAlpha20() throws Exception
  {
    List<Dependency> dependencies = new ArrayList<Dependency>();
    dependencies.add(VersionInfoXmlManager.parseDependency(new File("src/test/resources/versions-alpha-20.xml")));

    final ByteArrayOutputStream os = new ByteArrayOutputStream();

    try {

      new VersionInfoXmlManager().createVersionInfoFile("com.sap.ondevice.production.ios.tests", "MyApp", "1.0.0",
            new File(".", "src/test/resources/sync.info"), dependencies, os);

      Assert.assertEquals(IOUtil.toString(new FileInputStream(new File(
            "src/test/resources/versionInfoFileRewritten-alpha-20.xml").getAbsoluteFile()), "UTF-8"),
            IOUtil.toString(os.toByteArray(), "UTF-8"));
    }
    finally {
      IOUtils.closeQuietly(os);
    }
  }

  @Test
  public void testOldVersionInfoFormatBeta2() throws Exception
  {
    List<Dependency> dependencies = new ArrayList<Dependency>();
    dependencies.add(VersionInfoXmlManager.parseDependency(new File("src/test/resources/versions-beta-2.xml")));

    final ByteArrayOutputStream os = new ByteArrayOutputStream();

    try {

      new VersionInfoXmlManager().createVersionInfoFile("com.sap.ondevice.production.ios.tests", "MyApp", "1.0.0",
            new File(".", "src/test/resources/sync.info"), dependencies, os);

      Assert.assertEquals(IOUtil.toString(new FileInputStream(new File(
            "src/test/resources/versionInfoFileRewritten-beta-2.xml").getAbsoluteFile()), "UTF-8"),
            IOUtil.toString(os.toByteArray(), "UTF-8"));
    }
    finally {
      IOUtils.closeQuietly(os);
    }
  }

  @Test
  public void testOldVersionInfoFormatBeta3() throws Exception
  {
    List<Dependency> dependencies = new ArrayList<Dependency>();
    dependencies.add(VersionInfoXmlManager.parseDependency(new File("src/test/resources/versions-beta-3.xml")));

    final ByteArrayOutputStream os = new ByteArrayOutputStream();

    try {

      new VersionInfoXmlManager().createVersionInfoFile("com.sap.ondevice.production.ios.tests", "MyApp", "1.0.0",
            new File(".", "src/test/resources/sync.info"), dependencies, os);

      Assert.assertEquals(IOUtil.toString(new FileInputStream(new File(
            "src/test/resources/versionInfoFileRewritten-beta-3.xml").getAbsoluteFile()), "UTF-8"),
            IOUtil.toString(os.toByteArray(), "UTF-8"));
    }
    finally {
      IOUtils.closeQuietly(os);
    }
  }

}
