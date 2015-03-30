package com.hubspot.jinjava.lib.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value="Return a copy of the passed string, each line indented by 4 spaces. The first line is not indented. If you want to change the number of spaces or indent the first line too you can pass additional parameters to the filter:\n\n" +
          "{{ mytext|indent(2, true) }}\n" +
          "    indent by two spaces and indent the first line too.",
    params={
        @JinjavaParam("s"),
        @JinjavaParam(value="width", type="number", defaultValue="4"),
        @JinjavaParam(value="indentfirst", type="boolean", defaultValue="False")
    })
public class IndentFilter implements Filter {

  @Override
  public String getName() {
    return "indent";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    int width = 4;
    if(args.length > 0) {
      width = NumberUtils.toInt(args[0], 4);
    }
    
    boolean indentFirst = false;
    if(args.length > 1) {
      indentFirst = BooleanUtils.toBoolean(args[1]);
    }
    
    List<String> indentedLines = new ArrayList<>();
    for(String line : NEWLINE_SPLITTER.split(Objects.toString(var, ""))) {
      int thisWidth = indentedLines.size() == 0 && !indentFirst ? 0 : width;
      indentedLines.add(StringUtils.repeat(' ', thisWidth) + line);
    }

    return NEWLINE_JOINER.join(indentedLines);
  }

  private static final Splitter NEWLINE_SPLITTER = Splitter.on('\n');
  private static final Joiner NEWLINE_JOINER = Joiner.on('\n');
  
}
