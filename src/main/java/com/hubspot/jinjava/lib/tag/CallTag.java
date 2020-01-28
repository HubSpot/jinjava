package com.hubspot.jinjava.lib.tag;

import java.util.LinkedHashMap;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.JinjavaInterpreter.InterpreterScopeClosable;
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

  public static final String TAG_NAME = "call";

  private static final long serialVersionUID = 7231253469979314727L;

  @Override
  public String getName() {
    return TAG_NAME;
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    String macroExpr = "{{" + tagNode.getHelpers().trim() + "}}";

    try (InterpreterScopeClosable c = interpreter.enterScope()) {
      LinkedHashMap<String, Object> args = new LinkedHashMap<>();
      MacroFunction caller = new MacroFunction(tagNode.getChildren(), "caller", args, true, interpreter.getContext(),
          interpreter.getLineNumber(), interpreter.getPosition());
      interpreter.getContext().addGlobalMacro(caller);

      return interpreter.render(macroExpr);
    }
  }

}
