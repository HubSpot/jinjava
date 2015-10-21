package com.hubspot.jinjava.el.ext;

import javax.el.ELException;

import de.odysseus.el.tree.impl.Parser.ExtensionHandler;
import de.odysseus.el.tree.impl.Parser.ExtensionPoint;
import de.odysseus.el.tree.impl.Scanner;
import de.odysseus.el.tree.impl.ast.AstIdentifier;
import de.odysseus.el.tree.impl.ast.AstNode;

public class NamedParameterOperator {

  public static final Scanner.ExtensionToken TOKEN = new Scanner.ExtensionToken("=");

  public static final ExtensionHandler HANDLER = new ExtensionHandler(ExtensionPoint.ADD) {
    @Override
    public AstNode createAstNode(AstNode... children) {
      if (!(children[0] instanceof AstIdentifier)) {
        throw new ELException("Expected IDENTIFIER, found " + children[0].toString());
      }
      AstIdentifier name = (AstIdentifier) children[0];
      return new AstNamedParameter(name, children[1]);
    }
  };

}
