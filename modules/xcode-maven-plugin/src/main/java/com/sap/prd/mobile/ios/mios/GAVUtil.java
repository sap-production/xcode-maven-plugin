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
import static com.sap.prd.mobile.ios.mios.versioninfo.Coordinates.CLASSIFIER;
import static com.sap.prd.mobile.ios.mios.versioninfo.Coordinates.EXTENSION;
import static com.sap.prd.mobile.ios.mios.versioninfo.Coordinates.GROUP_ID;
import static com.sap.prd.mobile.ios.mios.versioninfo.Coordinates.VERSION;

import org.apache.maven.artifact.handler.ArtifactHandler;
import org.sonatype.aether.util.artifact.DefaultArtifact;

// Format:  <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>
class GAVUtil
{
  static String toColonNotation(String groupId, String artifactId, String version, String extension, String classifier)
  {
    // For our use case we expect all parameter to be not null (or empty).
    // Our use case is handling side artifacts with requires a classifier an an extension.
    ensureNotNullOrEmpty(groupId, GROUP_ID);
    ensureNotNullOrEmpty(artifactId, ARTIFACT_ID);
    ensureNotNullOrEmpty(version, VERSION);
    ensureNotNullOrEmpty(extension, EXTENSION);
    ensureNotNullOrEmpty(classifier, CLASSIFIER);

    return groupId + ":" + artifactId + ":" + extension + ":" + classifier + ":" + version;
  }

  private static void ensureNotNullOrEmpty(String str, String parameterName)
  {
    if (str == null || str.isEmpty())
      throw new IllegalStateException("Value of parameter '" + parameterName + "' was null or empty.");

  }

  static org.apache.maven.artifact.DefaultArtifact getArtifact(String coords)
  {
    DefaultArtifact aetherArtifact = new DefaultArtifact(coords);
    return new org.apache.maven.artifact.DefaultArtifact(aetherArtifact.getGroupId(), aetherArtifact.getArtifactId(),
          aetherArtifact.getVersion(), null, aetherArtifact.getExtension(), aetherArtifact.getClassifier(), null) {

      // The DefaultArtifact created here is only used internally as a
      // vehicle. Since we do not set a scope and an artifact handler
      // these methods cannot work in a reasonable way. Instead returning
      // null which might lead to problems later on we throw an
      // exception right when the corresponding methods are called.

      @Override
      public String getScope()
      {
        throw new UnsupportedOperationException();
      }

      @Override
      public ArtifactHandler getArtifactHandler()
      {
        throw new UnsupportedOperationException();
      }
    };
  }
}
