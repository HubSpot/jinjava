package com.hubspot.jinjava.el.ext;

import de.odysseus.el.misc.TypeConverter;
import de.odysseus.el.tree.impl.Parser.ExtensionHandler;
import de.odysseus.el.tree.impl.Parser.ExtensionPoint;
import de.odysseus.el.tree.impl.Scanner;
import de.odysseus.el.tree.impl.ast.AstBinary;
import de.odysseus.el.tree.impl.ast.AstBinary.SimpleOperator;
import de.odysseus.el.tree.impl.ast.AstNode;

public class StringConcatOperator extends SimpleOperator {

  @Override
  protected Object apply(TypeConverter converter, Object o1, Object o2) {
    String o1s = converter.convert(o1, String.class);
    String o2s = converter.convert(o2, String.class);

    return new StringBuilder(o1s).append(o2s).toString();
  }

  public static final Scanner.ExtensionToken TOKEN = new Scanner.ExtensionToken("~");
  public static final StringConcatOperator OP = new StringConcatOperator();

  public static final ExtensionHandler HANDLER = new ExtensionHandler(ExtensionPoint.ADD) {
    @Override
    public AstNode createAstNode(AstNode... children) {
      return new AstBinary(children[0], children[1], OP);
    }
  };

}
