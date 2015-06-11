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
package com.hubspot.jinjava.tree;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.parse.TextToken;

public class TextNode extends Node {
  private static final long serialVersionUID = 8488738480534354216L;

  private final TextToken master;

  public TextNode(TextToken token) {
    super(token, token.getLineNumber());
    master = token;
  }

  @Override
  public String render(JinjavaInterpreter interpreter) {
    return master.output();
  }

  @Override
  public String toString() {
    return master.toString();
  }

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

}
