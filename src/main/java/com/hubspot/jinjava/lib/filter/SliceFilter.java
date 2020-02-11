package com.hubspot.jinjava.lib.filter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;

@JinjavaDoc(
    value = "Slice an iterator and return a list of lists containing those items.",
    input = @JinjavaParam(value = "value", type = "sequence", desc = "The sequence or dict that the filter is applied to", required = true),
    params = {
        @JinjavaParam(value = "slices", type = "number", desc = "Specifies how many items will be sliced", required = true),
    },
    snippets = {
        @JinjavaSnippet(
            desc = "create a div containing three ul tags that represent columns",
            code = "{% set items = ['laptops', 'tablets', 'smartphones', 'smart watches', 'TVs'] %}\n" +
                "<div class=\"columwrapper\">\n" +
                "  {% for column in items|slice(3) %}\n" +
                "    <ul class=\"column-{{ loop.index }}\">\n" +
                "    {% for item in column %}\n" +
                "      <li>{{ item }}</li>\n" +
                "    {% endfor %}\n" +
                "    </ul>\n" +
                "  {% endfor %}\n" +
                "</div>\n")
    })
public class SliceFilter implements Filter {

  @Override
  public String getName() {
    return "slice";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    ForLoop loop = ObjectIterator.getLoop(var);

    if (args.length < 1) {
      throw new TemplateSyntaxException(interpreter, getName(), "requires 1 argument (number of slices)");
    }

    int slices = NumberUtils.toInt(args[0], 3);
    List<List<Object>> result = new ArrayList<>();

    List<Object> currentList = null;
    int i = 0;
    while(loop.hasNext()) {
      Object next = loop.next();
      if (i % slices == 0) {
        currentList = new ArrayList<>(slices);
        result.add(currentList);
      }
      currentList.add(next);
      i++;
    }

    if (args.length > 1 && currentList != null) {
      while (currentList.size() < slices) {
        currentList.add(args[1]);
      }
    }

    return result;
  }

}
