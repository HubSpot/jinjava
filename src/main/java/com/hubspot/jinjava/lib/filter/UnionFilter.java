package com.hubspot.jinjava.lib.filter;

import com.google.common.collect.Sets;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import java.util.ArrayList;
import java.util.Set;

@JinjavaDoc(
  value = "Returns a list containing elements present in either list",
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
  snippets = { @JinjavaSnippet(code = "{{ [1, 2, 3]|union([2, 3, 4]) }}") }
)
public class UnionFilter extends AbstractSetFilter {

  @Override
  public Object filter(Set<Object> varSet, Set<Object> argSet) {
    return new ArrayList<>(Sets.union(varSet, argSet));
  }

  @Override
  public String getName() {
    return "union";
  }
}
