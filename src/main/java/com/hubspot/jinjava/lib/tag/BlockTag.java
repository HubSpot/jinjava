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

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaHasCodeBody;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.doc.annotations.JinjavaTextMateSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.output.BlockInfo;
import com.hubspot.jinjava.tree.output.BlockPlaceholderOutputNode;
import com.hubspot.jinjava.tree.output.OutputNode;
import com.hubspot.jinjava.util.HelperStringTokenizer;
import com.hubspot.jinjava.util.WhitespaceUtils;

/**
 * {% block name %}
 *
 */
@JinjavaDoc(
  value = "Blocks are regions in a template which can be overridden by child templates",
  params = {
    @JinjavaParam(
      value = "block_name",
      desc = "A unique name for the block that should be used in both the parent and child template"
    ),
  },
  snippets = {
    @JinjavaSnippet(
      code = "{% extends \"custom/page/web_page_basic/my_template.html\" %}\n" +
      "{% block my_sidebar %}\n" +
      "   <!--Content that will render within a block of the same name in the parent template-->\n" +
      "{% endblock %}"
    ),
  }
)
@JinjavaHasCodeBody
@JinjavaTextMateSnippet(code = "{% block ${1:name} %}\n$0\n{% endblock $1 %}")
public class BlockTag implements Tag {

  public static final String TAG_NAME = "block";

  private static final long serialVersionUID = -2362317415797088108L;

  @Override
  public OutputNode interpretOutput(TagNode tagNode, JinjavaInterpreter interpreter) {
    HelperStringTokenizer tagData = new HelperStringTokenizer(tagNode.getHelpers());
    if (!tagData.hasNext()) {
      throw new TemplateSyntaxException(
        tagNode.getMaster().getImage(),
        "Tag 'block' expects an identifier",
        tagNode.getLineNumber(),
        tagNode.getStartPosition()
      );
    }

    String blockName = WhitespaceUtils.unquote(tagData.next());

    interpreter.addBlock(
      blockName,
      new BlockInfo(
        tagNode.getChildren(),
        interpreter.getContext().getCurrentPathStack().peek(),
        interpreter.getContext().getCurrentPathStack().getTopLineNumber(),
        interpreter.getContext().getCurrentPathStack().getTopStartPosition()
      )
    );

    return new BlockPlaceholderOutputNode(blockName);
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    throw new UnsupportedOperationException(
      "BlockTag must be rendered directly via interpretOutput() method"
    );
  }

  @Override
  public String getName() {
    return TAG_NAME;
  }
}
