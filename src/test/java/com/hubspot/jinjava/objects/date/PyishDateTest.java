package com.hubspot.jinjava.objects.date;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import org.junit.Test;

public class PyishDateTest {

  @Test
  public void itUsesCurrentTimeWhenNoneProvided() {
    PyishDate d = new PyishDate((Long) null);
    assertThat(d.toDate()).isCloseTo(new Date(), 10000);
  }

  @Test
  public void testStrfmt() {
    PyishDate d = new PyishDate(ZonedDateTime.parse("2013-11-12T14:15:00+00:00"));
    assertThat(d.strftime("%m %d %y")).isEqualTo("11 12 13");
  }

  @Test
  public void testIsoformat() {
    PyishDate d = new PyishDate(ZonedDateTime.parse("2015-05-13T12:00:00+00:00"));
    assertThat(d.isoformat()).isEqualTo("2015-05-13");
  }

  @Test
  public void testEquals() {
    PyishDate d1 = new PyishDate(ZonedDateTime.parse("2013-11-12T14:15:00+00:00"));
    PyishDate d2 = new PyishDate(ZonedDateTime.parse("2013-11-12T14:15:00+00:00"));
    assertThat(d1).isEqualTo(d2);
  }

  @Test
  public void testNotEquals() {
    PyishDate d1 = new PyishDate(ZonedDateTime.parse("2013-11-12T14:15:00+00:00"));
    PyishDate d2 = new PyishDate(ZonedDateTime.now(ZoneOffset.UTC));
    assertThat(d1).isNotEqualTo(d2);
  }

  @Test(expected = NullPointerException.class)
  public void testNullDateNotAllowed() {
    new PyishDate((Date) null);
  }

  @Test(expected = NullPointerException.class)
  public void testNullDateTimeNotAllowed() {
    new PyishDate((ZonedDateTime) null);
  }

  @Test(expected = NullPointerException.class)
  public void testNullStringNotAllowed() {
    new PyishDate((String) null);
  }
}
