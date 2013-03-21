package com.sap.prd.mobile.ios.mios;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ModuleBuildTest extends XCodeTest
{
  private static File remoteRepositoryDirectory = null;
  private static String dynamicVersion = null, testName = null;

  private static File testExecutionDirectoryLibrary = getTestExecutionDirectory(testName, "moduleBuild/MyLibrary");
  private static File testExecutionDirectoryApplication = getTestExecutionDirectory(testName, "moduleBuild/MyApp");

  @BeforeClass
  public static void __setup() throws Exception
  {
    dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());
    testName = ModuleBuildTest.class.getName() + File.separator
          + Thread.currentThread().getStackTrace()[1].getMethodName();

    remoteRepositoryDirectory = getRemoteRepositoryDirectory(ModuleBuildTest.class.getName());

    prepareRemoteRepository(remoteRepositoryDirectory);

    Properties pomReplacements = new Properties();
    pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);

    test(testName, new File(getTestRootDirectory(), "moduleBuild"), "deploy",
          THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements);

    testExecutionDirectoryLibrary = getTestExecutionDirectory(testName, "moduleBuild/MyLibrary");
    testExecutionDirectoryApplication = getTestExecutionDirectory(testName, "moduleBuild/MyApp");

  }

  @Test
  public void testRedirectFileForPomFileOfLibrary() throws IOException, IOException
  {
    testRedirectFile(testExecutionDirectoryLibrary, "MyLibrary", "1.0.1", "pom");
  }

  @Test
  public void testRedirectFileForPomFileOfApplication() throws IOException, IOException
  {
    testRedirectFile(testExecutionDirectoryApplication, "MyApp", "1.0.0", "pom");
  }
  
  private void testRedirectFile(final File testExecutionDirectory, final String projecName, final String version, String type) throws IOException{

    final File redirectFile = new File(testExecutionDirectory,
          "target/artifacts/com.sap.ondevice.production.ios.tests/" + projecName + "/" + projecName + "." + type + ".htm");

    InputStream is = null;
    try {
      is = new FileInputStream(redirectFile);
      final String content = IOUtils.toString(is);
      Assert
        .assertTrue(
              "OTA redirect file ("
                    + redirectFile
                    + ") for the pom file of the Appliction is invalid. It does not contain a valid reference to " + projecName +  " pom file.",
              content.contains("/com/sap/ondevice/production/ios/tests/" + projecName + "/" + version + "/" + projecName + "-" + version + "." + type));
    }
    finally {
      IOUtils.closeQuietly(is);
    }

  }

  
}