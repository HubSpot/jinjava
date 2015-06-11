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
package com.hubspot.jinjava.lib.tag;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.TagNode;

@JinjavaDoc(value = "", hidden = true)
public class ElseTag implements Tag {

  private static final long serialVersionUID = 1082768429113702148L;
  static final String ELSE = "else";

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    return "";
  }

  @Override
  public String getEndTagName() {
    return null;
  }

  @Override
  public String getName() {
    return ELSE;
  }

}
