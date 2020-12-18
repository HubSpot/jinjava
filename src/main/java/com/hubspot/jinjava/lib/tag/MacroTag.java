package com.hubspot.jinjava.lib.tag;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.tree.TagNode;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

@JinjavaDoc(
  value = "Macros allow you to print multiple statements with a dynamic value or values",
  params = {
    @JinjavaParam(value = "macro_name", desc = "The name given to a macro"),
    @JinjavaParam(
      value = "argument_names",
      desc = "Named arguments that are dynamically, when the macro is run"
    )
  },
  snippets = {
    @JinjavaSnippet(
      desc = "Basic macro syntax",
      code = "{% macro name_of_macro(argument_name, argument_name2) %}\n" +
      "    {{ argument_name }}\n" +
      "    {{ argument_name2 }}\n" +
      "{% endmacro %}\n" +
      "{{ name_of_macro(\"value to pass to argument 1\", \"value to pass to argument 2\") }}"
    ),
    @JinjavaSnippet(
      desc = "Example of a macro used to print CSS3 properties with the various vendor prefixes",
      code = "{% macro trans(value) %}\n" +
      "   -webkit-transition: {{value}};\n" +
      "   -moz-transition: {{value}};\n" +
      "   -o-transition: {{value}};\n" +
      "   -ms-transition: {{value}};\n" +
      "   transition: {{value}};\n" +
      "{% endmacro %}"
    ),
    @JinjavaSnippet(
      desc = "The macro can then be called like a function. The macro is printed for anchor tags in CSS.",
      code = "a { {{ trans(\"all .2s ease-in-out\") }} }"
    )
  }
)
public class MacroTag implements Tag {
  public static final String TAG_NAME = "macro";

  private static final long serialVersionUID = 8397609322126956077L;

  public static final Pattern CHILD_MACRO_PATTERN = Pattern.compile(
    "([a-zA-Z_][\\w_]*)\\.([a-zA-Z_][\\w_]*)[^(]*\\(([^)]*)\\)"
  );

  public static final Pattern MACRO_PATTERN = Pattern.compile(
    "([a-zA-Z_][\\w_]*)[^(]*\\(([^)]*)\\)"
  );
  private static final Splitter ARGS_SPLITTER = Splitter
    .on(',')
    .omitEmptyStrings()
    .trimResults();

  @Override
  public String getName() {
    return TAG_NAME;
  }

  @Override
  public boolean isRenderedInValidationMode() {
    return true;
  }

  @Override
  @SuppressWarnings("unchecked")
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    String name;
    String args;
    String parentName = "";
    Matcher childMatcher = CHILD_MACRO_PATTERN.matcher(tagNode.getHelpers());
    if (childMatcher.find()) {
      parentName = childMatcher.group(1);
      name = childMatcher.group(2);
      args = Strings.nullToEmpty(childMatcher.group(3));
    } else {
      Matcher matcher = MACRO_PATTERN.matcher(tagNode.getHelpers());
      if (!matcher.find()) {
        throw new TemplateSyntaxException(
          tagNode.getMaster().getImage(),
          "Unable to parse macro definition: " + tagNode.getHelpers(),
          tagNode.getLineNumber(),
          tagNode.getStartPosition()
        );
      }

      name = matcher.group(1);
      args = Strings.nullToEmpty(matcher.group(2));
    }

    LinkedHashMap<String, Object> argNamesWithDefaults = new LinkedHashMap<>();

    boolean deferred = populateArgNames(
      tagNode.getLineNumber(),
      interpreter,
      args,
      argNamesWithDefaults
    );

    MacroFunction macro = new MacroFunction(
      tagNode.getChildren(),
      name,
      argNamesWithDefaults,
      false,
      interpreter.getContext(),
      interpreter.getLineNumber(),
      interpreter.getPosition()
    );
    macro.setDeferred(deferred);

    if (StringUtils.isNotEmpty(parentName)) {
      try {
        Map<String, Object> macroOfParent;
        if (!(interpreter.getContext().get(parentName) instanceof DeferredValue)) {
          macroOfParent =
            (Map<String, Object>) interpreter
              .getContext()
              .getOrDefault(parentName, new HashMap<>());
          macroOfParent.put(macro.getName(), macro);
          if (!interpreter.getContext().containsKey(parentName)) {
            interpreter.getContext().put(parentName, macroOfParent);
          }
        } else {
          Object originalValue =
            ((DeferredValue) interpreter.getContext().get(parentName)).getOriginalValue();
          if (originalValue instanceof Map) {
            ((Map<String, Object>) originalValue).put(macro.getName(), macro);
          } else {
            macroOfParent = new HashMap<>();
            macroOfParent.put(macro.getName(), macro);
            interpreter
              .getContext()
              .put(parentName, DeferredValue.instance(macroOfParent));
          }
        }
      } catch (ClassCastException e) {
        throw new TemplateSyntaxException(
          tagNode.getMaster().getImage(),
          "Unable to parse macro as a child of: " + parentName,
          tagNode.getLineNumber(),
          tagNode.getStartPosition()
        );
      }
    } else {
      interpreter.getContext().addGlobalMacro(macro);
    }

    if (deferred) {
      throw new DeferredValueException(
        name,
        tagNode.getLineNumber(),
        tagNode.getStartPosition()
      );
    }

    return "";
  }

  public static boolean populateArgNames(
    int lineNumber,
    JinjavaInterpreter interpreter,
    String args,
    LinkedHashMap<String, Object> argNamesWithDefaults
  ) {
    List<String> argList = Lists.newArrayList(ARGS_SPLITTER.split(args));
    boolean deferred = false;
    for (int i = 0; i < argList.size(); i++) {
      String arg = argList.get(i);

      if (arg.contains("=")) {
        String argName = StringUtils.substringBefore(arg, "=").trim();
        StringBuilder argValStr = new StringBuilder(
          StringUtils.substringAfter(arg, "=").trim()
        );

        if (
          StringUtils.startsWith(argValStr, "[") && !StringUtils.endsWith(argValStr, "]")
        ) {
          while (i + 1 < argList.size() && !StringUtils.endsWith(argValStr, "]")) {
            argValStr.append(", ").append(argList.get(i + 1));
            i++;
          }
        }

        try {
          Object argVal = interpreter.resolveELExpression(
            argValStr.toString(),
            lineNumber
          );
          argNamesWithDefaults.put(argName, argVal);
        } catch (DeferredValueException e) {
          deferred = true;
        }
      } else {
        argNamesWithDefaults.put(arg, null);
      }
    }
    return deferred;
  }
}
