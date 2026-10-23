package com.hubspot.jinjava.el.ext;

import com.hubspot.jinjava.el.ext.eager.EagerAstBinary;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.DummyObject;
import de.odysseus.el.misc.TypeConverter;
import de.odysseus.el.tree.impl.Parser.ExtensionHandler;
import de.odysseus.el.tree.impl.Parser.ExtensionPoint;
import de.odysseus.el.tree.impl.Scanner;
import de.odysseus.el.tree.impl.ast.AstBinary;
import de.odysseus.el.tree.impl.ast.AstBinary.SimpleOperator;
import de.odysseus.el.tree.impl.ast.AstNode;
import java.util.Map;

public class StringConcatOperator
  extends SimpleOperator
  implements StringBuildingOperator {

  @Override
  protected Object apply(TypeConverter converter, Object o1, Object o2) {
    String o1s = coerceToString(converter, o1);
    String o2s = coerceToString(converter, o2);

    return getStringBuilder().append(o1s).append(o2s).toString();
  }

  private String coerceToString(TypeConverter converter, Object value) {
    JinjavaInterpreter interpreter = JinjavaInterpreter.getCurrent();
    if (
      value instanceof Map &&
      !(value instanceof DummyObject) &&
      interpreter != null &&
      interpreter.getConfig().getLegacyOverrides().isUsePyishObjectMapper()
    ) {
      return interpreter.getAsString(value);
    }
    return converter.convert(value, String.class);
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
