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
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.JinjavaInterpreter.InterpreterScopeClosable;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.tree.TagNode;

@JinjavaDoc(
    value = "In some cases it can be useful to pass a macro to another macro. For this purpose you can use the special call block.",
    snippets = {
        @JinjavaSnippet(
            desc = "This is a simple dialog rendered by using a macro and a call block",
            code = " {% macro render_dialog(title, class='dialog') %}\n" +
                "  <div class=\"{{ class }}\">\n" +
                "      <h2>{{ title }}</h2>\n" +
                "      <div class=\"contents\">\n" +
                "          {{ caller() }}\n" +
                "      </div>\n" +
                "  </div>\n" +
                " {% endmacro %}\n\n" +

    " {% call render_dialog('Hello World') %}\n" +
                "     This is a simple dialog rendered by using a macro and\n" +
                "     a call block.\n" +
                " {% endcall %}"),
        @JinjavaSnippet(
            desc = "It’s also possible to pass arguments back to the call block. This makes it useful as replacement for loops. "
                + "Generally speaking a call block works exactly like an macro, just that it doesn’t have a name. Here an example "
                + "of how a call block can be used with arguments",
            code = " {% macro dump_users(users) %}\n" +
                "   <ul>\n" +
                "     {% for user in users %}\n" +
                "       <li><p>{{ user.username|e }}</p>{{ caller(user) }}</li>\n" +
                "     {%- endfor %}\n" +
                "   </ul>\n" +
                " {% endmacro %}\n\n" +

    " {% call(user) dump_users(list_of_user) %}\n" +
                "  <dl>\n" +
                "       <dl>Realname</dl>\n" +
                "       <dd>{{ user.realname|e }}</dd>\n" +
                "       <dl>Description</dl>\n" +
                "       <dd>{{ user.description }}</dd>\n" +
                "  </dl>\n" +
                " {% endcall %}")
    })
public class CallTag implements Tag {

  private static final long serialVersionUID = 7231253469979314727L;

  private static final Pattern CALL_PATTERN = Pattern.compile("(?:\\(([^\\)]*)\\))?(.*)");
  private static final Splitter ARGS_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();

  @Override
  public String getName() {
    return "call";
  }

  @Override
  public String getEndTagName() {
    return "endcall";
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    Matcher matcher = CALL_PATTERN.matcher(tagNode.getHelpers().trim());
    if (!matcher.find()) {
      throw new TemplateSyntaxException(tagNode.getMaster().getImage(), "Unable to parse call block: " + tagNode.getHelpers(), tagNode.getLineNumber(), tagNode.getStartPosition());
    }

    String args = Strings.nullToEmpty(matcher.group(1));
    String macro = matcher.group(2);

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

    String macroExpr = "{{" + macro + "}}";

    try (InterpreterScopeClosable c = interpreter.enterScope()) {
      MacroFunction caller = new MacroFunction(tagNode.getChildren(), "caller", argNamesWithDefaults, false, false, true, interpreter.getContext());
      interpreter.getContext().addGlobalMacro(caller);

      return interpreter.render(macroExpr);
    }
  }

}
