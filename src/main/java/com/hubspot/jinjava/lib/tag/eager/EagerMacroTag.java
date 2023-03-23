package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.lib.fn.eager.EagerMacroFunction;
import com.hubspot.jinjava.lib.tag.MacroTag;
import com.hubspot.jinjava.tree.TagNode;
import java.util.LinkedHashMap;

@Beta
public class EagerMacroTag extends MacroTag {

  @Override
  protected MacroFunction constructMacroFunction(
    TagNode tagNode,
    JinjavaInterpreter interpreter,
    String name,
    LinkedHashMap<String, Object> argNamesWithDefaults
  ) {
    return new EagerMacroFunction(
      tagNode.getChildren(),
      name,
      argNamesWithDefaults,
      false,
      interpreter.getContext(),
      interpreter.getLineNumber(),
      interpreter.getPosition()
    );
  }
}
