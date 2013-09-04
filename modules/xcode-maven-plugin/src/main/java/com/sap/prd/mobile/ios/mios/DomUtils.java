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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class DomUtils
{

  private DomUtils()
  {
    throw new UnsupportedOperationException("To prevent getting instances.");
  }

  /**
   * Serializes a DOM. The OutputStream handed over to this method is not closed inside this method.
   */
  static void serialize(final Document doc, final OutputStream os, final String encoding)
        throws TransformerFactoryConfigurationError, TransformerException, IOException
  {
    if (doc == null)
      throw new IllegalArgumentException("No document provided.");

    if (os == null)
      throw new IllegalArgumentException("No output stream provided");

    if (encoding == null || encoding.isEmpty())
      throw new IllegalArgumentException("No encoding provided.");

    final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    transformerFactory.setAttribute("indent-number", Integer.valueOf(2));
    final Transformer t = transformerFactory.newTransformer();

    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
    t.setOutputProperty(OutputKeys.METHOD, "xml");
    t.setOutputProperty(OutputKeys.INDENT, "yes");
    t.setOutputProperty(OutputKeys.ENCODING, encoding);

    final OutputStreamWriter osw = new OutputStreamWriter(os, encoding);
    t.transform(new DOMSource(doc), new StreamResult(osw));
    osw.flush();
  }

  static void validateDocument(Document doc) throws SAXException, IOException
  {
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    InputStream schemaIs = null;

    try {
      schemaIs = XCodeVersionInfoMojo.class

        .getResourceAsStream("/com/sap/tip/production/xcode/VersionInfoSchema.xsd");
      StreamSource[] inputSources = new StreamSource[1];
      inputSources[0] = new StreamSource(schemaIs);

      Schema schema = schemaFactory.newSchema(inputSources);

      Validator validator = schema.newValidator();

      validator.validate(new DOMSource(doc));
    }
    finally {
      if (schemaIs != null) {
        schemaIs.close();
      }
    }
  }
}
