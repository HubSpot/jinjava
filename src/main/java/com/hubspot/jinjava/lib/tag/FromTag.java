package com.hubspot.jinjava.lib.tag;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.hubspot.algebra.Result;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.doc.annotations.JinjavaTextMateSnippet;
import com.hubspot.jinjava.interpret.AutoCloseableSupplier;
import com.hubspot.jinjava.interpret.AutoCloseableSupplier.AutoCloseableImpl;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TagCycleException;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

@JinjavaDoc(
  value = "Alternative to the import tag that lets you import and use specific macros from one template to another",
  params = {
    @JinjavaParam(value = "path", desc = "Design Manager path to file to import from"),
    @JinjavaParam(
      value = "macro_name",
      desc = "Name of macro or comma separated macros to import (import macro_name)"
    ),
  },
  snippets = {
    @JinjavaSnippet(
      desc = "This example uses an html file containing two macros.",
      code = "{% macro header(tag, title_text) %}\n" +
      "    <header> <{{ tag }}>{{ title_text }} </{{tag}}> </header>\n" +
      "{% endmacro %}\n" +
      "{% macro footer(tag, footer_text) %}\n" +
      "    <footer> <{{ tag }}>{{ footer_text }} </{{tag}}> </footer>\n" +
      "{% endmacro %}"
    ),
    @JinjavaSnippet(
      desc = "The macro html file is accessed from a different template, but only the footer macro is imported and executed",
      code = "{% from 'custom/page/web_page_basic/my_macros.html' import footer %}\n" +
      "{{ footer('h2', 'My footer info') }}"
    ),
  }
)
@JinjavaTextMateSnippet(code = "{% from '${1:path}' import ${2:macro_name} %}")
public class FromTag implements Tag {

  public static final String TAG_NAME = "from";

  private static final long serialVersionUID = 6152691434172265022L;

