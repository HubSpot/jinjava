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
package com.hubspot.jinjava.tree;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.DisabledException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.MissingEndTagException;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorItem;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.interpret.UnexpectedTokenException;
import com.hubspot.jinjava.interpret.UnknownTagException;
import com.hubspot.jinjava.lib.tag.EndTag;
import com.hubspot.jinjava.lib.tag.FlexibleTag;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.tree.parse.ExpressionToken;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.tree.parse.TextToken;
import com.hubspot.jinjava.tree.parse.Token;
import com.hubspot.jinjava.tree.parse.TokenScanner;
import com.hubspot.jinjava.tree.parse.TokenScannerSymbols;
import com.hubspot.jinjava.tree.parse.UnclosedToken;
import com.hubspot.jinjava.tree.parse.WhitespaceControlParser;
import org.apache.commons.lang3.StringUtils;

public class TreeParser {

  private final PeekingIterator<Token> scanner;
  private final JinjavaInterpreter interpreter;
  private final TokenScannerSymbols symbols;
  private final WhitespaceControlParser whitespaceControlParser;

  private Node parent;

  public TreeParser(JinjavaInterpreter interpreter, String input) {
    this.scanner =
      Iterators.peekingIterator(new TokenScanner(input, interpreter.getConfig()));
    this.interpreter = interpreter;
    this.symbols = interpreter.getConfig().getTokenScannerSymbols();
    this.whitespaceControlParser =
      interpreter.getConfig().getLegacyOverrides().isParseWhitespaceControlStrictly()
        ? WhitespaceControlParser.STRICT
        : WhitespaceControlParser.LENIENT;
  }

  public Node buildTree() {
    Node root = new RootNode(symbols);

    parent = root;

    while (scanner.hasNext()) {
      Node node = nextNode();

      if (node != null) {
        if (
          node instanceof TextNode &&
          getLastSibling() instanceof TextNode &&
          !interpreter.getConfig().getLegacyOverrides().isAllowAdjacentTextNodes()
        ) {
          // merge adjacent text nodes so whitespace control properly applies
          ((TextToken) getLastSibling().getMaster()).mergeImageAndContent(
              (TextToken) node.getMaster()
            );
        } else {
          parent.getChildren().add(node);
        }
      }
    }

    do {
      if (parent != root) {
        interpreter.addError(
          TemplateError.fromException(
            new MissingEndTagException(
              ((TagNode) parent).getEndName(),
              parent.getMaster().getImage(),
              parent.getLineNumber(),
              parent.getStartPosition()
            )
          )
        );
        parent = parent.getParent();
      }
    } while (parent.getParent() != null);

    return root;
  }

  /**
   * @return null if EOF or error
   */

  private Node nextNode() {
    Token token = scanner.next();
    if (token.isLeftTrim() && isTrimmingEnabledForToken(token, interpreter.getConfig())) {
      final Node lastSibling = getLastSibling();
      if (lastSibling instanceof TextNode) {
        lastSibling.getMaster().setRightTrim(true);
      }
    }

    if (token.getType() == symbols.getFixed()) {
      if (token instanceof UnclosedToken) {
        interpreter.addError(
          new TemplateError(
            ErrorType.WARNING,
            ErrorReason.SYNTAX_ERROR,
            ErrorItem.TAG,
            "Unclosed token",
            "token",
            token.getLineNumber(),
            token.getStartPosition(),
            null
          )
        );
      }
      return text((TextToken) token);
    } else if (token.getType() == symbols.getExprStart()) {
      return expression((ExpressionToken) token);
    } else if (token.getType() == symbols.getTag()) {
      return tag((TagToken) token);
    } else if (token.getType() == symbols.getNote()) {
      String commentClosed = symbols.getClosingComment();
      if (!token.getImage().endsWith(commentClosed)) {
        interpreter.addError(
          new TemplateError(
            ErrorType.WARNING,
            ErrorReason.SYNTAX_ERROR,
            ErrorItem.TAG,
            "Unclosed comment",
            "comment",
            token.getLineNumber(),
            token.getStartPosition(),
            null
          )
        );
      }
    } else {
      interpreter.addError(
        TemplateError.fromException(
          new UnexpectedTokenException(
            token.getImage(),
            token.getLineNumber(),
            token.getStartPosition()
          )
        )
      );
    }
    return null;
  }

  private Node getLastSibling() {
    if (parent == null || parent.getChildren().isEmpty()) {
      return null;
    }
    return parent.getChildren().getLast();
  }

