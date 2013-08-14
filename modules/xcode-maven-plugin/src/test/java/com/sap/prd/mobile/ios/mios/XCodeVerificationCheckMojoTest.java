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
import static com.sap.prd.mobile.ios.mios.versioninfo.Coordinates.VERSION;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.sonatype.aether.artifact.Artifact;

import com.sap.prd.mobile.ios.mios.XCodeVerificationCheckMojo.InvalidProtocolException;
import com.sap.prd.mobile.ios.mios.XCodeVerificationCheckMojo.Location;
import com.sap.prd.mobile.ios.mios.XCodeVerificationCheckMojo.NoProtocolException;
import com.sap.prd.mobile.ios.mios.verificationchecks.v_1_0_0.Check;
import com.sap.prd.mobile.ios.mios.verificationchecks.v_1_0_0.Checks;

public class XCodeVerificationCheckMojoTest
{

  private static Logger log = new XCodePluginLogger();
  static {
    LogManager.getLogManager().addLogger(log);
  }

  @Test
  public void testNoGav() throws Exception
  {
    Checks checks = loadChecks("src/test/checks/noGAV.xml");

    Set<Artifact> dependencies = new HashSet<Artifact>();

    for (Check check : checks.getCheck())
    {
      Artifact dep = XCodeVerificationCheckMojo.parseDependency(check);
      if (dep != null)
      {
        dependencies.add(dep);
      }
    }
    assertEquals("Check dependencies number unexpected", 0, dependencies.size());
  }

  @Test
  public void testEmptyGav() throws Exception
  {
    Checks checks = loadChecks("src/test/checks/emptyGAV.xml");

    Set<Artifact> dependencies = new HashSet<Artifact>();

    for (Check check : checks.getCheck())
    {
      Artifact dep = XCodeVerificationCheckMojo.parseDependency(check);
      if (dep != null) {
        dependencies.add(dep);
      }
    }
    assertEquals("Check dependencies number unexpected", 1, dependencies.size());
  }

  @Test
  public void testMissingGroupId() throws Exception
  {
    testPartOfGavInvalidOrMissing("src/test/checks/noGroupId.xml", GROUP_ID, "myGroupId");
  }

  @Test
  public void testMissingArtifactId() throws Exception
  {
    testPartOfGavInvalidOrMissing("src/test/checks/noArtifactId.xml", ARTIFACT_ID, "myArtifactId");
  }

  @Test
  public void testMissingVersion() throws Exception
  {
    testPartOfGavInvalidOrMissing("src/test/checks/noVersion.xml", VERSION, "1.0.0");
  }

  @Test
  public void testEmptyGroupId() throws Exception
  {
    testPartOfGavInvalidOrMissing("src/test/checks/emptyGroupId.xml", GROUP_ID, "myGroupId");
  }

  @Test
  public void testEmptyArtifactId() throws Exception
  {
    testPartOfGavInvalidOrMissing("src/test/checks/emptyArtifactId.xml", ARTIFACT_ID, "myArtifactId");
  }

  @Test
  public void testEmptyVersion() throws Exception
  {
    testPartOfGavInvalidOrMissing("src/test/checks/emptyVersion.xml", VERSION, "1.0.0");
  }

  @Test
  public void testGetCheckDescriptorWithFileProtocol() throws Exception, IOException
  {
    Reader r = null;

    try {
      r = XCodeVerificationCheckMojo.getChecksDescriptor("file:src/test/checks/checks.xml");
      JAXBContext.newInstance(Checks.class).createUnmarshaller().unmarshal(r);
    }
    finally {
      IOUtils.closeQuietly(r);
    }
  }

  @Test(expected = XCodeVerificationCheckMojo.InvalidProtocolException.class)
  public void testGetCheckDescriptorWithInvalidProtocol() throws Exception, IOException
  {
    XCodeVerificationCheckMojo.getChecksDescriptor("ftp://example.com");
  }

  @Test(expected = NoProtocolException.class)
  public void testGetCheckDescriptorWithoutProtocol() throws Exception, IOException
  {
    XCodeVerificationCheckMojo.getChecksDescriptor("example.com");
  }

  private void testPartOfGavInvalidOrMissing(String location, String attNameFix, String attValueFix) throws Exception
  {
    Checks checks = loadChecks(location);

    for (Check check : checks.getCheck())
    {
      try {
        XCodeVerificationCheckMojo.parseDependency(check);
        fail("Dependency could be parsed. Expected was missing attribute '" + attNameFix + "'.");
      }
      catch (XCodeException ex) {
      }

      Method m = Check.class.getMethod(getSetterName(attNameFix), new Class[] { String.class });
      m.invoke(checks.getCheck().get(0), new Object[] { attValueFix });
      Artifact dependency = XCodeVerificationCheckMojo.parseDependency(check);

      if (dependency == null)
      {
        fail("Dependency could not be parsed after fix. Fixed attribute is: '" + attNameFix + "'.");
      }
    }
  }

