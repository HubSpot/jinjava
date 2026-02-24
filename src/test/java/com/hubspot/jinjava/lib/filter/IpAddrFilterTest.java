package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.objects.SafeString;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;

public class IpAddrFilterTest extends BaseInterpretingTest {

  private IpAddrFilter ipAddrFilter;
  private Ipv4Filter ipv4Filter;
  private Ipv6Filter ipv6Filter;

  @Before
  public void setup() {
    ipAddrFilter = new IpAddrFilter();
    ipv4Filter = new Ipv4Filter();
    ipv6Filter = new Ipv6Filter();
  }

  @Test
  public void itAcceptsValidIpV4Address() {
    assertThat(ipAddrFilter.filter("255.182.100.1", interpreter)).isEqualTo(true);
    assertThat(ipAddrFilter.filter("125.0.100.1", interpreter)).isEqualTo(true);
    assertThat(ipAddrFilter.filter("128.0.0.1", interpreter)).isEqualTo(true);
    assertThat(ipAddrFilter.filter("   128.0.0.1   ", interpreter)).isEqualTo(true);
  }

  @Test
  public void itRejectsInvalidIpV4Address() {
    assertThat(ipAddrFilter.filter("255.182.100.abc", interpreter)).isEqualTo(false);
    assertThat(ipAddrFilter.filter("125.512.100.1", interpreter)).isEqualTo(false);
    assertThat(ipAddrFilter.filter("125.512.100.1.1", interpreter)).isEqualTo(false);
    assertThat(ipAddrFilter.filter("125.512.100", interpreter)).isEqualTo(false);
    assertThat(ipAddrFilter.filter(104, interpreter)).isEqualTo(false);
  }

  @Test
  public void itAcceptsValidIpV6Address() {
    assertThat(
      ipAddrFilter.filter("1200:0000:AB00:1234:0000:2552:7777:1313", interpreter)
    )
      .isEqualTo(true);
    assertThat(ipAddrFilter.filter("21DA:D3:0:2F3B:2AA:FF:FE28:9C5A", interpreter))
      .isEqualTo(true);
    assertThat(ipAddrFilter.filter("2000::", interpreter)).isEqualTo(true);
  }

  @Test
  public void itRejectsInvalidIpV6Address() {
    assertThat(ipAddrFilter.filter("1200::AB00:1234::2552:7777:1313", interpreter))
      .isEqualTo(false);
    assertThat(
      ipAddrFilter.filter("1200:0000:AB00:1234:O000:2552:7777:1313", interpreter)
    )
      .isEqualTo(false);
    assertThat(ipAddrFilter.filter("1200::AB00:1234::2552:7777:1313:1232", interpreter))
      .isEqualTo(false);
    assertThat(ipAddrFilter.filter("321", interpreter)).isEqualTo(false);
  }

  @Test
  public void itReturnsIpv4AddressPrefix() {
    assertThat(ipAddrFilter.filter("255.182.100.1/24", interpreter, "prefix"))
      .isEqualTo(24);
  }

  @Test
  public void itReturnsIpv6AddressPrefix() {
    assertThat(
      ipAddrFilter.filter(
        "1200:0000:AB00:1234:0000:2552:7777:1313/43",
        interpreter,
        "prefix"
      )
    )
      .isEqualTo(43);
  }

  @Test
  public void itRejectsInvalidIpAddressPrefix() {
    assertThat(ipAddrFilter.filter("255.182.100.abc/24", interpreter, "prefix"))
      .isEqualTo(null);
  }

  @Test
  public void itReturnsIpv4AddressNetMask() {
    assertThat(ipAddrFilter.filter("255.182.100.1/10", interpreter, "netmask"))
      .isEqualTo("255.192.0.0");
  }

  @Test
  public void itReturnsIpv6AddressNetMask() {
    assertThat(
      ipAddrFilter.filter(
        "1200:0000:AB00:1234:0000:2552:7777:1313/43",
        interpreter,
        "netmask"
      )
    )
      .isEqualTo("ffff:ffff:ffe0::");
  }

