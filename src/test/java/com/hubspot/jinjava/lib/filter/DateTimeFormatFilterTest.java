package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.date.StrftimeFormatter;


public class DateTimeFormatFilterTest {

  JinjavaInterpreter interpreter;
  DateTimeFormatFilter filter;

  ZonedDateTime d;
  
  @Before
  public void setup() {
    Locale.setDefault(Locale.ENGLISH);
    interpreter = new Jinjava().newInterpreter();
    filter = new  DateTimeFormatFilter();
    d = ZonedDateTime.parse("2013-11-06T14:22:00.000+00:00[UTC]");
  }

  @Test
  public void itUsesTodayIfNoDateProvided() throws Exception {
    assertThat(filter.filter(null, interpreter)).isEqualTo(StrftimeFormatter.format(ZonedDateTime.now(ZoneOffset.UTC)));
  }
  
  @Test
  public void itSupportsLongAsInput() throws Exception {
    assertThat(filter.filter(d, interpreter)).isEqualTo(StrftimeFormatter.format(d));
  }
  
  @Test
  public void itUsesDefaultFormatStringIfNoneSpecified() throws Exception {
    assertThat(filter.filter(d, interpreter)).isEqualTo("14:22 / 06-11-2013");
  }
  
  @Test
  public void itUsesSpecifiedFormatString() throws Exception {
    assertThat(filter.filter(d, interpreter, "%B %d, %Y, at %I:%M %p")).isEqualTo("November 06, 2013, at 02:22 PM");
  }
  
  @Test
  public void itHandlesVarsAndLiterals() throws Exception {
    interpreter.getContext().put("d", d);
    interpreter.getContext().put("foo", "%Y-%m");
    
    assertThat(interpreter.renderString("{{ d|datetimeformat(foo) }}")).isEqualTo("2013-11");
    assertThat(interpreter.renderString("{{ d|datetimeformat(\"%Y-%m-%d\") }}")).isEqualTo("2013-11-06");
    assertThat(interpreter.getErrors()).isEmpty();
  }

}
