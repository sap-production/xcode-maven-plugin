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

import org.sonatype.aether.artifact.Artifact;

public class SideArtifactNotFoundException extends XCodeException
{

  private static final long serialVersionUID = -9102670897968423620L;

  private final Artifact sideArtifact;

  public SideArtifactNotFoundException(String message, Artifact sideArtifact)
  {
    this(message, sideArtifact, null);
  }

  public SideArtifactNotFoundException(String message, Artifact sideArtifact, Throwable ex)
  {
    super(message, ex);
    this.sideArtifact = sideArtifact;
  }

  public Artifact getSideArtifact()
  {
    return this.sideArtifact;
  }
}
