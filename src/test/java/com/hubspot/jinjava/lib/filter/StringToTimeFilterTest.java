package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseJinjavaTest;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class StringToTimeFilterTest extends BaseJinjavaTest {

  @Before
  public void setup() {
    jinjava.getGlobalContext().registerClasses(EscapeJsFilter.class);
  }

  @Test
  public void itConvertsStringToTime() {
    String datetime = "2018-07-14T14:31:30+0530";
    String format = "yyyy-MM-dd'T'HH:mm:ssZ";

    Map<String, Object> vars = ImmutableMap.of("test", datetime, "format", format);

    assertThat(jinjava.render("{{ test|strtotime(format)|unixtimestamp }}", vars))
      .isEqualTo("1531558890000");
  }

  @Test
  public void itErrorsOnNonStringInput() {
    int datetime = 123123;
    String format = "yyyy-MM-dd'T'HH:mm:ssZ";

    Map<String, Object> vars = ImmutableMap.of("datetime", datetime, "format", format);

    assertThat(
      jinjava.renderForResult("{{ datetime|strtotime(format) }}", vars).getErrors()
    )
      .hasSize(1);
  }
}
