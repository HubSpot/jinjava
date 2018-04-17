package com.hubspot.jinjava.lib.filter;

import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;

import com.google.common.collect.Lists;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ObjectIterator;
import com.hubspot.jinjava.util.Variable;

@JinjavaDoc(
    value = "Sort an iterable.",
    params = {
        @JinjavaParam(value = "value", type = "iterable", desc = "The sequence or dict to sort through iteration"),
        @JinjavaParam(value = "reverse", type = "boolean", defaultValue = "False", desc = "Boolean to reverse the sort order"),
        @JinjavaParam(value = "case_sensitive", type = "boolean", defaultValue = "False", desc = "Determines whether or not the sorting is case sensitive"),
        @JinjavaParam(value = "attribute", desc = "Specifies an attribute to sort by")
    },
    snippets = {
        @JinjavaSnippet(
            code = "{% for item in iterable|sort %}\n" +
                "    ...\n" +
                "{% endfor %}"),
        @JinjavaSnippet(
            desc = "This filter requires all parameters to sort by an attribute in HubSpot. Below is a set of posts that are retrieved and alphabetized by 'name'.",
            code = "{% set my_posts = blog_recent_posts('default', limit=5) %}\n" +
                "{% for item in my_posts|sort(False, False,'name') %}\n" +
                "    {{ item.name }}<br>\n" +
                "{% endfor %}")
    })
public class SortFilter implements Filter {

  @Override
  public String getName() {
    return "sort";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if (var == null) {
      return var;
    }

    boolean reverse = false;
    if (args.length > 0) {
      reverse = BooleanUtils.toBoolean(args[0]);
    }

    boolean caseSensitive = false;
    if (args.length > 1) {
      caseSensitive = BooleanUtils.toBoolean(args[1]);
    }

    String attr = null;
    if (args.length > 2) {
      attr = args[2];
    }

    List<?> result = Lists.newArrayList(ObjectIterator.getLoop(var));
    result.sort(new ObjectComparator(interpreter, reverse, caseSensitive, attr));

    return result;
  }

  private static class ObjectComparator implements Comparator<Object> {
    private final boolean reverse;
    private final boolean caseSensitive;
    private final Variable variable;

    ObjectComparator(JinjavaInterpreter interpreter, boolean reverse, boolean caseSensitive, String attr) {
      this.reverse = reverse;
      this.caseSensitive = caseSensitive;

      if (attr != null) {
        this.variable = new Variable(interpreter, "o." + attr);
      }
      else {
        this.variable = null;
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compare(Object o1, Object o2) {
      int result = 0;

      if (variable != null) {
        o1 = variable.resolve(o1);
        o2 = variable.resolve(o2);
      }

      if (o1 instanceof String && !caseSensitive) {
        result = ((String) o1).compareToIgnoreCase((String) o2);
      }
      else if (Comparable.class.isAssignableFrom(o1.getClass()) && Comparable.class.isAssignableFrom(o2.getClass())) {
        result = ((Comparable<Object>) o1).compareTo(o2);
      }

      if (reverse) {
        result = -1 * result;
      }

      return result;
    }

  }
}
