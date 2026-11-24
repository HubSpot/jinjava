package com.hubspot.jinjava.el.ext;

import com.hubspot.jinjava.el.ext.eager.EagerAstBinary;
import de.odysseus.el.misc.TypeConverter;
import de.odysseus.el.tree.impl.Parser.ExtensionHandler;
import de.odysseus.el.tree.impl.Parser.ExtensionPoint;
import de.odysseus.el.tree.impl.Scanner;
import de.odysseus.el.tree.impl.ast.AstBinary;
import de.odysseus.el.tree.impl.ast.AstBinary.SimpleOperator;
import de.odysseus.el.tree.impl.ast.AstNode;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
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
      return containsConverted(converter, o1, collection, collection::contains);
    }

    if (Map.class.isAssignableFrom(o2.getClass())) {
      Map<?, ?> map = (Map<?, ?>) o2;
      if (map.isEmpty()) {
        return Boolean.FALSE;
      }

      return containsConverted(converter, o1, map.keySet(), map::containsKey);
    }

    return Boolean.FALSE;
  }

  private static Boolean containsConverted(
    TypeConverter converter,
    Object needle,
    Iterable<?> values,
    Predicate<Object> contains
  ) {
    if (needle == null) {
      return containsNull(converter, values, contains);
    }

    for (Object value : values) {
      if (value == null) {
        continue;
      }

      Object converted;
      try {
        converted = converter.convert(needle, value.getClass());
      } catch (ELException e) {
        continue;
      }

      if (contains.test(converted)) {
        return Boolean.TRUE;
      }
    }
    return Boolean.FALSE;
  }

  private static Boolean containsNull(
    TypeConverter converter,
    Iterable<?> values,
    Predicate<Object> contains
  ) {
    for (Object value : values) {
      if (value == null) {
        return Boolean.TRUE;
      }
      try {
        return contains.test(converter.convert(null, value.getClass()));
      } catch (ELException e) {
        return Boolean.FALSE;
      }
    }
    return Boolean.FALSE;
  }

  @Override
  public String toString() {
    return TOKEN.getImage();
  }

  public static final CollectionMembershipOperator OP =
    new CollectionMembershipOperator();
  public static final Scanner.ExtensionToken TOKEN = new Scanner.ExtensionToken("in");

  public static final ExtensionHandler HANDLER = getHandler(false);
  public static final ExtensionHandler EAGER_HANDLER = getHandler(true);

  private static ExtensionHandler getHandler(boolean eager) {
    return new ExtensionHandler(ExtensionPoint.CMP) {
      @Override
      public AstNode createAstNode(AstNode... children) {
        return eager
          ? new EagerAstBinary(children[0], children[1], OP)
          : new AstBinary(children[0], children[1], OP);
      }
    };
  }
}
