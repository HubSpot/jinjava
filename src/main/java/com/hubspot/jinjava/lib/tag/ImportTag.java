package com.hubspot.jinjava.lib.tag;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.doc.annotations.JinjavaTextMateSnippet;
import com.hubspot.jinjava.interpret.AutoCloseableSupplier;
import com.hubspot.jinjava.interpret.AutoCloseableSupplier.AutoCloseableImpl;
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
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.HelperStringTokenizer;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

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
    @JinjavaParam(
      value = "import_name",
      desc = "Give a name to the imported file to access macros from"
    ),
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
    ),
  }
)
@JinjavaTextMateSnippet(code = "{% import '${1:path}' ${2: as ${3:import_name}} %}")
public class ImportTag implements Tag {

  public static final String TAG_NAME = "import";

  private static final long serialVersionUID = 8433638845398005260L;

  @Override
  public String getName() {
    return TAG_NAME;
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    List<String> helper = getHelpers((TagToken) tagNode.getMaster());

    String contextVar = getContextVar(helper);

    try (
      AutoCloseableImpl<Optional<String>> maybeTemplateFile = getTemplateFileWithWrapper(
        helper,
        (TagToken) tagNode.getMaster(),
        interpreter
      )
        .get()
    ) {
      if (maybeTemplateFile.value().isEmpty()) {
        return "";
      }
      String templateFile = maybeTemplateFile.value().get();
      try (
        AutoCloseableImpl<Node> node = parseTemplateAsNode(interpreter, templateFile)
          .get()
      ) {
        JinjavaInterpreter child = interpreter
          .getConfig()
          .getInterpreterFactory()
          .newInstance(interpreter);
        child.getContext().put(Context.IMPORT_RESOURCE_PATH_KEY, templateFile);

        JinjavaInterpreter.pushCurrent(child);

        try {
          child.render(node.value());
        } finally {
          JinjavaInterpreter.popCurrent();
        }

        interpreter.addAllChildErrors(templateFile, child.getErrorsCopy());

        Map<String, Object> childBindings = child.getContext().getSessionBindings();

        // If the template depends on deferred values it should not be rendered and all defined variables and macros should be deferred too
        if (!child.getContext().getDeferredNodes().isEmpty()) {
          handleDeferredNodesDuringImport(
            node.value(),
            contextVar,
            childBindings,
            child,
            interpreter
          );
          throw new DeferredValueException(
            templateFile,
            tagNode.getLineNumber(),
            tagNode.getStartPosition()
          );
        }

        integrateChild(contextVar, childBindings, child, interpreter);
        return "";
      } catch (IOException e) {
        throw new InterpretException(
          e.getMessage(),
          e,
          tagNode.getLineNumber(),
          tagNode.getStartPosition()
        );
      }
    }
  }

  public static void integrateChild(
    String contextVar,
    Map<String, Object> childBindings,
    JinjavaInterpreter child,
    JinjavaInterpreter parent
  ) {
    if (StringUtils.isBlank(contextVar)) {
      for (MacroFunction macro : child.getContext().getGlobalMacros().values()) {
        parent.getContext().addGlobalMacro(macro);
      }
      childBindings.remove(Context.GLOBAL_MACROS_SCOPE_KEY);
      parent
        .getContext()
        .putAll(getChildBindingsWithoutImportResourcePath(childBindings));
    } else {
      childBindings.putAll(child.getContext().getGlobalMacros());
      childBindings.remove(Context.GLOBAL_MACROS_SCOPE_KEY);
      parent.getContext().put(contextVar, childBindings);
    }
  }

  public static Map<String, Object> getChildBindingsWithoutImportResourcePath(
    Map<String, Object> childBindings
  ) {
    Map<String, Object> filteredMap = new HashMap<>();
    // Don't remove them from childBindings, because it is needed in a macro function's localContextScope
    childBindings
      .entrySet()
      .stream()
      .filter(entry -> !entry.getKey().equals(Context.IMPORT_RESOURCE_PATH_KEY))
      .forEach(entry -> filteredMap.put(entry.getKey(), entry.getValue()));
    return filteredMap;
  }

