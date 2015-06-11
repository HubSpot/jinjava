package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class WordCountFilterTest {

  WordCountFilter filter;

  @Before
  public void setup() {
    filter = new WordCountFilter();
  }

  @Test
  public void singleWord() {
    assertThat(filter.filter("foo", null)).isEqualTo(1);
  }

  @Test
  public void multiWords() {
    assertThat(filter.filter("this is foo.\nfoo is coo. hooray for foo.", null)).isEqualTo(9);
  }

}
