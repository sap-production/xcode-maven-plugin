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
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.sap.prd.mobile.ios.mios.DomUtils;

public class DomUtilsTest
{

  @Test
  public void testInvalidDoc() throws ParserConfigurationException, SAXException, IOException
  {
    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document doc = builder.parse(new File("src/test/resources/invalidXML.xml"));
    try {
      DomUtils.validateDocument(doc);
      Assert.fail("Invalid document not detected");
    }
    catch (SAXException e) {
      Assert.assertEquals(
            "cvc-complex-type.2.4.b: The content of element 'scm' is not complete. One of '{revision}' is expected.",
            e.getMessage());
    }
  }

  @Test
  public void testValidDoc() throws ParserConfigurationException, SAXException, IOException
  {
    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document doc = builder.parse(new File("src/test/resources/validXML.xml"));
    DomUtils.validateDocument(doc);
  }

}
