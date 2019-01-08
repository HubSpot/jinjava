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
package com.hubspot.jinjava.lib.tag;

import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.util.LengthLimitingStringBuilder;
import com.hubspot.jinjava.util.ObjectTruthValue;

@JinjavaDoc(
    value = "Outputs inner content if expression evaluates to true, otherwise evaluates any elif blocks, finally outputting content of any else block present",
    snippets = {
        @JinjavaSnippet(
            code = "{% if condition %}\n" +
                "If the condition is true print this to template.\n" +
                "{% endif %}"),
        @JinjavaSnippet(
            code = "{% if number <= 2 %}\n" +
                "Variable named number is less than or equal to 2.\n" +
                "{% elif number <= 4 %}\n" +
                "Variable named number is less than or equal to 4.\n" +
                "{% elif number <= 6 %}\n" +
                "Variable named number is less than or equal to 6.\n" +
                "{% else %}\n" +
                "Variable named number is greater than 6.\n" +
                "{% endif %}")
    })
public class IfTag implements Tag {

  private static final long serialVersionUID = -3784039314941268904L;
  private static final String TAGNAME = "if";
  private static final String ENDTAGNAME = "endif";

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    if (StringUtils.isBlank(tagNode.getHelpers())) {
      throw new TemplateSyntaxException(tagNode.getMaster().getImage(), "Tag 'if' expects expression", tagNode.getLineNumber(), tagNode.getStartPosition());
    }

    LengthLimitingStringBuilder sb = new LengthLimitingStringBuilder(interpreter.getConfig().getMaxOutputSize());

    Iterator<Node> nodeIterator = tagNode.getChildren().iterator();

    boolean parentValidationMode = interpreter.getContext().isValidationMode();

    boolean execute = isPositiveIfElseNode(tagNode, interpreter);
    boolean executedAnyBlock = false;

    try {

      while (nodeIterator.hasNext()) {

        executedAnyBlock = executedAnyBlock || execute;
        if (interpreter.isValidationMode() && !parentValidationMode) {
          interpreter.getContext().setValidationMode(!execute);
        }

        Node node = nodeIterator.next();
        if (TagNode.class.isAssignableFrom(node.getClass())) {
          TagNode tag = (TagNode) node;
          if (tag.getName().equals(ElseIfTag.ELSEIF)) {
            execute = !executedAnyBlock && isPositiveIfElseNode(tag, interpreter);
            continue;
          } else if (tag.getName().equals(ElseTag.ELSE)) {
            execute = !executedAnyBlock;
            continue;
          }
        }

        if (execute) {
          sb.append(node.render(interpreter));
        } else if (interpreter.getContext().isValidationMode()) {
          node.render(interpreter);
        }
      }

    } finally {
      interpreter.getContext().setValidationMode(parentValidationMode);
    }

    return sb.toString();
  }

  protected boolean isPositiveIfElseNode(TagNode tagNode, JinjavaInterpreter interpreter) {
    return ObjectTruthValue.evaluate(interpreter.resolveELExpression(tagNode.getHelpers(), tagNode.getLineNumber()));
  }

  @Override
  public String getEndTagName() {
    return ENDTAGNAME;
  }

  @Override
  public String getName() {
    return TAGNAME;
  }

}
