package com.hubspot.jinjava.lib.filter;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.googlecode.ipv6.IPv6Network;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.ObjectIterator;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.SubnetUtils;

@JinjavaDoc(
  value = "Evaluates to true if the value is a valid IPv4 or IPv6 address",
  input = @JinjavaParam(
    value = "value",
    type = "string",
    desc = "String to check IP Address",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = "function",
      type = "string",
      defaultValue = "prefix",
      desc = "Name of function. Supported functions: 'prefix', 'netmask', 'network', 'address', 'broadcast'"
    ),
  },
  snippets = {
    @JinjavaSnippet(
      desc = "This example shows how to test if a string is a valid ip address",
      code = "{% set ip = '1.0.0.1' %}\n" +
      "{% if ip|ipaddr %}\n" +
      "    The string is a valid IP address\n" +
      "{% endif %}"
    ),
    @JinjavaSnippet(
      desc = "This example shows how to filter list of ip addresses",
      code = "{{ ['192.108.0.1', null, True, 13, '2000::'] | ipaddr }}",
      output = "['192.108.0.1', '2000::']"
    ),
  }
)
public class IpAddrFilter implements Filter {

  private static final Pattern IP4_PATTERN = Pattern.compile(
    "((0*1?\\d\\d?|0*2[0-4]\\d|0*25[0-5])\\.){3}(0*1?\\d\\d?|0*2[0-4]\\d|0*25[0-5])(/0*([1-2]?\\d|3[0-2])?)?"
  );
  private static final Pattern IP6_PATTERN = Pattern.compile(
    "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}(/0*(\\d{1,2}|1[0-1]\\d|12[0-8])?)?$"
  );
  private static final Pattern IP6_COMPRESSED_PATTERN = Pattern.compile(
    "^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)" +
    "(/0*(\\d{1,2}|1[0-1]\\d|12[0-8])?)?$"
  );

