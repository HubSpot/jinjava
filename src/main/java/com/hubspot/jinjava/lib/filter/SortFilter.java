package com.hubspot.jinjava.lib.filter;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ObjectIterator;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

@JinjavaDoc(
  value = "Sort an iterable.",
  input = @JinjavaParam(
    value = "value",
    type = "iterable",
    desc = "The sequence or dict to sort through iteration",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = SortFilter.REVERSE_PARAM,
      type = "boolean",
      defaultValue = "False",
      desc = "Boolean to reverse the sort order"
    ),
    @JinjavaParam(
      value = SortFilter.CASE_SENSITIVE_PARAM,
      type = "boolean",
      defaultValue = "False",
      desc = "Determines whether or not the sorting is case sensitive"
    ),
    @JinjavaParam(
      value = SortFilter.ATTRIBUTE_PARAM,
      desc = "Specifies an attribute to sort by"
    )
  },
  snippets = {
    @JinjavaSnippet(
      code = "{% for item in iterable|sort %}\n" + "    ...\n" + "{% endfor %}"
    ),
    @JinjavaSnippet(
      desc = "This filter requires all parameters to sort by an attribute in HubSpot. Below is a set of posts that are retrieved and alphabetized by 'name'.",
      code = "{% set my_posts = blog_recent_posts('default', limit=5) %}\n" +
      "{% for item in my_posts|sort(False, False,'name') %}\n" +
      "    {{ item.name }}<br>\n" +
      "{% endfor %}"
    )
  }
)
public class SortFilter extends AbstractFilter implements Filter {
  private static final Splitter DOT_SPLITTER = Splitter.on('.').omitEmptyStrings();
  private static final Joiner DOT_JOINER = Joiner.on('.');
  public static final String REVERSE_PARAM = "reverse";
  public static final String CASE_SENSITIVE_PARAM = "case_sensitive";
  public static final String ATTRIBUTE_PARAM = "attribute";

  @Override
  public String getName() {
    return "sort";
  }

  @Override
  public Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Map<String, Object> parsedArgs
  ) {
    if (var == null) {
      return null;
    }

    boolean reverse = (boolean) parsedArgs.get(REVERSE_PARAM);
    boolean caseSensitive = (boolean) parsedArgs.get(CASE_SENSITIVE_PARAM);
    String attribute = (String) parsedArgs.get(ATTRIBUTE_PARAM);

    if (parsedArgs.containsKey(ATTRIBUTE_PARAM) && attribute == null) {
      throw new InvalidArgumentException(interpreter, this, InvalidReason.NULL, 2);
    }

    List<String> attr = StringUtils.isNotEmpty(attribute)
      ? DOT_SPLITTER.splitToList(attribute)
      : Collections.emptyList();
    return Lists
      .newArrayList(ObjectIterator.getLoop(var))
      .stream()
      .sorted(
        Comparator.comparing(
          o -> mapObject(interpreter, o, attr),
          new ObjectComparator(reverse, caseSensitive)
        )
      )
      .collect(Collectors.toList());
  }

  private Object mapObject(
    JinjavaInterpreter interpreter,
    Object o,
    List<String> propertyChain
  ) {
    if (o == null) {
      throw new InvalidInputException(interpreter, this, InvalidReason.NULL_IN_LIST);
    }

    if (propertyChain.isEmpty()) {
      return o;
    }

    Object result = interpreter.resolveProperty(o, propertyChain);
    if (result == null) {
      throw new InvalidArgumentException(
        interpreter,
        this,
        InvalidReason.NULL_ATTRIBUTE_IN_LIST,
        2,
        DOT_JOINER.join(propertyChain)
      );
    }
    return result;
  }

  private static class ObjectComparator implements Comparator<Object>, Serializable {
    private final boolean reverse;
    private final boolean caseSensitive;

    ObjectComparator(boolean reverse, boolean caseSensitive) {
      this.reverse = reverse;
      this.caseSensitive = caseSensitive;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compare(Object o1, Object o2) {
      int result = 0;

      if (o1 instanceof String && !caseSensitive) {
        result = ((String) o1).compareToIgnoreCase((String) o2);
      } else if (
        Comparable.class.isAssignableFrom(o1.getClass()) &&
        Comparable.class.isAssignableFrom(o2.getClass())
      ) {
        result = ((Comparable<Object>) o1).compareTo(o2);
      }

      if (reverse) {
        result = -1 * result;
      }

      return result;
    }
  }
}
