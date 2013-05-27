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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.sap.prd.mobile.ios.mios.XCodeContext.SourceCodeLocation;
import com.sap.prd.mobile.ios.mios.validationchecks.v_1_0_0.Check;
import com.sap.prd.mobile.ios.mios.validationchecks.v_1_0_0.Checks;

/**
 * Provides the possibility to perform validation checks.<br>
 * The check classes and their severities are described in an additional xml document, defined in <code>xcode.verification.checks.definitionFile</code>.<br>
 * The specific checks have to be implemented in a separate project. The coordinates of that projects needs to be provided on the <code>check</code> node belonging to the test as attributes
 * <code>groupId</code>, <code>artifactId</code> and <code>version</code>.<br>
 * The classpath for this goal will be extended by the jars found under the specified GAVs.
 * <br>
 * Example checks definition:
 * <pre>&lt;checks&gt;
 *   &lt;check groupId="my.group.id" artifactId="artifactId" version="1.0.0" severity="ERROR" class="com.my.MyValidationCheck1"/&gt;
 *   &lt;check groupId="my.group.id" artifactId="artifactId" version="1.0.0" severity="WARNING" class="com.my.MyValidationCheck2"/&gt;
 * &lt;/checks&gt;</pre>
 * @goal validation-check
 * 
 */
public class XCodeValidationCheckMojo extends BuildContextAwareMojo
{
  private final static String COLON = ":";

  private enum Protocol {

    HTTP() {

      @Override
      Reader getCheckDefinitions(String location) throws IOException
      {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet(getName() + COLON + location);

        String response = httpClient.execute(get, new BasicResponseHandler());
        return new StringReader(response);
      }
    }, FILE() {

      @Override
      Reader getCheckDefinitions(String location) throws IOException
      {
        final File f = new File(location);
        if (!f.canRead()) {
          throw new IOException("Cannot read checkDefintionFile '" + f + "'.");
        }

        return new InputStreamReader((new FileInputStream(f)), "UTF-8");
      }
    };
    abstract Reader getCheckDefinitions(String location) throws IOException;

    String getName() {
      return name().toLowerCase(Locale.ENGLISH);
    }

    static String getProtocols() {
      final StringBuilder sb = new StringBuilder(16);
      for(Protocol p : Protocol.values()) {
        if(sb.length() != 0)
          sb.append(", ");
        sb.append(p.getName());
      }
      return sb.toString();
    }
  }

  static class NoProtocolException extends XCodeException {

    private static final long serialVersionUID = -7510547403353515108L;
    
    NoProtocolException(String message) {
      super(message);
    }
  };

  static class InvalidProtocolException extends XCodeException {

    private static final long serialVersionUID = -5510547403353515108L;
    
    InvalidProtocolException(String message) {
      super(message);
    }
  };


  
  /**
   * The entry point to Aether, i.e. the component doing all the work.
   * 
   * @component
   */
  protected RepositorySystem repoSystem;

  /**
   * The current repository/network configuration of Maven.
   * 
   * @parameter default-value="${repositorySystemSession}"
   * @readonly
   */
  protected RepositorySystemSession repoSession;

  /**
   * The project's remote repositories to use for the resolution of project dependencies.
   * 
   * @parameter default-value="${project.remoteProjectRepositories}"
   * @readonly
   */
  protected List<RemoteRepository> projectRepos;

  /**
   * Parameter, which conrols the validation goal execution. By default, the validation goal will be skipped.
   * @parameter expression="${xcode.verification.checks.skip}" default-value="true"
   * @readonly
   */
  private boolean skip;

