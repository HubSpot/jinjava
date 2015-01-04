package com.hubspot.jinjava.objects.date;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Test;


public class PyishDateTest {

  @Test
  public void itUsesCurrentTimeWhenNoneProvided() {
    PyishDate d = new PyishDate((Long) null);
    assertThat(d.toDate()).isCloseTo(new Date(), 10000);
  }

  @Test
  public void testStrfmt() {
    PyishDate d = new PyishDate(DateTime.parse("2013-11-12T14:15:00").toDate());
    assertThat(d.strftime("%m %d %y")).isEqualTo("11 12 13");
  }

  @Test
  public void testEquals() {
    PyishDate d1 = new PyishDate(DateTime.parse("2013-11-12T14:15:00").toDate());
    PyishDate d2 = new PyishDate(DateTime.parse("2013-11-12T14:15:00").toDate());
    assertThat(d1).isEqualTo(d2);
  }

  @Test
  public void testNotEquals() {
    PyishDate d1 = new PyishDate(DateTime.parse("2013-11-12T14:15:00").toDate());
    PyishDate d2 = new PyishDate(new DateTime());
    assertThat(d1).isNotEqualTo(d2);
  }

  @Test(expected=NullPointerException.class)
  public void testNullDateNotAllowed() {
    new PyishDate((Date)null);
  }

  @Test(expected=NullPointerException.class)
  public void testNullDateTimeNotAllowed() {
    new PyishDate((DateTime)null);
  }

  @Test(expected=NullPointerException.class)
  public void testNullStringNotAllowed() {
    new PyishDate((String)null);
  }
}
