package com.hubspot.jinjava.lib.filter;

import java.util.Objects;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.text.WordUtils;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;

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
