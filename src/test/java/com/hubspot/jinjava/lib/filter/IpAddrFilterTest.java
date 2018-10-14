package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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

}