  @Test
  public void itReturnsIpv4AddressBroadcast() {
    assertThat(ipAddrFilter.filter("192.168.0.1/20", interpreter, "broadcast"))
      .isEqualTo("192.168.15.255");
  }

  @Test
  public void itReturnsIpv6AddressBroadcast() {
    assertThat(
      ipAddrFilter.filter(
        "1200:0000:AB00:1234:0000:2552:7777:1313/43",
        interpreter,
        "broadcast"
      )
    )
      .isEqualTo("1200:0:ab1f:ffff:ffff:ffff:ffff:ffff");
  }

  @Test
  public void itReturnsIpv4AddressAddress() {
    assertThat(ipAddrFilter.filter("192.168.0.1/20", interpreter, "address"))
      .isEqualTo("192.168.0.1");
    assertThat(ipAddrFilter.filter("192.168.0.2", interpreter, "address"))
      .isEqualTo("192.168.0.2");
  }

  @Test
  public void itReturnsIpv6AddressAddress() {
    assertThat(
      ipAddrFilter.filter(
        "1200:0000:AB00:1234:0000:2552:7777:1313/43",
        interpreter,
        "address"
      )
    )
      .isEqualTo("1200:0000:AB00:1234:0000:2552:7777:1313");
    assertThat(
      ipAddrFilter.filter(
        "1200:0000:AB00:1234:0000:2552:7777:1314",
        interpreter,
        "address"
      )
    )
      .isEqualTo("1200:0000:AB00:1234:0000:2552:7777:1314");
  }

  @Test
  public void itReturnsIpv4AddressNetwork() {
    assertThat(ipAddrFilter.filter("192.168.0.1/20", interpreter, "network"))
      .isEqualTo("192.168.0.0");
  }

  @Test
  public void itReturnsIpv6AddressNetwork() {
    assertThat(
      ipAddrFilter.filter(
        "1200:0000:AB00:1234:0000:2552:7777:1313/43",
        interpreter,
        "network"
      )
    )
      .isEqualTo("1200:0:ab00::");
  }

  @Test
  public void itReturnsIpv4AddressGateway() {
    assertThat(ipAddrFilter.filter("192.168.0.10/20", interpreter, "gateway"))
      .isEqualTo("192.168.0.1");
  }

  @Test
  public void itReturnsIpv6AddressGateway() {
    assertThat(
      ipAddrFilter.filter(
        "1200:0000:AB00:1234:0000:2552:7777:1313/43",
        interpreter,
        "gateway"
      )
    )
      .isEqualTo("1200:0:ab00::1");
  }

  @Test
  public void itAddsErrorOnInvalidCidrAddress() {
    assertThatThrownBy(() ->
        ipAddrFilter.filter("192.168.0.1/200", interpreter, "broadcast")
      )
      .hasMessageContaining("must be a valid CIDR address");
  }

  @Test
  public void itAddsErrorOnInvalidFunctionName() {
    assertThatThrownBy(() ->
        ipAddrFilter.filter("192.168.0.1/20", interpreter, "notAFunction")
      )
      .hasMessageContaining("must be one of");
  }

  @Test
  public void itFiltersValidIpv4Addresses() {
    List<Object> validAddresses = Arrays.asList(
      "255.182.100.1",
      "   128.0.0.1   ",
      "192.168.1.0/000024",
      "0000192.0168.000000.1"
    );
    List<Object> invalidAddresses = Arrays.asList(
      "255.182.100.abc",
      "125.512.100.1",
      "125.512.100.1.1",
      "125.512.100",
      "192.168.1.1/33",
      104,
      "1200:0000:AB00:1234:0000:2552:7777:1313",
      "1200::AB00:1234::2552:7777:1313",
      "2000::",
      "321",
      null,
      true
    );
    List<Object> allAddresses = Stream
      .concat(validAddresses.stream(), invalidAddresses.stream())
      .collect(Collectors.toList());
    assertThat(ipv4Filter.filter(allAddresses, interpreter)).isEqualTo(validAddresses);
  }

