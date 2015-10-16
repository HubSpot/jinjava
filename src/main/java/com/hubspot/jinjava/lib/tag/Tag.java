/**********************************************************************
 * Copyright (c) 2014 HubSpot Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************/
package com.hubspot.jinjava.lib.tag;

import java.io.Serializable;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.Importable;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.output.OutputNode;
import com.hubspot.jinjava.tree.output.RenderedOutputNode;

public interface Tag extends Importable, Serializable {

  default OutputNode interpretOutput(TagNode tagNode, JinjavaInterpreter interpreter) {
    return new RenderedOutputNode(interpret(tagNode, interpreter));
  }

  String interpret(TagNode tagNode, JinjavaInterpreter interpreter);

  /**
   * @return Get name of end tag lowerCase Null if it's a single tag without content.
   */
  String getEndTagName();

}
