package com.hubspot.jinjava.lib.tag;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaTextMateSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.NotInLoopException;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.util.ForLoop;

/**
 * Implements the common loopcontrol `continue`, as in the jinja2.ext.loopcontrols extension
 * @author ccutrer
 */

@JinjavaDoc(value = "Stops executing the current iteration of the current for loop")
@JinjavaTextMateSnippet(
  code = "{% for item in [1, 2, 3, 4] %}{% if item % 2 == 0 %}{% continue %}{% endif %}{{ item }}{% endfor %}"
)
public class ContinueTag implements Tag {

  public static final String TAG_NAME = "continue";

  @Override
  public String getName() {
    return TAG_NAME;
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    Object loop = interpreter.getContext().get(ForTag.LOOP);
    if (loop instanceof ForLoop) {
      ForLoop forLoop = (ForLoop) loop;
      forLoop.doContinue();
    } else {
      throw new NotInLoopException(TAG_NAME);
    }
    return "";
  }

  @Override
  public String getEndTagName() {
    return null;
  }

  @Override
  public boolean isRenderedInValidationMode() {
    return true;
  }
}
