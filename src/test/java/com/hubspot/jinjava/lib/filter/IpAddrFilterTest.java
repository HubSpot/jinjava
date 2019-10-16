package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class IpAddrFilterTest {

  private IpAddrFilter ipAddrFilter;
  private Ipv4Filter ipv4Filter;
  private Ipv6Filter ipv6Filter;
  private JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    ipAddrFilter = new IpAddrFilter();
    ipv4Filter = new Ipv4Filter();
    ipv6Filter = new Ipv6Filter();
    interpreter = new Jinjava().newInterpreter();
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
    assertThat(ipAddrFilter.filter("1200:0000:AB00:1234:0000:2552:7777:1313", interpreter)).isEqualTo(true);
    assertThat(ipAddrFilter.filter("21DA:D3:0:2F3B:2AA:FF:FE28:9C5A", interpreter)).isEqualTo(true);
    assertThat(ipAddrFilter.filter("2000::", interpreter)).isEqualTo(true);
  }

  @Test
  public void itRejectsInvalidIpV6Address() {
    assertThat(ipAddrFilter.filter("1200::AB00:1234::2552:7777:1313", interpreter)).isEqualTo(false);
    assertThat(ipAddrFilter.filter("1200:0000:AB00:1234:O000:2552:7777:1313", interpreter)).isEqualTo(false);
    assertThat(ipAddrFilter.filter("1200::AB00:1234::2552:7777:1313:1232", interpreter)).isEqualTo(false);
    assertThat(ipAddrFilter.filter("321", interpreter)).isEqualTo(false);
  }

  @Test
  public void itReturnsIpv4AddressPrefix() {
    assertThat(ipAddrFilter.filter("255.182.100.1/24", interpreter, "prefix")).isEqualTo(24);
  }

  @Test
  public void itReturnsIpv6AddressPrefix() {
    assertThat(ipAddrFilter.filter("1200:0000:AB00:1234:0000:2552:7777:1313/43", interpreter, "prefix"))
        .isEqualTo(43);
  }

  @Test
  public void itRejectsInvalidIpAddressPrefix() {
    assertThat(ipAddrFilter.filter("255.182.100.abc/24", interpreter, "prefix")).isEqualTo(null);
  }

  @Test
  public void itReturnsIpAddressNetMask() {
    assertThat(ipAddrFilter.filter("255.182.100.1/10", interpreter, "netmask")).isEqualTo("255.192.0.0");
  }

  @Test
  public void itReturnsIpAddressBroadcast() {
    assertThat(ipAddrFilter.filter("192.168.0.1/20", interpreter, "broadcast")).isEqualTo("192.168.15.255");
  }

  @Test
  public void itReturnsIpv4AddressAddress() {
    assertThat(ipAddrFilter.filter("192.168.0.1/20", interpreter, "address")).isEqualTo("192.168.0.1");
  }

  @Test
  public void itReturnsIpv6AddressAddress() {
    assertThat(ipAddrFilter.filter("1200:0000:AB00:1234:0000:2552:7777:1313/43", interpreter, "address"))
      .isEqualTo("1200:0000:AB00:1234:0000:2552:7777:1313");
  }


  @Test
  public void itReturnsIpAddressNetwork() {
    assertThat(ipAddrFilter.filter("192.168.0.1/20", interpreter, "network")).isEqualTo("192.168.0.0");
  }

  @Test
  public void itAddsErrorOnInvalidCidrAddress() {
    assertThatThrownBy(() -> ipAddrFilter.filter("192.168.0.1/200", interpreter, "broadcast"))
        .hasMessageContaining("must be a valid CIDR address");
  }

  @Test
  public void itAddsErrorOnInvalidFunctionName() {
    assertThatThrownBy(() -> ipAddrFilter.filter("192.168.0.1/20", interpreter, "notAFunction"))
        .hasMessageContaining("must be one of");
  }

  @Test
  public void itFiltersValidIpv4Addresses() {
    List<Object> validAddresses = Arrays.asList("255.182.100.1", "   128.0.0.1   ");
    List<Object> invalidAddresses = Arrays.asList("255.182.100.abc", "125.512.100.1", "125.512.100.1.1", "125.512.100",
        104, "1200:0000:AB00:1234:0000:2552:7777:1313", "1200::AB00:1234::2552:7777:1313", "2000::", "321", null, true);
    List<Object> allAddresses = Stream.concat(validAddresses.stream(), invalidAddresses.stream()).collect(Collectors.toList());
    assertThat(ipv4Filter.filter(allAddresses, interpreter)).isEqualTo(validAddresses);
  }

  @Test
  public void itFiltersValidIpv6Addresses() {
    List<Object> validAddresses = Arrays.asList("1200:0000:AB00:1234:0000:2552:7777:1313", "2000::");
    List<Object> invalidAddresses = Arrays.asList("255.182.100.abc", "   128.0.0.1   ", "125.0.100.1", "125.512.100",
        104, "1200:0000:AB00:1234:O000:2552:7777:1313", "1200::AB00:1234::2552:7777:1313:1232", "321", null, true);
    List<Object> allAddresses = Stream.concat(validAddresses.stream(), invalidAddresses.stream()).collect(Collectors.toList());
    assertThat(ipv6Filter.filter(allAddresses, interpreter)).isEqualTo(validAddresses);
  }

  @Test
  public void itFiltersIpAddressesAddress() {
    List<Object> inputAddresses = Arrays.asList("192.24.2.1", "host.fqdn", "::1", "192.168.32.0/24", "fe80::100/10",
        true, "", null, 13);
    List<Object> expectedAddresses = Arrays.asList("192.168.32.0", "fe80::100");
    assertThat(ipAddrFilter.filter(inputAddresses, interpreter, "address")).isEqualTo(expectedAddresses);
  }

  @Test
  public void itFiltersIpAddressesPrefix() {
    List<Object> inputAddresses = Arrays.asList("192.24.2.1", "host.fqdn", "::1", "192.168.32.0/24", "fe80::100/10",
        true, "", null, 13);
    List<Object> expectedAddresses = Arrays.asList("24", "10");
    assertThat(ipAddrFilter.filter(inputAddresses, interpreter, "prefix")).isEqualTo(expectedAddresses);
  }

  @Test
  public void itFiltersIpAddressesNetwork() {
    List<Object> inputAddresses = Arrays.asList("192.24.2.1", "host.fqdn", "::1", "192.168.32.0/24", "fe80::100/10",
        true, null, 13);
    List<Object> expectedAddresses = Arrays.asList("192.168.32.0");
    assertThat(ipAddrFilter.filter(inputAddresses, interpreter, "network")).isEqualTo(expectedAddresses);
  }

  @Test
  public void itFiltersIpAddressesNetmask() {
    List<Object> inputAddresses = Arrays.asList("192.24.2.1", "host.fqdn", "::1", "192.168.32.0/24", "fe80::100/10",
        true, null, 13);
    List<Object> expectedAddresses = Arrays.asList("255.255.255.0");
    assertThat(ipAddrFilter.filter(inputAddresses, interpreter, "netmask")).isEqualTo(expectedAddresses);
  }

  @Test
  public void itFiltersIpAddressesBroadcast() {
    List<Object> inputAddresses = Arrays.asList("192.24.2.1", "host.fqdn", "::1", "192.168.32.0/24", "fe80::100/10",
        true, null, 13);
    List<Object> expectedAddresses = Arrays.asList("192.168.32.255");
    assertThat(ipAddrFilter.filter(inputAddresses, interpreter, "broadcast")).isEqualTo(expectedAddresses);
  }
}
