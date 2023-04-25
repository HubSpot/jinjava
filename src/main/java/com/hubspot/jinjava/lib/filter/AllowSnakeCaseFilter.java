package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.collections.PyMap;
import com.hubspot.jinjava.objects.collections.SizeLimitingPyMap;
import com.hubspot.jinjava.objects.collections.SnakeCaseAccessibleMap;
import java.util.Map;

@JinjavaDoc(
  value = "Allow keys on the provided camelCase map to be accessed using snake_case",
  input = @JinjavaParam(
    value = "map",
    type = "dict",
    desc = "The dict to make keys accessible using snake_case",
    required = true
  ),
  snippets = { @JinjavaSnippet(code = "{{ {'fooBar': 'baz'}|allow_snake_case }}") }
)
public class AllowSnakeCaseFilter implements Filter {
  public static final String NAME = "allow_snake_case";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if (!(var instanceof Map)) {
      return var;
    }
    Map<String, Object> map = (Map<String, Object>) var;
    if (map instanceof PyMap) {
      map = ((PyMap) map).toMap();
    }
    return new SnakeCaseAccessibleMap(
      new SizeLimitingPyMap(map, interpreter.getConfig().getMaxMapSize())
    );
  }
}
