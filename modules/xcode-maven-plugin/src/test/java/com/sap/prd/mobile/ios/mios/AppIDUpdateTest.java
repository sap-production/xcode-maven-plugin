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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.sap.prd.mobile.ios.mios.xcodeprojreader.BuildConfiguration;
import com.sap.prd.mobile.ios.mios.xcodeprojreader.Plist;
import com.sap.prd.mobile.ios.mios.xcodeprojreader.ProjectFile;
import com.sap.prd.mobile.ios.mios.xcodeprojreader.jaxb.JAXBPlistParser;

public class AppIDUpdateTest extends XCodeTest
{

  private static File projectDirectory;

  @BeforeClass
  public static void setupProjectDirectory()
  {
    projectDirectory = new File(new File(".").getAbsoluteFile(), "target/tests/"
          + AppIDUpdateTest.class.getName());

  }

  @Before
  public void ensureCleanProjectDirectoryAndFilterPom() throws Exception
  {
    ensureCleanProjectDirectoryAndFilterPom(projectDirectory);
  }

  @After
  public void cleanupProjectDirectory() throws Exception
  {
    MacFileUtil.setWritableRecursive(true, projectDirectory);
    MacFileUtil.deleteDirectory(projectDirectory);
  }

  @Test
  public void testUpdateAppID() throws Exception
  {

    final Log log = EasyMock.createNiceMock(Log.class);

    EasyMock.replay(log);

    File projectDirectoryApplication = new File(projectDirectory, "MyApp");

    PlistManager plistMgr = new PlistManager(log, new File(projectDirectoryApplication, "src/xcode"), "MyApp");
    BuildConfiguration buildConfig = getBuildConfig(plistMgr.getXCodeProjectFile(), "Release");
    plistMgr.changeAppId("internal", new HashSet<File>(), buildConfig);

    PListAccessor plist = new PListAccessor(new File(new File(projectDirectoryApplication, "src/xcode"),
          "MyApp-Info.plist"));

    Assert.assertEquals("com.sap.tip.production.inhouse.epdist.internal",
          plist.getStringValue("CFBundleIdentifier"));
  }

  /**
   * The Xcode project configuration refers to a plist file that does not exist. It is assured, that
   * a correct exception is raised.
   */
  @Test
  public void testMissingPlist() throws Exception
  {
    final Log log = EasyMock.createNiceMock(Log.class);

    EasyMock.replay(log);

    File projectDirectoryApplication = new File(projectDirectory, "MissingPlistApp");
    PlistManager plistMgr = new PlistManager(log, new File(projectDirectoryApplication, "src/xcode"), "MyApp");
    BuildConfiguration buildConfig = getBuildConfig(plistMgr.getXCodeProjectFile(), "Release");
    try {
      plistMgr.changeAppId("internal", new HashSet<File>(), buildConfig);
    }
    catch (MojoExecutionException ex) {
      String msgPattern = "The Xcode project refers to the Info\\.plist file .* that does not exist.";
      Pattern pattern = Pattern.compile(msgPattern, Pattern.CASE_INSENSITIVE);
      Matcher matcher = pattern.matcher(ex.getMessage());
      Assert.assertTrue(
            "Wrong exception message. Expected the pattern '" + msgPattern + "' but was " + ex.getMessage(),
            matcher.find());
    }
  }

  private BuildConfiguration getBuildConfig(File xCodeProjectFile, String buildConfigName) throws IOException,
        SAXException, ParserConfigurationException, JAXBException
  {
    JAXBPlistParser parser = new JAXBPlistParser();
    parser.convert(xCodeProjectFile, xCodeProjectFile);
    Plist plist = parser.load(xCodeProjectFile.getCanonicalPath());
    ProjectFile projFile = new ProjectFile(plist);
    return projFile.getProject().getTargets().get(0).getBuildConfigurationList().getBuildConfigurations().getByName(buildConfigName);

  }
}
