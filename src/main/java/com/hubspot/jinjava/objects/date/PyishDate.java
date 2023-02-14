package com.hubspot.jinjava.objects.date;

import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.PyWrapper;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * an object which quacks like a python date
 *
 * @author jstehler
 *
 */
public final class PyishDate
  extends Date
  implements Serializable, PyWrapper, PyishSerializable {
  private static final long serialVersionUID = 1L;
  public static final String PYISH_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
  public static final String FULL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  public static final String PYISH_DATE_CUSTOM_DATE_FORMAT_CONTEXT_KEY =
    "Jinjava_PyishDate_Custom_Format_Key";

  private final ZonedDateTime date;

  private String dateFormat = PYISH_DATE_FORMAT;

  public PyishDate(ZonedDateTime dt) {
    super(dt.toInstant().toEpochMilli());
    this.date = dt;
  }

  public PyishDate(Date d) {
    this(ZonedDateTime.ofInstant(d.toInstant(), ZoneOffset.UTC));
  }

  public PyishDate(String publishDateStr) {
    this(NumberUtils.toLong(Objects.requireNonNull(publishDateStr), 0L));
  }

  public PyishDate(Long epochMillis) {
    this(
      ZonedDateTime.ofInstant(
        Instant.ofEpochMilli(
          Optional
            .ofNullable(epochMillis)
            .orElseGet(
              () ->
                JinjavaInterpreter
                  .getCurrentMaybe()
                  .map(JinjavaInterpreter::getConfig)
                  .map(JinjavaConfig::getDateTimeProvider)
                  .map(DateTimeProvider::getCurrentTimeMillis)
                  .orElseGet(System::currentTimeMillis)
            )
        ),
        ZoneOffset.UTC
      )
    );
  }

  public PyishDate(Instant instant) {
    this(ZonedDateTime.ofInstant(instant, ZoneOffset.UTC));
  }

  public String isoformat() {
    return strftime("yyyy-MM-dd");
  }

  public String strftime(String fmt) {
    return StrftimeFormatter.format(date, fmt);
  }

  @SuppressWarnings("deprecation")
  @Override
  public int getYear() {
    return date.getYear();
  }

  @SuppressWarnings("deprecation")
  @Override
  public int getMonth() {
    return date.getMonthValue();
  }

  @SuppressWarnings("deprecation")
  @Override
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

  public String getDateFormat() {
    return dateFormat;
  }

  public void setDateFormat(String dateFormat) {
    this.dateFormat = dateFormat;
  }

  public PyishDate withDateFormat(String dateFormat) {
    setDateFormat(dateFormat);
    return this;
  }

  public Date toDate() {
    return Date.from(date.toInstant());
  }

  public ZonedDateTime toDateTime() {
    return date;
  }

  @Override
  public String toString() {
    if (
      JinjavaInterpreter.getCurrent() != null &&
      JinjavaInterpreter
        .getCurrent()
        .getContext()
        .containsKey(PYISH_DATE_CUSTOM_DATE_FORMAT_CONTEXT_KEY)
    ) {
      return strftime(
        JinjavaInterpreter
          .getCurrent()
          .getContext()
          .get(PYISH_DATE_CUSTOM_DATE_FORMAT_CONTEXT_KEY)
          .toString()
      );
    }

    return strftime(dateFormat);
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

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Appendable & CharSequence> T appendPyishString(T appendable)
    throws IOException {
    return (T) appendable
      .append("('")
      .append(strftime(FULL_DATE_FORMAT))
      .append("'|strtotime(")
      .append(PyishObjectMapper.getAsPyishStringOrThrow(FULL_DATE_FORMAT))
      .append(")).withDateFormat(")
      .append(PyishObjectMapper.getAsPyishStringOrThrow(dateFormat))
      .append(')');
  }
}
