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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

class RemoveTrailingCharactersVersionTransformer
{

  private final int limit;

  RemoveTrailingCharactersVersionTransformer()
  {
    this(-1);
  }

  RemoveTrailingCharactersVersionTransformer(final int limit)
  {
    this.limit = limit;
  }

  public String transform(String version) throws NumberFormatException
  {

    final String originalVersion = version;

    if (version == null)
      throw new NullPointerException("Version was null.");

    String[] parts = version.split("\\.");

    List<String> result = new ArrayList<String>();

    int length = (limit == -1 ? parts.length : Math.min(parts.length, limit));

    for (int i = 0; i < length; i++) {

      String part = removeTrailingNonNumbers(parts[i]);

      if (part.trim().isEmpty())
        part = "0";

      if (Long.parseLong(part) < 0) {
        throw new NumberFormatException("Invalid version found: '" + originalVersion
              + "'. Negativ version part found: " + parts[i] + ".");
      }

      result.add(part);

      if (!parts[i].matches("\\d+"))
        break;

    }

    while (result.size() < limit)
      result.add("0");

    return StringUtils.join(result, '.');
  }

  private String removeTrailingNonNumbers(String part)
  {
    StringBuilder result = new StringBuilder(part.length());

    char[] c = new char[1];

    for (int i = 0, l = part.length(); i < l; i++) {

      c[0] = part.charAt(i);

      if (new String(c).matches("\\d") || (i == 0 && new String(c).matches("-")))
        result.append(c);
      else
        break;
    }

    return result.toString();
  }
}
