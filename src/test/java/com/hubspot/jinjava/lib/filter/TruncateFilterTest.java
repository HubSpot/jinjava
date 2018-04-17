package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@RunWith(MockitoJUnitRunner.class)
public class TruncateFilterTest {

  @Mock
  JinjavaInterpreter interpreter;
  @InjectMocks
  TruncateFilter filter;

  @Test
  public void itPassesThroughSmallEnoughText() throws Exception {
    String s = StringUtils.rightPad("", 255, 'x');
    assertThat(filter.filter(s, interpreter)).isEqualTo(s);
  }

  @Test
  public void itTruncatesText() throws Exception {
    assertThat(filter.filter(StringUtils.rightPad("", 256, 'x') + "y", interpreter, "255", "True").toString()).hasSize(258).endsWith("x...");
  }

  @Test
  public void itTruncatesToSpecifiedLength() throws Exception {
    assertThat(filter.filter("foo bar", interpreter, "5", "True")).isEqualTo("foo b...");
  }

  @Test
  public void itDiscardsLastWordWhenKillwordsFalse() throws Exception {
    assertThat(filter.filter("foo bar", interpreter, "5")).isEqualTo("foo ...");
  }

  @Test
  public void itTruncatesWithDifferentEndingIfSpecified() throws Exception {
    assertThat(filter.filter("foo bar", interpreter, "5", "True", "!")).isEqualTo("foo b!");
  }

}