  @Override
  public String getName() {
    return TAG_NAME;
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    List<String> helper = getHelpers((TagToken) tagNode.getMaster());

    try (
      AutoCloseableImpl<Result<String, TagCycleException>> templateFileResult =
        getTemplateFileWithWrapper(helper, (TagToken) tagNode.getMaster(), interpreter)
          .get()
    ) {
      return templateFileResult
        .value()
        .match(
          err -> {
            String path = StringUtils.trimToEmpty(helper.get(0));
            interpreter.addError(
              new TemplateError(
                ErrorType.WARNING,
                ErrorReason.EXCEPTION,
                ErrorItem.TAG,
                "From cycle detected for path: '" + path + "'",
                null,
                ((TagToken) tagNode.getMaster()).getLineNumber(),
                ((TagToken) tagNode.getMaster()).getStartPosition(),
                err,
                BasicTemplateErrorCategory.FROM_CYCLE_DETECTED,
                ImmutableMap.of("path", path)
              )
            );
            return "";
          },
          templateFile -> {
            Map<String, String> imports = getImportMap(helper);

            try {
              String template = interpreter.getResource(templateFile);
              Node node = interpreter.parse(template);

              JinjavaInterpreter child = interpreter
                .getConfig()
                .getInterpreterFactory()
                .newInstance(interpreter);
              child.getContext().put(Context.IMPORT_RESOURCE_PATH_KEY, templateFile);
              try (
                AutoCloseableImpl<JinjavaInterpreter> a = JinjavaInterpreter
                  .closeablePushCurrent(child)
                  .get()
              ) {
                child.render(node);
              }

              interpreter.addAllChildErrors(templateFile, child.getErrorsCopy());

              boolean importsDeferredValue = integrateChild(imports, child, interpreter);

              if (importsDeferredValue) {
                handleDeferredNodesDuringImport(
                  (TagToken) tagNode.getMaster(),
                  templateFile,
                  imports,
                  child,
                  interpreter
                );
              }

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
        );
    }
  }

  public static void handleDeferredNodesDuringImport(
    TagToken tagToken,
    String templateFile,
    Map<String, String> imports,
    JinjavaInterpreter child,
    JinjavaInterpreter interpreter
  ) {
    for (Map.Entry<String, String> importMapping : imports.entrySet()) {
      Object val = child.getContext().getGlobalMacro(importMapping.getKey());
      if (val != null) {
        MacroFunction macro = (MacroFunction) val;
        macro.setDeferred(true);
        interpreter.getContext().addGlobalMacro(macro);
      } else {
        val = child.getContext().get(importMapping.getKey());
        if (val != null) {
          interpreter
            .getContext()
            .put(importMapping.getValue(), DeferredValue.instance());
        }
      }
    }

    throw new DeferredValueException(
      templateFile,
      tagToken.getLineNumber(),
      tagToken.getStartPosition()
    );
  }

  public static boolean integrateChild(
    Map<String, String> imports,
    JinjavaInterpreter child,
    JinjavaInterpreter interpreter
  ) {
    boolean importsDeferredValue = false;
    for (Map.Entry<String, String> importMapping : imports.entrySet()) {
      Object val = child.getContext().getGlobalMacro(importMapping.getKey());

      if (val != null) {
        MacroFunction toImport = (MacroFunction) val;
        if (!importMapping.getKey().equals(importMapping.getValue())) {
          toImport = toImport.cloneWithNewName(importMapping.getValue());
        }
        interpreter.getContext().addGlobalMacro(toImport);
      } else {
        val = child.getContext().get(importMapping.getKey());

        if (val != null) {
          interpreter.getContext().put(importMapping.getValue(), val);
          if (val instanceof DeferredValue) {
            importsDeferredValue = true;
          }
        }
      }
    }
    return importsDeferredValue;
  }

  public static Map<String, String> getImportMap(List<String> helper) {
    Map<String, String> imports = new LinkedHashMap<>();

    PeekingIterator<String> args = Iterators.peekingIterator(
      helper.subList(2, helper.size()).iterator()
    );

    while (args.hasNext()) {
      String fromName = args.next();
      String importName = fromName;

      if (args.hasNext() && args.peek() != null && args.peek().equals("as")) {
        args.next();
        importName = args.next();
      }

      imports.put(fromName, importName);
    }
    return imports;
  }

  public static AutoCloseableSupplier<Result<String, TagCycleException>> getTemplateFileWithWrapper(
    List<String> helper,
    TagToken tagToken,
    JinjavaInterpreter interpreter
  ) {
    String templateFile = interpreter.resolveString(
      helper.get(0),
      tagToken.getLineNumber(),
      tagToken.getStartPosition()
    );
    templateFile = interpreter.resolveResourceLocation(templateFile);
    interpreter.getContext().addDependency("coded_files", templateFile);
    return interpreter
      .getContext()
      .getFromPathStack()
      .closeablePush(templateFile, tagToken.getLineNumber(), tagToken.getStartPosition());
  }

  @Deprecated
  public static Optional<String> getTemplateFile(
    List<String> helper,
    TagToken tagToken,
    JinjavaInterpreter interpreter
  ) {
    return getTemplateFileWithWrapper(helper, tagToken, interpreter)
      .dangerouslyGetWithoutClosing()
      .match(
        err -> {
          interpreter.addError(
            new TemplateError(
              ErrorType.WARNING,
              ErrorReason.EXCEPTION,
              ErrorItem.TAG,
              "From cycle detected for path: '" + err.getPath() + "'",
              null,
              tagToken.getLineNumber(),
              tagToken.getStartPosition(),
              err,
              BasicTemplateErrorCategory.FROM_CYCLE_DETECTED,
              ImmutableMap.of("path", err.getPath())
            )
          );
          return Optional.empty();
        },
        Optional::of
      );
  }

  public static List<String> getHelpers(TagToken tagToken) {
    List<String> helper = new HelperStringTokenizer(tagToken.getHelpers())
      .splitComma(true)
      .allTokens();
    if (helper.size() < 3 || !helper.get(1).equals("import")) {
      throw new TemplateSyntaxException(
        tagToken.getImage(),
        "Tag 'from' expects import list: " + helper,
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
