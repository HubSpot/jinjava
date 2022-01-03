package com.hubspot.jinjava.el.ext;

import com.hubspot.jinjava.objects.collections.PyList;
import com.hubspot.jinjava.el.tree.Bindings;
import com.hubspot.jinjava.el.tree.impl.ast.AstParameters;
import java.util.Collections;
import jakarta.el.ELContext;

public class AstTuple extends AstList {

  public AstTuple(AstParameters elements) {
    super(elements);
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    PyList list = (PyList) super.eval(bindings, context);
    return new PyList(Collections.unmodifiableList(list.toList()));
  }

  @Override
  public String toString() {
    return String.format("(%s)", elementsToString());
  }
}
