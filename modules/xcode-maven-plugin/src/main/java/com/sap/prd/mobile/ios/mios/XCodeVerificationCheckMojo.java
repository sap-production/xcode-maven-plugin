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

import static java.lang.String.format;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
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
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.sap.prd.mobile.ios.mios.XCodeContext.SourceCodeLocation;
import com.sap.prd.mobile.ios.mios.verificationchecks.v_1_0_0.Check;
import com.sap.prd.mobile.ios.mios.verificationchecks.v_1_0_0.Checks;

/**
 * Provides the possibility to perform verification checks.<br>
 * The check classes and their severities are described in an additional xml document, defined in
 * <code>xcode.verification.checks.definitionFile</code>.<br>
 * The specific checks have to be implemented in a separate project. The coordinates of that
 * projects needs to be provided on the <code>check</code> node belonging to the test as attributes
 * <code>groupId</code>, <code>artifactId</code> and <code>version</code>.<br>
 * The classpath for this goal will be extended by the jars found under the specified GAVs. <br>
 * Example checks definition:
 * 
 * <pre>
 * &lt;checks&gt;
 *   &lt;check groupId="my.group.id" artifactId="artifactId" version="1.0.0" severity="ERROR" class="com.my.MyVerificationCheck1"/&gt;
 *   &lt;check groupId="my.group.id" artifactId="artifactId" version="1.0.0" severity="WARNING" class="com.my.MyVerificationCheck2"/&gt;
 * &lt;/checks&gt;
 * </pre>
 * 
 * @goal verification-check
 * 
 */
public class XCodeVerificationCheckMojo extends BuildContextAwareMojo
{
  private final static String COLON = ":";

  private enum Protocol
  {

    HTTP() {

      @Override
      Reader getCheckDefinitions(String location) throws IOException
      {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet(getName() + COLON + location);

        String response = httpClient.execute(get, new BasicResponseHandler());
        return new StringReader(response);
      }

    },
    HTTPS() {

      @Override
      Reader getCheckDefinitions(String location) throws IOException
      {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet(getName() + COLON + location);

        String response = httpClient.execute(get, new BasicResponseHandler());
        return new StringReader(response);
      }

    },
    FILE() {

      @Override
      Reader getCheckDefinitions(String location) throws IOException
      {
        if (location.startsWith("//")) location = location.substring(2);
        final File f = new File(location);
        if (!f.canRead()) {
          throw new IOException("Cannot read checkDefintionFile '" + f + "'.");
        }

        return new InputStreamReader((new FileInputStream(f)), "UTF-8");
      }
    };
    abstract Reader getCheckDefinitions(String location) throws IOException;

    String getName()
    {
      return name().toLowerCase(Locale.ENGLISH);
    }

    static String getProtocols()
    {
      final StringBuilder sb = new StringBuilder(16);
      for (Protocol p : Protocol.values()) {
        if (sb.length() != 0)
          sb.append(", ");
        sb.append(p.getName());
      }
      return sb.toString();
    }
  }

  static class NoProtocolException extends XCodeException
  {

    private static final long serialVersionUID = -7510547403353515108L;

    NoProtocolException(String message)
    {
      this(message, null);
    }
    
    NoProtocolException(String message, Throwable cause) {
      super(message, cause);
    }
  };

  static class InvalidProtocolException extends XCodeException
  {

    private static final long serialVersionUID = -5510547403353515108L;

