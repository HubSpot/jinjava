package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.date.StrftimeFormatter;


public class DateTimeFormatFilterTest {

  JinjavaInterpreter interpreter;
  DateTimeFormatFilter filter;

  DateTime d;
  
  @Before
  public void setup() {
    Jinjava jinjava = new Jinjava();
    Context context = new Context();
    interpreter = new JinjavaInterpreter(jinjava, context, jinjava.getGlobalConfig());
    filter = new  DateTimeFormatFilter();
    d = DateTime.parse("2013-11-06T14:22:00.000");
  }

  @Test
  public void itUsesTodayIfNoDateProvided() throws Exception {
    assertThat(filter.filter(null, interpreter)).isEqualTo(StrftimeFormatter.format(DateTime.now(DateTimeZone.UTC)));
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
