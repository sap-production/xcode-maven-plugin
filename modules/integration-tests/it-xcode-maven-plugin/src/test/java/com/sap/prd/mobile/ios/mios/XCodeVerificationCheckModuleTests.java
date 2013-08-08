/*
 * #%L
 * it-xcode-maven-plugin
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.Verifier;
import org.apache.maven.model.Model;
import org.junit.Test;

public class XCodeVerificationCheckModuleTests extends XCodeTest
{

  @Test
  public void testModuleBuildCleanInstallSeparateGoal() throws Exception
  {

    XCodeTestParameters params = prepareParameters("clean", "install",
          getMavenXcodePluginGroupId() + ":" + getMavenXcodePluginArtifactId() + ":"
                + getMavenXcodePluginVersion() + ":" + "verification-check");

    Verifier v = test(params);

    v.verifyTextInLog("Verification check 'com.sap.prd.mobile.ios.mios.TestMetadataCheck failed. 7430190670433136460");
  }

  @Test
  public void testModuleBuildCleanInstall() throws Exception
  {
    XCodeTestParameters params = prepareParameters("clean", "install");

    Verifier v = test(params);

    v.verifyTextInLog("Verification check 'com.sap.prd.mobile.ios.mios.TestMetadataCheck failed. 7430190670433136460");
  }

  @Test
  public void testModuleBuildCleanPackageSeparateGoal() throws Exception
  {
    XCodeTestParameters params = prepareParameters("clean", "package",
          getMavenXcodePluginGroupId() + ":" + getMavenXcodePluginArtifactId() + ":"
                + getMavenXcodePluginVersion() + ":" + "verification-check");

    Verifier v = test(params);

    v.verifyTextInLog("Verification check 'com.sap.prd.mobile.ios.mios.TestMetadataCheck failed. 7430190670433136460");
  }

  private XCodeTestParameters prepareParameters(String... targets) throws IOException
  {
    final String testName = getTestName();

    final String dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());

    final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass().getName());

    Map<String, String> pomReplacements = new HashMap<String, String>();
    pomReplacements.put(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
    pomReplacements.put(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);

    final List<String> additionalCommandLineOptions = Arrays.asList("-Dxcode.verification.checks.skip=false",
          "-Dxcode.verification.checks.definitionFile=file:./checkDefinitions.xml");

    XCodeTestParameters.Builder builder = new XCodeTestParameters.Builder();
    builder.setTestName(testName).setProjectDirectory(new File(getTestRootDirectory(), "moduleBuild"))
      .addAdditionalCommandLineOptions(additionalCommandLineOptions).addPomReplacements(pomReplacements)
      .setModifier(new ModuleProjectModifier()).addTargets(targets);
    return builder.create();
  }

  private class ModuleProjectModifier extends AbstractProjectModifier
  {

    @Override
    void execute() throws Exception
    {
      FileUtils.copyDirectory(new File(testExecutionDirectory, "MyApp"), new File(testExecutionDirectory, "MyApp2"));

      final File pom = new File(testExecutionDirectory, "MyApp2/pom.xml");
      Model model = getModel(pom);
      model.setArtifactId("MyApp2");
      persistModel(pom, model);

      final File myAppProjectFile = new File(testExecutionDirectory, "MyApp2/src/xcode/MyApp.xcodeproj");
      FileUtils
        .copyDirectory(myAppProjectFile, new File(testExecutionDirectory, "MyApp2/src/xcode/MyApp2.xcodeproj"));
      FileUtils.deleteDirectory(myAppProjectFile);

      final File pom2 = new File(testExecutionDirectory, "pom.xml");
      model = getModel(pom2);
      List<String> modules = model.getModules();
      modules.add("MyApp2/pom.xml");
      persistModel(pom2, model);

      String checks = FileUtils.readFileToString(new File("src/test/resources/checkDefinitions.xml"));
      checks = checks.replaceAll("\\$\\{SERVERITY\\}", "WARNING");
      checks = checks.replaceAll("\\$\\{xcode.maven.plugin.version\\}", getMavenXcodePluginVersion()); //plugin version is not acurate, but does work.
      FileUtils.writeStringToFile(new File(testExecutionDirectory, "checkDefinitions.xml"), checks);
    }

  }

}
