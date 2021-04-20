package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import java.util.HashMap;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;

public class FileSizeFormatFilterTest extends BaseJinjavaTest {

  @Before
  public void setup() {
    Locale.setDefault(Locale.ENGLISH);
  }

  @Test
  public void testFileSizeFormatFilter() {
    assertThat(jinjava.render("{{12|filesizeformat}}", new HashMap<String, Object>()))
      .isEqualTo("12 Bytes");
    assertThat(jinjava.render("{{1000|filesizeformat}}", new HashMap<String, Object>()))
      .isEqualTo("1.0 KB");
    assertThat(
        jinjava.render("{{1024|filesizeformat(true)}}", new HashMap<String, Object>())
      )
      .isEqualTo("1.0 KiB");
    assertThat(
        jinjava.render("{{3531836|filesizeformat(true)}}", new HashMap<String, Object>())
      )
      .isEqualTo("3.4 MiB");
  }
}
