package com.hubspot.jinjava.lib.filter;

import java.util.Objects;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.text.WordUtils;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Return a copy of the string passed to the filter wrapped after 79 characters.",
    params = {
        @JinjavaParam(value = "s", desc = "String to wrap after a certain number of chracters"),
        @JinjavaParam(value = "width", type = "number", defaultValue = "79", desc = "Sets the width of spaces at which to wrap the text"),
        @JinjavaParam(value = "break_long_words", type = "boolean", defaultValue = "True", desc = "If true, long words will be broken when wrapped")
    },
    snippets = {
        @JinjavaSnippet(
            desc = "Since HubSpot's compiler automatically strips whitespace, this filter will only work in tags where whitespace is retained, such as a <pre>",
            code = "<pre>\n" +
                "    {{ \"Lorem ipsum dolor sit amet, consectetur adipiscing elit\"|wordwrap(10) }}\n" +
                "</pre>")
    })
public class WordWrapFilter implements Filter {

  @Override
  public String getName() {
    return "wordwrap";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    String str = Objects.toString(var, "");

    int wrapLength = 79;
    if (args.length > 0) {
      wrapLength = NumberUtils.toInt(args[0], 79);
    }

    boolean wrapLongWords = true;
    if (args.length > 1) {
      wrapLongWords = BooleanUtils.toBoolean(args[1]);
    }

    return WordUtils.wrap(str, wrapLength, "\n", wrapLongWords);
  }

}
