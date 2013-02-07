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
public class XCodePackageDSymMojo extends BuildContextAwareMojo
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
        catch (XCodeException e) {
          throw new MojoExecutionException(e.getMessage(), e);
        }
      }
    }

  }

  private void packageAndAttachDSym(String sdk, String config) throws IOException, NoSuchArchiverException,
        ArchiverException, XCodeException, MojoExecutionException
  {

    final String productName = getProductName(config, sdk);

    final String fixedProductName = getFixedProductName(productName);
    
    final File root = XCodeBuildLayout.getAppFolder(getXCodeCompileDirectory(), config, sdk);
    final File dsymRoot = new File(root, productName + ".app.dSYM");
  
    if (dsymRoot.canRead()) {
    	  
      Archiver archiver = archiverManager.getArchiver("zip");

      File destination = new File(new File(new File(project.getBuild().getDirectory()), config + "-" + sdk),
            fixedProductName + ".app.dSYM.zip");

      archiver.addDirectory(root, new String[] { productName + ".app.dSYM/**/*" }, null);
      archiver.setDestFile(destination);
      archiver.createArchive();
      getLog().info("dSYM packaged (" + destination + ")");

      prepareDSymFileForDeployment(project, config, sdk, destination);
    }
    else {

      if (shouldExistDSym(sdk, config)) {
        throw new XCodeException(
              "DSym file should be created but could not be found at the expected location: '"
                    + dsymRoot
                    + "'. In case you prefere not to have dSym files set xcode property '"
                    + EffectiveBuildSettings.GCC_GENERATE_DEBUGGING_SYMBOLS + "' to 'NO'.");
      }

      getLog().info("dSYM packaging skipped. " +  productName + ".app.dSYM does not exists for configuration '" + config + "' and sdk ' " + sdk + "' .");
    }

  }

  private boolean shouldExistDSym(final String sdk, final String config) throws XCodeException
  {
    final String generateDSym = EffectiveBuildSettings.getBuildSetting(
          getXCodeContext(XCodeContext.SourceCodeLocation.WORKING_COPY, config, sdk), getLog(),
          EffectiveBuildSettings.GCC_GENERATE_DEBUGGING_SYMBOLS);

    return generateDSym == null || generateDSym.equalsIgnoreCase("YES");
  }

  private void prepareDSymFileForDeployment(final MavenProject mavenProject, final String configuration,
        final String sdk, final File dSymFile)
  {

    projectHelper.attachArtifact(mavenProject, "zip", configuration + "-" + sdk + "-app.dSYM", dSymFile);
  }

}
