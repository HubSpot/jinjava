package com.hubspot.jinjava.lib.filter;

import com.google.common.collect.Sets;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

@JinjavaDoc(
  value = "Returns a list containing elements present in the first list but not the second list",
  input = @JinjavaParam(
    value = "value",
    type = "sequence",
    desc = "The first list",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = "list",
      type = "sequence",
      desc = "The second list",
      required = true
    )
  },
  snippets = { @JinjavaSnippet(code = "{{ [1, 2, 3]|difference([2, 3, 4]) }}") }
)
public class DifferenceFilter extends AbstractSetFilter {

  @Override
  public Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Object[] args,
    Map<String, Object> kwargs
  ) {
    Set<Object> varSet = objectToSet(var);
    Set<Object> argSet = objectToSet(parseArgs(interpreter, args));

    attachMismatchedTypesWarning(interpreter, varSet, argSet);

    return new ArrayList<>(Sets.difference(varSet, argSet));
  }

  @Override
  public String getName() {
    return "difference";
  }
}
