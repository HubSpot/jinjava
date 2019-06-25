package com.hubspot.jinjava.lib.filter;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;

import java.util.*;

@JinjavaDoc(
    value = "Group a sequence of objects by a common attribute.",
    input = @JinjavaParam(value = "value", desc = "The dict to iterate through and group by a common attribute", required = true),
    params = {
        @JinjavaParam(value = "attribute", desc = "The common attribute to group by", required = true)
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

    if (args.length < 1) {
      throw new TemplateSyntaxException(interpreter, getName(), "requires 1 argument (attr name to group by)");
    }

    String attr = args[0];

    ForLoop loop = ObjectIterator.getLoop(var);
    Multimap<String, Object> groupBuckets = LinkedListMultimap.create();
    Map<String, Object> groupMapRaw = new HashMap<>();

    while (loop.hasNext()) {
      Object val = loop.next();

      Object resolvedProperty = interpreter.resolveProperty(val, attr);
      String grouper = Objects.toString(resolvedProperty);
      groupBuckets.put(grouper, val);

      if (!groupMapRaw.containsKey(grouper)) {
        groupMapRaw.put(grouper, resolvedProperty);
      }
    }

    List<Group> groups = new ArrayList<>();
    for (String grouper : groupBuckets.keySet()) {
      List<Object> list = Lists.newArrayList(groupBuckets.get(grouper));
      groups.add(new Group(grouper, groupMapRaw.get(grouper), list));
    }

    return groups;
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, Object... args) {
    return null;
  }

  public static class Group {
    private final String grouper;
    private final Object grouperObject;
    private final List<Object> list;

    public Group(String grouper, Object grouperObject, List<Object> list) {
      this.grouper = grouper;
      this.grouperObject = grouperObject;
      this.list = list;
    }

    public String getGrouper() {
      return grouper;
    }

    public Object getGrouperObject() {
      return grouperObject;
    }

    public List<Object> getList() {
      return list;
    }
  }

}
