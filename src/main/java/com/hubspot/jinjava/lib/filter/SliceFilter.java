package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JinjavaDoc(
  value = "Slice an iterator and return a list of lists containing those items.",
  input = @JinjavaParam(
    value = "value",
    type = "sequence",
    desc = "The sequence or dict that the filter is applied to",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = SliceFilter.SLICES_PARAM,
      type = "int",
      desc = "Specifies how many items will be sliced",
      required = true
    ),
    @JinjavaParam(
      value = SliceFilter.FILL_WITH_PARAM,
      type = "object",
      desc = "Specifies which object to use to fill missing values on final iteration",
      required = false
    )
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
      "</div>\n"
    )
  }
)
public class SliceFilter extends AbstractFilter implements Filter {
  public static final String SLICES_PARAM = "slices";
  public static final String FILL_WITH_PARAM = "fillWith";

  @Override
  public String getName() {
    return "slice";
  }

  @Override
  public Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Map<String, Object> parsedArgs
  ) {
    ForLoop loop = ObjectIterator.getLoop(var);

    int slices = (int) parsedArgs.get(SLICES_PARAM);
    Object fillWith = parsedArgs.get(FILL_WITH_PARAM);
    if (slices <= 0) {
      throw new InvalidArgumentException(
        interpreter,
        this,
        InvalidReason.POSITIVE_NUMBER,
        0,
        slices
      );
    }
    List<List<Object>> result = new ArrayList<>();

    List<Object> currentList = null;
    int i = 0;
    while (loop.hasNext()) {
      Object next = loop.next();
      if (i % slices == 0) {
        currentList = new ArrayList<>(slices);
        result.add(currentList);
      }
      currentList.add(next);
      i++;
    }

    if (fillWith != null && currentList != null) {
      while (currentList.size() < slices) {
        currentList.add(fillWith);
      }
    }

    return result;
  }
}
