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
package com.sap.prd.mobile.ios.mios.task;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

import com.sap.prd.mobile.ios.mios.AbstractXCodeBuildMojo;
import com.sap.prd.mobile.ios.mios.EffectiveBuildSettings;
import com.sap.prd.mobile.ios.mios.XCodeBuildLayout;
import com.sap.prd.mobile.ios.mios.XCodeException;

public class PackageDSymTask
{

  private Log log;
  private String productName;
  private MavenProjectHelper projectHelper;
  private MavenProject mavenProject;
  private ArchiverManager archiverManager;
  private File compileDir;
  private String configuration;
  private String sdk;

  public PackageDSymTask setLog(Log log)
  {
    this.log = log;
    return this;
  }

  public PackageDSymTask setProductName(String productName)
  {
    this.productName = productName;
    return this;
  }

  public PackageDSymTask setMavenProject(MavenProject mavenProject)
  {
    this.mavenProject = mavenProject;
    return this;
  }

  public PackageDSymTask setArchiverManager(ArchiverManager archiverManager)
  {
    this.archiverManager = archiverManager;
    return this;
  }

  public PackageDSymTask setConfiguration(String configuration)
  {
    this.configuration = configuration;
    return this;
  }

  public PackageDSymTask setSdk(String sdk)
  {
    this.sdk = sdk;
    return this;
  }

  public PackageDSymTask setProjectHelper(MavenProjectHelper projectHelper)
  {
    this.projectHelper = projectHelper;
    return this;
  }

  public PackageDSymTask setCompileDir(File compileDir)
  {
    this.compileDir = compileDir;
    return this;
  }


  /**
   * Packages the debug symbols generated during the Xcode build and prepares the generated artifact
   * for deployment. The debug symbols are generated only if the "Generate Debug Symbols" in Xcode
   * is active.
   */
  public void execute() throws XCodeException
  {

    try {
    final String productName;

    if (this.productName != null) {
      productName = this.productName.trim();

      if (productName.isEmpty())
        throw new IllegalStateException("ProductName from pom file was empty.");

    }
    else {
      productName = EffectiveBuildSettings.getProductName(mavenProject, configuration, sdk);

      if (productName == null || productName.isEmpty())
        throw new IllegalStateException("Product Name not found in effective build settings file");
    }

    final String fixedProductName = AbstractXCodeBuildMojo.getFixedProductName(productName);

    String generateDSym = new EffectiveBuildSettings(mavenProject, configuration, sdk).getBuildSetting(
      EffectiveBuildSettings.GCC_GENERATE_DEBUGGING_SYMBOLS);

    if (generateDSym == null || generateDSym.equalsIgnoreCase("YES")) {

      final File root = new File(XCodeBuildLayout.getAppFolder(compileDir, configuration, sdk), productName
            + ".app.dSYM");

      Archiver archiver = archiverManager.getArchiver("zip");

      File dSymFile = new File(new File(new File(mavenProject.getBuild().getDirectory()), configuration + "-" + sdk),
            fixedProductName + ".app.dSYM.zip");

      archiver.addDirectory(root, new String[] { "**/*" }, null);
      archiver.setDestFile(dSymFile);
      archiver.createArchive();
      log.info("dSYM packaged (" + dSymFile + ")");

      projectHelper.attachArtifact(mavenProject, "zip", configuration + "-" + sdk + "-app.dSYM", dSymFile);
    }
    else {
      log
        .info("dSYM packaging skipped.Generate Debug Symbols is not enabled for configuration " + configuration + " .");
    }
    }
    catch (ArchiverException ex) {
      throw new XCodeException("Packaging the Debug Symbols failed", ex);
    }
    catch (IOException ex) {
      throw new XCodeException("Packaging the Debug Symbols failed", ex);
    }
    catch (NoSuchArchiverException ex) {
      throw new XCodeException("Packaging the Debug Symbols failed", ex);
    }
  }

}
