package com.hubspot.jinjava.lib.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;

/**
 * A filter that batches items. It works pretty much like slice just the other way round. 
 * It returns a list of lists with the given number of items. If you provide a second parameter 
 * this is used to fill up missing items. See this example:
 * 
 * <table>
 * {%- for row in items|batch(3, '&nbsp;') %}
 *   <tr>
 *   {%- for column in row %}
 *     <td>{{ column }}</td>
 *   {%- endfor %}
 *   </tr>
 * {%- endfor %}
 * </table>
 * 
 * @author jstehler
 */
public class BatchFilter implements Filter {

  @Override
  public String getName() {
    return "batch";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if(var == null || args.length == 0) {
      return Collections.emptyList();
    }
    
    int lineCount = NumberUtils.toInt(args[0], 0);
    if(lineCount == 0) {
      return Collections.emptyList();
    }
    
    Object fillWith = args.length > 1 ? args[1] : null;
    
    ForLoop loop = ObjectIterator.getLoop(var);
    List<List<Object>> result = new ArrayList<>();
    List<Object> currentRow = null;
    
    while(loop.hasNext()) {
      Object item = loop.next();
      
      if(currentRow == null) {
        currentRow = new ArrayList<>();
        result.add(currentRow);
      }
      
      currentRow.add(item);
      
      if(currentRow.size() == lineCount) {
        currentRow = null;
      }
    }
    
    if(currentRow != null) {
      while(currentRow.size() < lineCount) {
        currentRow.add(fillWith);
      }
    }
    
    return result;
  }
  
}
