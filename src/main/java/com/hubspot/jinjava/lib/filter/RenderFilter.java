package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.el.ext.DeferredInvocationResolutionException;
import com.hubspot.jinjava.el.ext.eager.RenderFlatTempVariable;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.Objects;
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

  @Override
  public String getName() {
    return "render";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    int numDeferredTokensStart = interpreter.getContext().getDeferredTokens().size();
    String result;
    if (args.length > 0) {
      String firstArg = args[0];
      result =
        interpreter.renderFlat(
          Objects.toString(var),
          NumberUtils.toLong(
            firstArg,
            JinjavaConfig.newBuilder().build().getMaxOutputSize()
          )
        );
    } else {
      result = interpreter.renderFlat(Objects.toString(var));
    }
    if (interpreter.getContext().getDeferredTokens().size() > numDeferredTokensStart) {
      String tempVarName = RenderFlatTempVariable.getVarName(result);
      interpreter
        .getContext()
        .getParent()
        .put(tempVarName, new RenderFlatTempVariable(result));
      throw new DeferredInvocationResolutionException(tempVarName);
    }
    return result;
  }
}
