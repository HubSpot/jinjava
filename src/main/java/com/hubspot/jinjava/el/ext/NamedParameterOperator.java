package com.hubspot.jinjava.el.ext;

import com.hubspot.jinjava.el.ext.eager.EagerAstNamedParameter;
import de.odysseus.el.tree.impl.Parser.ExtensionHandler;
import de.odysseus.el.tree.impl.Parser.ExtensionPoint;
import de.odysseus.el.tree.impl.Scanner;
import de.odysseus.el.tree.impl.ast.AstIdentifier;
import de.odysseus.el.tree.impl.ast.AstNode;
import javax.el.ELException;

public class NamedParameterOperator {

  public static final Scanner.ExtensionToken TOKEN = new Scanner.ExtensionToken("=");

  public static final ExtensionHandler HANDLER = getHandler(false);

  public static ExtensionHandler getHandler(boolean eager) {
    return new ExtensionHandler(ExtensionPoint.ADD) {
      @Override
      public AstNode createAstNode(AstNode... children) {
        if (!(children[0] instanceof AstIdentifier)) {
          throw new ELException("Expected IDENTIFIER, found " + children[0].toString());
        }
        AstIdentifier name = (AstIdentifier) children[0];
        return eager
          ? new EagerAstNamedParameter(name, children[1])
          : new AstNamedParameter(name, children[1]);
      }
    };
  }
}
