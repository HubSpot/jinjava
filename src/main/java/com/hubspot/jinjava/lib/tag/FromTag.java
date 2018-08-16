package com.hubspot.jinjava.lib.tag;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.FromTagCycleException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorItem;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.interpret.errorcategory.BasicTemplateErrorCategory;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.util.HelperStringTokenizer;

@JinjavaDoc(
    value = "Alternative to the import tag that lets you import and use specific macros from one template to another",
    params = {
        @JinjavaParam(value = "path", desc = "Design Manager path to file to import from"),
        @JinjavaParam(value = "macro_name", desc = "Name of macro or comma separated macros to import (import macro_name)")
    },
    snippets = {
        @JinjavaSnippet(
            desc = "This example uses an html file containing two macros.",
            code = "{% macro header(tag, title_text) %}\n" +
                "    <header> <{{ tag }}>{{ title_text }} </{{tag}}> </header>\n" +
                "{% endmacro %}\n" +
                "{% macro footer(tag, footer_text) %}\n" +
                "    <footer> <{{ tag }}>{{ footer_text }} </{{tag}}> </footer>\n" +
                "{% endmacro %}"),
        @JinjavaSnippet(
            desc = "The macro html file is accessed from a different template, but only the footer macro is imported and executed",
            code = "{% from 'custom/page/web_page_basic/my_macros.html' import footer %}\n" +
                "{{ footer('h2', 'My footer info') }}"),
    })
public class FromTag implements Tag {

  private static final long serialVersionUID = 6152691434172265022L;

  @Override
  public String getName() {
    return "from";
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    List<String> helper = new HelperStringTokenizer(tagNode.getHelpers()).splitComma(true).allTokens();
    if (helper.size() < 3 || !helper.get(1).equals("import")) {
      throw new TemplateSyntaxException(tagNode.getMaster().getImage(), "Tag 'from' expects import list: " + helper, tagNode.getLineNumber(), tagNode.getStartPosition());
    }

    String templateFile = interpreter.resolveString(helper.get(0), tagNode.getLineNumber(), tagNode.getStartPosition());
    try {
      interpreter.getContext().pushFromStack(templateFile, tagNode.getLineNumber(), tagNode.getStartPosition());
    } catch (FromTagCycleException e) {
      interpreter.addError(new TemplateError(ErrorType.WARNING, ErrorReason.EXCEPTION, ErrorItem.TAG,
          "From cycle detected for path: '" + templateFile + "'", null, tagNode.getLineNumber(), tagNode
          .getStartPosition(), e,
          BasicTemplateErrorCategory.FROM_CYCLE_DETECTED, ImmutableMap.of("path", templateFile)));
      return "";
    }
    try {
      Map<String, String> imports = new LinkedHashMap<>();

      PeekingIterator<String> args = Iterators.peekingIterator(helper.subList(2, helper.size()).iterator());

      while (args.hasNext()) {
        String fromName = args.next();
        String importName = fromName;

        if (args.hasNext() && args.peek() != null && args.peek().equals("as")) {
          args.next();
          importName = args.next();
        }

        imports.put(fromName, importName);
      }

      try {
        String template = interpreter.getResource(templateFile);
        Node node = interpreter.parse(template);

        JinjavaInterpreter child = new JinjavaInterpreter(interpreter);
        child.render(node);

        interpreter.addAllErrors(child.getErrorsCopy());

        for (Map.Entry<String, String> importMapping : imports.entrySet()) {
          Object val = child.getContext().getGlobalMacro(importMapping.getKey());

          if (val != null) {
            interpreter.getContext().addGlobalMacro((MacroFunction) val);
          } else {
            val = child.getContext().get(importMapping.getKey());

            if (val != null) {
              interpreter.getContext().put(importMapping.getValue(), val);
            }
          }
        }

        return "";
      } catch (IOException e) {
        throw new InterpretException(e.getMessage(), e, tagNode.getLineNumber(), tagNode.getStartPosition());
      }
    } finally {
      interpreter.getContext().popFromStack();
    }
  }

  @Override
  public String getEndTagName() {
    return null;
  }

}
