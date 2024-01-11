package com.hubspot.jinjava.el.ext;

import com.hubspot.jinjava.el.ext.eager.EagerAstUnary;
import de.odysseus.el.misc.TypeConverter;
import de.odysseus.el.tree.impl.Parser.ExtensionHandler;
import de.odysseus.el.tree.impl.Parser.ExtensionPoint;
import de.odysseus.el.tree.impl.Scanner;
import de.odysseus.el.tree.impl.Scanner.ExtensionToken;
import de.odysseus.el.tree.impl.ast.AstNode;
import de.odysseus.el.tree.impl.ast.AstUnary;
import de.odysseus.el.tree.impl.ast.AstUnary.SimpleOperator;

public class AbsOperator extends SimpleOperator {

  public static final ExtensionToken TOKEN = new Scanner.ExtensionToken("+");
  public static final AbsOperator OP = new AbsOperator();

  public static final ExtensionHandler HANDLER = getHandler(false);

  @Override
  protected Object apply(TypeConverter converter, Object o) {
    if (o == null) {
      return null;
    }

    if (o instanceof Float) {
      return Math.abs((Float) o);
    }
    if (o instanceof Double) {
      return Math.abs((Double) o);
    }
    if (o instanceof Integer) {
      return Math.abs((Integer) o);
    }
    if (o instanceof Long) {
      return Math.abs((Long) o);
    }

    throw new IllegalArgumentException(
      "Unable to apply abs operator on object of type: " + o.getClass()
    );
  }

  @Override
  public String toString() {
    return "+";
  }

  public static ExtensionHandler getHandler(boolean eager) {
    return new ExtensionHandler(ExtensionPoint.UNARY) {
      @Override
      public AstNode createAstNode(AstNode... children) {
        return eager ? new EagerAstUnary(children[0], OP) : new AstUnary(children[0], OP);
      }
    };
  }
}
