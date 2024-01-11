package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;

@JinjavaDoc(
  value = "Evaluates to true if the value is a valid IPv6 address",
  input = @JinjavaParam(
    value = "value",
    type = "string",
    desc = "String to check IPv6 Address",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = "function",
      type = "string",
      defaultValue = "prefix",
      desc = "Name of function. " +
      "Supported functions: 'prefix', 'netmask', 'network', 'address', 'broadcast'"
    ),
  },
  snippets = {
    @JinjavaSnippet(
      desc = "This example shows how to test if a string is a valid ipv6 address",
      code = "{% set ip = '2000::' %}\n" +
      "{% if ip|ipv6 %}\n" +
      "    The string is a valid IPv6 address\n" +
      "{% endif %}"
    ),
    @JinjavaSnippet(
      desc = "This example shows how to filter list of ipv6 addresses",
      code = "{{ ['192.108.0.1', null, True, 13, '2000::'] | ipv6 }}",
      output = "['2000::']"
    ),
  }
)
public class Ipv6Filter extends IpAddrFilter {

  @Override
  protected boolean validIp(String address) {
    return validIpv6(address);
  }

  @Override
  public String getName() {
    return "ipv6";
  }
}
