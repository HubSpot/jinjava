package com.hubspot.jinjava.lib.filter;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;

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
    if(args.length > 0) {
      trimUrlLimit = NumberUtils.toInt(args[0], Integer.MAX_VALUE);
    }
    
    String fmt = "<a href=\"%s\"";
    
    boolean nofollow = false;
    if(args.length > 1) {
      nofollow = BooleanUtils.toBoolean(args[1]);
    }
    
    String target = "";
    if(args.length > 2) {
      target = args[2];
    }
    
    if(nofollow) {
      fmt += " rel=\"nofollow\"";
    }

    if(StringUtils.isNotBlank(target)) {
      fmt += " target=\"" + target + "\"";
    }
    
    fmt += ">%s</a>";
    
    while(m.find()) {
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
