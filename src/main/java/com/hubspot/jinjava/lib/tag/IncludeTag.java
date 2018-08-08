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

import static com.hubspot.jinjava.util.Logging.ENGINE_LOG;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.IncludeTagCycleException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorItem;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.interpret.errorcategory.BasicTemplateErrorCategory;
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

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    long thisStartMs = System.currentTimeMillis();
    HelperStringTokenizer helper = new HelperStringTokenizer(tagNode.getHelpers());
    if (!helper.hasNext()) {
      throw new TemplateSyntaxException(tagNode.getMaster().getImage(), "Tag 'include' expects template path", tagNode.getLineNumber(), tagNode.getStartPosition());
    }

    String path = StringUtils.trimToEmpty(helper.next());
    final String renderName = String.format("IncludeTag:%s", path);
    interpreter.startRender(renderName);
    String templateFile = interpreter.resolveString(path, tagNode.getLineNumber(), tagNode.getStartPosition());

    try {
      interpreter.getContext().getIncludePathStack().push(templateFile, tagNode.getLineNumber(), tagNode.getStartPosition());
    } catch (IncludeTagCycleException e) {
      interpreter.addError(new TemplateError(ErrorType.WARNING, ErrorReason.EXCEPTION, ErrorItem.TAG,
          "Include cycle detected for path: '" + templateFile + "'", null, tagNode.getLineNumber(), tagNode.getStartPosition(), e,
          BasicTemplateErrorCategory.INCLUDE_CYCLE_DETECTED, ImmutableMap.of("path", templateFile)));
      return "";
    }

    try {
      long startMs = System.currentTimeMillis();
      interpreter.startRender("getResource");
      String template = interpreter.getResource(templateFile);
      interpreter.endRender("getResource");
      long costMs = System.currentTimeMillis() - startMs;
      ENGINE_LOG.info("{}IncludeTag getResource time: {} {}",
          StringUtils.repeat("  ", interpreter.getContext().getIncludePathStack().size()),
          costMs, path);

      interpreter.startRender("renderResource");
      Node node = interpreter.parse(template);

      interpreter.getContext().addDependency("coded_files", templateFile);

      JinjavaInterpreter child = new JinjavaInterpreter(interpreter);

      startMs = System.currentTimeMillis();
      String result = child.render(node);
      costMs = System.currentTimeMillis() - startMs;
      ENGINE_LOG.info("{}IncludeTag child render time: {} {}",
          StringUtils.repeat("  ", interpreter.getContext().getIncludePathStack().size()),
          costMs, path);

      interpreter.getErrors().addAll(child.getErrors());
      interpreter.endRender("renderResource");
      return result;

    } catch (IOException e) {
      throw new InterpretException(e.getMessage(), e, tagNode.getLineNumber(), tagNode.getStartPosition());
    } finally {
      interpreter.getContext().getIncludePathStack().pop();
      ENGINE_LOG.warn("{}IncludeTag render time: {} {}",
          StringUtils.repeat("  ", interpreter.getContext().getIncludePathStack().size()),
          System.currentTimeMillis() - thisStartMs, path);
      interpreter.endRender(renderName);
    }
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
