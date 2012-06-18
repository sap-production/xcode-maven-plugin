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

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import junit.framework.Assert;

import org.junit.Test;

import com.sap.prd.mobile.ios.mios.OTAManager;

public class OTAManagerTest
{

  @Test
  public void testOtaUrlIsNull() throws Exception
  {
    OTAManager otaManager = new OTAManager(null, "", "", "", "", "");

    Assert.assertFalse(otaManager.generateOtaHTML());

    CharArrayWriter charWriter = new CharArrayWriter();
    otaManager.writeOtaHtml(new PrintWriter(charWriter));

    Assert.assertTrue(charWriter.size() == 0);
  }
}
