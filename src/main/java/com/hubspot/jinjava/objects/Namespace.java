package com.hubspot.jinjava.objects;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * It is a java version of a namespace functionality in jinja2.
 *
 * @author dominik symonowicz
 */
public class Namespace {

  Map<String,Object> variables = new HashMap<>();

  public void put(String name, Object value){
    variables.put(name,value);
  }

  public Object get(String name){
    return variables.getOrDefault(name,"");
  }

  public Map<String,Object> getAsDictionary(){
    return variables;
  }

  public boolean contains(String name) {
    return variables.containsKey(name);
  }

}
