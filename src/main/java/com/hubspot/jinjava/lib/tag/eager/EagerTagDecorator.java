package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.HelperStringTokenizer;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

public abstract class EagerTagDecorator<T extends Tag> implements Tag {
  private T tag;

  public EagerTagDecorator(T tag) {
    this.tag = tag;
  }

  public abstract String eagerInterpret(TagNode tagNode, JinjavaInterpreter interpreter);

  public String getEagerImage(TagToken tagToken, JinjavaInterpreter interpreter) {
    HelperStringTokenizer tokenizer = new HelperStringTokenizer(tagToken.getHelpers());
    Set<String> deferredHelpers = new HashSet<>();
    StringJoiner joiner = new StringJoiner(" ");
    joiner.add("{%").add(tagToken.getTagName());
    for (String token : tokenizer.allTokens()) {
      try {
        joiner.add(
          interpreter.resolveString(
            token,
            tagToken.getLineNumber(),
            tagToken.getStartPosition()
          )
        );
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
