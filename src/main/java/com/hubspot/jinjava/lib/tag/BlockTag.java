/**********************************************************************
Copyright (c) 2014 HubSpot Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 **********************************************************************/
package com.hubspot.jinjava.lib.tag;

import java.util.List;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.util.HelperStringTokenizer;
import com.hubspot.jinjava.util.WhitespaceUtils;

/**
 * {% block name %}
 *
 */
@JinjavaDoc(
    value = "Blocks are regions in a template which can be overridden by child templates",
    params = {
        @JinjavaParam(value = "block_name", desc = "A unique name for the block that should be used in both the parent and child template")
    },
    snippets = {
        @JinjavaSnippet(
            code = "{% extends \"custom/page/web_page_basic/my_template.html\" %}\n" +
                "{% block my_sidebar %}\n" +
                "   <!--Content that will render within a block of the same name in the parent template-->\n" +
                "{% endblock %}"),
    })
public class BlockTag implements Tag {

  private static final long serialVersionUID = -2362317415797088108L;
  private static final String TAGNAME = "block";
  private static final String ENDTAGNAME = "endblock";

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    List<String> helper = new HelperStringTokenizer(tagNode.getHelpers()).allTokens();
    if (helper.isEmpty()) {
      throw new InterpretException("Tag 'block' expects an identifier", tagNode.getLineNumber());
    }

    String blockName = WhitespaceUtils.unquote(helper.get(0));

    interpreter.addBlock(blockName, tagNode.getChildren());
    return JinjavaInterpreter.BLOCK_STUB_START + blockName + JinjavaInterpreter.BLOCK_STUB_END;
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
