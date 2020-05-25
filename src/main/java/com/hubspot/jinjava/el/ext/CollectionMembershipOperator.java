package com.hubspot.jinjava.el.ext;

import de.odysseus.el.misc.TypeConverter;
import de.odysseus.el.tree.impl.Parser.ExtensionHandler;
import de.odysseus.el.tree.impl.Parser.ExtensionPoint;
import de.odysseus.el.tree.impl.Scanner;
import de.odysseus.el.tree.impl.ast.AstBinary;
import de.odysseus.el.tree.impl.ast.AstBinary.SimpleOperator;
import de.odysseus.el.tree.impl.ast.AstNode;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import javax.el.ELException;
import org.apache.commons.lang3.StringUtils;

public class CollectionMembershipOperator extends SimpleOperator {

  @Override
  public Object apply(TypeConverter converter, Object value, Object maybeIterable) {
    if (maybeIterable == null) {
      return Boolean.FALSE;
    }

    if (CharSequence.class.isAssignableFrom(maybeIterable.getClass())) {
      return StringUtils.contains(
        (CharSequence) maybeIterable,
        Objects.toString(value, "")
      );
    }

    if (maybeIterable instanceof Iterable) {
      for (Object element : (Iterable) maybeIterable) {
        if (element == null) {
          if (value == null) {
            return true;
          }
        } else {
          try {
            Object castedValue = converter.convert(value, element.getClass());
            if (element.equals(castedValue)) {
              return true;
            }
          } catch (ELException e) {
            return false;
          }
        }
      }
      return false;
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
