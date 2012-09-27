package com.sap.prd.mobile.ios.mios;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Packages the framework built by Xcode and prepares the generated artifact for deployment.
 * 
 * @goal package-framework
 * 
 */
public class XCodePackageFrameworkMojo extends AbstractXCodeMojo
{

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {

    EffectiveBuildSettings effBuildSettings = new EffectiveBuildSettings(project, getPrimaryFmwkConfiguration(),
          "iphoneos");
    String productName = effBuildSettings.getBuildSetting(EffectiveBuildSettings.PRODUCT_NAME);
    String builtProductsDirName = effBuildSettings.getBuildSetting(EffectiveBuildSettings.BUILT_PRODUCTS_DIR);

    File builtProductsDir = new File(builtProductsDirName);
    String fmwkDirName = productName + ".framework";
    File fmwkDir = new File(builtProductsDir, fmwkDirName);

    validateFrmkStructure(fmwkDir);

    String artifactName = productName + ".xcode-framework-zip";
    zipFmwk(builtProductsDir, artifactName, fmwkDirName);

    File mainArtifact = new File(builtProductsDir, artifactName);
    project.getArtifact().setFile(mainArtifact);
    getLog().info("Main artifact file '" + mainArtifact + "' attached for " + project.getArtifact());
  }

  private void zipFmwk(File workingDirectory, String artifactName, String zipDirName) throws MojoExecutionException
  {
    try {
      String[] zipCmd = new String[] { "zip", "-r", "-y", "-q", artifactName, zipDirName };
      getLog().info("Executing: " + StringUtils.join(zipCmd, ' '));
      int exitCode = Forker.forkProcess(System.out, workingDirectory, zipCmd);
      if (exitCode != 0) {
        throw new MojoExecutionException("Could not package the Xcode framework.");
      }
    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not package the Xcode framework.", e);
    }
  }

  private void validateFrmkStructure(File fmwkDir) throws MojoExecutionException
  {
    FrameworkStructureValidator fmwkValidator = new FrameworkStructureValidator(fmwkDir);
    List<String> validationErrors = fmwkValidator.validate();
    if (!validationErrors.isEmpty()) {
      throw new MojoExecutionException("The validation of the built framework '" + fmwkDir.getAbsolutePath()
            + "' failed: " + validationErrors);
    }
  }

}
