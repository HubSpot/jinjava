package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class IpAddrFilterTest {

  private IpAddrFilter ipAddrFilter;
  private JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    ipAddrFilter = new IpAddrFilter();
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
  public void itReturnsIpAddressPrefix() {
    assertThat(ipAddrFilter.filter("255.182.100.1/24", interpreter, "prefix")).isEqualTo(24);
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
  public void itReturnsIpAddressAddress() {
    assertThat(ipAddrFilter.filter("192.168.0.1/20", interpreter, "address")).isEqualTo("192.168.0.1");
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
  public void itWorksWithSafeFilters() throws Exception {
    String ipAddress = "192.168.0.1";
    String broadcastIpAddress = "192.168.0.1/20";
    String invalidIpAddress = "192.168.0.999";
    interpreter.getContext().put("safe_ip", ipAddress);
    interpreter.getContext().put("invalid_ip", invalidIpAddress);
    interpreter.getContext().put("broadcast_ip", broadcastIpAddress);
    Assertions.assertThat(interpreter.renderFlat("{{ safe_ip|safe|ipaddr }}")).isEqualTo(String.valueOf(true));
    Assertions.assertThat(interpreter.renderFlat("{{ broadcast_ip|safe|ipaddr('broadcast')}}")).isEqualTo("192.168.15.255");
    Assertions.assertThat(interpreter.renderFlat("{{ invalid_ip|safe|ipaddr }}")).isEqualTo(String.valueOf(false));
    Assertions.assertThat(interpreter.renderFlat("{{ safe_ip|ipaddr }}")).isEqualTo(String.valueOf(true));
  }
}
