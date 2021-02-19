package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.OutputTooBigException;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorItem;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.LengthLimitingStringBuilder;
import com.hubspot.jinjava.util.ObjectIterator;
import java.util.Map;
import java.util.Objects;

@JinjavaDoc(
  value = "Return a string which is the concatenation of the strings in the sequence.",
  input = @JinjavaParam(value = "value", desc = "The values to join", required = true),
  params = {
    @JinjavaParam(
      value = JoinFilter.SEPARATOR_PARAM,
      desc = "The separator string used to join the items",
      defaultValue = "(empty String)"
    ),
    @JinjavaParam(
      value = JoinFilter.ATTRIBUTE_PARAM,
      desc = "Optional dict object attribute to use in joining"
    )
  },
  snippets = {
    @JinjavaSnippet(code = "{{ [1, 2, 3]|join('|') }}", output = "1|2|3"),
    @JinjavaSnippet(code = "{{ [1, 2, 3]|join }}", output = "123"),
    @JinjavaSnippet(
      desc = "It is also possible to join certain attributes of an object",
      code = "{{ users|join(', ', attribute='username') }}"
    )
  }
)
public class JoinFilter extends AbstractFilter implements Filter {
  public static final String SEPARATOR_PARAM = "d";
  public static final String ATTRIBUTE_PARAM = "attribute";

  @Override
  public String getName() {
    return "join";
  }

  @Override
  public Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Map<String, Object> parsedArgs
  ) {
    LengthLimitingStringBuilder stringBuilder = new LengthLimitingStringBuilder(
      interpreter.getConfig().getMaxStringLength()
    );

    String separator = (String) parsedArgs.get(SEPARATOR_PARAM);

    String attr = (String) parsedArgs.get(ATTRIBUTE_PARAM);

    ForLoop loop = ObjectIterator.getLoop(var);
    boolean first = true;
    while (loop.hasNext()) {
      Object val = loop.next();

      if (attr != null) {
        val = interpreter.resolveProperty(val, attr);
      }

      try {
        if (!first) {
          stringBuilder.append(separator);
        } else {
          first = false;
        }
        stringBuilder.append(Objects.toString(val, ""));
      } catch (OutputTooBigException ex) {
        interpreter.addError(
          new TemplateError(
            ErrorType.WARNING,
            ErrorReason.OTHER,
            ErrorItem.FILTER,
            String.format(
              "Result of %s filter has been truncated to the max String length of %d",
              getName(),
              interpreter.getConfig().getMaxStringLength()
            ),
            null,
            interpreter.getLineNumber(),
            interpreter.getPosition(),
            ex
          )
        );

        return stringBuilder.toString();
      }
    }

    return stringBuilder.toString();
  }
}
