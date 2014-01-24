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

import static com.sap.prd.mobile.ios.mios.versioninfo.Coordinates.ARTIFACT_ID;
import static com.sap.prd.mobile.ios.mios.versioninfo.Coordinates.GROUP_ID;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.sap.prd.mobile.ios.mios.versioninfo.v_1_2_2.Coordinates;
import com.sap.prd.mobile.ios.mios.versioninfo.v_1_2_2.Dependency;
import com.sap.prd.mobile.ios.mios.versioninfo.v_1_2_2.SCM;

public class VersionInfoPListManagerTest
{

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();
  private File plistFile;
  private PListAccessor plistAccessor;

  @Before
  public void setup() throws MojoExecutionException
  {
    tempFolder.newFolder("versionPListTest");
    plistFile = new File(tempFolder.getRoot(), "versions.plist");
    new VersionInfoPListManager().createVersionInfoPlistFile("groupId", "artifactId", "1.0.0", new File(".",
          "src/test/resources/sync.info"), new ArrayList<Dependency>(), plistFile, true);
    plistAccessor = new PListAccessor(plistFile);
  }

  @Test
  public void testStraightForward() throws Exception
  {

    Assert.assertTrue(plistFile.exists());
    Assert.assertEquals(GROUP_ID, plistAccessor.printValue("coordinates:groupId"));
    Assert.assertEquals(ARTIFACT_ID, plistAccessor.printValue("coordinates:artifactId"));
    Assert.assertEquals("1.0.0", plistAccessor.printValue("coordinates:version"));

    Assert.assertEquals("9876", plistAccessor.printValue("scm:connection"));
    Assert.assertEquals("1234", plistAccessor.printValue("scm:revision"));
  }

  @Test(expected = IllegalStateException.class)
  public void testDepDontExistsForward() throws IOException
  {
    plistAccessor.printValue("dependencies:0");
  }

  @Test
  public void testSimpleDependency() throws Exception
  {
    Dependency dep = new Dependency();
    Coordinates coord = new Coordinates();
    coord.setArtifactId("depArtifactId");
    coord.setGroupId("depGroupId");
    coord.setVersion("depVersion");

    SCM scm = new SCM();
    scm.setConnection("scm:perforce:DEPPERFORCEHOST:9999://DEP_DEPOT_PATH/");
    scm.setRevision("depRevision");

    dep.setCoordinates(coord);
    dep.setScm(scm);

    List<Dependency> dependencies = new ArrayList<Dependency>();
    dependencies.add(dep);
    new VersionInfoPListManager().addDependencyToPlist(dependencies, plistAccessor, "dependencies:", true);

    Assert.assertEquals("depArtifactId", plistAccessor.printValue("dependencies:0:coordinates:artifactId"));
    Assert.assertEquals("depGroupId", plistAccessor.printValue("dependencies:0:coordinates:groupId"));
    Assert.assertEquals("depVersion", plistAccessor.printValue("dependencies:0:coordinates:version"));

    Assert.assertEquals("9999",
          plistAccessorPerforce.printValue("dependencies:0:scm:connection"));
    Assert.assertEquals("depRevision", plistAccessorPerforce.printValue("dependencies:0:scm:revision"));

  }

  @Test(expected = IllegalStateException.class)
  public void testTransitiveDependency() throws Exception
  {
    Dependency dep = new Dependency();
    Coordinates coord = new Coordinates();
    coord.setArtifactId("depArtifactId");
    coord.setGroupId("depGroupId");
    coord.setVersion("depVersion");

    SCM scm = new SCM();
    scm.setConnection("scm:perforce:DEPPERFORCEPORT://DEP_DEPOT_PATH/");
    scm.setRevision("depRevision");

    dep.setCoordinates(coord);
    dep.setScm(scm);

    Dependency transitivDep = new Dependency();
    Coordinates transitivDepCoord = new Coordinates();
    transitivDepCoord.setArtifactId("transitivDepArtifactId");
    transitivDepCoord.setGroupId("transitivDepGroupId");
    transitivDepCoord.setVersion("transitiveDepVersion");

    SCM transitivDepScm = new SCM();
    transitivDepScm.setConnection("scm:perforce:TRANSDEPPERFORCEHOST:9999://TRANS_DEP_DEPOT_PATH/");
    transitivDepScm.setRevision("transitiveDepRevision");

    transitivDep.setCoordinates(transitivDepCoord);
    transitivDep.setScm(transitivDepScm);

    dep.addDependency(transitivDep);
    List<Dependency> dependencies = new ArrayList<Dependency>();
    dependencies.add(dep);

    new VersionInfoPListManager().addDependencyToPlist(dependencies, plistAccessor, "dependencies:", true);

    Assert.assertEquals("transitivDepArtifactId",
          plistAccessor.printValue("dependencies:0:dependencies:0:coordinates:artifactId"));
    Assert.assertEquals("transitivDepGroupId",
          plistAccessor.printValue("dependencies:0:dependencies:0:coordinates:groupId"));
    Assert.assertEquals("transitiveDepVersion",
          plistAccessor.printValue("dependencies:0:dependencies:0:coordinates:version"));

    Assert.assertEquals("9999",
          plistAccessorPerforce.printValue("dependencies:0:dependencies:0:scm:connection"));
    Assert
      .assertEquals("transitiveDepRevision", plistAccessor.printValue("dependencies:0:dependencies:0:scm:revision"));

    plistAccessor.printValue("dependencies:0:dependencies:1:scm:revision");

  }
}
