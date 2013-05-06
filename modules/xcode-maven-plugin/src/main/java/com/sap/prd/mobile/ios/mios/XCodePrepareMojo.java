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

import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;

/**
 * Prepares the local build environment. Copies and unpacks the artifacts of the
 * referenced projects into the target folder.
 * 
 * @goal prepare-xcode-build
 * @requiresDependencyResolution
 */
public class XCodePrepareMojo extends AbstractXCodeMojo {

	/**
	 * @parameter expression="${project}"
	 * @readonly
	 * @required
	 */
	public MavenProject project;

	/**
	 * @component role="org.codehaus.plexus.archiver.manager.ArchiverManager"
	 * @required
	 */
	private ArchiverManager archiverManager;

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
	 * The project's remote repositories to use for the resolution of project
	 * dependencies.
	 * 
	 * @parameter default-value="${project.remoteProjectRepositories}"
	 * @readonly
	 */
	protected List<RemoteRepository> projectRepos;

	/**
	 * If set to <code>true</code> the dependency resolution will try to
	 * retrieve the fat libs instead of the sdk specific ones. In all cases the
	 * lib resolution will try to fallback to the other library type if the
	 * preferred type is not available.
	 * 
	 * @parameter expression="${xcode.preferFatLibs}" default-value="false"
	 * @since 1.5.2
	 */
	protected boolean preferFatLibs;

	/**
	 * @parameter expression="${xcode.useSymbolicLinks}" default-value="false"
	 */
	private boolean useSymbolicLinks;

  /**
   * @parameter
   * @deprecated
   */
  private Map<String, String> additionalPackagingTypes;

  /**
   * @parameter
   */
	private Set<PackagingTypeDescriptor> packagingTypeDescriptors;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

	  Set<PackagingTypeDescriptor> _packagingTypeDescriptors = new HashSet<PackagingTypeDescriptor>();
	  if(packagingTypeDescriptors != null) {
	    _packagingTypeDescriptors.addAll(packagingTypeDescriptors);
	  }

    handleLegacyDescriptors(_packagingTypeDescriptors);

		try {
			new XCodePrepareBuildManager(getLog(), archiverManager,
					repoSession, repoSystem, projectRepos, useSymbolicLinks,
					_packagingTypeDescriptors).setPreferFalLibs(preferFatLibs)
					.prepareBuild(project, getConfigurations(), getSDKs());
		} catch (XCodeException ex) {
			throw new MojoExecutionException(
					"Cannot prepare build environment", ex);
		} catch (IOException ex) {
			throw new MojoExecutionException(
					"Cannot prepare build environment", ex);
		}
	}

  private void handleLegacyDescriptors(Set<PackagingTypeDescriptor> _packagingTypeDescriptors)
  {
    if (additionalPackagingTypes == null || additionalPackagingTypes.isEmpty())
      return;

    getLog().warn(
          "AdditionalPackagingTypes has been found: '" + additionalPackagingTypes
                + "'. This property is deprecated. Use packagingTypeDescriptors instead.");

    final Set<PackagingTypeDescriptor> descriptors = new TreeSet<PackagingTypeDescriptor>(
          new Comparator<PackagingTypeDescriptor>() {

            @Override
            public int compare(PackagingTypeDescriptor o1, PackagingTypeDescriptor o2)
            {
              return o1.getPackagingType().compareTo(o2.getPackagingType());
            }
          });

    if(packagingTypeDescriptors != null) {
      descriptors.addAll(packagingTypeDescriptors);
    }

    for (Map.Entry<String, String> legacyDescriptor : additionalPackagingTypes.entrySet()) {
      PackagingTypeDescriptor legacyDesc = new PackagingTypeDescriptor().setPackagingType(legacyDescriptor.getKey())
        .setAction(legacyDescriptor.getValue());

      if (descriptors.contains(legacyDesc)) {
        getLog()
          .warn("Descriptor '"
                + legacyDesc
                + " is defined in the deprectated way (additionalPackagingTypes) and also via packagingTypeDescriptors. Remove the definition via additionalPackagingTypes.");
      }
      else {
        _packagingTypeDescriptors.add(legacyDesc);
      }
    }
  }
}
