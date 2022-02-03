package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.fn.Functions;
import com.hubspot.jinjava.objects.date.PyishDate;

@JinjavaDoc(
  value = "Converts a datetime string and datetime format to a datetime object",
  input = @JinjavaParam(
    value = "datetimeString",
    desc = "Datetime string",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = "datetimeFormat",
      desc = "Format of the datetime string",
      required = true
    )
  },
  snippets = { @JinjavaSnippet(code = "{% mydatetime|unixtimestamp %}") }
)
public class StringToTimeFilter implements Filter {

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if (args.length < 1) {
      throw new TemplateSyntaxException(
        interpreter,
        getName(),
        "requires 1 argument (datetime format string)"
      );
    }

    if (var == null) {
      return null;
    }

    //Preserve behaviour for String which are now Pyishdates passed
    if (var instanceof PyishDate){
      return var;
    }

    if (!(var instanceof String)) {
      throw new InvalidInputException(interpreter, this, InvalidReason.STRING);
    }

    return Functions.stringToTime((String) var, args[0]);
  }

  @Override
  public String getName() {
    return "strtotime";
  }
}
