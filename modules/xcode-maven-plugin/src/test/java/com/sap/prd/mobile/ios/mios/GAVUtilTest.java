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

import org.apache.maven.artifact.Artifact;
import org.junit.Assert;
import org.junit.Test;

import com.sap.prd.mobile.ios.mios.GAVUtil;

public class GAVUtilTest
{

  @Test
  public void testToColonNotation() throws Exception
  {
    Assert.assertEquals("com.sap.mytest:testArtifact:tar:classifier:1.0.0",
          GAVUtil.toColonNotation("com.sap.mytest", "testArtifact", "1.0.0", "tar", "classifier"));
  }

  @Test
  public void testToArtifact() throws Exception
  {
    Artifact artifact = GAVUtil.getArtifact("com.sap.mytest:testArtifact:tar:classifier:1.0.0");

    Assert.assertEquals("com.sap.mytest", artifact.getGroupId());
    Assert.assertEquals("testArtifact", artifact.getArtifactId());
    Assert.assertEquals("1.0.0", artifact.getVersion());
    Assert.assertEquals("classifier", artifact.getClassifier());
    Assert.assertEquals("tar", artifact.getType());

  }
}
