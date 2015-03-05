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

import static com.hubspot.jinjava.parse.ParserConstants.TOKEN_EXPR_START;
import static com.hubspot.jinjava.parse.ParserConstants.TOKEN_FIXED;
import static com.hubspot.jinjava.parse.ParserConstants.TOKEN_NOTE;
import static com.hubspot.jinjava.parse.ParserConstants.TOKEN_TAG;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.MissingEndTagException;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.UnexpectedTokenException;
import com.hubspot.jinjava.interpret.UnknownTagException;
import com.hubspot.jinjava.parse.ExpressionToken;
import com.hubspot.jinjava.parse.FixedToken;
import com.hubspot.jinjava.parse.TagToken;
import com.hubspot.jinjava.parse.Token;
import com.hubspot.jinjava.parse.TokenParser;

public class TreeParser {

  private final TokenParser parser;
  
  public TreeParser(JinjavaInterpreter interpreter, String input){
    this.parser = new TokenParser(interpreter, input);
  }

  public Node parseTree() {
    Node root = new RootNode();
    tree(root, RootNode.TREE_ROOT_END);
    return root;
  }

  private void tree(Node node, String endName) {
    Token token;
    TagToken tag;
    while (parser.hasNext()) {
      token = parser.next();
      switch (token.getType()) {
      case TOKEN_FIXED:
        TextNode tn = new TextNode((FixedToken) token);
        node.add(tn);
        break;
      case TOKEN_NOTE:
        break;
      case TOKEN_EXPR_START:
        VariableNode vn = new VariableNode((ExpressionToken) token);
        node.add(vn);
        break;
      case TOKEN_TAG:
        tag = (TagToken) token;
        if (tag.getTagName().equalsIgnoreCase(endName)) {
          return;
        }
        try {
          TagNode tg = new TagNode((TagToken) token, parser.getInterpreter());
          node.add(tg);
          if (tg.getEndName() != null) {
            tree(tg, tg.getEndName());
          }
        } catch (UnknownTagException e) {
          parser.getInterpreter().addError(TemplateError.fromException(e));
        }
        break;
      default:
        parser.getInterpreter().addError(TemplateError.fromException(new UnexpectedTokenException(token.getImage(), node.getLineNumber())));
      }
    }
    // can't reach end tag
    if (endName != null && !endName.equals(RootNode.TREE_ROOT_END)) {
      parser.getInterpreter().addError(TemplateError.fromException(
          new MissingEndTagException(endName, node.toString(), node.getLineNumber())));
    }
  }
}
