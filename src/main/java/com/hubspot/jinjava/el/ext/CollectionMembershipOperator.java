package com.hubspot.jinjava.el.ext;

import de.odysseus.el.misc.TypeConverter;
import de.odysseus.el.tree.impl.Parser.ExtensionHandler;
import de.odysseus.el.tree.impl.Parser.ExtensionPoint;
import de.odysseus.el.tree.impl.Scanner;
import de.odysseus.el.tree.impl.ast.AstBinary;
import de.odysseus.el.tree.impl.ast.AstBinary.SimpleOperator;
import de.odysseus.el.tree.impl.ast.AstNode;
import java.util.Collection;
import java.util.Objects;
import javax.el.ELException;
import org.apache.commons.lang3.StringUtils;

public class CollectionMembershipOperator extends SimpleOperator {

  @Override
  public Object apply(TypeConverter converter, Object value, Object iterable) {
    if (iterable == null) {
      return Boolean.FALSE;
    }

    if (CharSequence.class.isAssignableFrom(iterable.getClass())) {
      return StringUtils.contains((CharSequence) iterable, Objects.toString(value, ""));
    }

    if (Collection.class.isAssignableFrom(iterable.getClass())) {
      Collection<?> collection = (Collection<?>) iterable;

      for (Object element : collection) {
        if (element == null) {
          if (value == null) {
            return Boolean.TRUE;
          }
        } else {
          try {
            return collection.contains(converter.convert(value, element.getClass()));
          } catch (ELException e) {
            return Boolean.FALSE;
          }
        }
      }
    }

    return Boolean.FALSE;
  }

  public static final CollectionMembershipOperator OP = new CollectionMembershipOperator();
  public static final Scanner.ExtensionToken TOKEN = new Scanner.ExtensionToken("in");

  public static final ExtensionHandler HANDLER = new ExtensionHandler(
    ExtensionPoint.CMP
  ) {

    @Override
    public AstNode createAstNode(AstNode... children) {
      return new AstBinary(children[0], children[1], OP);
    }
  };
}
