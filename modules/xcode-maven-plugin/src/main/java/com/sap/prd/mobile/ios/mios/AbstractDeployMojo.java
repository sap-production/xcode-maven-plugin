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

import java.lang.reflect.InvocationTargetException;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.transfer.TransferListener;

/**
 * Registers a TransferListener on Aether that reacts on successful deployments. For each ipa file
 * deployed successfully a pointer file is written. This pointer file redirects to XXX
 * 
 */
abstract class AbstractDeployMojo extends AbstractXCodeMojo
{
  /**
   * The current repository/network configuration of Maven.
   * 
   * @parameter default-value="${repositorySystemSession}"
   * @readonly
   */
  protected RepositorySystemSession repoSession;

  protected TransferListener getTransferListener() throws SecurityException, IllegalAccessException,
        InvocationTargetException, NoSuchMethodException
  {
    //
    // [Q] Why do we use reflection here? What about a simple downcast to DefaultRepositorySystemSession?
    // [A] We cannot downcast since the downcast fails. The DefaultRepositorySystemSession that is available
    //     here is loaded by another class loader than the DefaultRepositorySystemSession used by the actual class.
    //     Hence we are in different runtime packages and the downcast is not possible.

    return (TransferListener) this.repoSession.getClass()
      .getMethod("getTransferListener", new Class[0]).invoke(this.repoSession, new Object[0]);
  }

  protected void setTransferListener(TransferListener transferListener) throws SecurityException,
        IllegalAccessException, InvocationTargetException, NoSuchMethodException
  {

    this.repoSession.getClass().getMethod("setTransferListener", new Class[] { TransferListener.class })
      .invoke(this.repoSession, transferListener);

    getLog().info(
          "TransferListener '" + toString(transferListener) + "' has been set.");
  }

  protected String toString(TransferListener listener)
  {

    if (listener == null)
      return "<null>";
    return listener.getClass().getName() + "@" + System.identityHashCode(listener);
  }
}
