package com.hubspot.jinjava.objects;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * It is a java version of a class namespace that will help stores variables globally.
 *
 * @author dominik symonowicz
 */
public class GlobalVariables {

    Map<String,Object> variables = new HashMap<>();

    public void setVariable(String name, Object value){
        variables.put(name,value);
    }

    public Object getVariableFor(String name){
        return variables.getOrDefault(name,"");
    }

    public void reset(){
        variables.clear();
    }

    public int size(){
        return variables.size();
    }

    public boolean contains(String name) {
        return variables.containsKey(name);
    }
}
