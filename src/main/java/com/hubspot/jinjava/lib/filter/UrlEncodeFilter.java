package com.hubspot.jinjava.lib.filter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Throwables;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Escape strings for use in URLs (uses UTF-8 encoding). It accepts both dictionaries and regular strings as well as pairwise iterables.",
    snippets = {
        @JinjavaSnippet(code = "{{ \"Escape & URL encode this string\"|urlencode }}")
    })
public class UrlEncodeFilter implements Filter {

  @Override
  public String getName() {
    return "urlencode";
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
          paramPair.append(urlEncode(Objects.toString(param.getKey())));
          paramPair.append("=");
          paramPair.append(urlEncode(Objects.toString(param.getValue())));

          paramPairs.add(paramPair.toString());
        }

        return StringUtils.join(paramPairs, "&");
      }

      return urlEncode(var.toString());
    }

    return urlEncode(args[0]);
  }

  private String urlEncode(String s) {
    try {
      return URLEncoder.encode(s, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw Throwables.propagate(e);
    }
  }

}
