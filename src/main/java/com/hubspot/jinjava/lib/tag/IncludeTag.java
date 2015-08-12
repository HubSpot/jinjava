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

import static com.hubspot.jinjava.util.Logging.ENGINE_LOG;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.util.HelperStringTokenizer;

@JinjavaDoc(
    value = "includes multiple files in one template or stylesheet",
    params = {
        @JinjavaParam(value = "path", desc = "Design Manager path to the file that you would like to include")
    },
    snippets = {
        @JinjavaSnippet(code = "{% include \"custom/page/web_page_basic/my_footer.html\" %}"),
        @JinjavaSnippet(code = "{% include \"generated_global_groups/2781996615.html\" %}"),
        @JinjavaSnippet(code = "{% include \"hubspot/styles/patches/recommended.css\" %}")
    })
public class IncludeTag implements Tag {
  private static final long serialVersionUID = -8391753639874726854L;
  private static final String INCLUDE_PATH_PROPERTY = "__includeP@th__";

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    HelperStringTokenizer helper = new HelperStringTokenizer(tagNode.getHelpers());
    if (!helper.hasNext()) {
      throw new InterpretException("Tag 'include' expects template path", tagNode.getLineNumber());
    }

    String path = StringUtils.trimToEmpty(helper.next());

    if (isPathInRenderStack(interpreter.getContext(), path)) {
      ENGINE_LOG.debug("Path {} is already in include stack", path);
      return "";
    }

    String templateFile = interpreter.resolveString(path, tagNode.getLineNumber());
    try {
      String template = interpreter.getResource(templateFile);
      Node node = interpreter.parse(template);
      JinjavaInterpreter child = new JinjavaInterpreter(interpreter);
      child.getContext().addDependency("coded_files", templateFile);
      child.getContext().put(INCLUDE_PATH_PROPERTY, path);
      interpreter.getContext().put(JinjavaInterpreter.INSERT_FLAG, true);

      String result = child.render(node);
      interpreter.getErrors().addAll(child.getErrors());

      return result;

    } catch (IOException e) {
      throw new InterpretException(e.getMessage(), e, tagNode.getLineNumber());
    }
  }

  private boolean isPathInRenderStack(Context context, String path) {
    Context current = context;
    do {
      String includePath = (String) current.get(INCLUDE_PATH_PROPERTY);

      if (StringUtils.equals(path, includePath)) {
        return true;
      }

      current = current.getParent();

    } while (current != null);

    return false;
  }

  @Override
  public String getEndTagName() {
    return null;
  }

  @Override
  public String getName() {
    return "include";
  }

}
