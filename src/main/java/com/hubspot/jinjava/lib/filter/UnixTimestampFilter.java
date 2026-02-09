package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.features.BuiltInFeatures;
import com.hubspot.jinjava.features.DateTimeFeatureActivationStrategy;
import com.hubspot.jinjava.features.FeatureActivationStrategy;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.lib.fn.Functions;

@JinjavaDoc(
  value = "Gets the UNIX timestamp value (in milliseconds) of a date object",
  input = @JinjavaParam(value = "value", desc = "The date variable", required = true),
  snippets = { @JinjavaSnippet(code = "{% mydatetime|unixtimestamp %}") }
)
public class UnixTimestampFilter implements Filter {

  @Override
  public String getName() {
    return "unixtimestamp";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if (var == null) {
      interpreter.addError(
        TemplateError.fromMissingFilterArgException(
          new InvalidArgumentException(
            interpreter,
            "unixtimestamp",
            "unixtimestamp filter called with null datetime"
          )
        )
      );

      FeatureActivationStrategy feat = interpreter
        .getConfig()
        .getFeatures()
        .getActivationStrategy(BuiltInFeatures.FIXED_DATE_TIME_FILTER_NULL_ARG);

      if (feat.isActive(interpreter.getContext())) {
        var = ((DateTimeFeatureActivationStrategy) feat).getActivateAt();
      }
    }

    return Functions.unixtimestamp(var);
  }
}
