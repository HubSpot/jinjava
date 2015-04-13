package com.hubspot.jinjava.lib.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;
import com.hubspot.jinjava.util.VariableChain;


@JinjavaDoc(
    value="Return a string which is the concatenation of the strings in the sequence. "
        + "The separator between elements is an empty string per default, you can define "
        + "it with the optional parameter",
    params={
        @JinjavaParam("value"),
        @JinjavaParam(value="d", desc="separator string"),
        @JinjavaParam(value="attr", desc="object attribute to use in joining")
    },
    snippets={
        @JinjavaSnippet(
            code="{{ [1, 2, 3]|join('|') }}",
            output="1|2|3"),
        @JinjavaSnippet(
            code="{{ [1, 2, 3]|join }}",
            output="123"),
        @JinjavaSnippet(
            desc="It is also possible to join certain attributes of an object",
            code="{{ users|join(', ', attribute='username') }}")
    })
public class JoinFilter implements Filter {

  @Override
  public String getName() {
    return "join";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    List<String> vals = new ArrayList<>();
    
    String separator = "";
    if(args.length > 0) {
      separator = args[0];
    }
    
    String attr = null;
    if(args.length > 1) {
      attr = args[1];
    }
    
    ForLoop loop = ObjectIterator.getLoop(var);
    while(loop.hasNext()) {
      Object val = loop.next();
      
      if(attr != null) {
        val = new VariableChain(Lists.newArrayList(attr), val).resolve();
      }
      
      vals.add(Objects.toString(val, ""));
    }
    
    return Joiner.on(separator).join(vals);
  }

}
