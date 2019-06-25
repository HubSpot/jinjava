package com.hubspot.jinjava.lib.filter;

import com.google.common.collect.Sets;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

import java.util.ArrayList;
import java.util.Map;

@JinjavaDoc(
    value = "Returns a list containing elements present in only one list.",
    input =  @JinjavaParam(value = "value", type = "sequence", desc = "The first list", required = true),
    params = {
        @JinjavaParam(value = "list", type = "sequence", desc = "The second list", required = true)
    },
    snippets = {
        @JinjavaSnippet(
            code = "{{ [1, 2, 3]|symmetric_difference([2, 3, 4]) }}")
    })
public class SymmetricDifferenceFilter extends AbstractSetFilter {

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, Object... args) {
        return null;
    }

    @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, Object[] args, Map<String, Object> kwargs) {
    return new ArrayList<>(Sets.symmetricDifference(objectToSet(var), objectToSet(parseArgs(interpreter, args))));
  }

  @Override
  public String getName() {
    return "symmetric_difference";
  }
}