  @Test
  public void itFiltersValidIpv6Addresses() {
    List<Object> validAddresses = Arrays.asList(
      "1200:0000:AB00:1234:0000:2552:7777:1313",
      "2000::",
      "2000::/99",
      "1200:0000:AB00:1234:0000:2552:7777:1313/0000021",
      "1200:0000:AB00:1234:0000:2552:7777:1313/128"
    );
    List<Object> invalidAddresses = Arrays.asList(
      "255.182.100.abc",
      "   128.0.0.1   ",
      "125.0.100.1",
      "125.512.100",
      104,
      "1200:0000:AB00:1234:O000:2552:7777:1313",
      "1200::AB00:1234::2552:7777:1313:1232",
      "1200:0000:AB00:1234:0000:2552:7777:1313/132",
      "321",
      null,
      true
    );
    List<Object> allAddresses = Stream
      .concat(validAddresses.stream(), invalidAddresses.stream())
      .collect(Collectors.toList());
    assertThat(ipv6Filter.filter(allAddresses, interpreter)).isEqualTo(validAddresses);
  }

  @Test
  public void itFiltersIpAddressesInMap() {
    Map<Integer, Object> validAddresses = new HashMap<>();
    validAddresses.put(1, "192.24.2.1");
    validAddresses.put(3, "192.168.32.0");
    validAddresses.put(6, "fe80::100");
    Map<Integer, Object> invalidAddresses = new HashMap<>();
    invalidAddresses.put(2, null);
    invalidAddresses.put(4, 13);
    invalidAddresses.put(5, true);

    Map<Integer, Object> allAddresses = new HashMap<>(validAddresses);
    allAddresses.putAll(invalidAddresses);
    assertThat(ipAddrFilter.filter(allAddresses, interpreter)).isEqualTo(validAddresses);
  }

  @Test
  public void itFiltersIpAddressesAddress() {
    List<Object> inputAddresses = Arrays.asList(
      "192.24.2.1",
      "host.fqdn",
      "::1",
      "192.168.32.0/24",
      "fe80::100/10",
      true,
      "",
      null,
      13
    );
    List<Object> expectedAddresses = Arrays.asList(
      "192.24.2.1",
      "::1",
      "192.168.32.0",
      "fe80::100"
    );
    assertThat(ipAddrFilter.filter(inputAddresses, interpreter, "address"))
      .isEqualTo(expectedAddresses);
  }

  @Test
  public void itFiltersIpAddressesPrefix() {
    List<Object> inputAddresses = Arrays.asList(
      "192.24.2.1",
      "host.fqdn",
      "::1",
      "192.168.32.0/24",
      "fe80::100/10",
      true,
      "",
      null,
      13
    );
    List<Object> expectedAddresses = Arrays.asList("24", "10");
    assertThat(ipAddrFilter.filter(inputAddresses, interpreter, "prefix"))
      .isEqualTo(expectedAddresses);
  }

  @Test
  public void itFiltersIpAddressesNetwork() {
    List<Object> inputAddresses = Arrays.asList(
      "192.24.2.1",
      "host.fqdn",
      "::1",
      "192.168.32.0/24",
      "fe80::100/10",
      true,
      null,
      13
    );
    List<Object> expectedAddresses = Arrays.asList("192.168.32.0", "fe80::");
    assertThat(ipAddrFilter.filter(inputAddresses, interpreter, "network"))
      .isEqualTo(expectedAddresses);
  }

  @Test
  public void itFiltersIpAddressesGateway() {
    List<Object> inputAddresses = Arrays.asList(
      "192.24.2.1",
      "host.fqdn",
      "::1",
      "192.168.32.0/24",
      "fe80::100/10",
      true,
      null,
      13
    );
    List<Object> expectedAddresses = Arrays.asList("192.168.32.1", "fe80::1");
    assertThat(ipAddrFilter.filter(inputAddresses, interpreter, "gateway"))
      .isEqualTo(expectedAddresses);
  }

