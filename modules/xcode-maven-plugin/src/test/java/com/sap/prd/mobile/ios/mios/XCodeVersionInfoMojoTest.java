package com.sap.prd.mobile.ios.mios;

import static java.lang.String.format;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.easymock.EasyMock;
import org.junit.Test;
import org.sonatype.aether.artifact.Artifact;

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

}
