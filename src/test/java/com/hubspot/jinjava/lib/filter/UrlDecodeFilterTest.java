package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseInterpretingTest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class UrlDecodeFilterTest extends BaseInterpretingTest {

  private UrlDecodeFilter filter;

  @Before
  public void setup() {
    filter = new UrlDecodeFilter();
  }

  @Test
  public void itDecodesDictAsParamPairs() {
    Map<String, String> dict = new LinkedHashMap<>();
    dict.put("foo", "bar%3Dset");
    dict.put("other", "val");

    assertThat(filter.filter(dict, interpreter)).isEqualTo("foo=bar=set&other=val");
  }

  @Test
  public void itDecodesVarString() {
    assertThat(filter.filter("http%3A%2F%2Ffoo.com%3Fbar%26food", interpreter))
      .isEqualTo("http://foo.com?bar&food");
  }

  @Test
  public void itDecodesArgWhenNoVar() {
    assertThat(filter.filter(null, interpreter, "foo%26you")).isEqualTo("foo&you");
  }
}
