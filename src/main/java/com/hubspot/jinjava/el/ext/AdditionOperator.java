package com.hubspot.jinjava.el.ext;

import java.util.Objects;

import de.odysseus.el.misc.NumberOperations;
import de.odysseus.el.misc.TypeConverter;
import de.odysseus.el.tree.impl.ast.AstBinary;

public class AdditionOperator extends AstBinary.SimpleOperator {

  @Override
  protected Object apply(TypeConverter converter, Object o1, Object o2) {
    if(o1 instanceof String || o2 instanceof String) {
      return Objects.toString(o1).concat(Objects.toString(o2));
    }
    
    return NumberOperations.add(converter, o1, o2);
  }

  public static final AdditionOperator OP = new AdditionOperator();
}
