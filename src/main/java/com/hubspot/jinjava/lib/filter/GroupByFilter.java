package com.hubspot.jinjava.lib.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;

@JinjavaDoc(
    value = "Group a sequence of objects by a common attribute.",
    params = {
        @JinjavaParam(value = "value", desc = "The dict to iterate through and group by a common attribute"),
        @JinjavaParam(value = "attribute", desc = "The common attribute to group by")
    },
    snippets = {
        @JinjavaSnippet(
            desc = "If you have a list of dicts or objects that represent persons with gender, first_name and last_name attributes and you want to group all users by genders you can do something like this",
            code = "<ul>\n" +
                "    {% for group in contents|groupby('blog_post_author') %}\n" +
                "        <li>{{ group.grouper }}<ul>\n" +
                "            {% for content in group.list %}\n" +
                "                <li>{{ content.name }}</li>\n" +
                "            {% endfor %}</ul></li>\n" +
                "     {% endfor %}\n" +
                "</ul>")
    })
public class GroupByFilter implements Filter {

  @Override
  public String getName() {
    return "groupby";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if (args.length == 0) {
      throw new InterpretException(getName() + " requires an attr name to group on", interpreter.getLineNumber());
    }

    String attr = args[0];

    ForLoop loop = ObjectIterator.getLoop(var);
    Multimap<String, Object> groupBuckets = ArrayListMultimap.create();

    while (loop.hasNext()) {
      Object val = loop.next();

      String grouper = Objects.toString(interpreter.resolveProperty(val, attr));
      groupBuckets.put(grouper, val);
    }

    List<Group> groups = new ArrayList<>();
    for (String grouper : groupBuckets.keySet()) {
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
