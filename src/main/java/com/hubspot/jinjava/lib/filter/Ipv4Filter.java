package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;

@JinjavaDoc(
  value = "Evaluates to true if the value is a valid IPv4 address",
  input = @JinjavaParam(
    value = "value",
    type = "string",
    desc = "String to check IPv4 Address",
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
      desc = "This example shows how to test if a string is a valid ipv4 address",
      code = "{% set ip = '192.108.0.' %}\n" +
      "{% if ip|ipv4 %}\n" +
      "    The string is a valid IPv4 address\n" +
      "{% endif %}"
    ),
    @JinjavaSnippet(
      desc = "This example shows how to filter list of ipv4 addresses",
      code = "{{ ['192.108.0.1', null, True, 13, '2000::'] | ipv4 }}",
      output = "['192.108.0.']"
    ),
  }
)
public class Ipv4Filter extends IpAddrFilter {

  @Override
  protected boolean validIp(String address) {
    return validIpv4(address);
  }

  @Override
  public String getName() {
    return "ipv4";
  }
}
