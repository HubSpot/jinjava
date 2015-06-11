package com.hubspot.jinjava.lib.filter;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Converts URLs in plain text into clickable links.",
    params = {
        @JinjavaParam(value = "value", desc = "string URL to convert to an anchor"),
        @JinjavaParam(value = "trim_url_limit", type = "number", desc = "Sets a character limit"),
        @JinjavaParam(value = "nofollow", type = "boolean", defaultValue = "False", desc = "Adds nofollow to generated link tag"),
        @JinjavaParam(value = "target", desc = "Adds target attr to generated link tag")
    },
    snippets = {
        @JinjavaSnippet(
            desc = "links are shortened to 40 chars and defined with rel=\"nofollow\"",
            code = "{{ \"http://www.hubspot.com\"|urlize(40) }}"),
        @JinjavaSnippet(
            desc = "If target is specified, the target attribute will be added to the <a> tag",
            code = "{{ \"http://www.hubspot.com\"|urlize(10, true, target='_blank') }}"),
    })
public class UrlizeFilter implements Filter {

  @Override
  public String getName() {
    return "urlize";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    Matcher m = URL_RE.matcher(Objects.toString(var, ""));
    StringBuffer result = new StringBuffer();

    int trimUrlLimit = Integer.MAX_VALUE;
    if (args.length > 0) {
      trimUrlLimit = NumberUtils.toInt(args[0], Integer.MAX_VALUE);
    }

    String fmt = "<a href=\"%s\"";

    boolean nofollow = false;
    if (args.length > 1) {
      nofollow = BooleanUtils.toBoolean(args[1]);
    }

    String target = "";
    if (args.length > 2) {
      target = args[2];
    }

    if (nofollow) {
      fmt += " rel=\"nofollow\"";
    }

    if (StringUtils.isNotBlank(target)) {
      fmt += " target=\"" + target + "\"";
    }

    fmt += ">%s</a>";

    while (m.find()) {
      String url = m.group();
      String urlShort = StringUtils.abbreviate(url, trimUrlLimit);

      m.appendReplacement(result, String.format(fmt, url, urlShort));
    }

    m.appendTail(result);
    return result.toString();
  }

  private static final Pattern URL_RE = Pattern.compile(
      "(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]",
      Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

}
