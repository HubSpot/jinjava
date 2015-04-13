package com.hubspot.jinjava.lib.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;


@JinjavaDoc(
    value="Create an SGML/XML attribute string based on the items in a dict. All values that are neither none "
        + "nor undefined are automatically escaped. It automatically prepends a space in front of the item if "
        + "the filter returned something unless the second parameter is false.",
    params={
        @JinjavaParam(value="d", type="dict"),
        @JinjavaParam(value="autospace", type="boolean", defaultValue="True", desc="automatically prepend a space in front of the item")
    },
    snippets={
        @JinjavaSnippet(
            code="<ul{{ {'class': 'my_list', 'missing': none,\n" +
                "        'id': 'list-%d'|format(variable)}|xmlattr }}>\n" +
                "...\n" +
                "</ul>",
            output="<ul class=\"my_list\" id=\"list-42\">\n" +
                "...\n" +
                "</ul>")
    })
public class XmlAttrFilter implements Filter {

  @Override
  public String getName() {
    return "xmlattr";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if(var == null || !Map.class.isAssignableFrom(var.getClass())) {
      return var;
    }
    
    @SuppressWarnings("unchecked")
    Map<String, Object> dict = (Map<String, Object>) var;
    List<String> attrs = new ArrayList<>();

    for(Map.Entry<String, Object> entry : dict.entrySet()) {
      attrs.add(new StringBuilder(entry.getKey())
        .append("=\"")
        .append(StringEscapeUtils.escapeXml10(Objects.toString(entry.getValue(), "")))
        .append("\"")
        .toString());
    }
    
    String space = " ";
    if(args.length > 0 && !BooleanUtils.toBoolean(args[0])) {
      space = "";
    }
    
    return space + StringUtils.join(attrs, "\n");
  }

}
