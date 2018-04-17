package com.hubspot.jinjava.lib.filter;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.BooleanUtils;

import com.google.common.collect.Lists;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Sort a dict and yield (key, value) pairs.",
    params = {
        @JinjavaParam(value = "value", desc = "Dict to sort"),
        @JinjavaParam(value = "case_sensitive", type = "boolean", defaultValue = "False", desc = "Determines whether or not the sorting is case sensitive"),
        @JinjavaParam(value = "by", type = "enum key|value", defaultValue = "key", desc = "Sort by dict key or value")
    },
    snippets = {
        @JinjavaSnippet(
            desc = "Sort the dict by value, case insensitive",
            code = "{% for item in contact|dictsort(false, 'value') %}\n" +
                "    {{item}}\n" +
                "{% endfor %}")
    })
public class DictSortFilter implements Filter {

  @Override
  public String getName() {
    return "dictsort";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if (var == null || !Map.class.isAssignableFrom(var.getClass())) {
      return var;
    }

    boolean caseSensitive = false;
    if (args.length > 0) {
      caseSensitive = BooleanUtils.toBoolean(args[0]);
    }

    boolean sortByKey = true;
    if (args.length > 1) {
      sortByKey = "value".equalsIgnoreCase(args[1]);
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> dict = (Map<String, Object>) var;

    List<Map.Entry<String, Object>> sorted = Lists.newArrayList(dict.entrySet());
    sorted.sort(new MapEntryComparator(caseSensitive, sortByKey));

    return sorted;
  }

  private static class MapEntryComparator implements Comparator<Map.Entry<String, Object>>, Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean caseSensitive;
    private final boolean sortByKey;

    MapEntryComparator(boolean caseSensitive, boolean sortByKey) {
      this.caseSensitive = caseSensitive;
      this.sortByKey = sortByKey;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compare(Entry<String, Object> o1, Entry<String, Object> o2) {
      Object sVal1 = sortByKey ? o1.getKey() : o1.getValue();
      Object sVal2 = sortByKey ? o2.getKey() : o2.getValue();

      int result = 0;

      if (!caseSensitive && sVal1 instanceof String && sVal2 instanceof String) {
        result = ((String) sVal1).compareToIgnoreCase((String) sVal2);
      }
      else if (Comparable.class.isAssignableFrom(sVal1.getClass()) && Comparable.class.isAssignableFrom(sVal2.getClass())) {
        result = ((Comparable<Object>) sVal1).compareTo(sVal2);
      }

      return result;
    }

  }

}
