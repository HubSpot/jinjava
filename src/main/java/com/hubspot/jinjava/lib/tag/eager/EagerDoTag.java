package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.tag.DoTag;
import com.hubspot.jinjava.tree.parse.TagToken;
import org.apache.commons.lang3.StringUtils;

@Beta
public class EagerDoTag extends EagerStateChangingTag<DoTag> {

  public EagerDoTag() {
    super(new DoTag());
  }

  public EagerDoTag(DoTag doTag) {
    super(doTag);
  }

  @Override
  public String getEagerTagImage(TagToken tagToken, JinjavaInterpreter interpreter) {
    String expr = tagToken.getHelpers();
    if (StringUtils.isBlank(expr)) {
      throw new TemplateSyntaxException(
        interpreter,
        tagToken.getImage(),
        "Tag 'do' expects expression"
      );
    }
    return EagerPrintTag.interpretExpression(expr, tagToken, interpreter, false);
  }
}
