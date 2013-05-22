package com.sap.prd.mobile.ios.mios;

/*
 * #%L
 * Xcode Maven Plugin
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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.sonatype.aether.transfer.TransferListener;

/**
 * Registers a TransferListener on Aether that reacts on successful deployments. For each ipa file
 * deployed successfully a pointer file is written. This pointer file redirects to XXX
 * 
 * 
 * @goal post-deploy
 * 
 */
public class PostDeployMojo extends AbstractDeployMojo
{

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    try {

      //
      // [Q] Why do we use reflection here? What about a simple downcast to DefaultRepositorySystemSession?
      // [A] We cannot downcast since the downcast fails. The DefaultRepositorySystemSession that is available
      //     here is loaded by another class loader than the DefaultRepositorySystemSession used by the actual class.
      //     Hence we are in different runtime packages and the downcast is not possible.

      final TransferListener transferListener = getTransferListener();

      if (transferListener instanceof PreDeployMojo.PrepareIpaPointerFileTransferListener) {

        final TransferListener forward = ((PreDeployMojo.PrepareIpaPointerFileTransferListener) transferListener)
          .getForward();

        setTransferListener(forward);
      }
      else {
        getLog().info(
              "Found transfer listener '" + toString(transferListener)
                    + "'. Will not replace this transfer listener since it is not an instance of '"
                    + PreDeployMojo.PrepareIpaPointerFileTransferListener.class.getName() + "'.");
      }
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new MojoExecutionException(
            "Cannot set transfer listener for creating artifact redirect HTML files: " + e.getMessage(), e);
    }
  }
}
