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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.sap.prd.mobile.ios.mios.versioninfo.v_1_2_2.Coordinates;
import com.sap.prd.mobile.ios.mios.versioninfo.v_1_2_2.Dependency;
import com.sap.prd.mobile.ios.mios.versioninfo.v_1_2_2.SCM;
import com.sap.prd.mobile.ios.mios.versioninfo.v_1_2_2.Versions;

/**
 * Generates a <artifact-id>-<version>-version.xml for reproducibility reasons. This versions.xml
 * contains information about the scm location and revision of the built project and all its
 * dependencies. Expects a sync.info file in the root folder of the project as input. Additionally,
 * in case of dependencies, the information from the versions.xml files of the dependent projects is
 * added.
 * 
 */
public class VersionInfoXmlManager
{
  private static final String THREEDOTS = "...";

  private final static String SCHEMA_VERSION = "1.2.2";
  private final static String SCHEMA_GROUP_ID = "com.sap.prd.mobile.ios.mios";
  private final static String SCHEMA_ARTIFACT_ID = "versionschema";
  private final static String SCHEMA_REPOSITORY = "deploy.release.ondevice.hosted";
  private final static String NEXUS_URL = "http://nexus.wdf.sap.corp:8081/nexus";

  void createVersionInfoFile(final String groupId, final String artifactId, final String version,
        final File syncInfoFile, List<Dependency> dependencies, OutputStream versionInfoStream)
        throws MojoExecutionException
  {

    InputStream is = null;

    try {

      is = new FileInputStream(syncInfoFile);

      final Properties versionInfo = new Properties();

      versionInfo.load(is);

      createVersionInfoFile(groupId, artifactId, version, versionInfo, dependencies, versionInfoStream);

    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not load sync info from file '" + syncInfoFile + "'.", e);
    }
    catch (JAXBException e) {
      throw new MojoExecutionException("Could not load sync info from file '" + syncInfoFile + "'.", e);
    } finally {
      IOUtils.closeQuietly(is);
    }
  }

