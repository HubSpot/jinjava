package com.hubspot.jinjava.lib.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.Functions;
import com.hubspot.jinjava.objects.date.StrftimeFormatter;

@JinjavaDoc(
    value = "Formats a date object",
    input = @JinjavaParam(value = "value", defaultValue = "current time", desc = "The date variable or UNIX timestamp to format", required = true),
    params = {
        @JinjavaParam(value = "format", defaultValue = StrftimeFormatter.DEFAULT_DATE_FORMAT, desc = "The format of the date determined by the directives added to this parameter"),
        @JinjavaParam(value = "timezone", defaultValue = "utc", desc = "Time zone of output date")
    },
    snippets = {
        @JinjavaSnippet(code = "{% content.updated|datetimeformat('%B %e, %Y') %}"),
        @JinjavaSnippet(code = "{% content.updated|datetimeformat('%a %A %w %d %e %b %B %m %y %Y %H %I %k %l %p %M %S %f %z %Z %j %U %W %c %x %X %%') %}")
    })
public class DateTimeFormatFilter implements Filter {

  @Override
  public String getName() {
    return "datetimeformat";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter,
      Object... args) {

    List<String> stringArgs = new ArrayList<>();
    for(Object arg: args) {
        stringArgs.add(Objects.toString(arg));
    }
    String[] filterArgs = new String[stringArgs.size()];
    for(int i=0; i < filterArgs.length; i++) {
        filterArgs[i] = stringArgs.get(i);
    }

    if (args.length > 0) {
      return Functions.dateTimeFormat(var, filterArgs);
    } else {
      return Functions.dateTimeFormat(var);
    }
  }

}
