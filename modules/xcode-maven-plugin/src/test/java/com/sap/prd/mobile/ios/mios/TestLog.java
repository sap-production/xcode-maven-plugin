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

import static com.sap.prd.mobile.ios.mios.TestLog.Severity.DEBUG;
import static com.sap.prd.mobile.ios.mios.TestLog.Severity.ERROR;
import static com.sap.prd.mobile.ios.mios.TestLog.Severity.INFO;
import static com.sap.prd.mobile.ios.mios.TestLog.Severity.WARNING;
import static java.lang.String.format;
import static java.util.regex.Pattern.quote;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.maven.plugin.logging.Log;

public class TestLog implements Log
{

  public enum Severity
  {
    ERROR("[TestLog-ERROR]"),
    WARNING("[TestLog-WARNING]"),
    INFO("[TestLog-INFO]"),
    DEBUG("[TestLog-DEBUG]");

    private final String value;

    Severity(String value)
    {
      this.value = value;
    }

    public String toString()
    {
      return value;
    }
  };

  private final StringBuffer BUFFER;
  private static final String NL = System.getProperty("path.separator");

  public TestLog()
  {
    BUFFER = new StringBuffer();
  }

  private void print(Severity level, CharSequence content)
  {
    BUFFER.append(format("%s: %s%s", level, content, NL));
  }

  private void print(Severity level, Throwable error)
  {
    String stackTrace = ExceptionUtils.getStackTrace(error);
    BUFFER.append(format("%s: %s%s%s", level, error.toString(), NL, stackTrace));
  }

  public boolean contains(Severity expectedSeverity, String expectedLog)
  {
    final String log = BUFFER.toString();
    if (expectedSeverity != null && expectedLog != null) {
      return log.matches(format("^%s.*%s.*$", quote(expectedSeverity.toString()), quote(expectedLog)));
    }
    if (expectedSeverity == null && expectedLog != null) {
      return log.matches(format("^.*%s.*$", quote(expectedLog)));
    }
    if (expectedSeverity != null && expectedLog == null) {
      return log.matches(format("^%s.*$", quote(expectedSeverity.toString())));
    }
    return true;
  }

  public String getLogContent()
  {
    return BUFFER.toString();
  }

  @Override
  public boolean isDebugEnabled()
  {
    return true;
  }

  @Override
  public void debug(CharSequence content)
  {
    print(DEBUG, content);
  }

  @Override
  public void debug(CharSequence content, Throwable error)
  {
    print(DEBUG, content);
    print(DEBUG, error);
  }

  @Override
  public void debug(Throwable error)
  {
    print(DEBUG, error);
  }

  @Override
  public boolean isInfoEnabled()
  {
    return true;
  }

  @Override
  public void info(CharSequence content)
  {
    print(INFO, content);
  }

  @Override
  public void info(CharSequence content, Throwable error)
  {
    print(INFO, content);
    print(INFO, error);
  }

  @Override
  public void info(Throwable error)
  {
    print(INFO, error);
  }

  @Override
  public boolean isWarnEnabled()
  {
    return true;
  }

  @Override
  public void warn(CharSequence content)
  {
    print(WARNING, content);
  }

  @Override
  public void warn(CharSequence content, Throwable error)
  {
    print(WARNING, content);
    print(WARNING, error);
  }

  @Override
  public void warn(Throwable error)
  {
    print(WARNING, error);
  }

  @Override
  public boolean isErrorEnabled()
  {
    return true;
  }

  @Override
  public void error(CharSequence content)
  {
    print(ERROR, content);
  }

  @Override
  public void error(CharSequence content, Throwable error)
  {
    print(ERROR, content);
    print(ERROR, error);
  }

  @Override
  public void error(Throwable error)
  {
    print(ERROR, error);
  }

}
