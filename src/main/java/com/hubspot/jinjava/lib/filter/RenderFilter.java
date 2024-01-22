package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import java.util.Objects;
import java.util.Stack;
import org.apache.commons.lang3.math.NumberUtils;

@JinjavaDoc(
  value = "Renders a template string early to be used by other filters and functions",
  input = @JinjavaParam(value = "s", desc = "String to render", required = true),
  snippets = {
    @JinjavaSnippet(
      code = "{{ \"{% if my_val %} Hello {% else %} world {% endif %}\"|render }}"
    ),
  }
)
public class RenderFilter implements Filter {
  public static final boolean IGNORE_MODULE_DISABLED_EXCEPTION = true;


  @Override
  public String getName() {
    return "render";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if (IGNORE_MODULE_DISABLED_EXCEPTION) {
      removeModuleErrors(interpreter);
    }
    if (args.length > 0) {
      String firstArg = args[0];
      return interpreter.render(
        Objects.toString(var),
        NumberUtils.toLong(
          firstArg,
          JinjavaConfig.newBuilder().build().getMaxOutputSize()
        )
      );
    }
    return interpreter.render(Objects.toString(var));
  }

  private void removeModuleErrors(JinjavaInterpreter jinjavaInterpreter) {
    Stack<TemplateError> validErrors = new Stack<>();
    while (jinjavaInterpreter.getLastError().isPresent()) {
      TemplateError te = jinjavaInterpreter.getLastError().get();
      jinjavaInterpreter.removeLastError();
      if (te.getReason() == ErrorReason.DISABLED && te.getMessage().contains("module")) {
        continue;
      }
      validErrors.add(te);
    }

    while (!validErrors.empty()) {
      jinjavaInterpreter.addError(validErrors.pop());
    }
  }
}