  private Node text(TextToken textToken) {
    if (interpreter.getConfig().isLstripBlocks()) {
      if (scanner.hasNext()) {
        final int nextTokenType = scanner.peek().getType();
        if (nextTokenType == symbols.getTag() || nextTokenType == symbols.getNote()) {
          textToken =
            new TextToken(
              StringUtils.stripEnd(textToken.getImage(), "\t "),
              textToken.getLineNumber(),
              textToken.getStartPosition(),
              symbols,
              whitespaceControlParser
            );
        }
      }
    }

    final Node lastSibling = getLastSibling();

    // if last sibling was a tag and has rightTrimAfterEnd, strip whitespace
    if (
      lastSibling != null &&
      isRightTrim(lastSibling) &&
      isTrimmingEnabledForToken(lastSibling.getMaster(), interpreter.getConfig())
    ) {
      textToken.setLeftTrim(true);
    }

    // for first TextNode child of TagNode where rightTrim is enabled, mark it for left trim
    if (
      parent instanceof TagNode && lastSibling == null && parent.getMaster().isRightTrim()
    ) {
      textToken.setLeftTrim(true);
    }

    TextNode n = new TextNode(textToken);
    n.setParent(parent);
    return n;
  }

  private boolean isRightTrim(Node lastSibling) {
    if (lastSibling instanceof TagNode) {
      return (
          ((TagNode) lastSibling).getEndName() == null ||
          (((TagNode) lastSibling).getTag() instanceof FlexibleTag &&
            !((FlexibleTag) ((TagNode) lastSibling).getTag()).hasEndTag(
                (TagToken) lastSibling.getMaster()
              ))
        )
        ? lastSibling.getMaster().isRightTrim()
        : lastSibling.getMaster().isRightTrimAfterEnd();
    }
    return lastSibling.getMaster().isRightTrim();
  }

  private Node expression(ExpressionToken expressionToken) {
    ExpressionNode n = createExpressionNode(expressionToken);
    n.setParent(parent);
    return n;
  }

  private ExpressionNode createExpressionNode(ExpressionToken expressionToken) {
    return new ExpressionNode(
      interpreter.getContext().getExpressionStrategy(),
      expressionToken
    );
  }

  private Node tag(TagToken tagToken) {
    Tag tag;
    try {
      tag = interpreter.getContext().getTag(tagToken.getTagName());
      if (tag == null) {
        interpreter.addError(
          TemplateError.fromException(new UnknownTagException(tagToken))
        );
        return null;
      }
    } catch (DisabledException e) {
      interpreter.addError(
        new TemplateError(
          ErrorType.FATAL,
          ErrorReason.DISABLED,
          ErrorItem.TAG,
          e.getMessage(),
          tagToken.getTagName(),
          interpreter.getLineNumber(),
          tagToken.getStartPosition(),
          e
        )
      );
      return null;
    }

    if (tag instanceof EndTag) {
      endTag(tag, tagToken);
      return null;
    }

    TagNode node = new TagNode(tag, tagToken, symbols);
    node.setParent(parent);

    if (
      node.getEndName() != null &&
      (!(tag instanceof FlexibleTag) || ((FlexibleTag) tag).hasEndTag(tagToken))
    ) {
      parent.getChildren().add(node);
      parent = node;
      return null;
    }

    return node;
  }

  private void endTag(Tag tag, TagToken tagToken) {
    if (parent.getMaster() != null) { // root node
      parent.getMaster().setRightTrimAfterEnd(tagToken.isRightTrim());
    }

    boolean hasMatchingStartTag = false;
    while (!(parent instanceof RootNode)) {
      TagNode parentTag = (TagNode) parent;
      parent = parent.getParent();

      if (parentTag.getEndName().equals(tag.getEndTagName())) {
        hasMatchingStartTag = true;
        break;
      } else {
        interpreter.addError(
          TemplateError.fromException(
            new TemplateSyntaxException(
              tagToken.getImage(),
              "Mismatched end tag, expected: " + parentTag.getEndName(),
              tagToken.getLineNumber(),
              tagToken.getStartPosition()
            )
          )
        );
      }
    }
    if (!hasMatchingStartTag) {
      interpreter.addError(
        new TemplateError(
          ErrorType.WARNING,
          ErrorReason.SYNTAX_ERROR,
          ErrorItem.TAG,
          "Missing start tag",
          tag.getName(),
          tagToken.getLineNumber(),
          tagToken.getStartPosition(),
          null
        )
      );
    }
  }

  private boolean isTrimmingEnabledForToken(Token token, JinjavaConfig jinjavaConfig) {
    if (token instanceof TagToken || token instanceof TextToken) {
      return true;
    }
    return jinjavaConfig.getLegacyOverrides().isUseTrimmingForNotesAndExpressions();
  }
}
