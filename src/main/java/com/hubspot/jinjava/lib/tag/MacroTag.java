package com.hubspot.jinjava.lib.tag;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.tree.TagNode;


@JinjavaDoc(value="Macros are comparable with functions in regular programming languages. They are "
    + "useful to put often used idioms into reusable functions to not repeat yourself.\n\n"
    + "If the macro was defined in a different template you have to import it first.\n\n" +

"Inside macros you have access to three special variables:\n\n" +

"varargs\n" +
"  If more positional arguments are passed to the macro than accepted by \n" +
"  the macro they end up in the special varargs variable as list of values.\n" +
"kwargs\n" +
"  Like varargs but for keyword arguments. All unconsumed keyword arguments \n" +
"  are stored in this special variable.\n" +
"caller\n" +
"  If the macro was called from a call tag the caller is stored in this \n" +
"  variable as macro which can be called.\n\n" +

"Macros also expose some of their internal details. The following attributes \n" +
"are available on a macro object:\n\n" +

"name\n" +
"  The name of the macro. {{ input.name }} will print input.\n" +
"arguments\n" +
"  A tuple of the names of arguments the macro accepts.\n" +
"defaults\n" +
"  A tuple of default values.\n" +
"catch_kwargs\n" +
"  This is true if the macro accepts extra keyword arguments (ie: accesses the \n" +
"  special kwargs variable).\n" +
"catch_varargs\n" +
"  This is true if the macro accepts extra positional arguments (ie: accesses \n" +
"  the special varargs variable).\n" +
"caller\n" +
"  This is true if the macro accesses the special caller variable and may be \n" +
"  called from a call tag.\n\n" +

"If a macro name starts with an underscore it’s not exported and can’t be imported.",
    snippets={
      @JinjavaSnippet(desc="Here a small example of a macro that renders a form element",
          code="{% macro input(name, value='', type='text', size=20) -%}\n" +
                "  <input type=\"{{ type }}\" name=\"{{ name }}\" value=\"{{ value|e }}\" size=\"{{ size }}\">\n" +
                "{%- endmacro %}"),
      @JinjavaSnippet(desc="The macro can then be called like a function in the namespace",
          code="{{ input('username') }}\n{{ input('password', type='password') }}")
    })
public class MacroTag implements Tag {

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
    if(!matcher.find()) {
      throw new InterpretException("Unable to parse macro definition: " + tagNode.getHelpers());
    }

    String name = matcher.group(1);
    String args = Objects.firstNonNull(matcher.group(2), "");

    LinkedHashMap<String, Object> argNamesWithDefaults = new LinkedHashMap<>();

    List<String> argList = Lists.newArrayList(ARGS_SPLITTER.split(args));
    for(int i = 0; i < argList.size(); i++) {
      String arg = argList.get(i);

      if(arg.contains("=")) {
        String argName = StringUtils.substringBefore(arg, "=").trim();
        String argValStr = StringUtils.substringAfter(arg, "=").trim();

        if(argValStr.startsWith("[") && !argValStr.endsWith("]")) {
          while(i + 1 < argList.size() && !argValStr.endsWith("]")) {
            argValStr += ", " + argList.get(i + 1);
            i++;
          }
        }

        Object argVal = interpreter.resolveELExpression(argValStr, tagNode.getLineNumber());
        argNamesWithDefaults.put(argName, argVal);
      }
      else {
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

  private static final Pattern MACRO_PATTERN = Pattern.compile("([a-zA-Z_][\\w_]*)[^\\(]*\\(([^\\)]*)\\)");
  private static final Splitter ARGS_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();

}
