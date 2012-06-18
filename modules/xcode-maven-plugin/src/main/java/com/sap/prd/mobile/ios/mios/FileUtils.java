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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileUtils
{

  /**
   * 
   * @param parent
   *          The parent directory
   * @param child
   *          The child direcory
   * @return the part of the path that represents the delta between <code>parent</code> and
   *         <code>child</code>.
   * @throws IllegalStateExcpetion
   *           in case <code>child</code> is not a child of <code>parent</code>.
   */
  public static String getDelta(File parent, File child)
  {

    final List<String> _baseDir = split(parent.getAbsoluteFile());
    final List<String> _sourceDir = split(child.getAbsoluteFile());

    int index = 0;

    for (int size = _baseDir.size(); index < size; index++)
      if (!_baseDir.get(index).equals(_sourceDir.get(index)))
        throw new IllegalStateException("Source directory '" + child + "' is not a child of the base directory '"
              + parent + "'.");

    StringBuilder path = new StringBuilder();

    boolean first = true;
    for (int size = _sourceDir.size(); index < size; index++) {

      if (!first)
        path.append(File.separator);
      path.append(_sourceDir.get(index));
      first = false;
    }

    return path.toString();
  }

  private static List<String> split(File dir)
  {

    List<String> result = new ArrayList<String>();

    do {

      result.add(dir.getName());
      dir = dir.getParentFile();

    } while (dir != null);

    Collections.reverse(result);

    return result;

  }
}