  public static void handleDeferredNodesDuringImport(
    Node node,
    String contextVar,
    Map<String, Object> childBindings,
    JinjavaInterpreter child,
    JinjavaInterpreter interpreter
  ) {
    node
      .getChildren()
      .forEach(deferredChild -> interpreter.getContext().handleDeferredNode(deferredChild)
      );
    if (StringUtils.isBlank(contextVar)) {
      for (MacroFunction macro : child.getContext().getGlobalMacros().values()) {
        macro.setDeferred(true);
        interpreter.getContext().addGlobalMacro(macro);
      }
      childBindings.remove(Context.GLOBAL_MACROS_SCOPE_KEY);
      childBindings
        .keySet()
        .forEach(key -> interpreter.getContext().put(key, DeferredValue.instance()));
    } else {
      for (Map.Entry<String, MacroFunction> macroEntry : child
        .getContext()
        .getGlobalMacros()
        .entrySet()) {
        MacroFunction macro = macroEntry.getValue();
        macro.setDeferred(true);
        childBindings.put(macroEntry.getKey(), macro);
      }
      childBindings.remove(Context.GLOBAL_MACROS_SCOPE_KEY);
      interpreter.getContext().put(contextVar, DeferredValue.instance(childBindings));
    }
  }

  public static AutoCloseableSupplier<Node> parseTemplateAsNode(
    JinjavaInterpreter interpreter,
    String templateFile
  ) throws IOException {
    return interpreter
      .getContext()
      .getCurrentPathStack()
      .closeablePush(templateFile, interpreter.getLineNumber(), interpreter.getPosition())
      .map(currentPath -> interpreter.parse(interpreter.getResource(templateFile)));
  }

  public static AutoCloseableSupplier<Optional<String>> getTemplateFileWithWrapper(
    List<String> helper,
    TagToken tagToken,
    JinjavaInterpreter interpreter
  ) {
    String path = StringUtils.trimToEmpty(helper.get(0));
    String templateFile = interpreter.resolveString(
      path,
      tagToken.getLineNumber(),
      tagToken.getStartPosition()
    );
    templateFile = interpreter.resolveResourceLocation(templateFile);
    interpreter.getContext().addDependency("coded_files", templateFile);
    try {
      return interpreter
        .getContext()
        .getImportPathStack()
        .closeablePush(
          templateFile,
          tagToken.getLineNumber(),
          tagToken.getStartPosition()
        )
        .map(Optional::of);
    } catch (ImportTagCycleException e) {
      interpreter.addError(
        new TemplateError(
          ErrorType.WARNING,
          ErrorReason.EXCEPTION,
          ErrorItem.TAG,
          "Import cycle detected for path: '" + path + "'",
          null,
          tagToken.getLineNumber(),
          tagToken.getStartPosition(),
          e,
          BasicTemplateErrorCategory.IMPORT_CYCLE_DETECTED,
          ImmutableMap.of("path", path)
        )
      );
      return AutoCloseableSupplier.of(Optional.empty());
    }
  }

  @Deprecated
  public static Optional<String> getTemplateFile(
    List<String> helper,
    TagToken tagToken,
    JinjavaInterpreter interpreter
  ) {
    return getTemplateFileWithWrapper(helper, tagToken, interpreter)
      .dangerouslyGetWithoutClosing();
  }

  public static String getContextVar(List<String> helper) {
    String contextVar = "";

    if (helper.size() > 2 && "as".equals(helper.get(1))) {
      contextVar = helper.get(2);
    }
    return contextVar;
  }

  public static List<String> getHelpers(TagToken tagToken) {
    List<String> helper = new HelperStringTokenizer(tagToken.getHelpers()).allTokens();
    if (helper.isEmpty()) {
      throw new TemplateSyntaxException(
        tagToken.getImage(),
        "Tag 'import' expects 1 helper, was: " + helper.size(),
        tagToken.getLineNumber(),
        tagToken.getStartPosition()
      );
    }
    return helper;
  }

  @Override
  public String getEndTagName() {
    return null;
  }
}