  private static final Splitter PREFIX_SPLITTER = Splitter.on('/');
  private static final String PREFIX_STRING = "prefix";
  private static final String NETMASK_STRING = "netmask";
  private static final String ADDRESS_STRING = "address";
  private static final String BROADCAST_STRING = "broadcast";
  private static final String NETWORK_STRING = "network";
  private static final String GATEWAY_STRING = "gateway";
  private static final String PUBLIC_STRING = "public";
  private static final String PRIVATE_STRING = "private";
  private static final String AVAILABLE_FUNCTIONS = Joiner
    .on(", ")
    .join(
      PREFIX_STRING,
      NETMASK_STRING,
      ADDRESS_STRING,
      BROADCAST_STRING,
      NETWORK_STRING,
      GATEWAY_STRING,
      PUBLIC_STRING,
      PRIVATE_STRING
    );

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... args) {
    if (object == null) {
      return false;
    }

    String parameter = getParameter(args);
    if (object instanceof Map) {
      return getMapOfFilteredItems(interpreter, parameter, object);
    }

    if (object instanceof Iterable) {
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

  private Object getMapOfFilteredItems(
    JinjavaInterpreter interpreter,
    String parameter,
    Object map
  ) {
    Iterator<Map.Entry<Object, Object>> iterator = ((Map) map).entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<Object, Object> item = iterator.next();

      String filteredItem = filterItem(interpreter, parameter, item.getValue());
      if (filteredItem != null) {
        item.setValue(filteredItem);
      } else {
        iterator.remove();
      }
    }

    return map;
  }

  private List<String> getFilteredItems(
    JinjavaInterpreter interpreter,
    String parameter,
    Object object
  ) {
    List<String> filteredItems = new ArrayList<>();
    ForLoop loop = ObjectIterator.getLoop(object);

    while (loop.hasNext()) {
      String filteredItem = filterItem(interpreter, parameter, loop.next());
      if (filteredItem != null) {
        filteredItems.add(filteredItem);
      }
    }

    return filteredItems;
  }

  private String filterItem(
    JinjavaInterpreter interpreter,
    String parameter,
    Object item
  ) {
    Object value;
    try {
      value = getValue(interpreter, item, parameter);
    } catch (InvalidArgumentException exception) {
      return null;
    }

    if (value instanceof String || value instanceof Integer) {
      return String.valueOf(value);
    }

    if (value instanceof Boolean && (Boolean) value) {
      return (String) item;
    }

    return null;
  }

  private Object getValue(
    JinjavaInterpreter interpreter,
    Object object,
    String parameter
  ) {
    if (!(object instanceof String)) {
      return null;
    }

    String fullAddress = ((String) object).trim();
    if (StringUtils.isEmpty(parameter)) {
      return validIp(fullAddress);
    }

    List<String> parts = PREFIX_SPLITTER.splitToList(fullAddress);
    if (
      parts.size() == 1 &&
      (parameter.equalsIgnoreCase(PUBLIC_STRING) ||
        parameter.equalsIgnoreCase(PRIVATE_STRING))
    ) {
      parts = new ArrayList<>(parts);
      parts.add("0");
    }

    if (parts.size() > 2) {
      return null;
    }

    String ipAddress = parts.get(0);
    if (!validIp(ipAddress)) {
      return null;
    }

    if (parameter.equalsIgnoreCase(ADDRESS_STRING)) {
      return ipAddress;
    }

    if (parts.size() != 2) {
      return null;
    }

    String prefixString = parts.get(1);
    Integer prefix;
    try {
      prefix = Integer.parseInt(prefixString);
    } catch (NumberFormatException ex) {
      throw new InvalidArgumentException(
        interpreter,
        this,
        InvalidReason.NUMBER_FORMAT,
        0,
        prefixString
      );
    }

    boolean isv4 = IP4_PATTERN.matcher(ipAddress).matches();
    switch (parameter) {
      case PREFIX_STRING:
        return prefix;
      case NETMASK_STRING:
        return isv4
          ? getSubnetUtils(interpreter, fullAddress).getInfo().getNetmask()
          : getIpv6Network(interpreter, fullAddress).getNetmask().asAddress().toString();
      case BROADCAST_STRING:
        return isv4
          ? getSubnetUtils(interpreter, fullAddress).getInfo().getBroadcastAddress()
          : getIpv6Network(interpreter, fullAddress).getLast().toString();
      case NETWORK_STRING:
        return isv4
          ? getSubnetUtils(interpreter, fullAddress).getInfo().getNetworkAddress()
          : getIpv6Network(interpreter, fullAddress).toString().split("/")[0];
      case GATEWAY_STRING:
        return isv4
          ? getSubnetUtils(interpreter, fullAddress).getInfo().getLowAddress()
          : getIpv6Network(interpreter, fullAddress).getFirst().add(1).toString();
      case PUBLIC_STRING:
        return !isIpAddressPrivate(getInetAddress(ipAddress), isv4);
      case PRIVATE_STRING:
        return isIpAddressPrivate(getInetAddress(ipAddress), isv4);
      default:
        throw new InvalidArgumentException(
          interpreter,
          this,
          InvalidReason.ENUM,
          1,
          parameter,
          AVAILABLE_FUNCTIONS
        );
    }
  }

  private SubnetUtils getSubnetUtils(JinjavaInterpreter interpreter, String address) {
    try {
      return new SubnetUtils(address);
    } catch (IllegalArgumentException e) {
      throw new InvalidArgumentException(
        interpreter,
        this,
        InvalidReason.CIDR,
        0,
        address
      );
    }
  }

  private IPv6Network getIpv6Network(JinjavaInterpreter interpreter, String address) {
    try {
      return IPv6Network.fromString(address);
    } catch (IllegalArgumentException e) {
      throw new InvalidArgumentException(interpreter, this.getName(), e.getMessage());
    }
  }

  private InetAddress getInetAddress(String ipAddress) {
    try {
      return InetAddress.getByName(ipAddress);
    } catch (UnknownHostException e) {
      return null;
    }
  }

  private boolean isIpAddressPrivate(InetAddress inetAddress, boolean isv4) {
    if (inetAddress == null) {
      return false;
    }

    return isv4
      ? inetAddress.isSiteLocalAddress()
      : inetAddress.isSiteLocalAddress() ||
      inetAddress.isLinkLocalAddress() ||
      inetAddress.isLoopbackAddress();
  }

  protected boolean validIp(String address) {
    return validIpv4(address) || validIpv6(address);
  }

  protected boolean validIpv4(String address) {
    return IP4_PATTERN.matcher(address).matches();
  }

  protected boolean validIpv6(String address) {
    return (
      IP6_PATTERN.matcher(address).matches() ||
      IP6_COMPRESSED_PATTERN.matcher(address).matches()
    );
  }

  @Override
  public boolean preserveSafeString() {
    return false;
  }

  @Override
  public String getName() {
    return "ipaddr";
  }
}