    InvalidProtocolException(String message)
    {
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
   * Parameter, which conrols the verification goal execution. By default, the verification goal will be
   * skipped.
   * 
   * @parameter expression="${xcode.verification.checks.skip}" default-value="true"
   * @since 1.9.3
   */
  private boolean skip;

  /**
   * The location where the check definition file is present. Could be a file on the local file
   * system or a remote located file, accessed via HTTP.
   * 
   * @parameter expression="${xcode.verification.checks.definitionFile}"
   * @since 1.9.3
   */
  private String checkDefinitionFile;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {

    if (skip) {

      getLog().info(
            String.format("Verification check goal has been skipped intentionally since parameter 'xcode.verification.checks.skip' is '%s'.", skip));
      return;
    }

    try {

      final Checks checks = getChecks(checkDefinitionFile);

      if (checks.getCheck().isEmpty()) {
        getLog().warn(String.format("No checks configured in '%s'.", checkDefinitionFile));
      }

      Map<Check, Exception> failedChecks = new HashMap<Check, Exception>();

      for(Check check : checks.getCheck()) {
        try {
          final ClassRealm verificationCheckRealm = extendClasspath(check);
          final Exception ex = performCheck(verificationCheckRealm, check);
          if(ex != null)
          {
            failedChecks.put(check,  ex);
          }
        } catch(DuplicateRealmException ex) {
          throw new MojoExecutionException(String.format("Check '%s' was configured twice in check definition file '%s'", check.getClazz(), checkDefinitionFile), ex);
        }
      }

      handleExceptions(failedChecks);

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
    catch (DependencyCollectionException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  private Exception performCheck(ClassRealm verificationCheckRealm, final Check checkDesc)
        throws MojoExecutionException
  {
    getLog().info(String.format("Performing verification check '%s'.", checkDesc.getClazz()));

    if (getLog().isDebugEnabled()) {

      final ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
      final PrintStream ps = new PrintStream(byteOs);

      try {
        verificationCheckRealm.display(ps);
        ps.close();
        getLog().debug(String.format("Using classloader for loading verification check '%s':%s%s", checkDesc.getClazz(), System.getProperty("line.separator"), new String(byteOs.toByteArray())));
      }
      finally {
        IOUtils.closeQuietly(ps);
      }
    }

    try {
    final Class<?> verificationCheckClass = Class.forName(checkDesc.getClazz(), true, verificationCheckRealm);
    
    getLog().debug(String.format("Verificaiton check class %s has been loaded by %s.", verificationCheckClass.getName(), verificationCheckClass.getClassLoader()));
    getLog().debug(String.format("Verification check super class %s has been loaded by %s.", verificationCheckClass.getSuperclass().getName(), verificationCheckClass.getSuperclass().getClassLoader()));
    getLog().debug(String.format("%s class used by this class (%s) has been loaded by %s.", VerificationCheck.class.getName(), this.getClass().getName(), VerificationCheck.class.getClassLoader() ));

    for (final String configuration : getConfigurations()) {
      for (final String sdk : getSDKs()) {
        getLog().info(
              String.format("Executing verification check: '%s' for configuration '%s' and sdk '%s'.", verificationCheckClass.getName(), configuration, sdk));
        final VerificationCheck verificationCheck = (VerificationCheck) verificationCheckClass.newInstance();
        verificationCheck.setXcodeContext(getXCodeContext(SourceCodeLocation.WORKING_COPY, configuration, sdk));
        verificationCheck.setMavenProject(project);
        verificationCheck.setLog(getLog());
        try {
          verificationCheck.check();
        }
        catch (VerificationException ex) {
          return ex;
        }
        catch (RuntimeException ex) {
          return ex;
        }
      }
    }
    return null;
    } catch(ClassNotFoundException ex) {
      throw new MojoExecutionException(
            "Could not load verification check '"
                  + checkDesc.getClazz()
                  + "'. May be your classpath has not been properly extended. Additional dependencies need to be provided with 'xcode.additionalClasspathElements'. "
                  + ex.getMessage(), ex);
    }
    catch(NoClassDefFoundError err) {
      getLog().error(String.format("Could not load verification check '%s'. " +
          "May be your classpath has not been properly extended. " +
          "Additional dependencies need to be declard inside the check definition file: %s",
             checkDesc.getClazz(), err.getMessage()), err);
      throw err;
    }
    catch (InstantiationException ex) {
      throw new MojoExecutionException(String.format("Could not instanciate verification check '%s': %s", checkDesc.getClazz(), ex.getMessage()), ex);
    }
    catch (IllegalAccessException ex) {
      throw new MojoExecutionException(String.format("Could not access verification check '%s': %s", checkDesc.getClazz(), ex.getMessage()), ex);
    }

  }

  private void handleExceptions(Map<Check, Exception> failedChecks)
        throws MojoExecutionException
  {
    boolean mustFailedTheBuild = false;
    for (Check failedCheck : failedChecks.keySet()) {
      handleException(failedCheck, failedChecks.get(failedCheck));
      if (failedCheck.getSeverity().equalsIgnoreCase("ERROR")) {
        mustFailedTheBuild = true;
      }
    }
    if (mustFailedTheBuild) {
      throw new MojoExecutionException("Verification checks failed. See the log file for details.");
    }
  }

  private void handleException(Check failedCheck, final Exception e)
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

  private ClassRealm extendClasspath(Check check) throws XCodeException, DependencyCollectionException, DuplicateRealmException, MalformedURLException
  {
    final org.sonatype.aether.artifact.Artifact artifact = parseDependency(check, getLog());

    final ClassLoader loader = this.getClass().getClassLoader();

    if (! (loader instanceof ClassRealm)) {

      throw new XCodeException("Could not add jar to classpath. Class loader '" + loader
            + "' is not an instance of '" + ClassRealm.class.getName() + "'.");
    }

    final ClassRealm classRealm = (ClassRealm) loader;

      if(artifact == null)
      {
        return classRealm;
      }

        final Set<String> scopes = new HashSet<String>(Arrays.asList(org.apache.maven.artifact.Artifact.SCOPE_COMPILE,
                                                                     org.apache.maven.artifact.Artifact.SCOPE_PROVIDED,
                                                                     org.apache.maven.artifact.Artifact.SCOPE_RUNTIME,
                                                                     org.apache.maven.artifact.Artifact.SCOPE_SYSTEM)); // do not resolve dependencies with scope "test".

        final XCodeDownloadManager downloadManager = new XCodeDownloadManager(projectRepos, repoSystem, repoSession, getLog());

        final Set<org.sonatype.aether.artifact.Artifact> theEmptyOmitsSet = Collections.emptySet();
        final Set<org.sonatype.aether.artifact.Artifact> omits = downloadManager.resolveArtifactWithTransitveDependencies(new Dependency(getXcodeMavenPluginGav(), org.apache.maven.artifact.Artifact.SCOPE_COMPILE), scopes, theEmptyOmitsSet);
        final Set<org.sonatype.aether.artifact.Artifact> artifacts = downloadManager.resolveArtifactWithTransitveDependencies(new Dependency(artifact, org.apache.maven.artifact.Artifact.SCOPE_COMPILE), scopes, omits);

        final ClassRealm childClassRealm = classRealm.createChildRealm(classRealm.getId() + "-" + check.getClazz());

        addDependencies(childClassRealm, artifacts);

        return childClassRealm;
  }

  private void addDependencies(final ClassRealm childClassRealm, Set<org.sonatype.aether.artifact.Artifact> artifacts) throws MalformedURLException
  {
    for(org.sonatype.aether.artifact.Artifact a : artifacts)
    {
        childClassRealm.addURL(a.getFile().toURI().toURL());
    }
  }

  static org.sonatype.aether.artifact.Artifact parseDependency(final Check check, final Log log)
        throws XCodeException
  {
    final String groupId = check.getGroupId();
    final String artifactId = check.getArtifactId();
    final String version = check.getVersion();
    
    if (StringUtils.isEmpty(groupId) && StringUtils.isEmpty(artifactId) && StringUtils.isEmpty(version)) {
      log.info(
        "No coordinates maintained for check represented by class '" + check.getClazz()
              + "'. Assuming this check is already contained in the classpath.");
      return null;
    }

    if(StringUtils.isEmpty(groupId))
      throw new XCodeException(String.format("groupId for check %s is null or emtpy", check.getClazz()));

    if(StringUtils.isEmpty(artifactId))
      throw new XCodeException(String.format("artifactId for check %s is null or emtpy", check.getClazz()));

    if(StringUtils.isEmpty(version))
      throw new XCodeException(String.format("version for check %s is null or emtpy", check.getClazz()));

    return new DefaultArtifact(groupId, artifactId, "jar", version);
  }

  static Checks getChecks(final String checkDefinitionFileLocation) throws XCodeException, IOException, JAXBException
  {
    Reader checkDefinitions = null;

    try {
      checkDefinitions = getChecksDescriptor(checkDefinitionFileLocation);
      return (Checks) JAXBContext.newInstance(Checks.class).createUnmarshaller().unmarshal(checkDefinitions);
    }
    finally {
      IOUtils.closeQuietly(checkDefinitions);
    }
  }
  
  org.sonatype.aether.artifact.Artifact getXcodeMavenPluginGav() throws XCodeException {

    InputStream is = null;
    
    try {
        is = XCodeVerificationCheckMojo.class.getResourceAsStream("/misc/project.properties");
        
        if(is == null)
        {
          throw new XCodeException("Cannot get the GAV of the xcode-maven-plugin");
        }
        
        Properties props = new Properties();
        props.load(is);
        
        final String groupId = props.getProperty("xcode-plugin-groupId");
        final String artifactId = props.getProperty("xcode-plugin-artifactId");
        final String version = props.getProperty("xcode-plugin-version");
        return new DefaultArtifact(groupId, artifactId, "jar", version);
    } catch(final IOException ex) {
        throw new XCodeException("Cannot get the GAV for the xcode-maven-plugin", ex);
    } finally {
      IOUtils.closeQuietly(is);
    }
  }

  static Reader getChecksDescriptor(final String checkDefinitionFileLocation) throws XCodeException, IOException
  {
    if (checkDefinitionFileLocation == null || checkDefinitionFileLocation.trim().isEmpty()) {
      throw new XCodeException("CheckDefinitionFile was not configured. Cannot perform verification checks. Define check definition file with paramater 'xcode.verification.checks.definitionFile'.");
    }

    Location location;
    try {
      location = Location.getLocation(checkDefinitionFileLocation);
    }
    catch (NoProtocolException e) {
      throw new NoProtocolException(format("No protocol found: %s. Provide a protocol [%s]" +
            " for parameter 'xcode.verification.checks.definitionFile', e.g. http://example.com/checkDefinitions.xml.",
            checkDefinitionFileLocation, Protocol.getProtocols()), e);
    }

    try {
      Protocol protocol = Protocol.valueOf(location.protocol);
      return protocol.getCheckDefinitions(location.location);
    }
    catch (IllegalArgumentException ex) {
      throw new InvalidProtocolException(format("Invalid protocol provided: '%s'. Supported values are:'%s'.",
            location.protocol, Protocol.getProtocols()));
    }
    catch (IOException ex) {
      throw new IOException(format("Cannot get check definitions from '%s'.", checkDefinitionFileLocation), ex);
    }
  }

  static class Location
  {
    static Location getLocation(final String locationUriString) throws NoProtocolException
    {
      URI uri = URI.create(locationUriString.trim());
      String protocol = uri.getScheme();
      if (protocol == null) throw new NoProtocolException(locationUriString);
      String location = uri.getPath();
      if (location == null) {
        location = uri.getSchemeSpecificPart();
      }
      return new Location(protocol, location);
    }
    
    final String protocol;
    final String location;

    public Location(String protocol, String location)
    {
      this.protocol = protocol.toUpperCase(Locale.ENGLISH);
      this.location = location;
    }
  }
}
