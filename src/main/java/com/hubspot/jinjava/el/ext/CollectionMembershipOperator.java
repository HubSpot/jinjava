package com.hubspot.jinjava.el.ext;

import com.hubspot.jinjava.el.ext.eager.EagerAstBinaryDecorator;
import de.odysseus.el.misc.TypeConverter;
import de.odysseus.el.tree.impl.Parser.ExtensionHandler;
import de.odysseus.el.tree.impl.Parser.ExtensionPoint;
import de.odysseus.el.tree.impl.Scanner;
import de.odysseus.el.tree.impl.ast.AstBinary;
import de.odysseus.el.tree.impl.ast.AstBinary.SimpleOperator;
import de.odysseus.el.tree.impl.ast.AstNode;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import javax.el.ELException;
import org.apache.commons.lang3.StringUtils;

public class CollectionMembershipOperator extends SimpleOperator {

  @Override
  public Object apply(TypeConverter converter, Object o1, Object o2) {
    if (o2 == null) {
      return Boolean.FALSE;
    }

    if (CharSequence.class.isAssignableFrom(o2.getClass())) {
      return StringUtils.contains((CharSequence) o2, Objects.toString(o1, ""));
    }

    if (Collection.class.isAssignableFrom(o2.getClass())) {
      Collection<?> collection = (Collection<?>) o2;

      for (Object value : collection) {
        if (value == null) {
          if (o1 == null) {
            return Boolean.TRUE;
          }
        } else {
          try {
            return collection.contains(converter.convert(o1, value.getClass()));
          } catch (ELException e) {
            return Boolean.FALSE;
          }
        }
      }
    }

    if (Map.class.isAssignableFrom(o2.getClass())) {
      Map map = (Map) o2;
      if (!map.isEmpty()) {
        try {
          Class<?> keyClass = map.keySet().iterator().next().getClass();
          return map.containsKey(converter.convert(o1, keyClass));
        } catch (ELException e) {
          return Boolean.FALSE;
        }
      }
    }

    return Boolean.FALSE;
  }

  @Override
  public String toString() {
    return TOKEN.getImage();
  }

  public static final CollectionMembershipOperator OP = new CollectionMembershipOperator();
  public static final Scanner.ExtensionToken TOKEN = new Scanner.ExtensionToken("in");

  public static final ExtensionHandler HANDLER = getHandler(false);

  public static ExtensionHandler getHandler(boolean eager) {
    return new ExtensionHandler(ExtensionPoint.CMP) {

      @Override
      public AstNode createAstNode(AstNode... children) {
        return eager
          ? new EagerAstBinaryDecorator(children[0], children[1], OP)
          : new AstBinary(children[0], children[1], OP);
      }
    };
  }
}
