package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.IncludeTag;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.util.EagerReconstructionUtils;

@Beta
public class EagerIncludeTag extends EagerTagDecorator<IncludeTag> {

  public EagerIncludeTag(IncludeTag tag) {
    super(tag);
  }

  @Override
  public String innerInterpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    String templateFile = IncludeTag.resolveTemplateFile(tagNode, interpreter);
    int numDeferredTokensStart = interpreter.getContext().getDeferredTokens().size();
    String output = super.innerInterpret(tagNode, interpreter);
    if (interpreter.getContext().getDeferredTokens().size() > numDeferredTokensStart) {
      return EagerReconstructionUtils.wrapPathAroundText(
        output,
        templateFile,
        interpreter
      );
    }
    return output;
  }
}
