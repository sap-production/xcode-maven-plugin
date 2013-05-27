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

import static junit.framework.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;

import com.sap.prd.mobile.ios.mios.validationchecks.v_1_0_0.Check;
import com.sap.prd.mobile.ios.mios.validationchecks.v_1_0_0.Checks;

public class XCodeValidationCheckMojoTest
{
  
  @Test
  public void testNoGav() throws Exception {
    XCodeValidationCheckMojo.parseDependencies(loadChecks("src/test/checks/noGAV.xml"), new SystemStreamLog());
  }

  @Test
  public void testEmptyGav() throws Exception {
    XCodeValidationCheckMojo.parseDependencies(loadChecks("src/test/checks/emptyGAV.xml"), new SystemStreamLog());
  }

  @Test
  public void testMissingGroupId() throws Exception {
    testPartOfGavInvalidOrMissing("src/test/checks/noGroupId.xml", "groupId", "myGroupId");
  }

  @Test
  public void testMissingArtifactId() throws Exception {
    testPartOfGavInvalidOrMissing("src/test/checks/noArtifactId.xml", "artifactId", "myArtifactId");
  }

  @Test
  public void testMissingVersion() throws Exception {
    testPartOfGavInvalidOrMissing("src/test/checks/noVersion.xml", "version", "1.0.0");
  }

  @Test
  public void testEmptyGroupId() throws Exception {
    testPartOfGavInvalidOrMissing("src/test/checks/emptyGroupId.xml", "groupId", "myGroupId");
  }

  @Test
  public void testEmptyArtifactId() throws Exception {
    testPartOfGavInvalidOrMissing("src/test/checks/emptyArtifactId.xml", "artifactId", "myArtifactId");
  }

  @Test
  public void testEmptyVersion() throws Exception{
    testPartOfGavInvalidOrMissing("src/test/checks/emptyVersion.xml", "version", "1.0.0");
  }

  @Test
  public void testGetCheckDescriptorWithFileProtocol() throws Exception, IOException {
    Reader r = null;
    
    try {
      r = XCodeValidationCheckMojo.getChecksDescriptor("file:src/test/checks/checks.xml");
      JAXBContext.newInstance(Checks.class).createUnmarshaller().unmarshal(r);
    } finally {
      IOUtils.closeQuietly(r);
    }
  }

  
  @Test(expected=XCodeValidationCheckMojo.InvalidProtocolException.class)
  public void testGetCheckDescriptorWithInvalidProtocol() throws Exception, IOException {
    XCodeValidationCheckMojo.getChecksDescriptor("ftp://example.com");
  }

  @Test(expected=XCodeValidationCheckMojo.NoProtocolException.class)
  public void testGetCheckDescriptorWithoutProtocol() throws Exception, IOException {
    XCodeValidationCheckMojo.getChecksDescriptor("example.com");
  }

  private void testPartOfGavInvalidOrMissing(String location, String attNameFix, String attValueFix) throws Exception {
    Checks checks = loadChecks(location);

    try {
      XCodeValidationCheckMojo.parseDependencies(checks, new SystemStreamLog());
      fail();
    } catch(MojoExecutionException ex) {
      System.out.println(ex.getMessage());
    }
    
    Method m = Check.class.getMethod(getSetterName(attNameFix), new Class[] {String.class});
    m.invoke(checks.getCheck().get(0), new Object[]{attValueFix});
    XCodeValidationCheckMojo.parseDependencies(checks, new SystemStreamLog());
  }
  
  private Checks loadChecks(String location) throws JAXBException {
    return (Checks) JAXBContext.newInstance(Checks.class).createUnmarshaller().unmarshal(new File(location));
  }

  private static String getSetterName(String attNameFix) {
    return "set" + firstCharToUpperCase(attNameFix);
  }

  private static String firstCharToUpperCase(String str) {
    char[] c = new char[str.length()];
    str.getChars(0, str.length(), c, 0);
    c[0] = Character.toUpperCase(c[0]);
    return new String(c);
  }
}
