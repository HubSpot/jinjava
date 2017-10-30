package com.hubspot.jinjava.lib.tag;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.tree.TagNode;

@JinjavaDoc(
    value = "HubL macros allow you to print multiple statements with a dynamic value or values",
    params = {
        @JinjavaParam(value = "macro_name", desc = "The name given to a macro"),
        @JinjavaParam(value = "argument_names", desc = "Named arguments that are dynamically, when the macro is run")
    },
    snippets = {
        @JinjavaSnippet(
            desc = "Basic macro syntax",
            code = "{% macro name_of_macro(argument_name, argument_name2) %}\n" +
                "    {{ argument_name }}\n" +
                "    {{ argument_name2 }}\n" +
                "{% endmacro %}\n" +
                "{{ name_of_macro(\"value to pass to argument 1\", \"value to pass to argument 2\") }}"),
        @JinjavaSnippet(
            desc = "Example of a macro used to print CSS3 properties with the various vendor prefixes",
            code = "{% macro trans(value) %}\n" +
                "   -webkit-transition: {{value}};\n" +
                "   -moz-transition: {{value}};\n" +
                "   -o-transition: {{value}};\n" +
                "   -ms-transition: {{value}};\n" +
                "   transition: {{value}};\n" +
                "{% endmacro %}"),
        @JinjavaSnippet(
            desc = "The macro can then be called like a function. The macro is printed for anchor tags in CSS.",
            code = "a { {{ trans(\"all .2s ease-in-out\") }} }"),
    })
public class MacroTag implements Tag {

  private static final long serialVersionUID = 8397609322126956077L;

  private static final Pattern MACRO_PATTERN = Pattern.compile("([a-zA-Z_][\\w_]*)[^\\(]*\\(([^\\)]*)\\)");
  private static final Splitter ARGS_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();

  @Override
  public String getName() {
    return "macro";
  }

  @Override
  public String getEndTagName() {
    return "endmacro";
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    Matcher matcher = MACRO_PATTERN.matcher(tagNode.getHelpers());
    if (!matcher.find()) {
      throw new TemplateSyntaxException(tagNode.getMaster().getImage(), "Unable to parse macro definition: " + tagNode.getHelpers(), tagNode.getLineNumber(), tagNode.getStartPosition());
    }

    String name = matcher.group(1);
    String args = Strings.nullToEmpty(matcher.group(2));

    LinkedHashMap<String, Object> argNamesWithDefaults = new LinkedHashMap<>();

    List<String> argList = Lists.newArrayList(ARGS_SPLITTER.split(args));
    for (int i = 0; i < argList.size(); i++) {
      String arg = argList.get(i);

      if (arg.contains("=")) {
        String argName = StringUtils.substringBefore(arg, "=").trim();
        StringBuilder argValStr = new StringBuilder(StringUtils.substringAfter(arg, "=").trim());

        if (StringUtils.startsWith(argValStr, "[") && !StringUtils.endsWith(argValStr, "]")) {
          while (i + 1 < argList.size() && !StringUtils.endsWith(argValStr, "]")) {
            argValStr.append(", ").append(argList.get(i + 1));
            i++;
          }
        }

        Object argVal = interpreter.resolveELExpression(argValStr.toString(), tagNode.getLineNumber());
        argNamesWithDefaults.put(argName, argVal);
      } else {
        argNamesWithDefaults.put(arg, null);
      }
    }

    boolean catchKwargs = false;
    boolean catchVarargs = false;
    boolean caller = false;

    MacroFunction macro = new MacroFunction(tagNode.getChildren(), name, argNamesWithDefaults,
        catchKwargs, catchVarargs, caller, interpreter.getContext());
    interpreter.getContext().addGlobalMacro(macro);

    return "";
  }
}
