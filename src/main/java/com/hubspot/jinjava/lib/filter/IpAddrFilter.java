package com.hubspot.jinjava.lib.filter;

import java.util.List;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;
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
            desc = "This example shows how to test if a string is a valid ip address",
            code = "{% set ip = '1.0.0.1' %}\n" +
                "{% if ip|ipaddr %}\n" +
                "    The string is a valid IP address\n" +
                "{% endif %}")
    })
public class IpAddrFilter implements Filter {

  private static final Pattern IP4_PATTERN = Pattern.compile("(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])");
  private static final Pattern IP6_PATTERN = Pattern.compile("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");
  private static final Pattern IP6_COMPRESSED_PATTERN = Pattern.compile("^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$");

  private static final Splitter PREFIX_SPLITTER = Splitter.on('/');
  private static final String PREFIX_STRING = "prefix";

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... args) {

    if (object == null) {
      return false;
    }

    if (args.length > 0) {
      String function = args[0].trim();
      if (function.equalsIgnoreCase(PREFIX_STRING)) {
        return getPrefix(object);
      }
    }

    if (object instanceof String) {
      return validIp(((String) object).trim());
    }

    return false;
  }

  private Integer getPrefix(Object object) {

    if (!(object instanceof String)) {
      return null;
    }

    String fullAddress = ((String) object).trim();

    List<String> parts = PREFIX_SPLITTER.splitToList(fullAddress);
    if (parts.size() != 2) {
      return null;
    }

    String ipAddress = parts.get(0);
    if (!validIp(ipAddress)) {
      return null;
    }

    String prefixString = parts.get(1);
    try {
      return Integer.parseInt(prefixString);
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private boolean validIp(String address) {
    return IP4_PATTERN.matcher(address).matches()
        || IP6_PATTERN.matcher(address).matches()
        || IP6_COMPRESSED_PATTERN.matcher(address).matches();
  }

  @Override
  public String getName() {
    return "ipaddr";
  }

}
