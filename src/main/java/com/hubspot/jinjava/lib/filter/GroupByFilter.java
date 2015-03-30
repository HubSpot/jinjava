package com.hubspot.jinjava.lib.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;
import com.hubspot.jinjava.util.VariableChain;


@JinjavaDoc(
    value="Group a sequence of objects by a common attribute.\n\n" +
          "If you for example have a list of dicts or objects that represent persons with gender, first_name and last_name attributes and you want to group all users by genders you can do something like the following snippet:\n" +
          "<ul>\n" +
          "{% for group in persons|groupby('gender') %}\n" +
          "    <li>{{ group.grouper }}<ul>\n" +
          "    {% for person in group.list %}\n" +
          "        <li>{{ person.first_name }} {{ person.last_name }}</li>\n" +
          "    {% endfor %}</ul></li>\n" +
          "{% endfor %}\n" +
          "</ul>\n" +
          "Additionally it’s possible to use tuple unpacking for the grouper and list:\n\n" +
          "<ul>\n" +
          "{% for grouper, list in persons|groupby('gender') %}\n" +
          "    ...\n" +
          "{% endfor %}\n" +
          "</ul>\n" +
          "As you can see the item we’re grouping by is stored in the grouper attribute and the list contains all the objects that have this grouper in common.\n\n" +
          "It’s possible to use dotted notation to group by the child attribute of another attribute.",
    params={
        @JinjavaParam("value"),
        @JinjavaParam("attribute")
    })
public class GroupByFilter implements Filter {

  @Override
  public String getName() {
    return "groupby";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if(args.length == 0) {
      throw new InterpretException(getName() + " requires an attr name to group on", interpreter.getLineNumber());
    }
    
    String attr = args[0];
    
    ForLoop loop = ObjectIterator.getLoop(var);
    Multimap<String, Object> groupBuckets = ArrayListMultimap.create();

    while(loop.hasNext()) {
      Object val = loop.next();
      
      String grouper = Objects.toString(new VariableChain(Lists.newArrayList(attr), val).resolve());
      groupBuckets.put(grouper, val);
    }
    
    List<Group> groups = new ArrayList<>();
    for(String grouper : groupBuckets.keySet()) {
      List<Object> list = Lists.newArrayList(groupBuckets.get(grouper));
      groups.add(new Group(grouper, list));
    }
    
    return groups;
  }

  public static class Group {
    private final String grouper;
    private final List<Object> list;
    
    public Group(String grouper, List<Object> list) {
      this.grouper = grouper;
      this.list = list;
    }
    
    public String getGrouper() {
      return grouper;
    }
    public List<Object> getList() {
      return list;
    }
  }
  
}
