package com.hubspot.jinjava.lib.tag;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.util.HelperStringTokenizer;

/**
 * Jinja2 supports putting often used code into macros. These macros can go into different 
 * templates and get imported from there. This works similar to the import statements in 
 * Python. It’s important to know that imports are cached and imported templates don’t have 
 * access to the current template variables, just the globals by default.
 * 
 * @author jstehler
 */
public class ImportTag implements Tag {

  @Override
  public String getName() {
    return "import";
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    List<String> helper = new HelperStringTokenizer(tagNode.getHelpers()).allTokens();
    if (helper.isEmpty()) {
      throw new InterpretException("Tag 'import' expects 1 helper >>> " + helper.size(), tagNode.getLineNumber());
    }
    
    String contextVar = "";
    
    if(helper.size() > 2 && "as".equals(helper.get(1))) {
      contextVar = helper.get(2);
    }
    
    String templateFile = interpreter.resolveString(helper.get(0), tagNode.getLineNumber());
    try {
      String template = interpreter.getResource(templateFile);
      Node node = interpreter.parse(template);
      
      if(StringUtils.isBlank(contextVar)) {
        interpreter.render(node);
      }
      else {
        JinjavaInterpreter child = new JinjavaInterpreter(interpreter);
        child.render(node);
        
        Map<String, Object> childBindings = child.getContext().getSessionBindings();
        for(Map.Entry<String, MacroFunction> macro : child.getContext().getGlobalMacros().entrySet()) {
          childBindings.put(macro.getKey(), macro.getValue());
        }
        childBindings.remove(Context.GLOBAL_MACROS_SCOPE_KEY);
        
        interpreter.getContext().put(contextVar, childBindings);
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
