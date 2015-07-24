package com.hubspot.jinjava.lib.tag;

import static com.hubspot.jinjava.util.Logging.ENGINE_LOG;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
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
                "{% endmacro %}"
        ),
        @JinjavaSnippet(
            desc = "The macro html file is imported from a different template. Macros are then accessed from the name given to the import.",
            code = "{% import 'custom/page/web_page_basic/my_macros.html' as header_footer %}\n" +
                "{{ header_footer.header('h1', 'My page title') }}\n" +
                "{{ header_footer.footer('h3', 'Company footer info') }}"
        )
    })
@SuppressWarnings("unchecked")
public class ImportTag implements Tag {
  private static final long serialVersionUID = 8433638845398005260L;
  private static final String IMPORT_PATH_PROPERTY = "__importP@th__";

  @Override
  public String getName() {
    return "import";
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    List<String> helper = new HelperStringTokenizer(tagNode.getHelpers()).allTokens();
    if (helper.isEmpty()) {
      throw new InterpretException("Tag 'import' expects 1 helper >>> " + helper.size(), tagNode.getLineNumber());
    }

    String contextVar = "";

    if (helper.size() > 2 && "as".equals(helper.get(1))) {
      contextVar = helper.get(2);
    }

    String path = StringUtils.trimToEmpty(helper.get(0));
    if (isPathInRenderStack(interpreter.getContext(), path)) {
      ENGINE_LOG.debug("Path {} is already in include stack", path);
      return "";
    }

    Set<String> importedPaths = (Set<String>) interpreter.getContext().get(IMPORT_PATH_PROPERTY);
    if (importedPaths == null) {
      importedPaths = new HashSet<String>();
      interpreter.getContext().put(IMPORT_PATH_PROPERTY, importedPaths);
    }
    importedPaths.add(path);

    String templateFile = interpreter.resolveString(path, tagNode.getLineNumber());
    try {
      String template = interpreter.getResource(templateFile);
      Node node = interpreter.parse(template);

      if (StringUtils.isBlank(contextVar)) {
        interpreter.render(node);
      }
      else {
        JinjavaInterpreter child = new JinjavaInterpreter(interpreter);
        child.render(node);

        interpreter.getErrors().addAll(child.getErrors());

        Map<String, Object> childBindings = child.getContext().getSessionBindings();
        for (Map.Entry<String, MacroFunction> macro : child.getContext().getGlobalMacros().entrySet()) {
          childBindings.put(macro.getKey(), macro.getValue());
        }
        childBindings.remove(Context.GLOBAL_MACROS_SCOPE_KEY);

        interpreter.getContext().put(contextVar, childBindings);
      }

      return "";
    } catch (IOException e) {
      throw new InterpretException(e.getMessage(), e, tagNode.getLineNumber());
    }
  }

  private boolean isPathInRenderStack(Context context, String path) {
    Context current = context;
    do {
      Set<String> importedPaths = (Set<String>) current.get(IMPORT_PATH_PROPERTY, new HashSet<String>());

      if (importedPaths.contains(path)) {
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

}
