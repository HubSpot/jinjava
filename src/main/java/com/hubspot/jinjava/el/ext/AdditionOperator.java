package com.hubspot.jinjava.el.ext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.odysseus.el.misc.NumberOperations;
import de.odysseus.el.misc.TypeConverter;
import de.odysseus.el.tree.impl.ast.AstBinary;

public class AdditionOperator extends AstBinary.SimpleOperator {

  @SuppressWarnings("unchecked")
  @Override
  protected Object apply(TypeConverter converter, Object o1, Object o2) {
    if (o1 instanceof Collection) {
      List<Object> result = new ArrayList<>((Collection<Object>) o1);

      if (o2 instanceof Collection) {
        result.addAll((Collection<Object>) o2);
      }
      else {
        result.add(o2);
      }

      return result;
    }
    else if (o1 instanceof Map && o2 instanceof Map) {
      Map<Object, Object> result = new HashMap<>((Map<Object, Object>) o1);
      result.putAll((Map<Object, Object>) o2);

      return result;
    }

    if (o1 instanceof String || o2 instanceof String) {
      return Objects.toString(o1).concat(Objects.toString(o2));
    }

    return NumberOperations.add(converter, o1, o2);
  }

  public static final AdditionOperator OP = new AdditionOperator();
}
