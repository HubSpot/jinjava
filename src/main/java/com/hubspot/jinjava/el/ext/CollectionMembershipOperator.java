package com.hubspot.jinjava.el.ext;

import java.util.Collection;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import de.odysseus.el.misc.TypeConverter;
import de.odysseus.el.tree.impl.Parser.ExtensionHandler;
import de.odysseus.el.tree.impl.Parser.ExtensionPoint;
import de.odysseus.el.tree.impl.Scanner;
import de.odysseus.el.tree.impl.ast.AstBinary;
import de.odysseus.el.tree.impl.ast.AstBinary.SimpleOperator;
import de.odysseus.el.tree.impl.ast.AstNode;

public class CollectionMembershipOperator extends SimpleOperator {

  @Override
  protected Object apply(TypeConverter converter, Object o1, Object o2) {
    if (o2 == null) {
      return Boolean.FALSE;
    }

    if (CharSequence.class.isAssignableFrom(o2.getClass())) {
      return StringUtils.contains((CharSequence) o2, Objects.toString(o1, ""));
    }

    if (Collection.class.isAssignableFrom(o2.getClass())) {
      return ((Collection<?>) o2).contains(o1);
    }

    return Boolean.FALSE;
  }

  public static final CollectionMembershipOperator OP = new CollectionMembershipOperator();
  public static final Scanner.ExtensionToken TOKEN = new Scanner.ExtensionToken("in");

  public static final ExtensionHandler HANDLER = new ExtensionHandler(ExtensionPoint.CMP) {
    @Override
    public AstNode createAstNode(AstNode... children) {
      return new AstBinary(children[0], children[1], OP);
    }
  };

}
