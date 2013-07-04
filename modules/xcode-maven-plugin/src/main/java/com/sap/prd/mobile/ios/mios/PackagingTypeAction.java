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

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.manager.ArchiverManager;

public enum PackagingTypeAction
{
  UNPACK() {

    public void perform(ArchiverManager archiverManager, MavenProject project, Artifact mainArtifact)
    {
      final File f = FolderLayout.getFolderForAdditionalPackagingTypeWithGA(project, mainArtifact.getGroupId(),
            mainArtifact.getArtifactId(), mainArtifact.getType());
      if (!f.exists() && !f.mkdirs())
        throw new IllegalStateException("Cannot create directory " + f);

      final File unpackMe = mainArtifact.getFile();
      final String archiverId = com.sap.prd.mobile.ios.mios.FileUtils.getAppendix(unpackMe);
      com.sap.prd.mobile.ios.mios.FileUtils.unarchive(archiverManager, archiverId, unpackMe, f);
    }
  },
  COPY() {
    public void perform(ArchiverManager archiverManager, MavenProject project, Artifact mainArtifact)
          throws IOException
    {
      final File f = FolderLayout.getFolderForAdditionalPackagingTypeWithGA(project, mainArtifact.getGroupId(),
            mainArtifact.getArtifactId(), mainArtifact.getType());
      if (!f.exists() && !f.mkdirs())
        throw new IllegalStateException("Cannot create directory " + f);

      final File copyMe = mainArtifact.getFile();
      FileUtils.copyFile(copyMe, new File(f, copyMe.getName()));
    }
  }
  ,
  BUNDLE() {
    public void perform(ArchiverManager archiverManager, MavenProject project, Artifact mainArtifact)
    {
      final File f = new File(FolderLayout.getFolderForAdditionalPackagingTypeWithGA(project,
            mainArtifact.getGroupId(), mainArtifact.getArtifactId(), mainArtifact.getType()),
            mainArtifact.getArtifactId() + ".bundle");
      if (!f.exists() && !f.mkdirs()) {
        throw new IllegalStateException("Cannot create directory " + f);
      }
      final File unpackMe = mainArtifact.getFile();
      final String archiverId = com.sap.prd.mobile.ios.mios.FileUtils.getAppendix(unpackMe);
      com.sap.prd.mobile.ios.mios.FileUtils.unarchive(archiverManager, archiverId, unpackMe, f);
    }
  };

  public abstract void perform(ArchiverManager archiverManager, MavenProject project, Artifact mainArtifact)
        throws IOException;
}
