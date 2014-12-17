package com.hubspot.jinjava.lib.tag;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.util.HelperStringTokenizer;

/**
 * Alternatively you can import names from the template into the current namespace:
 * 
 * {% from 'forms.html' import input as input_field, textarea %}
 * <dl>
 * <dt>Username</dt>
 * <dd>{{ input_field('username') }}</dd>
 * <dt>Password</dt>
 * <dd>{{ input_field('password', type='password') }}</dd>
 * </dl>
 * <p>
 * {{ textarea('comment') }}
 * </p>
 * 
 * @author jstehler
 */
public class FromTag implements Tag {

  @Override
  public String getName() {
    return "from";
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    List<String> helper = new HelperStringTokenizer(tagNode.getHelpers()).splitComma(true).allTokens();
    if (helper.size() < 3 || !helper.get(1).equals("import")) {
      throw new InterpretException("Tag 'from' expects import list: " + helper, tagNode.getLineNumber());
    }

    String templateFile = interpreter.resolveString(helper.get(0), tagNode.getLineNumber());
    Map<String, String> imports = new LinkedHashMap<>();
    
    PeekingIterator<String> args = Iterators.peekingIterator(helper.subList(2, helper.size()).iterator());
    
    while(args.hasNext()) {
      String fromName = args.next();
      String importName = fromName;

      if(args.hasNext() && args.peek() != null && args.peek().equals("as")) {
        args.next();
        importName = args.next();
      }
      
      imports.put(fromName, importName);
    }
    
    try {
      String template = interpreter.getResource(templateFile);
      Node node = interpreter.parse(template);

      JinjavaInterpreter child = new JinjavaInterpreter(interpreter);
      child.render(node);

      for(Map.Entry<String, String> importMapping : imports.entrySet()) {
        Object val = child.getContext().getGlobalMacro(importMapping.getKey());
        
        if(val != null) {
          interpreter.getContext().addGlobalMacro((MacroFunction) val);
        }
        else {
          val = child.getContext().get(importMapping.getKey());
          
          if(val != null) {
            interpreter.getContext().put(importMapping.getValue(), val);
          }
        }
      }

      return "";
    } catch (IOException e) {
      throw new InterpretException(e.getMessage(), e, tagNode.getLineNumber());
    }
  }

  @Override
  public String getEndTagName() {
    return null;
  }

}
