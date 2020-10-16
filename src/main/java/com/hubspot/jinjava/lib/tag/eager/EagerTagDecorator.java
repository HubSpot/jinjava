package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.ExpressionToken;
import com.hubspot.jinjava.tree.parse.NoteToken;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.tree.parse.TextToken;
import com.hubspot.jinjava.tree.parse.Token;
import com.hubspot.jinjava.util.HelperStringTokenizer;
import com.hubspot.jinjava.util.WhitespaceUtils;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import org.apache.commons.lang3.StringUtils;

public abstract class EagerTagDecorator<T extends Tag> implements Tag {
  private T tag;

  public EagerTagDecorator(T tag) {
    this.tag = tag;
  }

  public T getTag() {
    return tag;
  }

  public String eagerInterpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    StringBuilder result = new StringBuilder(
      getEagerImage(tagNode.getMaster(), interpreter)
    );

    JinjavaInterpreter eagerInterpreter = interpreter
      .getConfig()
      .getInterpreterFactory()
      .newInstance(interpreter);
    eagerInterpreter.getContext().setEagerMode(true);

    for (Node child : tagNode.getChildren()) {
      result.append(renderChild(child, eagerInterpreter));
    }

    if (StringUtils.isNotBlank(tagNode.getEndName())) {
      result.append(tagNode.reconstructEnd());
    }

    return result.toString();
  }

  public final Object renderChild(Node child, JinjavaInterpreter interpreter) {
    try {
      return child.render(interpreter);
    } catch (DeferredValueException e) {
      return getEagerImage(child.getMaster(), interpreter);
    }
  }

  public String getEagerImage(Token token, JinjavaInterpreter interpreter) {
    if (token instanceof TagToken) {
      return getEagerTagImage((TagToken) token, interpreter);
    } else if (token instanceof ExpressionToken) {
      return getEagerExpressionImage((ExpressionToken) token, interpreter);
    } else if (token instanceof TextToken) {
      return getEagerTextImage((TextToken) token, interpreter);
    } else if (token instanceof NoteToken) {
      return getEagerNoteImage((NoteToken) token, interpreter);
    }
    throw new DeferredValueException("Unsupported Token type");
  }

  public String getEagerTagImage(TagToken tagToken, JinjavaInterpreter interpreter) {
    HelperStringTokenizer tokenizer = new HelperStringTokenizer(tagToken.getHelpers())
    .splitComma(true);
    Set<String> deferredHelpers = new HashSet<>();
    StringJoiner joiner = new StringJoiner(" ");
    joiner
      .add(tagToken.getSymbols().getExpressionStartWithTag())
      .add(tagToken.getTagName());
    for (String helper : tokenizer.allTokens()) {
      try {
        String resolvedToken;
        if (WhitespaceUtils.isQuoted(helper)) {
          resolvedToken = helper;
        } else {
          Object val = interpreter.retraceVariable(
            helper,
            tagToken.getLineNumber(),
            tagToken.getStartPosition()
          );
          if (val == null) {
            resolvedToken = helper;
          } else {
            resolvedToken = String.format("'%s'", val);
          }
        }
        joiner.add(resolvedToken);
      } catch (DeferredValueException e) {
        deferredHelpers.add(helper);
        joiner.add(helper);
      }
    }
    interpreter
      .getContext()
      .handleEagerTagToken(new EagerToken(tagToken, deferredHelpers));

    joiner.add(tagToken.getSymbols().getExpressionEndWithTag());
    return joiner.toString();
  }

  public String getEagerExpressionImage(
    ExpressionToken expressionToken,
    JinjavaInterpreter interpreter
  ) {
    interpreter
      .getContext()
      .handleEagerTagToken(
        new EagerToken(expressionToken, Collections.singleton(expressionToken.getExpr()))
      );
    return expressionToken.getImage();
  }

  public String getEagerTextImage(TextToken textToken, JinjavaInterpreter interpreter) {
    interpreter
      .getContext()
      .handleEagerTagToken(
        new EagerToken(textToken, Collections.singleton(textToken.output()))
      );
    return textToken.getImage();
  }

  public String getEagerNoteImage(NoteToken noteToken, JinjavaInterpreter interpreter) {
    // Notes should not throw DeferredValueExceptions, but this will handle it anyway
    return "";
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    try {
      return tag.interpret(tagNode, interpreter);
    } catch (DeferredValueException e) {
      return eagerInterpret(tagNode, interpreter);
    }
  }

  @Override
  public String getName() {
    return tag.getName();
  }

  @Override
  public String getEndTagName() {
    return tag.getEndTagName();
  }

  @Override
  public boolean isRenderedInValidationMode() {
    return tag.isRenderedInValidationMode();
  }
}
