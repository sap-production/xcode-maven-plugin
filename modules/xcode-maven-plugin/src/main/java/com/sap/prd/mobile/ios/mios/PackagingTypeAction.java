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
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

public enum PackagingTypeAction
{
  UNPACK() {

    public void perform(ArchiverManager archiverManager, MavenProject project, Artifact mainArtifact)
    {
      final File f = FolderLayout.getFolderForExtractedAdditionlUnpackedArtifactsWithGA(project, mainArtifact.getGroupId(), mainArtifact.getArtifactId());
      if(!f.exists() && !f.mkdirs())
        throw new IllegalStateException("Cannot create directory " + f);
      
      final File unpackMe = mainArtifact.getFile();
      
      unarchive(archiverManager, unpackMe, f);
    }
  },
  COPY() {
    public void perform(ArchiverManager archiverManager, MavenProject project, Artifact mainArtifact) throws IOException
    {
      final File f = FolderLayout.getFolderForCopiedAdditionlUnpackedArtifactsWithGA(project, mainArtifact.getGroupId(), mainArtifact.getArtifactId());
      if(!f.exists() && !f.mkdirs())
        throw new IllegalStateException("Cannot create directory " + f);

      final File copyMe = mainArtifact.getFile();
      FileUtils.copyFile(copyMe, new File(f, copyMe.getName()));
    }
  }
  ,
  BUNDLE() {
    public void perform(ArchiverManager archiverManager, MavenProject project, Artifact mainArtifact)
    {
      final File f = new File(FolderLayout.getFolderForAdditionlBundlesWithGA(project, mainArtifact.getGroupId(), mainArtifact.getArtifactId()), mainArtifact.getArtifactId() + ".bundle");
      if(!f.exists() && !f.mkdirs()) {
        throw new IllegalStateException("Cannot create directory " + f);
      }
      final File unpackMe = mainArtifact.getFile();
      unarchive(archiverManager, unpackMe, f);
    }
  };

  public abstract void perform(ArchiverManager archiverManager, MavenProject project, Artifact mainArtifact) throws IOException;

  private static void unarchive(ArchiverManager archiverManager, final File source, final File destinationDirectory)
  {
    try {
      UnArchiver unarchiver = archiverManager.getUnArchiver(com.sap.prd.mobile.ios.mios.FileUtils.getAppendix(source));
      unarchiver.setSourceFile(source);
      unarchiver.setDestDirectory(destinationDirectory);
      unarchiver.extract();
    }
    catch (NoSuchArchiverException e) {
      throw new RuntimeException(e);
    }
    catch (ArchiverException e) {
      throw new RuntimeException(e);
    }
  }
}
