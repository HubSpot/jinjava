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

    double bAsDouble = converter.convert(b, Double.class);
    if (bAsDouble == 0.0) {
      throw new IllegalArgumentException(
        String.format(
          "Divisor for // (truncated division) cannot be zero: '%s' (%s) and '%s' (%s)",
          a,
          a.getClass().getSimpleName(),
          b,
          b.getClass().getSimpleName()
        )
      );
    }

    if (aInt && bInt) {
      Long aAsLong = converter.convert(a, Long.class);
      Long bAsLong = converter.convert(b, Long.class);
      return Math.floorDiv(aAsLong, bAsLong);
    }

    if (aNum && bNum) {
      Double aAsDouble = converter.convert(a, Double.class);
      return Math.floor(aAsDouble / bAsDouble);
    }

    throw new IllegalArgumentException(
      String.format(
        "Unsupported operand type(s) for //: '%s' (%s) and '%s' (%s)",
        a,
        (a == null ? "null" : a.getClass().getSimpleName()),
        b,
        (b == null ? "null" : b.getClass().getSimpleName())
      )
    );
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
