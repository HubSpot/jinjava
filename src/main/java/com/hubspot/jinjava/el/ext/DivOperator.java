package com.hubspot.jinjava.el.ext;

import com.hubspot.jinjava.interpret.errorcategory.DivideByZeroException;
import de.odysseus.el.misc.TypeConverter;
import de.odysseus.el.tree.impl.Parser.ExtensionHandler;
import de.odysseus.el.tree.impl.Parser.ExtensionPoint;
import de.odysseus.el.tree.impl.Scanner;
import de.odysseus.el.tree.impl.ast.AstBinary;
import de.odysseus.el.tree.impl.ast.AstBinary.SimpleOperator;
import de.odysseus.el.tree.impl.ast.AstNode;

/**
 * Created to allow for the detection and handling of divide-by-zero requests in EL expressions
 * (see PR 473 @ https://github.com/HubSpot/jinjava/pull/473)
 */
public class DivOperator extends SimpleOperator {
  public static final Scanner.ExtensionToken TOKEN = new Scanner.ExtensionToken("/");
  public static final DivOperator OP = new DivOperator();

  @Override
  protected Object apply(TypeConverter converter, Object a, Object b) {
    if (a == null || b == null) {
      throw new DivideByZeroException("Division operator argument may not be null");
    }

    Number numA = (Number) a;
    Number numB = (Number) b;
    if (numB.doubleValue() == 0.0) {
      throw new DivideByZeroException("Division operator divisor may not be zero");
    }

    return numA.doubleValue() / numB.doubleValue();
  }

  public static final ExtensionHandler HANDLER = new ExtensionHandler(
    ExtensionPoint.MUL
  ) {

    @Override
    public AstNode createAstNode(AstNode... children) {
      return new AstBinary(children[0], children[1], OP);
    }
  };
}
