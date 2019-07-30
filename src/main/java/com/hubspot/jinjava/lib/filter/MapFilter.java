package com.hubspot.jinjava.lib.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;

@JinjavaDoc(
    value = "Applies a filter on a sequence of objects or looks up an attribute.",
    input = @JinjavaParam(value = "value", type = "object", desc = "Sequence to apply filter or dict to lookup attribute", required = true),
    params = {
        @JinjavaParam(value = "attribute", desc = "Filter to apply to an object or dict attribute to lookup", required = true)
    },
    snippets = {
        @JinjavaSnippet(
            desc = "The basic usage is mapping on an attribute. Imagine you have a list of users but you are only interested in a list of usernames",
            code = "Users on this page: {{ users|map(attribute='username')|join(', ') }}"),
        @JinjavaSnippet(
            desc = "Alternatively you can let it invoke a filter by passing the name of the filter and the arguments afterwards. A good example would be applying a text conversion filter on a sequence",
            code = "{% set seq = ['item1', 'item2', 'item3'] %}\n" +
                "{{ seq|map('upper') }}")
    })
public class MapFilter implements AdvancedFilter {

  private static final String ATTRIBUTE_ARGUMENT = "attribute";

  @Override
  public String getName() {
    return "map";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, Object[] args, Map<String, Object> kwargs) {
    ForLoop loop = ObjectIterator.getLoop(var);

    if (args.length < 1 && kwargs.size() < 1) {
      throw new TemplateSyntaxException(interpreter, getName(), "requires 1 argument (name of filter or attribute to apply to given sequence)");
    }

    String attr;
    Filter apply = null;
    if (kwargs.containsKey(ATTRIBUTE_ARGUMENT)) {
      Object attrArg = kwargs.get(ATTRIBUTE_ARGUMENT);
      if (attrArg == null) {
        throw new InvalidArgumentException(interpreter, this, InvalidReason.NULL, ATTRIBUTE_ARGUMENT);
      }
      attr = attrArg.toString();
    } else {
      Object attrArg = args[0];
      if (attrArg == null) {
        throw new InvalidArgumentException(interpreter, this, InvalidReason.NULL, 0);
      }
      attr = attrArg.toString();
      apply = interpreter.getContext().getFilter(attr);
    }

    List<Object> result = new ArrayList<>();

    while (loop.hasNext()) {
      Object val = loop.next();
      if (apply != null) {
        val = apply.filter(val, interpreter);
      } else {
        val = interpreter.resolveProperty(val, attr);
      }

      result.add(val);
    }

    return result;
  }
}
