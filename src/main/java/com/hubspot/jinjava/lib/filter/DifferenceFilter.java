package com.hubspot.jinjava.lib.filter;

import com.google.common.collect.Sets;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import java.util.ArrayList;
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
    ),
  },
  snippets = { @JinjavaSnippet(code = "{{ [1, 2, 3]|difference([2, 3, 4]) }}") }
)
public class DifferenceFilter extends AbstractSetFilter {

  @Override
  public Object filter(Set<Object> varSet, Set<Object> argSet) {
    return new ArrayList<>(Sets.difference(varSet, argSet));
  }

  @Override
  public String getName() {
    return "difference";
  }
}
