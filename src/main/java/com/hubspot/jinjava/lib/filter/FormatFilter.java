package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.IllegalFormatConversionException;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.MissingFormatWidthException;
import java.util.Objects;

@JinjavaDoc(
  value = "Apply Python string formatting to an object.",
  input = @JinjavaParam(
    value = "value",
    desc = "String value to reformat",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = "args",
      type = "String...",
      desc = "Values to insert into string"
    )
  },
  snippets = {
    @JinjavaSnippet(
      desc = "%s can be replaced with other variables or values",
      code = "{{ \"Hi %s %s\"|format(contact.firstname, contact.lastname) }} "
    )
  }
)
public class FormatFilter implements AdvancedFilter {
  public static final String NAME = "format";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Object[] args,
    Map<String, Object> kwargs
  ) {
    try {
      return String.format(Objects.toString(var, ""), args);
    } catch (IllegalFormatException e) {
      if (e instanceof MissingFormatArgumentException) {
        throw new InvalidArgumentException(
          interpreter,
          NAME,
          "Missing format argument for '" +
          ((MissingFormatArgumentException) e).getFormatSpecifier() +
          "'"
        );
      } else if (e instanceof IllegalFormatConversionException) {
        throw new InvalidArgumentException(
          interpreter,
          NAME,
          "'" +
          args[0] +
          "' is not a compatible type for conversion to format specifier '" +
          ((IllegalFormatConversionException) e).getConversion() +
          "'"
        );
      } else if (e instanceof MissingFormatWidthException) {
        throw new InvalidArgumentException(
          interpreter,
          NAME,
          "'" +
          ((MissingFormatWidthException) e).getFormatSpecifier() +
          "' is missing a width"
        );
      }
      // could possibly handle other subclasses of IllegalFormatException here if they come up
      throw new InvalidArgumentException(interpreter, NAME, e.getMessage());
    }
  }
}
