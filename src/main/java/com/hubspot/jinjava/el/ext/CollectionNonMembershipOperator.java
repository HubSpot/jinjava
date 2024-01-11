package com.hubspot.jinjava.el.ext;

import com.hubspot.jinjava.el.ext.eager.EagerAstBinary;
import de.odysseus.el.misc.TypeConverter;
import de.odysseus.el.tree.impl.Parser.ExtensionHandler;
import de.odysseus.el.tree.impl.Parser.ExtensionPoint;
import de.odysseus.el.tree.impl.Scanner;
import de.odysseus.el.tree.impl.ast.AstBinary;
import de.odysseus.el.tree.impl.ast.AstBinary.SimpleOperator;
import de.odysseus.el.tree.impl.ast.AstNode;

public class CollectionNonMembershipOperator extends SimpleOperator {

  @Override
  public Object apply(TypeConverter converter, Object o1, Object o2) {
    return !(Boolean) IN_OP.apply(converter, o1, o2);
  }

  @Override
  public String toString() {
    return TOKEN.getImage();
  }

  public static final CollectionNonMembershipOperator NOT_IN_OP =
    new CollectionNonMembershipOperator();
  public static final CollectionMembershipOperator IN_OP =
    new CollectionMembershipOperator();
  public static final Scanner.ExtensionToken TOKEN = new Scanner.ExtensionToken("not in");

  public static final ExtensionHandler HANDLER = getHandler(false);
  public static final ExtensionHandler EAGER_HANDLER = getHandler(true);

  private static ExtensionHandler getHandler(boolean eager) {
    return new ExtensionHandler(ExtensionPoint.CMP) {
      @Override
      public AstNode createAstNode(AstNode... children) {
        return eager
          ? new EagerAstBinary(children[0], children[1], NOT_IN_OP)
          : new AstBinary(children[0], children[1], NOT_IN_OP);
      }
    };
  }
}
