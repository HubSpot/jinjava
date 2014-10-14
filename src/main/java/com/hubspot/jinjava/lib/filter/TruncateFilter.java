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

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.Functions;

/**
 * truncate(s, length=255, killwords=False, end='...')
 *   Return a truncated copy of the string. The length is specified with the first parameter which defaults to 255. 
 *   If the second parameter is true the filter will cut the text at length. Otherwise it will discard the last word. 
 *   If the text was in fact truncated it will append an ellipsis sign ("..."). If you want a different ellipsis sign 
 *   than "..." you can specify it using the third parameter.
 *   
 *   <pre>
 *   {{ "foo bar"|truncate(5) }}
 *       -&gt; "foo ..."
 *   {{ "foo bar"|truncate(5, True) }}
 *       -&gt; "foo b..."
 *   </pre>
 */
public class TruncateFilter implements Filter {

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... arg) {
    return Functions.truncate(object, (Object[]) arg);
  }

  @Override
  public String getName() {
    return "truncate";
  }

}
