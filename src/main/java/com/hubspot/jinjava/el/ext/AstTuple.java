package com.hubspot.jinjava.el.ext;

import java.util.Collections;

import javax.el.ELContext;

import com.hubspot.jinjava.objects.collections.PyList;

import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstParameters;

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
