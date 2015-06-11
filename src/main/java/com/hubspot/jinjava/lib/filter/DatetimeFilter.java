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

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(value = "", aliasOf = "datetimeformat")
public class DatetimeFilter extends DateTimeFormatFilter {

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... arg) {
    return super.filter(object, interpreter, arg);
  }

  @Override
  public String getName() {
    return "date";
  }

}
