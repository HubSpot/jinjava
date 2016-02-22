package com.hubspot.jinjava.el.ext;

import java.util.ArrayList;
import java.util.List;

import javax.el.ELContext;

import org.apache.commons.lang3.StringUtils;

import com.hubspot.jinjava.objects.collections.PyList;

import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstLiteral;
import de.odysseus.el.tree.impl.ast.AstParameters;

public class AstList extends AstLiteral {

  private final AstParameters elements;

  public AstList(AstParameters elements) {
    this.elements = elements;
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    List<Object> list = new ArrayList<>();

    for (int i = 0; i < elements.getCardinality(); i++) {
      list.add(elements.getChild(i).eval(bindings, context));
    }

    return new PyList(list);
  }

  @Override
  public void appendStructure(StringBuilder builder, Bindings bindings) {
    throw new UnsupportedOperationException("appendStructure not implemented in " + getClass().getSimpleName());
  }

  protected String elementsToString() {
    List<String> els = new ArrayList<>(elements.getCardinality());

    for (int i = 0; i < elements.getCardinality(); i++) {
      els.add(elements.getChild(i).toString());
    }

    return StringUtils.join(els, ", ");
  }

  @Override
  public String toString() {
    return String.format("[%s]", elementsToString());
  }

}
