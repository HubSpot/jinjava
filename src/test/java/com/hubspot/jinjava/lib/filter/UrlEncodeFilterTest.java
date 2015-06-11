package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class UrlEncodeFilterTest {

  JinjavaInterpreter interpreter;
  UrlEncodeFilter filter;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
    filter = new UrlEncodeFilter();
  }

  @Test
  public void itEncodesDictAsParamPairs() {
    Map<String, String> dict = new LinkedHashMap<>();
    dict.put("foo", "bar=set");
    dict.put("other", "val");

    assertThat(filter.filter(dict, interpreter)).isEqualTo("foo=bar%3Dset&other=val");
  }

  @Test
  public void itEncodesVarString() {
    assertThat(filter.filter("http://foo.com?bar&food", interpreter)).isEqualTo("http%3A%2F%2Ffoo.com%3Fbar%26food");
  }

  @Test
  public void itEncodesArgWhenNoVar() {
    assertThat(filter.filter(null, interpreter, "foo&you")).isEqualTo("foo%26you");
  }

}