  /**
   * The location where the check definition file is present. 
   * Could be a file on the local file system or a remote located file, accessed via HTTP. 
   * @parameter expression="${xcode.verification.checks.definitionFile}"
   * @readonly
   */
  private String checkDefinitionFile;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {

    if (skip) {

      getLog().info(
            "Verification check goal has been skipped intentionally since parameter 'xcode.verification.checks.skip' is '"
                  + skip + "'.");
      return;
    }

      try {

        final Checks checks = getChecks(checkDefinitionFile);

        extendClasspath(checks);

        performChecks(checks);

      }
      catch (XCodeException e) {
        throw new MojoExecutionException(e.getMessage(), e);
      }
      catch (IOException e) {
        throw new MojoExecutionException(e.getMessage(), e);
      }
      catch (JAXBException e) {
        throw new MojoExecutionException(e.getMessage(), e);
      }
  }

  private void performChecks(final Checks checks) throws MojoExecutionException
  {

    String verificationCheckClassName = null;
    Map<com.sap.prd.mobile.ios.mios.validationchecks.v_1_0_0.Check, Exception> failedChecks = new HashMap<com.sap.prd.mobile.ios.mios.validationchecks.v_1_0_0.Check, Exception>();
    try {

      if (checks.getCheck().isEmpty()) {
        getLog().warn("No checks configured in '" + checkDefinitionFile + "'.");
      }

      for (final com.sap.prd.mobile.ios.mios.validationchecks.v_1_0_0.Check checkDesc : checks.getCheck()) {

        verificationCheckClassName = checkDesc.getClazz();
        final Class<?> clazz = Class.forName(verificationCheckClassName);
        for (final String configuration : getConfigurations()) {
          for (final String sdk : getSDKs()) {
            getLog().info(
                  "Executing verification check: '" + clazz.getName() + "' for configuration '" + configuration
                        + "' and sdk '" + sdk + "'.");
            final ValidationCheck check = (ValidationCheck) clazz.newInstance();
            check.setXcodeContext(getXCodeContext(SourceCodeLocation.WORKING_COPY, configuration, sdk));
            check.setMavenProject(project);
            check.setLog(getLog());
            try {
              check.check();
            }
            catch (VerificationException ex) {
              failedChecks.put(checkDesc, ex);
            }
            catch (Exception ex) {
              failedChecks.put(checkDesc, ex);
            }
          }
        }
      }
      handleExceptions(failedChecks);
    }
    catch (MojoExecutionException e) {
      throw e;
    }
    catch (ClassNotFoundException e) {
      throw new MojoExecutionException(
            "Could not load verification check '"
                  + verificationCheckClassName
                  + "'. May be your classpath has not been properly extended. Additional dependencies need to be provided with 'xcode.additionalClasspathElements'. "
                  + e.getMessage(), e);
    }
    catch (InstantiationException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
    catch (IllegalAccessException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }
  
  private void handleExceptions(Map<com.sap.prd.mobile.ios.mios.validationchecks.v_1_0_0.Check, Exception> failedChecks)
        throws MojoExecutionException
  {
    boolean mustFailedTheBuild = false;
    for (com.sap.prd.mobile.ios.mios.validationchecks.v_1_0_0.Check failedCheck : failedChecks.keySet()) {
      handleException(failedCheck, failedChecks.get(failedCheck));
      if (failedCheck.getSeverity().equalsIgnoreCase("ERROR")) {
        mustFailedTheBuild = true;
      }
    }
    if (mustFailedTheBuild) {
      throw new MojoExecutionException("Validation checks failed. See the log file for details.");
    }
  }

  private void handleException(com.sap.prd.mobile.ios.mios.validationchecks.v_1_0_0.Check failedCheck, final Exception e)
        throws MojoExecutionException
  {
    final String message;
    if (e instanceof VerificationException) {
      message = "Verification check '" + failedCheck.getClazz() + " failed. " + e.getMessage();
    }
    else {
      message = "Cannot perform check: " + failedCheck.getClazz() + ". Error during test setup " + e.getMessage();
    }
    if (failedCheck.getSeverity().equalsIgnoreCase("WARNING")) {
      getLog().warn(message);
    }
    else {
      getLog().error(message);
    }
  }

  private void extendClasspath(Checks checks) throws MojoExecutionException
  {
    final Set<Artifact> dependencies = parseDependencies(checks, getLog());
    
    final ClassRealm classRealm;
    final ClassLoader loader = this.getClass().getClassLoader();
    if (loader instanceof ClassRealm) {
      classRealm = (ClassRealm) loader;
    }
    else {
      throw new RuntimeException("Could not add jar to classpath. Class loader '" + loader
            + "' is not an instance of '" + ClassRealm.class.getName() + "'.");
    }

    for (Artifact dependencyArtifact : dependencies) {

      final File jar;
      try {
        Artifact artifact = new XCodeDownloadManager(projectRepos, repoSystem, repoSession)
          .resolveArtifact(dependencyArtifact);
        jar = artifact.getFile();
      }
      catch (SideArtifactNotFoundException e1) {
        throw new MojoExecutionException(e1.getMessage(), e1);
      }

      try {
        classRealm.addURL(jar.toURI().toURL());
      }
      catch (final MalformedURLException e) {
        throw new MojoExecutionException(
              "Failed to add file '" + jar.getAbsolutePath() + "' to classloader: ", e);
      }
    }
  }

  static Set<Artifact> parseDependencies(final Checks checks, final Log log) throws MojoExecutionException
  {
      final Set<Artifact> result = new HashSet<Artifact>();

      for (Check check : checks.getCheck()) {

        final String coords = StringUtils.join(
              Arrays.asList(check.getGroupId(), check.getArtifactId(), check.getVersion()), ":");

        if (coords.equals("::")) {
          log.info(
                "No coordinates maintained for check represented by class '" + check.getClazz()
                      + "'. Assuming this check is already contained in the classpath.");
          continue;
        }

        if (coords.matches("^:.*|.*:$|.*::.*"))
          throw new MojoExecutionException("Invalid coordinates: '" + coords
                + "' maintained for check represented by class '" + check.getClazz()
                + "'. At least one of groupId, artifactId or version is missing.");

        result.add(new DefaultArtifact(coords));
      }

      return result;
  }

  static Checks getChecks(final String checkDefinitionFileLocation) throws XCodeException, IOException, JAXBException
  {
    Reader checkDefinitions = null;

    try {
      checkDefinitions = getChecksDescriptor(checkDefinitionFileLocation);
      return (Checks) JAXBContext.newInstance(Checks.class).createUnmarshaller().unmarshal(checkDefinitions);
    } finally {
      IOUtils.closeQuietly(checkDefinitions);
    }
  }
  
  static Reader getChecksDescriptor(final String checkDefinitionFileLocation) throws XCodeException, IOException {
    if (checkDefinitionFileLocation == null || checkDefinitionFileLocation.trim().isEmpty()) {
      throw new XCodeException("CheckDefinitionFile was not configured. Cannot perform verification checks");
    }

    final int index = checkDefinitionFileLocation.indexOf(COLON);

    if(index <= 0) {
      throw new NoProtocolException("No protocol found: " + checkDefinitionFileLocation + ". Provide a protocol "
            + Protocol.getProtocols() + " for parameter" + "'xcode.verification.checks.definitionFile'"
            + ". Provide a protocol, e.g. http://example.com/checkDefinitions.xml.");
    }

    final String protocol = checkDefinitionFileLocation.substring(0, index);
    final String location = checkDefinitionFileLocation.substring(index + COLON.length());

    try {
      return Protocol.valueOf(protocol.toUpperCase(Locale.ENGLISH)).getCheckDefinitions(location);
    } catch(IllegalArgumentException ex) {
      throw new InvalidProtocolException("Invalid protocol provided: '" + protocol + "'. Supported values are:'" + Protocol.getProtocols() + "'.");
    } catch(IOException ex) {
      throw new IOException("Cannot get check definitions from '" + checkDefinitionFileLocation + "'.", ex);
    }
  }
}
