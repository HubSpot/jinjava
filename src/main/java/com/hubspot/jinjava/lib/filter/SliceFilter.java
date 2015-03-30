package com.hubspot.jinjava.lib.filter;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.collect.Iterators;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;


@JinjavaDoc(
    value="Slice an iterator and return a list of lists containing those items. Useful if you want to create a div containing three ul tags that represent columns:\n\n" +
          
          "<div class=\"columwrapper\">\n" +
          "  {%- for column in items|slice(3) %}\n" +
          "    <ul class=\"column-{{ loop.index }}\">\n" +
          "    {%- for item in column %}\n" +
          "      <li>{{ item }}</li>\n" +
          "    {%- endfor %}\n" +
          "    </ul>\n" +
          "  {%- endfor %}\n" +
          "</div>\n" +
          "If you pass it a second argument it’s used to fill missing values on the last iteration.",
    params={
        @JinjavaParam(value="value", type="sequence"),
        @JinjavaParam(value="slices", type="number"),
        @JinjavaParam(value="fill_with", desc="used to fill missing values on the last iteration")
    })
public class SliceFilter implements Filter {

  @Override
  public String getName() {
    return "slice";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    ForLoop loop = ObjectIterator.getLoop(var);
    
    if(args.length == 0) {
      throw new InterpretException(getName() + " requires number of slices argument", interpreter.getLineNumber());
    }
    
    int slices = NumberUtils.toInt(args[0], 3);
    return Iterators.paddedPartition(loop, slices);
  }

}
