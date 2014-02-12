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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

public class SCMUtil {

  private static final String THREEDOTS = "...";
  private static final String PROTOCOL_PREFIX_PERFORCE = "scm:perforce:";
  private static final String PROTOCOL_PREFIX_GIT = "scm:git:";
  
  private final static Map<String, Integer> gitDefaultPorts = new HashMap<String, Integer>();
  
  static {
    gitDefaultPorts.put("ssh", 29418);
    gitDefaultPorts.put("http", 80);
    gitDefaultPorts.put("https", 443);
    gitDefaultPorts.put("git", 9418);
  }
  private static final int PERFORCE_DEFAULT_PORT = 1666;

  public static String getConnectionString(final Properties versionInfo, final boolean hideConfidentialInformation) throws IOException {

    final String type = versionInfo.getProperty("type");

    final StringBuilder connectionString = new StringBuilder(128);

    if (type != null && type.equals("git")) {

      final String repo = versionInfo.getProperty("repo");

      if(StringUtils.isBlank(repo))
      {
        if(! StringUtils.isBlank(versionInfo.getProperty("repo_1")))
        {
          throw new IllegalStateException("Multipe git repositories provided. This use case is not supported. Provide only one git repository."); 
        }
        throw new IllegalStateException("No git repository provided. ");
      }

      if(hideConfidentialInformation) {
        try {
          URI uri = new URI(repo);
          int port = uri.getPort();
          if(port == -1) 
          {
            final String scheme = uri.getScheme();
            if(scheme != null && gitDefaultPorts.containsKey(scheme))
            {
              port = gitDefaultPorts.get(scheme);
            }
          }
          connectionString.append(port);

          if(uri.getHost() != null)
          {
            connectionString.append(uri.getPath());
          }
        }
        catch (URISyntaxException e) {
          throw new IllegalStateException(String.format("Invalid repository uri: %s", repo));
        }
      } else {
        connectionString.append(PROTOCOL_PREFIX_GIT).append(repo);
      }

    } else {

      final String port = versionInfo.getProperty("port");

      if(StringUtils.isBlank(port))
        throw new IllegalStateException("No SCM port provided.");
            
      final String depotPath = versionInfo.getProperty("depotpath");

      if(hideConfidentialInformation) {
        try {
          URI perforceUri = new URI("perforce://" + port);
          
          int _port = perforceUri.getPort();
          if(_port == -1) {
            _port = PERFORCE_DEFAULT_PORT;
          }
          connectionString.append(_port);
          connectionString.append(depotPath);
        }
        catch (URISyntaxException e) {
          throw new IllegalStateException(String.format("Invalid port: %s", port));
        }
      } else {

        if(StringUtils.isBlank(depotPath))
          throw new IllegalStateException("No depot path provided.");

        connectionString.append(PROTOCOL_PREFIX_PERFORCE)
        .append(port).append(":")
        .append(getDepotPath(depotPath));
      } 
    }

    return connectionString.toString();
  }
  
  public static String getRevision(final Properties versionInfo) {

    final String type = versionInfo.getProperty("type");
    final String key;
    
    if(type != null && type.equals("git"))
    {
      key = "commitId";
    } else {
      key = "changelist";
    }
    
    final String value = versionInfo.getProperty(key);
    
    if(StringUtils.isBlank(value)) 
    {
      throw new IllegalStateException(String.format("%s has not been provided in sync.info file.", key));
    }
    
    return value;
  }

  private static String getDepotPath(String fullDepotPath)
  {
    if (fullDepotPath.endsWith(THREEDOTS)) {
      fullDepotPath = fullDepotPath.substring(0, fullDepotPath.length() - THREEDOTS.length());
    }
    return fullDepotPath;
  }
}
