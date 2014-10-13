package com.hubspot.jinjava.lib.filter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;

import com.google.common.collect.Lists;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ObjectIterator;
import com.hubspot.jinjava.util.VariableChain;

public class SortFilter implements Filter {

  @Override
  public String getName() {
    return "sort";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if(var == null) {
      return var;
    }
    
    boolean reverse = false;
    if(args.length > 0) {
      reverse = BooleanUtils.toBoolean(args[0]);
    }
    
    boolean caseSensitive = false;
    if(args.length > 1) {
      caseSensitive = BooleanUtils.toBoolean(args[1]);
    }
    
    String attr = null;
    if(args.length > 2) {
      attr = args[2];
    }
    
    List<?> result = Lists.newArrayList(ObjectIterator.getLoop(var));
    Collections.sort(result, new ObjectComparator(reverse, caseSensitive, attr));
    
    return result;
  }
  
  private static class ObjectComparator implements Comparator<Object> {
    private boolean reverse;
    private boolean caseSensitive;
    private String attr;
    
    public ObjectComparator(boolean reverse, boolean caseSensitive, String attr) {
      this.reverse = reverse;
      this.caseSensitive = caseSensitive;
      this.attr = attr;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compare(Object o1, Object o2) {
      int result = 0;
      
      if(attr != null) {
        o1 = new VariableChain(Lists.newArrayList(attr), o1).resolve();
        o2 = new VariableChain(Lists.newArrayList(attr), o2).resolve();
      }
      
      if(o1 instanceof String && !caseSensitive) {
        result = ((String) o1).compareToIgnoreCase((String) o2);
      }
      else if(Comparable.class.isAssignableFrom(o1.getClass()) && Comparable.class.isAssignableFrom(o2.getClass())) {
        result = ((Comparable<Object>) o1).compareTo(o2);
      }
      
      if(reverse) {
        result = -1 * result;
      }
      
      return result;
    }
    
  }
}
