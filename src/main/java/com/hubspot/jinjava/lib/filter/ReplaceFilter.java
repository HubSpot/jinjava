package com.hubspot.jinjava.lib.filter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;


@JinjavaDoc(
    value="Return a copy of the value with all occurrences of a substring replaced with a new one. " + 
          "The first argument is the substring that should be replaced, the second is the replacement " + 
          "string. If the optional third argument count is given, only the first count occurrences are replaced:\n\n" +

          "{{ \"Hello World\"|replace(\"Hello\", \"Goodbye\") }}\n" +
          "    -> Goodbye World\n\n" +
          
          "{{ \"aaaaargh\"|replace(\"a\", \"d'oh, \", 2) }}\n" +
          "    -> d'oh, d'oh, aaargh",
    params={
        @JinjavaParam("s"),
        @JinjavaParam("old"),
        @JinjavaParam("new"),
        @JinjavaParam(value="count", type="number", desc="replace only the first N occurrences")
    })
public class ReplaceFilter implements Filter {

  @Override
  public String getName() {
    return "replace";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter,
      String... args) {
    
    if(var == null) {
      throw new InterpretException("filter " + getName() + " requires a var to operate on");
    }
    if(args.length < 2) {
      throw new InterpretException("filter " + getName() + " requires two string args");
    }
    
    String s = (String) var;
    String toReplace = args[0];
    String replaceWith = args[1];
    Integer count = null;
    
    if(args.length > 2) {
      count = NumberUtils.createInteger(args[2]);
    }
    
    if(count == null) {
      return StringUtils.replace(s, toReplace, replaceWith);
    }
    else {
      return StringUtils.replace(s, toReplace, replaceWith, count);
    }
  }

}
