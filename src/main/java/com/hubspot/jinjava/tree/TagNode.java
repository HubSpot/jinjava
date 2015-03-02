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

import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.UnknownTagException;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.parse.TagToken;

public class TagNode extends Node {

  private static final long serialVersionUID = 2405693063353887509L;

  private TagToken master;
  private String endName = null;

  public TagNode(TagToken token, JinjavaInterpreter interpreter) {
    super(token, token.getLineNumber());
    master = token;
    Tag tag = interpreter.getContext().getTag(master.getTagName());
    if (tag == null) {
      throw new UnknownTagException(master.getTagName(), master.getImage(), token.getLineNumber());
    }
    endName = tag.getEndTagName();
  }
  
  private TagNode(TagNode n) {
    super(n.master, n.getLineNumber());
    master = n.master;
    endName = n.endName;
  }

  @Override
  public String render(JinjavaInterpreter interpreter) {
    Tag tag = interpreter.getContext().getTag(master.getTagName());
    try {
      return tag.interpret(this, interpreter);
    } catch (Exception e) {
      throw new InterpretException("Error rendering tag", e, master.getLineNumber());
    }
  }

  @Override
  public String toString() {
    return master.toString();
  }

  @Override
  public String getName() {
    return master.getTagName();
  }
  
  public String getEndName() {
    return endName;
  }

  public String getHelpers() {
    return master.getHelpers();
  }
  
  @Override
  public Node clone() {
    Node clone = new TagNode(this);
    clone.setChildren(this.getChildren().clone(clone));
    return clone;
  }
}
