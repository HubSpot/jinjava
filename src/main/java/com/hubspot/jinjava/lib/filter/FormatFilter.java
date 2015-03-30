package com.hubspot.jinjava.lib.filter;

import java.util.Objects;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value="apply string formatting to an object",
    params={
        @JinjavaParam("value"),
        @JinjavaParam(value="args", type="String...")
    })
public class FormatFilter implements Filter {

  @Override
  public String getName() {
    return "format";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    String fmt = Objects.toString(var, "");
    return String.format(fmt, (Object[]) args);
  }

}
