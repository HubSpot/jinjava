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
    value="Return a copy of the string passed to the filter wrapped after 79 characters. "
        + "You can override this default using the first parameter. If you set the second "
        + "parameter to false Jinja will not split words apart if they are longer than width. "
        + "By default, the newlines will be the default newlines for the environment, but this "
        + "can be changed using the wrapstring keyword argument.",
    params={
        @JinjavaParam("s"),
        @JinjavaParam(value="width", type="number", defaultValue="79"),
        @JinjavaParam(value="break_long_words", type="boolean", defaultValue="True")
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
    if(args.length > 0) {
      wrapLength = NumberUtils.toInt(args[0], 79);
    }

    boolean wrapLongWords = true;
    if(args.length > 1) {
      wrapLongWords = BooleanUtils.toBoolean(args[1]);
    }

    return WordUtils.wrap(str, wrapLength, "\n", wrapLongWords);
  }

}
