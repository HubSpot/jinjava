package com.hubspot.jinjava.objects.date;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.math.NumberUtils;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoField;

import com.hubspot.jinjava.objects.PyWrapper;

/**
 * an object which quacks like a python date
 *
 * @author jstehler
 *
 */
public final class PyishDate extends Date implements Serializable, PyWrapper {
  private static final long serialVersionUID = 1L;

  private final ZonedDateTime date;

  public PyishDate(ZonedDateTime dt) {
    super(dt.toInstant().toEpochMilli());
    this.date = dt;
  }

  public PyishDate(Date d) {
    this(ZonedDateTime.ofInstant(Instant.ofEpochMilli(d.getTime()), ZoneOffset.UTC));
  }

  public PyishDate(String publishDateStr) {
    this(NumberUtils.toLong(Objects.requireNonNull(publishDateStr), 0L));
  }

  public PyishDate(Long epochMillis) {
    this(ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(epochMillis == null ? System.currentTimeMillis() : epochMillis), ZoneOffset.UTC));
  }

  public String isoformat() {
    return strftime("yyyy-MM-dd");
  }

  public String strftime(String fmt) {
    return StrftimeFormatter.format(date, fmt);
  }

  @Override
  @SuppressWarnings("deprecation")
  public int getYear() {
    return date.getYear();
  }

  @Override
  @SuppressWarnings("deprecation")
  public int getMonth() {
    return date.getMonthValue();
  }

  @Override
  @SuppressWarnings("deprecation")
  public int getDay() {
    return date.getDayOfMonth();
  }

  public int getHour() {
    return date.getHour();
  }

  public int getMinute() {
    return date.getMinute();
  }

  public int getSecond() {
    return date.getSecond();
  }

  public int getMicrosecond() {
    return date.get(ChronoField.MILLI_OF_SECOND);
  }

  public Date toDate() {
    return new Date(date.toInstant().toEpochMilli());
  }

  public ZonedDateTime toDateTime() {
    return date;
  }

  @Override
  public String toString() {
    return strftime("yyyy-MM-dd HH:mm:ss");
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(date);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    PyishDate that = (PyishDate) obj;
    return Objects.equals(toDateTime(), that.toDateTime());
  }

}
