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

import static java.lang.String.format;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.aether.artifact.Artifact;
import org.xml.sax.SAXException;

import com.sap.prd.mobile.ios.mios.versioninfo.v_1_2_2.Dependency;


public class XCodeVersionInfoMojoTest
{

  @Test
  public void testAddParsedVersionsXmlDependency_valid() throws IOException
  {
    testAddParsedVersionsXmlDependency(
          new File("src/test/resources/validXML.xml"),
          null, null);
  }

  @Test
  public void testAddParsedVersionsXmlDependency_invalidHtml() throws IOException
  {
    testAddParsedVersionsXmlDependency(
          new File("src/test/resources/versions-invalidHtml.xml"),
          TestLog.Severity.WARNING, "contains invalid content (Scheme violation). Ignoring this file");
  }

  @Test
  public void testAddParsedVersionsXmlDependency_invalidEmpty() throws IOException
  {
    testAddParsedVersionsXmlDependency(
          new File("src/test/resources/versions-invalidEmpty.xml"),
          TestLog.Severity.WARNING, "contains invalid content (Non parsable XML). Ignoring this file");
  }

  @Test(expected = FileNotFoundException.class)
  public void testAddParsedVersionsXmlDependency_invalidDoesNotExist() throws IOException
  {
    testAddParsedVersionsXmlDependency(
          new File("src/test/resources/versions-invalidDoesNotExist.xml"),
          null, null);
  }

  private void testAddParsedVersionsXmlDependency(File file, TestLog.Severity expectedSeverity, String expectedLog)
        throws IOException
  {
    Artifact mockArtifact = EasyMock.createMock(Artifact.class);
    EasyMock.expect(mockArtifact.getFile()).andStubReturn(file);
    EasyMock.replay(mockArtifact);

    XCodeVersionInfoMojo xCodeVersionInfoMojo = new XCodeVersionInfoMojo();
    TestLog log = new TestLog();
    xCodeVersionInfoMojo.setLog(log);
    xCodeVersionInfoMojo.addParsedVersionsXmlDependency(new ArrayList<Dependency>(), mockArtifact);

    if (expectedSeverity != null || expectedLog != null) {
      assertTrue(format("Not contained in log: '%s' '%s', log was '%s'",
            expectedSeverity, expectedLog, log.getLogContent()),
            log.contains(expectedSeverity, expectedLog));
    }
  }

  public void testTransformVersionsXmlRegular() throws IOException, ParserConfigurationException, SAXException,
        TransformerFactoryConfigurationError, TransformerException, XCodeException
  {
    File originalXml = new File("src/test/resources/validXML.xml");
    File transformedXml = File.createTempFile("testTransformVersionsXmlRegular", ".xml");
    
    new XCodeVersionInfoMojo().transformVersionsXml(originalXml, transformedXml);
    
    String original = toString(originalXml);
    String transformed = toString(transformedXml);
    System.out.println("ORIGINAL: "+original);
    System.out.println("TRANSFORMED: "+transformed);
    Assert.assertTrue("Does not contain 'PERFORCEHOST': "+original, original.contains("PERFORCEHOST"));
    Assert.assertFalse("Still contains 'PERFORCEHOST': " + transformed, transformed.contains("PERFORCEHOST"));
    Assert.assertTrue("Does not contain '<connection>4321</connection>': " + transformed, transformed.contains("<connection>4321</connection>"));
  }
  
  private String toString(File transformedXml) throws IOException
  {
    FileInputStream fileInputStream = new FileInputStream(transformedXml);
    try {
      return IOUtils.toString(fileInputStream);
    } finally {
      IOUtils.closeQuietly(fileInputStream);
    }
  }
}
