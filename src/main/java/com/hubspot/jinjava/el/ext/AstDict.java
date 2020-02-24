package com.hubspot.jinjava.el.ext;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.el.ELContext;

import com.hubspot.jinjava.interpret.TemplateStateException;
import com.hubspot.jinjava.objects.collections.PyMap;

import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstIdentifier;
import de.odysseus.el.tree.impl.ast.AstLiteral;
import de.odysseus.el.tree.impl.ast.AstNode;
import de.odysseus.el.tree.impl.ast.AstString;

public class AstDict extends AstLiteral {

  private final Map<AstNode, AstNode> dict;

  public AstDict(Map<AstNode, AstNode> dict) {
    this.dict = dict;
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    Map<String, Object> resolved = new LinkedHashMap<>();

    for (Map.Entry<AstNode, AstNode> entry : dict.entrySet()) {
      AstNode entryKey = entry.getKey();
      String key;

      if (entryKey instanceof AstString) {
        key = Objects.toString(entryKey.eval(bindings, context));
      } else if (entryKey instanceof AstIdentifier) {
        Object result = entryKey.eval(bindings, context);
        key = result == null
            ? ((AstIdentifier) entryKey).getName() // this is for compatibility with the previous behavior
            : result.toString();
      } else {
        throw new TemplateStateException("Dict key must be a string or identifier, was: " + entryKey);
      }

      resolved.put(key, entry.getValue().eval(bindings, context));
    }

    return new PyMap(resolved);
  }

  @Override
  public void appendStructure(StringBuilder builder, Bindings bindings) {
    throw new UnsupportedOperationException("appendStructure not implemented in " + getClass().getSimpleName());
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder("{");

    for (Map.Entry<AstNode, AstNode> entry : dict.entrySet()) {
      s.append(entry.getKey()).append(":").append(entry.getValue());
    }

    return s.append("}").toString();
  }

}
