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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.sap.prd.mobile.ios.mios.XCodeContext.SourceCodeLocation;
import com.sap.prd.mobile.ios.mios.validationchecks.v_1_0_0.Checks;

/**
 * 
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
   * Comma separated list of GAVs. The corresponding jar files are added to the classpath.
   * @parameter expression="${xcode.additionalClasspathElements}"
   */
  private String dependencies;

  /**
   * @parameter expression="${xcode.verification.checks.skip}" default-value="true"
   * @readonly
   */
  private boolean skip;

  /**
   * 
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

    if (this.dependencies != null && !this.dependencies.trim().isEmpty()) {
      extendClasspath();
    }

    performChecks();
  }

  private void performChecks() throws MojoExecutionException
  {

    String verificationCheckClassName = null;
    try {

      if (getChecks().getCheck().isEmpty()) {
        getLog().warn("No checks configured in '" + checkDefinitionFile + "'.");
      }

      for (final com.sap.prd.mobile.ios.mios.validationchecks.v_1_0_0.Check checkDesc : getChecks().getCheck()) {
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
              String severity = checkDesc.getSeverity();
              final String message = "Verification check '" + check.getClass().getName() + " failed: "
                    + ex.getMessage();
              handleException(ex, severity, message);
            }
            catch (Exception ex) {
              String severity = checkDesc.getSeverity();

              final String message = "Cannot perform check: " + check.getClass().getName()
                    + ". Error during test setup: " + ex.getMessage();

              handleException(ex, severity, message);
              
            }
          }
        }
      }
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
    catch (IOException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
    catch (JAXBException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  private void handleException(Exception ex, String severity, final String message) throws MojoExecutionException
  {
    if(severity.equalsIgnoreCase("WARNING")) {
      getLog().warn(message);
    } else {
      throw new MojoExecutionException(message, ex);
    }
  }

  private void extendClasspath() throws MojoExecutionException
  {
    final Set<Artifact> dependencies = parseDependencies(this.dependencies);

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

  private Set<Artifact> parseDependencies(String deps)
  {

    Set<Artifact> result = new HashSet<Artifact>();

    if (deps == null || deps.trim().isEmpty())
      return result;

    Collection<String> coords = Arrays.asList(deps.split(","));

    for (String coord : coords) {
      result.add(new DefaultArtifact(coord));
    }

    return result;
  }

  private Checks getChecks() throws IOException, MojoExecutionException, JAXBException
  {

    if (checkDefinitionFile == null || checkDefinitionFile.trim().isEmpty()) {
      throw new MojoExecutionException("CheckDefinitionFile was not configured. Cannot perform verification checks");
    }

    final int index = checkDefinitionFile.indexOf(COLON);

    if(index <= 0) {
      throw new MojoExecutionException("No protocol found: " + checkDefinitionFile + ". Provide a protocol "
            + Protocol.getProtocols() + " for parameter" + "'xcode.verification.checks.definitionFile'"
            + ". Provide a protocol, e.g. http://example.com/checkDefinitions.xml.");
    }

    final String protocol = checkDefinitionFile.substring(0, index);
    final String location = checkDefinitionFile.substring(index + COLON.length());

    final Reader checkDefinitions;
    try {
      checkDefinitions = Protocol.valueOf(protocol.toUpperCase(Locale.ENGLISH)).getCheckDefinitions(location);
    } catch(IllegalArgumentException ex) {
      throw new MojoExecutionException("Invalid protocol provided: '" + protocol + "'. Supported values are:'" + Protocol.getProtocols() + "'.");
    } catch(IOException ex) {
      throw new IOException("Cannot get check definitions from '" + checkDefinitionFile + "'.", ex);
    }

    try {
      return (Checks) JAXBContext.newInstance(Checks.class).createUnmarshaller().unmarshal(checkDefinitions);
    } finally {
      IOUtils.closeQuietly(checkDefinitions);
    }
  }
}
