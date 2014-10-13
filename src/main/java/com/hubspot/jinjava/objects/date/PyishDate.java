package com.hubspot.jinjava.objects.date;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.common.base.Optional;
import com.hubspot.jinjava.objects.PyWrapper;

/**
 * an object which quacks like a python date
 * 
 * @author jstehler
 *
 */
public final class PyishDate extends Date implements Serializable, PyWrapper {
  private static final long serialVersionUID = 1L;

  private final DateTime date;

  public PyishDate(Date d) {
    this(new DateTime(Objects.requireNonNull(d), DateTimeZone.UTC));
  }
  
  public PyishDate(DateTime dt) {
    super(Objects.requireNonNull(dt).getMillis());
    this.date = dt;
  }

  public PyishDate(Long publishDate) {
    this(new DateTime(Optional.fromNullable(publishDate).or(System.currentTimeMillis()), DateTimeZone.UTC));
  }

  public PyishDate(String publishDateStr) {
    this(NumberUtils.toLong(Objects.requireNonNull(publishDateStr), 0L));
  }
  

  public String strftime(String fmt) {
    return StrftimeFormatter.format(date, fmt);
  }
  
  public int getYear() {
    return date.getYear();
  }
  
  public int getMonth() {
    return date.getMonthOfYear();
  }
  
  public int getDay() {
    return date.getDayOfMonth();
  }
  
  public int getHour() {
    return date.getHourOfDay();
  }
  
  public int getMinute() {
    return date.getMinuteOfHour();
  }
  
  public int getSecond() {
    return date.getSecondOfMinute();
  }
  
  public int getMicrosecond() {
    return date.getMillisOfSecond();
  }
  
  public Date toDate() {
    return date.toDate();
  }

  public DateTime toDateTime() {
    return date;
  }
  
  @Override
  public String toString() {
    return strftime("yyyy-MM-dd HH:mm:ss");
  }

  @Override
  public int hashCode() {
    return com.google.common.base.Objects.hashCode(date.getMillis());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    PyishDate that = (PyishDate) obj;
    return com.google.common.base.Objects.equal(toDateTime().getMillis(), that.toDateTime().getMillis());
  }

}
