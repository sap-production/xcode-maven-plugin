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

import static org.junit.Assert.*;

import org.junit.Test;

public class RemoveTrailingCharactersVersionTransformerTest
{
  private final static RemoveTrailingCharactersVersionTransformer cfBundleShortVersionStringTransformer = new RemoveTrailingCharactersVersionTransformer(
        3);
  private final static RemoveTrailingCharactersVersionTransformer cfBundleVersionTransformer = new RemoveTrailingCharactersVersionTransformer(
        -1);

  @Test
  public void testCFBundleShortVersionStringThreeDotVersion() throws Exception
  {
    assertEquals("1.2.3", cfBundleShortVersionStringTransformer.transform("1.2.3"));
  }

  @Test
  public void testCFBundleShortVersionStringVersionWithLeadingZero() throws Exception
  {
    assertEquals("1.02.3", cfBundleShortVersionStringTransformer.transform("1.02.3"));
  }

  public void testCFBundleShortVersionStringVersionWithEmptyPart() throws Exception
  {
    assertEquals("1.0.3", cfBundleShortVersionStringTransformer.transform("1..3"));
  }

  @Test(expected = NumberFormatException.class)
  public void testCFBundleShortVersionStringVersionWithNegativeVersionPart() throws Exception
  {
    // Such a version will not be accepted as valid CFBundleShortVersionString by apple !!!
    cfBundleShortVersionStringTransformer.transform("1.-2.3");
  }

  @Test
  public void testCFBundleShortVersionStringLongVersion() throws Exception
  {
    long part = System.currentTimeMillis();
    assertEquals("1.2." + part, cfBundleShortVersionStringTransformer.transform("1.2." + part));
  }

  @Test
  public void testCFBundleShortVersionStringTwoDotSnapshotVersion()
  {
    assertEquals("1.2.0", cfBundleShortVersionStringTransformer.transform("1.2-SNAPSHOT"));
  }

  @Test
  public void testCFBundleShortVersionStringThreeDotSnapshotVersion()
  {
    assertEquals("1.2.3", cfBundleShortVersionStringTransformer.transform("1.2.3-SNAPSHOT"));
  }

  @Test
  public void testCFBundleShortVersionStringTruncateVersion()
  {
    assertEquals("1.2.3", cfBundleShortVersionStringTransformer.transform("1.2.3.4"));
  }

  @Test
  public void testCFBundleShortVersionStringTruncateSnapshotVersion()
  {
    assertEquals("1.2.3", cfBundleShortVersionStringTransformer.transform("1.2.3.4-SNAPSHOT"));
  }

  @Test
  public void testCFBundleShortVersionStringShortVersion()
  {
    assertEquals("1.2.0", cfBundleShortVersionStringTransformer.transform("1.2"));
  }

  @Test
  public void testCFBundleShortVersionStringVersionWithoutDot()
  {
    assertEquals("1.0.0", cfBundleShortVersionStringTransformer.transform("1"));
  }

  @Test
  public void testCFBundleShortVersionStringEmptyVersion()
  {
    assertEquals("0.0.0", cfBundleShortVersionStringTransformer.transform(""));
  }

  @Test(expected = NullPointerException.class)
  public void tesCFBundleShortVersionStringNullVersion()
  {
    cfBundleShortVersionStringTransformer.transform(null);
  }

  @Test
  public void testCFBundleShortVersionStringVersionEndsWithDot()
  {
    assertEquals("1.0.0", cfBundleShortVersionStringTransformer.transform("1."));
  }

  @Test
  public void testCFBundleShortVersionStringVersionWithAlpha()
  {
    assertEquals("1.0.0", cfBundleShortVersionStringTransformer.transform("1.alpha-1.2"));
  }

  @Test
  public void testCFBundleShortVersionStringVersionWithAlphaAppendix()
  {
    assertEquals("1.1.0", cfBundleShortVersionStringTransformer.transform("1.1-alpha-1.2"));
  }

  @Test
  public void testCFBundleVersionDoNotTruncateVersion()
  {
    assertEquals("1.2.3.4", cfBundleVersionTransformer.transform("1.2.3.4"));
  }
}
