package com.sap.prd.mobile.ios.mios;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
  //private static File testExecutionDirectoryApplication = getTestExecutionDirectory(testName, "moduleBuild/MyApp");

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

    File redirectFile = new File(testExecutionDirectoryLibrary,
          "target/artifacts/com.sap.ondevice.production.ios.tests/MyLibrary/MyLibrary.pom.htm");
    String content = IOUtils.toString(new FileInputStream(redirectFile));
    Assert.assertTrue("OTA redirect file (" + redirectFile + ") for the pom file of the library is invalid.",
          content.contains("com/sap/ondevice/production/ios/tests/MyLibrary"));
  }
}
