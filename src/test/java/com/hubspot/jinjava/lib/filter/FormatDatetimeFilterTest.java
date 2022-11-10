package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.Jinjava;
import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

public class FormatDatetimeFilterTest {
  Jinjava jinjava;

  @Before
  public void setUp() throws Exception {
    jinjava = new Jinjava();
    jinjava.getGlobalContext().registerClasses(FormatDatetimeFilter.class);
  }

  @Test
  public void itFormatsLocalDateTimes() {
    LocalDateTime dateTime = LocalDateTime.of(2022, 11, 10, 17, 49, 7);

    assertThat(
        jinjava.render("{{ d | format_datetime }}", ImmutableMap.of("d", dateTime))
      )
      .isEqualTo("Nov 10, 2022, 5:49:07 PM");
  }
}
