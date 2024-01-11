package com.hubspot.jinjava.el.ext;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.collections.SizeLimitingPyList;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstLiteral;
import de.odysseus.el.tree.impl.ast.AstParameters;
import java.util.ArrayList;
import java.util.List;
import javax.el.ELContext;
import org.apache.commons.lang3.StringUtils;

public class AstList extends AstLiteral {

  protected final AstParameters elements;

  public AstList(AstParameters elements) {
    this.elements = elements;
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    List<Object> list = new ArrayList<>();

    for (int i = 0; i < elements.getCardinality(); i++) {
      list.add(elements.getChild(i).eval(bindings, context));
    }

    JinjavaInterpreter interpreter = (JinjavaInterpreter) context
      .getELResolver()
      .getValue(context, null, ExtendedParser.INTERPRETER);

    return new SizeLimitingPyList(list, interpreter.getConfig().getMaxListSize());
  }

  @Override
  public void appendStructure(StringBuilder builder, Bindings bindings) {
    throw new UnsupportedOperationException(
      "appendStructure not implemented in " + getClass().getSimpleName()
    );
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
