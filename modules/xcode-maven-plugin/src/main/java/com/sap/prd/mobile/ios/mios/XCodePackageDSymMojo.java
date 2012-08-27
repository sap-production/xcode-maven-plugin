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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

/**
 * Packages the debug symbols generated during the Xcode build and prepares the generated artifact
 * for deployment. The debug symbols are generated only if the "Generate Debug Symbols" in Xcode is
 * active.
 * 
 * @goal package-dsym
 * 
 */
public class XCodePackageDSymMojo extends AbstractXCodeMojo
{

  /**
   * @component role="org.codehaus.plexus.archiver.manager.ArchiverManager"
   * @required
   */
  private ArchiverManager archiverManager;

  /**
   * @component
   */
  private MavenProjectHelper projectHelper;

  /**
   * @parameter expression="${product.name}"
   */
  private String productName;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {

    for (final String sdk : getSDKs()) {
      for (final String config : getConfigurations()) {

        try {

          packageAndAttachDSym(sdk, config);

        }
        catch (IOException e) {
          throw new MojoExecutionException(e.getMessage(), e);
        }
        catch (NoSuchArchiverException e) {
          throw new MojoExecutionException(e.getMessage(), e);
        }
        catch (ArchiverException e) {
          throw new MojoExecutionException(e.getMessage(), e);
        }
      }
    }

  }

  private void packageAndAttachDSym(String sdk, String config) throws IOException, NoSuchArchiverException,
        ArchiverException
  {

    final String productName;

    if (this.productName != null) {
      productName = this.productName.trim();

      if (productName.isEmpty())
        throw new IllegalStateException("ProductName from pom file was empty.");

    }
    else {
      productName = EffectiveBuildSettings.getProductName(this.project, config, sdk);

      if (productName == null || productName.isEmpty())
        throw new IllegalStateException("Product Name not found in effective build settings file");
    }

    final String fixedProductName = getFixedProductName(productName);

    String generateDSym = new EffectiveBuildSettings(this.project, config, sdk).getBuildSetting(EffectiveBuildSettings.GCC_GENERATE_DEBUGGING_SYMBOLS);

    if (generateDSym == null || generateDSym.equalsIgnoreCase("YES")) {

      final File root = new File(XCodeBuildLayout.getAppFolder(getXCodeCompileDirectory(), config, sdk), productName
            + ".app.dSYM");

      Archiver archiver = archiverManager.getArchiver("zip");

      File destination = new File(new File(new File(project.getBuild().getDirectory()), config + "-" + sdk),
            fixedProductName + ".app.dSYM.zip");

      archiver.addDirectory(root, new String[] { "**/*" }, null);
      archiver.setDestFile(destination);
      archiver.createArchive();
      getLog().info("dSYM packaged (" + destination + ")");

      prepareDSymFileForDeployment(project, config, sdk, destination);
    }
    else {
      getLog().info("dSYM packaging skipped.Generate Debug Symbols is not enabled for configuration " + config + " .");
    }

  }

  private void prepareDSymFileForDeployment(final MavenProject mavenProject, final String configuration,
        final String sdk, final File dSymFile)
  {

    projectHelper.attachArtifact(mavenProject, "zip", configuration + "-" + sdk + "-app.dSYM", dSymFile);
  }

}
