package com.hubspot.jinjava.lib.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;


@JinjavaDoc(
    value="A filter that batches items. It works pretty much like slice just the other way round. "
        + "It returns a list of lists with the given number of items. If you provide a second parameter "
        + "this is used to fill up missing items.",
    params={
        @JinjavaParam("value"),
        @JinjavaParam(value="linecount", type="number", desc="lines to break into", defaultValue="0"),
        @JinjavaParam(value="fill_with", desc="used to fill up missing items")
    },
    snippets={
        @JinjavaSnippet(
            code="{% set items=[1, 2, 3, 4, 5] %}\n\n" +
                "<table>\n" +
                "{%- for row in items|batch(3, '&nbsp;') %}\n" +
                "  <tr>\n" +
                "  {%- for column in row %}\n" +
                "    <td>{{ column }}</td>\n" +
                "  {%- endfor %}\n" +
                "  </tr>\n" +
                "{%- endfor %}\n" +
                "</table>",
            output="<table>\n" +
                "  <tr>\n" +
                "    <td>1</td>\n" +
                "    <td>2</td>\n" +
                "    <td>3</td>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "    <td>4</td>\n" +
                "    <td>5</td>\n" +
                "    <td>&nbsp;</td>\n" +
                "  </tr>\n" +
                "</table>")
    }
)
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
