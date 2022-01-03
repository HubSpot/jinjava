package com.hubspot.jinjava.el.ext;

import com.hubspot.jinjava.el.ext.eager.EagerAstBinary;
import com.hubspot.jinjava.el.misc.TypeConverter;
import com.hubspot.jinjava.el.tree.impl.Parser.ExtensionHandler;
import com.hubspot.jinjava.el.tree.impl.Parser.ExtensionPoint;
import com.hubspot.jinjava.el.tree.impl.Scanner;
import com.hubspot.jinjava.el.tree.impl.ast.AstBinary;
import com.hubspot.jinjava.el.tree.impl.ast.AstBinary.SimpleOperator;
import com.hubspot.jinjava.el.tree.impl.ast.AstNode;

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

  @Override
  public String toString() {
    return TOKEN.getImage();
  }

  public static final ExtensionHandler HANDLER = getHandler(false);

  public static ExtensionHandler getHandler(boolean eager) {
    return new ExtensionHandler(ExtensionPoint.MUL) {

      @Override
      public AstNode createAstNode(AstNode... children) {
        return eager
          ? new EagerAstBinary(children[0], children[1], OP)
          : new AstBinary(children[0], children[1], OP);
      }
    };
  }
}