  private void createVersionInfoFile(final String groupId, final String artifactId, final String version,
        Properties versionInfo, List<Dependency> dependencies, OutputStream os)
        throws MojoExecutionException, JAXBException
  {
    try {

      final Versions versions = new Versions();

      for (final Dependency dep : dependencies)
        versions.addDependency(dep);

      final SCM scm = new SCM();
      scm.setConnection(ConnectionStringProvider.getConnectionString(versionInfo, false));
      scm.setRevision(versionInfo.getProperty("changelist"));

      final Coordinates coordinates = new Coordinates();
      coordinates.setGroupId(groupId);
      coordinates.setArtifactId(artifactId);
      coordinates.setVersion(version);

      versions.setScm(scm);
      versions.setCoordinates(coordinates);

      final JAXBContext context = JAXBContext.newInstance(Versions.class);

      final Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "urn:xml.sap.com:XCodePlugin:VersionInfo" + " "
            + NEXUS_URL + "/content/repositories/" + SCHEMA_REPOSITORY + "/" + SCHEMA_GROUP_ID.replace(".", "/") + "/"
            + SCHEMA_ARTIFACT_ID + "/" + SCHEMA_VERSION + "/" + SCHEMA_ARTIFACT_ID + "-" + SCHEMA_VERSION + ".xsd");

      final ByteArrayOutputStream byteOs = new ByteArrayOutputStream();

      marshaller.marshal(versions, byteOs);

      final byte[] b = byteOs.toByteArray();

      DomUtils.validateDocument(DocumentBuilderFactory.newInstance().newDocumentBuilder()
        .parse(new ByteArrayInputStream(b)));

      IOUtils.write(b, os);
    }
    catch (ParserConfigurationException e) {
      throw new MojoExecutionException("Cannot create versions.xml.", e);
    }
    catch (IOException e) {
      throw new MojoExecutionException("Cannot create versions.xml.", e);
    }
    catch (SAXException e) {
      throw new MojoExecutionException("Cannot create versions.xml.", e);
    }
  }

  private static String getDepotPath(String fullDepotPath)
  {
    if (fullDepotPath.endsWith(THREEDOTS)) {
      fullDepotPath = fullDepotPath.substring(0, fullDepotPath.length() - THREEDOTS.length());
    }
    return fullDepotPath;
  }

  static Dependency parseDependency(final File f) throws JAXBException, SAXException, IOException
  {

    final String schemaVersion = getSchemaVersion(f);

    if (schemaVersion == null) {
      return parseOldDependency(f);
    }
    else if (schemaVersion.equals("1.2.0")) {
      return parseOldVersionsWithoutSchema(f);
    }
    else if ((schemaVersion.equals("1.2.1")) || (schemaVersion.equals("1.2.2"))) {
      return parseDependency_1_2_2(f);
    }
    throw new IllegalStateException("Unknown schemaVersion: '" + schemaVersion + "'.");
  }

  private static Dependency parseOldDependency(File f) throws JAXBException
  {
    final JAXBContext context = JAXBContext
      .newInstance(com.sap.prd.mobile.ios.mios.versioninfo.v_0_0_0.Versions.class);
    final Unmarshaller unmarshaller = context.createUnmarshaller();

    final com.sap.prd.mobile.ios.mios.versioninfo.v_0_0_0.Versions versions = (com.sap.prd.mobile.ios.mios.versioninfo.v_0_0_0.Versions) unmarshaller
      .unmarshal(f);

    final Coordinates coordinates = new Coordinates();
    coordinates.setGroupId(versions.getCoordinates().getGroupId());
    coordinates.setArtifactId(versions.getCoordinates().getArtifactId());
    coordinates.setVersion(versions.getCoordinates().getVersion());

    final SCM scm = new SCM();
    scm.setConnection("scm:perforce:" + versions.getScm().getRepository() + ":"
          + getDepotPath(versions.getScm().getPath()));
    scm.setRevision(versions.getScm().getSnapshotId());

    final Dependency dependency = new Dependency();
    dependency.setCoordinates(coordinates);
    dependency.setScm(scm);

    if (versions.getDependencies() != null) {
      for (com.sap.prd.mobile.ios.mios.versioninfo.v_0_0_0.Dependency d : versions.getDependencies()) {

        marshalOldDependencies(dependency, d);
      }
    }

    return dependency;
  }

  private static void marshalOldDependencies(final Dependency dependency,
        com.sap.prd.mobile.ios.mios.versioninfo.v_0_0_0.Dependency d)
  {
    Dependency _d = new Dependency();
    Coordinates depCoorinates = new Coordinates();
    depCoorinates.setGroupId(d.getCoordinates().getGroupId());
    depCoorinates.setArtifactId(d.getCoordinates().getArtifactId());
    depCoorinates.setVersion(d.getCoordinates().getVersion());
    _d.setCoordinates(depCoorinates);

    SCM depSCM = new SCM();
    depSCM.setConnection("scm:perforce:" + d.getScm().getRepository() + ":" + getDepotPath(d.getScm().getPath()));
    depSCM.setRevision(d.getScm().getSnapshotId());
    _d.setScm(depSCM);
    dependency.addDependency(_d);

    for (com.sap.prd.mobile.ios.mios.versioninfo.v_0_0_0.Dependency __d : d.getDependencies())
      marshalOldDependencies(_d, __d);
  }

  private static Dependency parseOldVersionsWithoutSchema(File f) throws JAXBException
  {
    final JAXBContext context = JAXBContext
      .newInstance(com.sap.prd.mobile.ios.mios.versioninfo.v_1_2_0.Versions.class);
    final Unmarshaller unmarshaller = context.createUnmarshaller();

    final com.sap.prd.mobile.ios.mios.versioninfo.v_1_2_0.Versions versions = (com.sap.prd.mobile.ios.mios.versioninfo.v_1_2_0.Versions) unmarshaller
      .unmarshal(f);

    final Coordinates coordinates = new Coordinates();
    coordinates.setGroupId(versions.getCoordinates().getGroupId());
    coordinates.setArtifactId(versions.getCoordinates().getArtifactId());
    coordinates.setVersion(versions.getCoordinates().getVersion());

    final SCM scm = new SCM();
    scm.setConnection(versions.getScm().getConnection());
    scm.setRevision(versions.getScm().getRevision());

    final Dependency dependency = new Dependency();
    dependency.setCoordinates(coordinates);
    dependency.setScm(scm);

    if (versions.getDependencies() != null) {
      for (com.sap.prd.mobile.ios.mios.versioninfo.v_1_2_0.Dependency d : versions.getDependencies()) {
        marshalDependenciesVersionsWithoutSchema(dependency, d);
      }
    }

    return dependency;
  }

  private static void marshalDependenciesVersionsWithoutSchema(final Dependency dependency,
        com.sap.prd.mobile.ios.mios.versioninfo.v_1_2_0.Dependency d)
  {
    Dependency _d = new Dependency();
    Coordinates depCoorinates = new Coordinates();
    depCoorinates.setGroupId(d.getCoordinates().getGroupId());
    depCoorinates.setArtifactId(d.getCoordinates().getArtifactId());
    depCoorinates.setVersion(d.getCoordinates().getVersion());
    _d.setCoordinates(depCoorinates);

    SCM depSCM = new SCM();
    depSCM.setConnection(d.getScm().getConnection());
    depSCM.setRevision(d.getScm().getRevision());
    _d.setScm(depSCM);
    dependency.addDependency(_d);

    for (com.sap.prd.mobile.ios.mios.versioninfo.v_1_2_0.Dependency __d : d.getDependencies())
      marshalDependenciesVersionsWithoutSchema(_d, __d);
  }

  private static Dependency parseDependency_1_2_2(File f) throws JAXBException
  {
    final JAXBContext context = JAXBContext.newInstance(Versions.class);
    final Unmarshaller unmarshaller = context.createUnmarshaller();

    final Versions versions = (Versions) unmarshaller.unmarshal(f);

    final Dependency dependency = new Dependency();
    dependency.setCoordinates(versions.getCoordinates());
    dependency.setScm(versions.getScm());

    if (versions.getDependencies() != null)
      for (Dependency d : versions.getDependencies())
        dependency.addDependency(d);

    return dependency;
  }

  private static String getSchemaVersion(final File f) throws SAXException, IOException
  {

    class StopParsingException extends SAXException
    {
      private static final long serialVersionUID = -7499102356648608429L;
    };

    XMLReader xmlReader = XMLReaderFactory.createXMLReader();

    final InputStream s = new FileInputStream(f);

    final String[] result = new String[1];

    try {

      xmlReader.setContentHandler(new ContentHandler() {

        boolean repositoryTagFound = false;

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException
        {
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
        {
          if (localName.equals("versions")) {
            result[0] = atts.getValue("schemaVersion");

            if (result[0] != null)
              throw new StopParsingException();
          }

          if (localName.equals("repository")) {
            repositoryTagFound = true;
          }
        }

        @Override
        public void startDocument() throws SAXException
        {
        }

        @Override
        public void skippedEntity(String name) throws SAXException
        {
        }

        @Override
        public void setDocumentLocator(Locator locator)
        {
        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException
        {
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
        {
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException
        {
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException
        {
        }

        @Override
        public void endDocument() throws SAXException
        {
          if (!repositoryTagFound && result[0] == null)
            result[0] = "1.2.0";
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException
        {
        }
      });

      try {
        xmlReader.parse(new InputSource(s));
      }
      catch (StopParsingException ex) {
        //OK, StopParsingException is thrown when we know all we need from the document.
      }
    }
    finally {
      IOUtils.closeQuietly(s);
    }
    return result[0];
  }
}
