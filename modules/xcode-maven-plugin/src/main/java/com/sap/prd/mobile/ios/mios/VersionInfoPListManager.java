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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;

import com.sap.prd.mobile.ios.mios.versioninfo.v_1_2_2.Dependency;

public class VersionInfoPListManager
{

  void createVersionInfoPlistFile(final String groupId, final String artifactId, final String version,
        final File syncInfoFile, List<Dependency> dependencies, File file, boolean hideConfidentialInformation)
        throws MojoExecutionException
  {

    InputStream is = null;
    
    try {

      is = new FileInputStream(syncInfoFile);

      final Properties versionInfo = new Properties();

      versionInfo.load(is);

      createVersionInfoPlistFile(groupId, artifactId, version, versionInfo, dependencies, file,
            hideConfidentialInformation);

    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not load sync info from file '" + syncInfoFile + "'.", e);
    } finally {
      IOUtils.closeQuietly(is);
    }
  }

  private void createVersionInfoPlistFile(final String groupId, final String artifactId, final String version,
        Properties versionInfo, List<Dependency> dependencies, File file, boolean hideConfidentialInformation)
        throws MojoExecutionException
  {
    try {

      final String connectionString = getConnectionString(versionInfo, hideConfidentialInformation);

      PListAccessor plistAccessor = new PListAccessor(file);
      plistAccessor.createPlist();

      plistAccessor.addElement("coordinates", "dict");
      plistAccessor.addStringValueToDict(GROUP_ID, groupId, "coordinates");
      plistAccessor.addStringValueToDict(ARTIFACT_ID, artifactId, "coordinates");
      plistAccessor.addStringValueToDict(VERSION, version, "coordinates");

      plistAccessor.addElement("scm", "dict");
      plistAccessor.addStringValueToDict("connection", connectionString, "scm");
      plistAccessor.addStringValueToDict("revision", versionInfo.getProperty("changelist"), "scm");

      addDependencyToPlist(dependencies, plistAccessor, "dependencies:", hideConfidentialInformation);

    }

    catch (IOException e) {
      throw new MojoExecutionException("Cannot create versions.plist.", e);
    }
  }

  private String getConnectionString(Properties versionInfo, boolean hideConfidentialInformation)
        throws MojoExecutionException
  {
    if (!hideConfidentialInformation) {
      return "scm:perforce:"
            + versionInfo.getProperty("port") + ":" + getDepotPath(versionInfo.getProperty("depotpath"));
    }
    else {

      final String port = versionInfo.getProperty("port");

      final int colonIndex = port.indexOf(":");

      if (colonIndex == -1)
      {
        throw new MojoExecutionException("No colon found in perforce port : '" + port + "'.");
      }
      return port.substring(colonIndex + ":".length());
      
    }
  }

  void addDependencyToPlist(List<Dependency> dependencies, PListAccessor plistAccessor, String path,
        boolean hideConfidentialInformation) throws IOException
  {

    for (int i = 0; i < dependencies.size(); i++) {

      String _path = path + i;
      Dependency dep = dependencies.get(i);

      plistAccessor.addDictToArray("coordinates", _path);
      plistAccessor.addStringValueToDict(GROUP_ID, dep.getCoordinates().getGroupId(), _path
            + ":coordinates");
      plistAccessor.addStringValueToDict(ARTIFACT_ID, dep.getCoordinates().getArtifactId(), _path
            + ":coordinates");
      plistAccessor.addStringValueToDict(VERSION, dep.getCoordinates().getVersion(), _path
            + ":coordinates");

      plistAccessor.addDictToArray("scm", _path);
      String port = getScmPort(dep, hideConfidentialInformation);
      plistAccessor.addStringValueToDict("connection", port, _path + ":scm");
      plistAccessor.addStringValueToDict("revision", dep.getScm().getRevision(), _path + ":scm");
      addDependencyToPlist(dep.getDependencies(), plistAccessor, _path + ":dependencies:", hideConfidentialInformation);

    }
  }

  private String getScmPort(Dependency dep, boolean hideConfidentialInformation)
  {
    if (!hideConfidentialInformation) {
      return dep.getScm().getConnection();
    } else {

      String[] parts = dep.getScm().getConnection().split(":");
      String port;
      if (parts.length == 1)
      {
        port = parts[0];
      }
      else
      {
        port = parts[3]; //scm:perforce:PERFORCEHOST:4321:PATH
      }
      return port;
      
    }
  }

  private static final String THREEDOTS = "...";

  private static String getDepotPath(String fullDepotPath)
  {
    if (fullDepotPath.endsWith(THREEDOTS)) {
      fullDepotPath = fullDepotPath.substring(0, fullDepotPath.length() - THREEDOTS.length());
    }
    return fullDepotPath;
  }

}
