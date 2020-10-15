package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.HelperStringTokenizer;
import com.hubspot.jinjava.util.WhitespaceUtils;
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
      getEagerImage((TagToken) tagNode.getMaster(), interpreter)
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
      result.append("{% ").append(tagNode.getEndName()).append(" %}");
    }

    return result.toString();
  }

  public final Object renderChild(Node child, JinjavaInterpreter interpreter) {
    try {
      return child.render(interpreter);
    } catch (DeferredValueException e) {
      interpreter.getContext().handleDeferredNode(child);
      return child.reconstructImage();
    }
  }

  public String getEagerImage(TagToken tagToken, JinjavaInterpreter interpreter) {
    HelperStringTokenizer tokenizer = new HelperStringTokenizer(tagToken.getHelpers())
    .splitComma(true);
    Set<String> deferredHelpers = new HashSet<>();
    StringJoiner joiner = new StringJoiner(" ");
    joiner.add("{%").add(tagToken.getTagName());
    for (String token : tokenizer.allTokens()) {
      try {
        String resolvedToken;
        if (WhitespaceUtils.isQuoted(token)) {
          resolvedToken = token;
        } else {
          Object val = interpreter.retraceVariable(
            token,
            tagToken.getLineNumber(),
            tagToken.getStartPosition()
          );
          if (val == null) {
            resolvedToken = token;
          } else {
            resolvedToken = String.format("'%s'", val);
          }
        }
        joiner.add(resolvedToken);
      } catch (DeferredValueException e) {
        deferredHelpers.add(token);
        joiner.add(token);
      }
    }
    interpreter
      .getContext()
      .handleEagerTagToken(new EagerTagToken(tagToken, deferredHelpers));

    joiner.add("%}");
    return joiner.toString();
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
