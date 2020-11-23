package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

@JinjavaDoc(
  value = "Converts URLs in plain text into clickable links.",
  input = @JinjavaParam(
    value = "value",
    type = "string",
    desc = "string URL to convert to an anchor",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = UrlizeFilter.TRIM_URL_LIMIT_KEY,
      type = "int",
      desc = "Sets a character limit",
      defaultValue = "2147483647"
    ),
    @JinjavaParam(
      value = UrlizeFilter.NO_FOLLOW_KEY,
      type = "boolean",
      defaultValue = "False",
      desc = "Adds nofollow to generated link tag"
    ),
    @JinjavaParam(
      value = UrlizeFilter.TARGET_KEY,
      desc = "Adds target attr to generated link tag"
    )
  },
  snippets = {
    @JinjavaSnippet(
      desc = "links are shortened to 40 chars and defined with rel=\"nofollow\"",
      code = "{{ \"http://www.hubspot.com\"|urlize(40) }}"
    ),
    @JinjavaSnippet(
      desc = "If target is specified, the target attribute will be added to the <a> tag",
      code = "{{ \"http://www.hubspot.com\"|urlize(10, true, target='_blank') }}"
    )
  }
)
public class UrlizeFilter extends AbstractFilter implements Filter {
  public static final String TRIM_URL_LIMIT_KEY = "trim_url_limit";
  public static final String NO_FOLLOW_KEY = "nofollow";
  public static final String TARGET_KEY = "target";

  @Override
  public String getName() {
    return "urlize";
  }

  @Override
  public Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Map<String, Object> parsedArgs
  ) {
    Matcher m = URL_RE.matcher(Objects.toString(var, ""));
    StringBuffer result = new StringBuffer();

    int trimUrlLimit = ((int) parsedArgs.get(TRIM_URL_LIMIT_KEY));

    String fmt = "<a href=\"%s\"";

    boolean nofollow = (boolean) parsedArgs.get(NO_FOLLOW_KEY);

    String target = (String) parsedArgs.get(TARGET_KEY);

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
    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
  );
}
