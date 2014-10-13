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

import org.apache.commons.lang3.StringUtils;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class EscapeFilter implements Filter {

  private static final String SAMP = "&";
  private static final String BAMP = "&amp;";
  private static final String SGT = ">";
  private static final String BGT = "&gt;";
  private static final String SLT = "<";
  private static final String BLT = "&lt;";
  private static final String BSQ = "&#39;";
  private static final String BDQ = "&quot;";

  private static final String[] TO_REPLACE = new String[]{SAMP, SGT, SLT, "'", "\""};
  private static final String[] REPLACE_WITH = new String[]{BAMP, BGT, BLT, BSQ, BDQ};
  
  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... arg) {
    if (object instanceof String) {
      String value = (String) object;
      return StringUtils.replaceEach(value, TO_REPLACE, REPLACE_WITH);
    }
    return object;
  }

  @Override
  public String getName() {
    return "escape";
  }

}
