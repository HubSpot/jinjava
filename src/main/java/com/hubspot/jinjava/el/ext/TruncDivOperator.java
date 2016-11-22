package com.hubspot.jinjava.el.ext;

import de.odysseus.el.misc.TypeConverter;
import de.odysseus.el.tree.impl.Parser.ExtensionHandler;
import de.odysseus.el.tree.impl.Parser.ExtensionPoint;
import de.odysseus.el.tree.impl.Scanner;
import de.odysseus.el.tree.impl.ast.AstBinary;
import de.odysseus.el.tree.impl.ast.AstBinary.SimpleOperator;
import de.odysseus.el.tree.impl.ast.AstNode;

public class TruncDivOperator extends SimpleOperator {

  public static final Scanner.ExtensionToken TOKEN = new Scanner.ExtensionToken("//");
  public static final TruncDivOperator OP = new TruncDivOperator();

  @Override
  protected Object apply(TypeConverter converter, Object a, Object b) {

    boolean aInt = a instanceof Integer || a instanceof Long;
    boolean bInt = b instanceof Integer || b instanceof Long;
    boolean aNum = aInt || a instanceof Double || a instanceof Float;
    boolean bNum = bInt || b instanceof Double || b instanceof Float;

    if (aInt && bInt) {
      Long d = converter.convert(a, Long.class);
      Long e = converter.convert(b, Long.class);
      return Math.floorDiv(d, e);
    }
    if (aNum && bNum) {
      Double d = converter.convert(a, Double.class);
      Double e = converter.convert(b, Double.class);
      return Math.floor(d / e);
    }
    throw new IllegalArgumentException(String.format("Unsupported operand type(s) for //: '%s' (%s) and '%s' (%s)",
                                                     a,
                                                     (a == null ? "null" : a.getClass().getSimpleName()),
                                                     b,
                                                     (b == null ? "null" : b.getClass().getSimpleName())
                                                    )
    );
  }

  public static final ExtensionHandler HANDLER = new ExtensionHandler(ExtensionPoint.MUL) {
    @Override
    public AstNode createAstNode(AstNode... children) {
      return new AstBinary(children[0], children[1], OP);
    }
  };

}


