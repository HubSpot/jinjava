package com.hubspot.jinjava.lib.tag;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.ImportTagCycleException;
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

/**
 * Jinja2 supports putting often used code into macros. These macros can go into different templates and get imported from there. This works similar to the import statements in Python. It’s important to know that imports are cached and
 * imported templates don’t have access to the current template variables, just the globals by default.
 *
 * @author jstehler
 */

@JinjavaDoc(
    value = "Allows you to access and use macros from a different template",
    params = {
        @JinjavaParam(value = "path", desc = "Design Manager path to file to import"),
        @JinjavaParam(value = "import_name", desc = "Give a name to the imported file to access macros from")
    },
    snippets = {
        @JinjavaSnippet(
            desc = "This example uses an html file containing two macros.",
            code = "{% macro header(tag, title_text) %}\n" +
                "<header> <{{ tag }}>{{ title_text }} </{{tag}}> </header>\n" +
                "{% endmacro %}\n" +
                "{% macro footer(tag, footer_text) %}\n" +
                "<footer> <{{ tag }}>{{ footer_text }} </{{tag}}> </footer>\n" +
                "{% endmacro %}"),
        @JinjavaSnippet(
            desc = "The macro html file is imported from a different template. Macros are then accessed from the name given to the import.",
            code = "{% import 'custom/page/web_page_basic/my_macros.html' as header_footer %}\n" +
                "{{ header_footer.header('h1', 'My page title') }}\n" +
                "{{ header_footer.footer('h3', 'Company footer info') }}")
    })
public class ImportTag implements Tag {

  public static final String TAG_NAME = "import";

  private static final long serialVersionUID = 8433638845398005260L;

  @Override
  public String getName() {
    return TAG_NAME;
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    List<String> helper = new HelperStringTokenizer(tagNode.getHelpers()).allTokens();
    if (helper.isEmpty()) {
      throw new TemplateSyntaxException(tagNode.getMaster().getImage(), "Tag 'import' expects 1 helper, was: " + helper.size(), tagNode.getLineNumber(), tagNode.getStartPosition());
    }

    String contextVar = "";

    if (helper.size() > 2 && "as".equals(helper.get(1))) {
      contextVar = helper.get(2);
    }

    String path = StringUtils.trimToEmpty(helper.get(0));

    try {
      interpreter.getContext().getImportPathStack().push(path, tagNode.getLineNumber(), tagNode.getStartPosition());
    } catch (ImportTagCycleException e) {
      interpreter.addError(new TemplateError(ErrorType.WARNING, ErrorReason.EXCEPTION, ErrorItem.TAG,
          "Import cycle detected for path: '" + path + "'", null, tagNode.getLineNumber(), tagNode.getStartPosition(),
          e, BasicTemplateErrorCategory.IMPORT_CYCLE_DETECTED, ImmutableMap.of("path", path)));
      return "";
    }

    String templateFile = interpreter.resolveString(path, tagNode.getLineNumber(), tagNode.getStartPosition());
    templateFile = interpreter.resolveResourceLocation(templateFile);
    interpreter.getContext().addDependency("coded_files", templateFile);
    try {
      String template = interpreter.getResource(templateFile);
      Node node = interpreter.parse(template);

      JinjavaInterpreter child = interpreter.getConfig().getInterpreterFactory().newInstance(interpreter);
      child.getContext().put(Context.IMPORT_RESOURCE_PATH_KEY, templateFile);
      JinjavaInterpreter.pushCurrent(child);

      try {
        child.render(node);
      } finally {
        JinjavaInterpreter.popCurrent();
      }

      interpreter.addAllErrors(child.getErrorsCopy());

      Map<String, Object> childBindings = child.getContext().getSessionBindings();

      // If the template depends on deferred values it should not be rendered and all defined variables should be deferred too
      if (!child.getContext().getDeferredNodes().isEmpty()){
        node.getChildren().forEach(deferredChild -> interpreter.getContext().addDeferredNode(deferredChild));
        if (StringUtils.isBlank(contextVar)) {
          childBindings.remove(Context.GLOBAL_MACROS_SCOPE_KEY);
          childBindings.remove(Context.IMPORT_RESOURCE_PATH_KEY);
          childBindings.keySet().forEach(key -> interpreter.getContext().put(key, DeferredValue.instance()));
        } else {
          interpreter.getContext().put(contextVar, DeferredValue.instance());
        }

        throw new DeferredValueException(templateFile, tagNode.getLineNumber(), tagNode.getStartPosition());
      }

      if (StringUtils.isBlank(contextVar)) {
        for (MacroFunction macro : child.getContext().getGlobalMacros().values()) {
          interpreter.getContext().addGlobalMacro(macro);
        }
        childBindings.remove(Context.GLOBAL_MACROS_SCOPE_KEY);
        interpreter.getContext().putAll(childBindings);
      } else {
        for (Map.Entry<String, MacroFunction> macro : child.getContext().getGlobalMacros().entrySet()) {
          childBindings.put(macro.getKey(), macro.getValue());
        }
        childBindings.remove(Context.GLOBAL_MACROS_SCOPE_KEY);
        interpreter.getContext().put(contextVar, childBindings);
      }

      return "";
    } catch (IOException e) {
      throw new InterpretException(e.getMessage(), e, tagNode.getLineNumber(), tagNode.getStartPosition());
    }
  }

  @Override
  public String getEndTagName() {
    return null;
  }

}
