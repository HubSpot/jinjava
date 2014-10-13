package com.hubspot.jinjava.lib.filter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.BooleanUtils;

import com.google.common.collect.Lists;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class DictSortFilter implements Filter {

  @Override
  public String getName() {
    return "dictsort";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if(var == null || !Map.class.isAssignableFrom(var.getClass())) {
      return var;
    }
    
    boolean caseSensitive = false;
    if(args.length > 0) {
      caseSensitive = BooleanUtils.toBoolean(args[0]);
    }
    
    boolean sortByKey = true;
    if(args.length > 1) {
      sortByKey = "value".equalsIgnoreCase(args[1]);
    }
    
    @SuppressWarnings("unchecked")
    Map<String, Object> dict = (Map<String, Object>) var;
    
    List<Map.Entry<String, Object>> sorted = Lists.newArrayList(dict.entrySet());
    Collections.sort(sorted, new MapEntryComparator(caseSensitive, sortByKey));
    
    return sorted;
  }

  private static class MapEntryComparator implements Comparator<Map.Entry<String, Object>> {
    private final boolean caseSensitive;
    private final boolean sortByKey;
    
    public MapEntryComparator(boolean caseSensitive, boolean sortByKey) {
      this.caseSensitive = caseSensitive;
      this.sortByKey = sortByKey;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public int compare(Entry<String, Object> o1, Entry<String, Object> o2) {
      Object sVal1 = sortByKey ? o1.getKey() : o1.getValue();
      Object sVal2 = sortByKey ? o2.getKey() : o2.getValue();
      
      int result = 0;
      
      if(!caseSensitive && sVal1 instanceof String && sVal2 instanceof String) {
        result = ((String) sVal1).compareToIgnoreCase((String) sVal2);
      }
      else if(Comparable.class.isAssignableFrom(sVal1.getClass()) && Comparable.class.isAssignableFrom(sVal2.getClass())) {
        result = ((Comparable<Object>) sVal1).compareTo(sVal2);
      }
      
      return result;
    }
    
  }
  
}