  @Test
  public void itFiltersIpAddressesNetmask() {
    List<Object> inputAddresses = Arrays.asList(
      "192.24.2.1",
      "host.fqdn",
      "::1",
      "192.168.32.0/24",
      "fe80::100/10",
      true,
      null,
      13
    );
    List<Object> expectedAddresses = Arrays.asList("255.255.255.0", "ffc0::");
    assertThat(ipAddrFilter.filter(inputAddresses, interpreter, "netmask"))
      .isEqualTo(expectedAddresses);
  }

  @Test
  public void itFiltersIpAddressesWithParameterInMap() {
    Map<Integer, Object> inputAddresses = new HashMap<>();
    inputAddresses.put(1, "192.24.2.1");
    inputAddresses.put(2, null);
    inputAddresses.put(3, "192.168.32.0/24");
    inputAddresses.put(4, 13);
    inputAddresses.put(5, true);
    inputAddresses.put(6, "fe80::100/10");

    Map<Integer, Object> expectedAddresses = new HashMap<>();
    expectedAddresses.put(3, "255.255.255.0");
    expectedAddresses.put(6, "ffc0::");

    assertThat(ipAddrFilter.filter(inputAddresses, interpreter, "netmask"))
      .isEqualTo(expectedAddresses);
  }

  @Test
  public void itFiltersIpAddressesBroadcast() {
    List<Object> inputAddresses = Arrays.asList(
      "192.24.2.1",
      "host.fqdn",
      "::1",
      "192.168.32.0/24",
      "fe80::100/10",
      true,
      null,
      13
    );
    List<Object> expectedAddresses = Arrays.asList(
      "192.168.32.255",
      "febf:ffff:ffff:ffff:ffff:ffff:ffff:ffff"
    );
    assertThat(ipAddrFilter.filter(inputAddresses, interpreter, "broadcast"))
      .isEqualTo(expectedAddresses);
  }

  @Test
  public void itWorksWithSafeString() throws Exception {
    assertThat(
      ipAddrFilter.filter(
        new SafeString("1200:0000:AB00:1234:0000:2552:7777:1313"),
        interpreter
      )
    )
      .isEqualTo(true);
    assertThat(ipAddrFilter.filter(new SafeString("255.182.100.abc"), interpreter))
      .isEqualTo(false);
    assertThat(ipAddrFilter.filter(new SafeString("   128.0.0.1   "), interpreter))
      .isEqualTo(true);
    assertThat(
      ipAddrFilter.filter(new SafeString("255.182.100.1/10"), interpreter, "netmask")
    )
      .isEqualTo("255.192.0.0");
  }

  @Test
  public void itFiltersIpAddressesPublic() {
    List<Object> inputAddresses = Arrays.asList(
      "192.24.2.1",
      "host.fqdn",
      "192.168.32.0/24",
      "fe80::100/10",
      true,
      null,
      13,
      "",
      "2001:db8:32c:faad::/64"
    );
    List<Object> expectedAddresses = Arrays.asList(
      "192.24.2.1",
      "2001:db8:32c:faad::/64"
    );
    assertThat(ipAddrFilter.filter(inputAddresses, interpreter, "public"))
      .isEqualTo(expectedAddresses);
  }

  @Test
  public void itFiltersIpAddressesPrivate() {
    List<Object> inputAddresses = Arrays.asList(
      "192.24.2.1",
      "host.fqdn",
      "192.168.32.0/24",
      "fe80::100/10",
      true,
      null,
      13,
      "",
      "2001:db8:32c:faad::/64"
    );
    List<Object> expectedAddresses = Arrays.asList("192.168.32.0/24", "fe80::100/10");
    assertThat(ipAddrFilter.filter(inputAddresses, interpreter, "private"))
      .isEqualTo(expectedAddresses);
  }
}
