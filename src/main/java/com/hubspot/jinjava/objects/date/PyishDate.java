package com.hubspot.jinjava.objects.date;

import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.PyWrapper;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import java.io.IOException;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQuery;
import java.time.temporal.TemporalUnit;
import java.time.temporal.ValueRange;
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
            .orElseGet(() ->
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

  // ZonedDateTime delegate methods

  public ZoneId getZone() {
    return date.getZone();
  }

  public ZoneOffset getOffset() {
    return date.getOffset();
  }

  public ZonedDateTime withZoneSameLocal(ZoneId zone) {
    return date.withZoneSameLocal(zone);
  }

  public ZonedDateTime withZoneSameInstant(ZoneId zone) {
    return date.withZoneSameInstant(zone);
  }

  public ZonedDateTime withFixedOffsetZone() {
    return date.withFixedOffsetZone();
  }

  public ZonedDateTime withEarlierOffsetAtOverlap() {
    return date.withEarlierOffsetAtOverlap();
  }

  public ZonedDateTime withLaterOffsetAtOverlap() {
    return date.withLaterOffsetAtOverlap();
  }

  public LocalDateTime toLocalDateTime() {
    return date.toLocalDateTime();
  }

  public LocalDate toLocalDate() {
    return date.toLocalDate();
  }

  public LocalTime toLocalTime() {
    return date.toLocalTime();
  }

  public OffsetDateTime toOffsetDateTime() {
    return date.toOffsetDateTime();
  }

  @Override
  public Instant toInstant() {
    return date.toInstant();
  }

  public boolean isSupported(TemporalField field) {
    return date.isSupported(field);
  }

  public boolean isSupported(TemporalUnit unit) {
    return date.isSupported(unit);
  }

  public ValueRange range(TemporalField field) {
    return date.range(field);
  }

  public int get(TemporalField field) {
    return date.get(field);
  }

  public long getLong(TemporalField field) {
    return date.getLong(field);
  }

  public int getMonthValue() {
    return date.getMonthValue();
  }

  public int getDayOfMonth() {
    return date.getDayOfMonth();
  }

  public int getDayOfYear() {
    return date.getDayOfYear();
  }

  public DayOfWeek getDayOfWeek() {
    return date.getDayOfWeek();
  }

  public int getNano() {
    return date.getNano();
  }

  public ZonedDateTime with(TemporalAdjuster adjuster) {
    return date.with(adjuster);
  }

  public ZonedDateTime with(TemporalField field, long newValue) {
    return date.with(field, newValue);
  }

  public ZonedDateTime withYear(int year) {
    return date.withYear(year);
  }

  public ZonedDateTime withMonth(int month) {
    return date.withMonth(month);
  }

  public ZonedDateTime withDayOfMonth(int dayOfMonth) {
    return date.withDayOfMonth(dayOfMonth);
  }

  public ZonedDateTime withDayOfYear(int dayOfYear) {
    return date.withDayOfYear(dayOfYear);
  }

  public ZonedDateTime withHour(int hour) {
    return date.withHour(hour);
  }

  public ZonedDateTime withMinute(int minute) {
    return date.withMinute(minute);
  }

  public ZonedDateTime withSecond(int second) {
    return date.withSecond(second);
  }

  public ZonedDateTime withNano(int nanoOfSecond) {
    return date.withNano(nanoOfSecond);
  }

  public ZonedDateTime truncatedTo(TemporalUnit unit) {
    return date.truncatedTo(unit);
  }

  public ZonedDateTime plus(TemporalAmount amountToAdd) {
    return date.plus(amountToAdd);
  }

  public ZonedDateTime plus(long amountToAdd, TemporalUnit unit) {
    return date.plus(amountToAdd, unit);
  }

  public ZonedDateTime plusYears(long years) {
    return date.plusYears(years);
  }

  public ZonedDateTime plusMonths(long months) {
    return date.plusMonths(months);
  }

  public ZonedDateTime plusWeeks(long weeks) {
    return date.plusWeeks(weeks);
  }

  public ZonedDateTime plusDays(long days) {
    return date.plusDays(days);
  }

  public ZonedDateTime plusHours(long hours) {
    return date.plusHours(hours);
  }

  public ZonedDateTime plusMinutes(long minutes) {
    return date.plusMinutes(minutes);
  }

  public ZonedDateTime plusSeconds(long seconds) {
    return date.plusSeconds(seconds);
  }

  public ZonedDateTime plusNanos(long nanos) {
    return date.plusNanos(nanos);
  }

  public ZonedDateTime minus(TemporalAmount amountToSubtract) {
    return date.minus(amountToSubtract);
  }

  public ZonedDateTime minus(long amountToSubtract, TemporalUnit unit) {
    return date.minus(amountToSubtract, unit);
  }

  public ZonedDateTime minusYears(long years) {
    return date.minusYears(years);
  }

  public ZonedDateTime minusMonths(long months) {
    return date.minusMonths(months);
  }

  public ZonedDateTime minusWeeks(long weeks) {
    return date.minusWeeks(weeks);
  }

  public ZonedDateTime minusDays(long days) {
    return date.minusDays(days);
  }

  public ZonedDateTime minusHours(long hours) {
    return date.minusHours(hours);
  }

  public ZonedDateTime minusMinutes(long minutes) {
    return date.minusMinutes(minutes);
  }

  public ZonedDateTime minusSeconds(long seconds) {
    return date.minusSeconds(seconds);
  }

  public ZonedDateTime minusNanos(long nanos) {
    return date.minusNanos(nanos);
  }

  public <R> R query(TemporalQuery<R> query) {
    return date.query(query);
  }

  public long until(Temporal endExclusive, TemporalUnit unit) {
    return date.until(endExclusive, unit);
  }

  public String format(DateTimeFormatter formatter) {
    return date.format(formatter);
  }

  public long toEpochSecond() {
    return date.toEpochSecond();
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
