/**********************************************************************
Copyright (c) 2014 HubSpot Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 **********************************************************************/
package com.hubspot.jinjava.lib.filter;

import java.text.Format;

import org.apache.commons.lang3.time.FastDateFormat;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError;

@JinjavaDoc(
    value="formats a date object",
    params={
        @JinjavaParam(value="value", type="date"),
        @JinjavaParam(value="format")
    })
public class DatetimeFilter implements Filter {

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... arg) {
    if (object == null) {
      return object;
    }
    Format sdf;
    if (arg.length == 1) {
      sdf = FastDateFormat.getInstance(arg[0]);
    } else if (arg.length == 2) {
      sdf = FastDateFormat.getInstance(arg[0]);
    } else {
      throw new InterpretException("filter date expects 1 or 2 args >>> " + arg.length);
    }
    try {
      return sdf.format(object);
    } catch (Exception e) {
      interpreter.addError(TemplateError.fromSyntaxError(new InterpretException("datetime filter error formatting a datetime: " + object, e, interpreter.getLineNumber())));
    }
    return object;
  }

  @Override
  public String getName() {
    return "date";
  }

}
