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

import static junit.framework.Assert.assertTrue;
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

public class OTAManagerTest
{

  @Test
  public void testOtaUrlIsNull() throws Exception
  {
    OTAManager otaManager = new OTAManager(null, "", "", "", "", "", "", null);

    Assert.assertFalse(otaManager.generateOtaHTML());

    CharArrayWriter charWriter = new CharArrayWriter();
    try {
      otaManager.writeOtaHtml(new PrintWriter(charWriter));
      assertTrue(charWriter.size() == 0);
    }
    finally {
      closeQuietly(charWriter);
    }
  }

  @Test
  public void testOtaBasic() throws Exception
  {
    OTAManager otaManager = new OTAManager(new URL("http://abc123"), "Title",
          "com.sap.test", "1.0.0", "-ipa", "-ota", "", null);
    expectValuesInResult(otaManager,
          "src=\"http://abc123?title=Title&bundleIdentifier=com.sap.test&bundleVersion=1.0.0" +
                "&ipaClassifier=-ipa&otaClassifier=-ota\"");
  }

  @Test
  public void testOtaTemplate() throws Exception
  {
    OTAManager otaManager = new OTAManager(new URL("http://abc123"), "Title",
          "com.sap.test", "1.0.0", "-ipa", "-ota", "otaBuildTemplate2.html", null);
    expectValuesInResult(otaManager,
          "src=\"http://abc123?title=Title&bundleIdentifier=com.sap.test&bundleVersion=1.0.0",
          "My Custom Template!");
  }

  @Test
  public void testOtaTemplateWithUserParams() throws Exception
  {
    Map<String, String> params = new HashMap<String, String>();
    params.put("pKey1", "pValue1");
    params.put("pKey2", "pValue2");
    OTAManager otaManager = new OTAManager(new URL("http://abc123"), "Title",
          "com.sap.test", "1.0.0", "-ipa", "-ota", "otaBuildTemplate2.html", params);
    expectValuesInResult(otaManager,
          "src=\"http://abc123?title=Title&bundleIdentifier=com.sap.test&bundleVersion=1.0.0",
          "My Custom Template!",
          "XX MyValue1=pValue1 XX",
          "YY MyValue2=pValue2 YY");
  }

  private void expectValuesInResult(OTAManager otaManager, String... expected) throws IOException
  {
    assertTrue(otaManager.generateOtaHTML());

    StringWriter stringWriter = new StringWriter();
    try {
      otaManager.writeOtaHtml(new PrintWriter(stringWriter));
      String result = stringWriter.toString();
      assertTrue(result.length() > 0);

      for (String check : expected) {
        assertTrue("Not contained: '" + check + "' in '" + result + "'", result.contains(check));
      }

    }
    finally {
      closeQuietly(stringWriter);
    }
  }

}
