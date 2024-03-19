package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.math.NumberUtils;

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
      value = "slices",
      type = "number",
      desc = "Specifies how many items will be sliced. Maximum value is " +
      SliceFilter.MAX_SLICES +
      ". ",
      required = true
    ),
    @JinjavaParam(
      value = "fillWith",
      type = "object",
      desc = "Specifies which object to use to fill missing values on final iteration",
      required = false
    ),
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
    ),
  }
)
public class SliceFilter implements Filter {

  public static final int MAX_SLICES = 1000;

  @Override
  public String getName() {
    return "slice";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    ForLoop loop = ObjectIterator.getLoop(var);

    if (args.length < 1) {
      throw new TemplateSyntaxException(
        interpreter,
        getName(),
        "requires 1 argument (number of slices)"
      );
    }

    int slices = NumberUtils.toInt(args[0], 3);
    if (slices <= 0) {
      throw new InvalidArgumentException(
        interpreter,
        this,
        InvalidReason.POSITIVE_NUMBER,
        0,
        args[0]
      );
    } else if (slices > MAX_SLICES) {
      interpreter.addError(
        new TemplateError(
          TemplateError.ErrorType.WARNING,
          TemplateError.ErrorReason.OVER_LIMIT,
          TemplateError.ErrorItem.FILTER,
          "The limit input value of 'slices' parameter is too large, it's been reduced to " +
          MAX_SLICES,
          null,
          interpreter.getLineNumber(),
          interpreter.getPosition(),
          null
        )
      );
      slices = MAX_SLICES;
    }

    List<List<Object>> result = new ArrayList<>();
    List<Object> currentList = null;

    int i = 0;
    while (loop.hasNext()) {
      if (i % slices == 0) {
        if (currentList != null) {
          result.add(currentList);
        }
        currentList = new ArrayList<>();
      }
      currentList.add(loop.next());
      i++;
    }

    if (currentList != null && !currentList.isEmpty()) {
      result.add(currentList);
    }

    if (args.length > 1 && currentList != null) {
      Object fillWith = args[1];
      while (currentList.size() < slices) {
        currentList.add(fillWith);
      }
    }

    return result;
  }
}