  private Checks loadChecks(String location) throws JAXBException
  {
    return (Checks) JAXBContext.newInstance(Checks.class).createUnmarshaller().unmarshal(new File(location));
  }

  private static String getSetterName(String attNameFix)
  {
    return "set" + firstCharToUpperCase(attNameFix);
  }

  private static String firstCharToUpperCase(String str)
  {
    char[] c = new char[str.length()];
    str.getChars(0, str.length(), c, 0);
    c[0] = Character.toUpperCase(c[0]);
    return new String(c);
  }

  @Test(expected = NoProtocolException.class)
  public void testValidateLocationNoColon() throws InvalidProtocolException, NoProtocolException, MalformedURLException
  {
    testValidateLocation("a/b/c.xml", "FILE", "a/b/c.xml");
  }

  @Test
  public void testValidateLocationNoDoubleSlash() throws InvalidProtocolException, NoProtocolException, MalformedURLException
  {
    testValidateLocation("file:a/b/c.xml", "FILE", "a/b/c.xml");
  }

  @Test
  public void testValidateLocationSingleSlash() throws InvalidProtocolException, NoProtocolException, MalformedURLException
  {
    testValidateLocation("file:/a/b/c.xml", "FILE", "/a/b/c.xml");
  }

  @Test
  public void testValidateLocationDoubleSlashOnly() throws InvalidProtocolException, NoProtocolException, MalformedURLException
  {
    // a is interpreted as host according to specification
    testValidateLocation("file://a/b/c.xml", "FILE", "/b/c.xml");
  }

  @Test
  public void testValidateLocationDoubleSlashNoHost() throws InvalidProtocolException, NoProtocolException, MalformedURLException
  {
    testValidateLocation("file:///a/b/c.xml", "FILE", "/a/b/c.xml");
  }

  @Test
  public void testValidateLocationDoubleSlashWithHost() throws InvalidProtocolException, NoProtocolException, MalformedURLException
  {
    testValidateLocation("file://localhost/a/b/c.xml", "FILE", "/a/b/c.xml");
  }

  @Test
  public void testValidateLocationSpaces() throws InvalidProtocolException, NoProtocolException, MalformedURLException
  {
    testValidateLocation(" file://localhost/a/b/c.xml  ", "FILE", "/a/b/c.xml");
  }

  @Test
  public void testValidateLocationSpacesInsidePath() throws InvalidProtocolException, NoProtocolException, MalformedURLException
  {
    testValidateLocation(" file://localhost/a/b b/c.xml  ", "FILE", "/a/b b/c.xml");
  }

  @Test
  public void testValidateLocationHttp() throws InvalidProtocolException, NoProtocolException, MalformedURLException
  {
    testValidateLocation("http://localhost/a/b/c.xml", "HTTP", "localhost/a/b/c.xml");
    testValidateLocation("https://localhost/a/b/c.xml", "HTTPS", "localhost/a/b/c.xml");
  }

  @Test
  public void testValidateLocationHttpWithPort() throws InvalidProtocolException, NoProtocolException, MalformedURLException
  {
    testValidateLocation("http://localhost:8080/a/b/c.xml", "HTTP", "localhost:8080/a/b/c.xml");
    testValidateLocation("https://localhost:8080/a/b/c.xml", "HTTPS", "localhost:8080/a/b/c.xml");
  }

  @Test
  public void testValidateLocationHttpWithParameters() throws InvalidProtocolException, NoProtocolException, MalformedURLException
  {
    testValidateLocation("http://localhost:8080/a/b/c.xml?x=y", "HTTP", "localhost:8080/a/b/c.xml?x=y");
    testValidateLocation("https://localhost:8080/a/b/c.xml?x=y", "HTTPS", "localhost:8080/a/b/c.xml?x=y");
  }
  
  @Test
  public void testValidateLocationHttpWithAnchor() throws InvalidProtocolException, NoProtocolException, MalformedURLException
  {
    testValidateLocation("http://localhost:8080/a/b/c.xml#anchor", "HTTP", "localhost:8080/a/b/c.xml#anchor");
    testValidateLocation("https://localhost:8080/a/b/c.xml#anchor", "HTTPS", "localhost:8080/a/b/c.xml#anchor");
  }
  
  @Test(expected=MalformedURLException.class)
  public void testValidateLocationUnknownProtocol() throws InvalidProtocolException, NoProtocolException, MalformedURLException
  {
    testValidateLocation(" xyz://localhost/a/b/c.xml  ", "XYZ", "/a/b/c.xml");
  }

  private void testValidateLocation(String uri, String expectedProtocol, String expectedLocation)
        throws InvalidProtocolException, NoProtocolException, MalformedURLException
  {
    Location validateLocation = XCodeVerificationCheckMojo.Location.getLocation(uri);
    assertEquals(expectedProtocol, validateLocation.protocol);
    assertEquals(expectedLocation, validateLocation.location);
  }

}
