package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

@JinjavaDoc(
  value = "Create an HTML/XML attribute string based on the items in a dict.",
  input = @JinjavaParam(
    value = "dict",
    type = "dict",
    desc = "Dict to filter",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = "autospace",
      type = "boolean",
      defaultValue = "True",
      desc = "Automatically prepend a space in front of the item"
    ),
  },
  snippets = {
    @JinjavaSnippet(
      code = "{% set html_attributes = {'class': 'bold', 'id': 'sidebar'} %}\n" +
      "<div {{ html_attributes|xmlattr }}></div>"
    ),
  }
)
public class XmlAttrFilter implements Filter {

  // See https://html.spec.whatwg.org/#attribute-name-state Don't allow characters that would change the attribute name/value state
  private static final Pattern ILLEGAL_ATTRIBUTE_KEY_PATTERN = Pattern.compile(
    "[\\s/>=]"
  );

  @Override
  public String getName() {
    return "xmlattr";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if (var == null || !Map.class.isAssignableFrom(var.getClass())) {
      return var;
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> dict = (Map<String, Object>) var;
    List<String> attrs = new ArrayList<>();

    for (Map.Entry<String, Object> entry : dict.entrySet()) {
      if (ILLEGAL_ATTRIBUTE_KEY_PATTERN.matcher(entry.getKey()).find()) {
        throw new IllegalArgumentException(
          String.format("Invalid character in attribute name: %s", entry.getKey())
        );
      }
      attrs.add(
        new StringBuilder(entry.getKey())
          .append("=\"")
          .append(StringEscapeUtils.escapeXml10(Objects.toString(entry.getValue(), "")))
          .append("\"")
          .toString()
      );
    }

    String space = " ";
    if (args.length > 0 && !BooleanUtils.toBoolean(args[0])) {
      space = "";
    }

    return space + StringUtils.join(attrs, "\n");
  }
}
