package com.hubspot.jinjava.el.ext;

import com.hubspot.jinjava.el.ext.eager.EagerAstBinary;
import com.hubspot.jinjava.el.misc.TypeConverter;
import com.hubspot.jinjava.el.tree.impl.Parser.ExtensionHandler;
import com.hubspot.jinjava.el.tree.impl.Parser.ExtensionPoint;
import com.hubspot.jinjava.el.tree.impl.Scanner;
import com.hubspot.jinjava.el.tree.impl.ast.AstBinary;
import com.hubspot.jinjava.el.tree.impl.ast.AstBinary.SimpleOperator;
import com.hubspot.jinjava.el.tree.impl.ast.AstNode;

public class StringConcatOperator extends SimpleOperator {

  @Override
  protected Object apply(TypeConverter converter, Object o1, Object o2) {
    String o1s = converter.convert(o1, String.class);
    String o2s = converter.convert(o2, String.class);

    return new StringBuilder(o1s).append(o2s).toString();
  }

  @Override
  public String toString() {
    return TOKEN.getImage();
  }

  public static final Scanner.ExtensionToken TOKEN = new Scanner.ExtensionToken("~");
  public static final StringConcatOperator OP = new StringConcatOperator();

  public static final ExtensionHandler HANDLER = getHandler(false);

  public static ExtensionHandler getHandler(boolean eager) {
    return new ExtensionHandler(ExtensionPoint.ADD) {

      @Override
      public AstNode createAstNode(AstNode... children) {
        return eager
          ? new EagerAstBinary(children[0], children[1], OP)
          : new AstBinary(children[0], children[1], OP);
      }
    };
  }
}
