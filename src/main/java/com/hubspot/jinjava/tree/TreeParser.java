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

import static com.hubspot.jinjava.tree.parse.TokenScannerSymbols.TOKEN_EXPR_START;
import static com.hubspot.jinjava.tree.parse.TokenScannerSymbols.TOKEN_FIXED;
import static com.hubspot.jinjava.tree.parse.TokenScannerSymbols.TOKEN_NOTE;
import static com.hubspot.jinjava.tree.parse.TokenScannerSymbols.TOKEN_TAG;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.MissingEndTagException;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.UnexpectedTokenException;
import com.hubspot.jinjava.interpret.UnknownTagException;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.tree.parse.ExpressionToken;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.tree.parse.TextToken;
import com.hubspot.jinjava.tree.parse.Token;
import com.hubspot.jinjava.tree.parse.TokenScanner;

public class TreeParser {

  private final TokenScanner scanner;
  private final JinjavaInterpreter interpreter;
  
  public TreeParser(JinjavaInterpreter interpreter, String input){
    this.scanner = new TokenScanner(input);
    this.interpreter = interpreter;
  }

  public Node parseTree() {
    Node root = new RootNode();
    parseNodeChildren(root, RootNode.TREE_ROOT_END);
    return root;
  }

  private void parseNodeChildren(Node node, String endName) {
    while(scanner.hasNext()) {
      Token token = scanner.next();
      switch (token.getType()) {
      case TOKEN_FIXED:
        TextNode tn = new TextNode((TextToken) token);
        node.add(tn);
        break;
      case TOKEN_NOTE:
        break;
      case TOKEN_EXPR_START:
        VariableNode vn = new VariableNode((ExpressionToken) token);
        node.add(vn);
        break;
      case TOKEN_TAG:
        TagToken tagToken = (TagToken) token;
        if (tagToken.getTagName().equalsIgnoreCase(endName)) {
          return;
        }
        
        Tag tag = interpreter.getContext().getTag(tagToken.getTagName());
        if (tag == null) {
          interpreter.addError(TemplateError.fromException(new UnknownTagException(tagToken)));
        }

        TagNode tg = new TagNode(tag, tagToken);
        node.add(tg);

        if (tg.getEndName() != null) {
          parseNodeChildren(tg, tg.getEndName());
        }
        break;
      default:
        interpreter.addError(TemplateError.fromException(new UnexpectedTokenException(token.getImage(), node.getLineNumber())));
      }
    }
    // can't reach end tag
    if (endName != null && !endName.equals(RootNode.TREE_ROOT_END)) {
     interpreter.addError(TemplateError.fromException(
          new MissingEndTagException(endName, node.toString(), node.getLineNumber())));
    }
  }
}
