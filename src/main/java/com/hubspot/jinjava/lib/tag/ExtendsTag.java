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

import java.io.IOException;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.util.HelperStringTokenizer;

@JinjavaDoc(
    value = "Template inheritance allows you to build a base “skeleton” template that contains all the "
        + "common elements of your site and defines blocks that child templates can override.",
    params = {
        @JinjavaParam(value = "path", desc = "Design Manager file path to parent template")
    },
    snippets = {
        @JinjavaSnippet(
            desc = "This template, which we’ll call base.html, defines a simple HTML skeleton document that you might "
                + "use for a simple two-column page. It’s the job of “child” templates to fill the empty blocks with content.\n\n"
                + "In this example, the {% block %} tags define four blocks that child templates can fill in. All the block tag "
                + "does is tell the template engine that a child template may override those placeholders in the template.",
            code = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    {% block head %}\n" +
                "    <link rel=\"stylesheet\" href=\"style.css\" />\n" +
                "    <title>{% block title %}{% endblock %} - My Webpage</title>\n" +
                "    {% endblock %}\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id=\"content\">{% block content %}{% endblock %}</div>\n" +
                "    <div id=\"footer\">\n" +
                "        {% block footer %}\n" +
                "        &copy; Copyright 2008 by <a href=\"http://domain.invalid/\">you</a>.\n" +
                "        {% endblock %}\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>"),
        @JinjavaSnippet(
            desc = "The {% extends %} tag is the key here. It tells the template engine that this template “extends” another template. "
                + "When the template system evaluates this template, it first locates the parent. The extends tag should be the first "
                + "tag in the template. Everything before it is printed out normally and may cause confusion.",
            code = "{% extends \"custom/page/web_page_basic/my_template.html\" %}\n" +
                "{% block title %}Index{% endblock %}\n" +
                "{% block head %}\n" +
                "    {{ super() }}\n" +
                "    <style type=\"text/css\">\n" +
                "        .important { color: #336699; }\n" +
                "    </style>\n" +
                "{% endblock %}\n" +
                "{% block content %}\n" +
                "    <h1>Index</h1>\n" +
                "    <p class=\"important\">\n" +
                "      Welcome to my awesome homepage.\n" +
                "    </p>\n" +
                "{% endblock %}")
    })
public class ExtendsTag implements Tag {
  private static final long serialVersionUID = 4692863362280761393L;

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    HelperStringTokenizer tokenizer = new HelperStringTokenizer(tagNode.getHelpers());
    if (!tokenizer.hasNext()) {
      throw new TemplateSyntaxException(tagNode.getMaster().getImage(), "Tag 'extends' expects template path", tagNode.getLineNumber(), tagNode.getStartPosition());
    }

    String path = interpreter.resolveString(tokenizer.next(), tagNode.getLineNumber(), tagNode.getStartPosition());
    interpreter.getContext().getExtendPathStack().push(path, tagNode.getLineNumber(), tagNode.getStartPosition());

    try {
      String template = interpreter.getResource(path);
      Node node = interpreter.parse(template);

      interpreter.getContext().addDependency("coded_files", path);
      interpreter.addExtendParentRoot(node);

      return "";
    } catch (IOException e) {
      throw new InterpretException(e.getMessage(), e, tagNode.getLineNumber(), tagNode.getStartPosition());
    }
  }

  @Override
  public String getEndTagName() {
    return null;
  }

  @Override
  public String getName() {
    return "extends";
  }

}
