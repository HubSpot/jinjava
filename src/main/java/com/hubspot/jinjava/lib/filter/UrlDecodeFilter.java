package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

@JinjavaDoc(
  value = "Decodes encoded URL strings back to the original URL. It accepts both dictionaries and regular strings as well as pairwise iterables.",
  input = @JinjavaParam(
    value = "url",
    type = "string",
    desc = "the url to decode",
    required = true
  ),
  snippets = {
    @JinjavaSnippet(
      code = "{{ \"http%3A%2F%2Ffoo.com%3Fbar%26food\"|urldecode }}",
      output = "http://foo.com?bar&food"
    )
  }
)
public class UrlDecodeFilter implements Filter {

  @Override
  public String getName() {
    return "urldecode";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if (var == null && args.length == 0) {
      return "";
    }

    if (var != null) {
      if (Map.class.isAssignableFrom(var.getClass())) {
        @SuppressWarnings("unchecked")
        Map<Object, Object> dict = (Map<Object, Object>) var;

        List<String> paramPairs = new ArrayList<>();

        for (Map.Entry<Object, Object> param : dict.entrySet()) {
          StringBuilder paramPair = new StringBuilder();
          paramPair.append(urlDecode(Objects.toString(param.getKey())));
          paramPair.append("=");
          paramPair.append(urlDecode(Objects.toString(param.getValue())));

          paramPairs.add(paramPair.toString());
        }

        return StringUtils.join(paramPairs, "&");
      }

      return urlDecode(var.toString());
    }

    return urlDecode(args[0]);
  }

  private String urlDecode(String s) {
    try {
      return URLDecoder.decode(s, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
