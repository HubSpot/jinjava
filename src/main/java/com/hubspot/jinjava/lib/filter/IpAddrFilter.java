package com.hubspot.jinjava.lib.filter;

import java.util.regex.Pattern;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Evaluates to true if the value is a valid IPv4 or IPv6 address",
    params = {
        @JinjavaParam(value = "value", type = "string", desc = "String to check IP Address"),
    },
    snippets = {
        @JinjavaSnippet(
            desc = "This example is an alternative to using the is divisibleby expression test",
            code = "{% set ip = '1.0.0.1' %}\n" +
                "{% if ip|ipaddr %}\n" +
                "    The string is a valid IP address\n" +
                "{% endif %}")
    })
public class IpAddrFilter implements Filter {

  private static final Pattern IP4_PATTERN = Pattern.compile("(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])");
  private static final Pattern IP6_PATTERN = Pattern.compile("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");
  private static final Pattern IP6_COMPRESSED_PATTERN = Pattern.compile("^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$");

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... arg) {

    if (object == null) {
      return false;
    }

    if (object instanceof String) {
      String address = (String) object;
      return IP4_PATTERN.matcher(address).matches()
          || IP6_PATTERN.matcher(address).matches()
          || IP6_COMPRESSED_PATTERN.matcher(address).matches();
    }
    return false;
  }

  @Override
  public String getName() {
    return "ipaddr";
  }

}
