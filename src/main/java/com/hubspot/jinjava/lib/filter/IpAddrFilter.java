package com.hubspot.jinjava.lib.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.SubnetUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Evaluates to true if the value is a valid IPv4 or IPv6 address",
    input = @JinjavaParam(value = "value", type = "string", desc = "String to check IP Address", required = true),
    params = {
        @JinjavaParam(value = "function", type = "string", defaultValue = "prefix", desc = "Name of function. " +
                "Supported functions: 'prefix', 'netmask', 'network', 'address', 'broadcast'"),
    },
    snippets = {
        @JinjavaSnippet(
            desc = "This example shows how to test if a string is a valid ip address",
            code = "{% set ip = '1.0.0.1' %}\n" +
                "{% if ip|ipaddr %}\n" +
                "    The string is a valid IP address\n" +
                "{% endif %}"),
        @JinjavaSnippet(
            desc = "This example shows how to filter list of ip addresses",
            code = "{{ ['192.108.0.1', null, True, 13, '2000::'] | ipaddr }}",
            output = "['192.108.0.1', '2000::']"
        )
    })
public class IpAddrFilter implements Filter {

  private static final Pattern IP4_PATTERN = Pattern.compile("(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])");
  private static final Pattern IP6_PATTERN = Pattern.compile("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");
  private static final Pattern IP6_COMPRESSED_PATTERN = Pattern.compile("^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$");

  private static final Splitter PREFIX_SPLITTER = Splitter.on('/');
  private static final String PREFIX_STRING = "prefix";
  private static final String NETMASK_STRING = "netmask";
  private static final String ADDRESS_STRING = "address";
  private static final String BROADCAST_STRING = "broadcast";
  private static final String NETWORK_STRING = "network";
  private static final String AVAILABLE_FUNCTIONS = Joiner.on(", ").join(
      PREFIX_STRING,
      NETMASK_STRING,
      ADDRESS_STRING,
      BROADCAST_STRING,
      NETWORK_STRING);

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... args) {

    if (object == null) {
      return false;
    }

    String parameter = getParameter(args);
    if (object instanceof List) {
      return getFilteredItems(interpreter, parameter, object);
    }

    if (object instanceof String) {
      return getValue(interpreter, object, parameter);
    }

    return false;
  }

  private String getParameter(String... args) {
    if (args.length > 0) {
      return args[0].trim();
    }

    return null;
  }

  private List<String> getFilteredItems(JinjavaInterpreter interpreter, String parameter, Object object) {

    List<String> filteredItems = new ArrayList<>();
    for (Object item : (List) object) {
      Object value;
      try {
        value = getValue(interpreter, item, parameter);
      } catch (InvalidArgumentException exception) {
        continue;
      }

      if (value instanceof String || value instanceof Integer) {
        filteredItems.add(String.valueOf(value));
      }

      if (value instanceof Boolean && (Boolean) value) {
        filteredItems.add((String) item);
      }
    }

    return filteredItems;
  }

  private Object getValue(JinjavaInterpreter interpreter, Object object, String parameter) {

    if (!(object instanceof String)) {
      return null;
    }

    String fullAddress = ((String) object).trim();
    if (StringUtils.isEmpty(parameter)) {
      return validIp(fullAddress);
    }

    List<String> parts = PREFIX_SPLITTER.splitToList(fullAddress);
    if (parts.size() != 2) {
      return null;
    }

    String ipAddress = parts.get(0);
    if (!validIp(ipAddress)) {
      return null;
    }

    if (parameter.equalsIgnoreCase(ADDRESS_STRING)) {
      return ipAddress;
    }

    String prefixString = parts.get(1);
    Integer prefix;
    try {
      prefix = Integer.parseInt(prefixString);
    } catch (NumberFormatException ex) {
      throw new InvalidArgumentException(interpreter, this, InvalidReason.NUMBER_FORMAT, 0, prefixString);
    }

    switch (parameter) {
      case PREFIX_STRING:
        return prefix;
      case NETMASK_STRING:
        return getSubnetUtils(interpreter, fullAddress).getInfo().getNetmask();
      case BROADCAST_STRING:
        return getSubnetUtils(interpreter, fullAddress).getInfo().getBroadcastAddress();
      case NETWORK_STRING:
        return getSubnetUtils(interpreter, fullAddress).getInfo().getNetworkAddress();
      default:
        throw new InvalidArgumentException(interpreter, this, InvalidReason.ENUM, 1, parameter, AVAILABLE_FUNCTIONS);
    }
  }

  private SubnetUtils getSubnetUtils(JinjavaInterpreter interpreter, String address) {
    try {
      return new SubnetUtils(address);
    } catch (IllegalArgumentException e) {
      throw new InvalidArgumentException(interpreter, this, InvalidReason.CIDR, 0, address);
    }
  }

  protected boolean validIp(String address) {
    return validIpv4(address) || validIpv6(address);
  }

  protected boolean validIpv4(String address) {
    return IP4_PATTERN.matcher(address).matches();
  }

  protected boolean validIpv6(String address) {
    return IP6_PATTERN.matcher(address).matches() || IP6_COMPRESSED_PATTERN.matcher(address).matches();
  }

  @Override
  public String getName() {
    return "ipaddr";
  }

}
