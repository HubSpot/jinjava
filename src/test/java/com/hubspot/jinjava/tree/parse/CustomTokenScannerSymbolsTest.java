package com.hubspot.jinjava.tree.parse;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.lib.filter.JoinFilterTest.User;

public class CustomTokenScannerSymbolsTest {

  private Jinjava jinjava;
  private JinjavaConfig config;
  
  @Before
  public void setup() {
    config = JinjavaConfig.newBuilder().withTokenScannerSymbols(new CustomTokens()).build();
    jinjava = new Jinjava(config);
    jinjava.getGlobalContext().put("numbers", Lists.newArrayList(1L, 2L, 3L, 4L, 5L));
  }
  
  @Test
  public void testsThatCustomTokensDoesNotFail() {
    String template = "jinjava interpreter works correctly";
    assertThat(jinjava.render(template, new HashMap<String, Object>())).isEqualTo(template);
  }
  
  @Test
  public void testsCustomTokensWithFilters() {
    assertThat(jinjava.render("<% set d=d | default(\"some random value\") %><< d >>", new HashMap<>())).isEqualTo("some random value");
    assertThat(jinjava.render("<< [1, 2, 3, 3]|union(null) >>", new HashMap<>())).isEqualTo("[1, 2, 3]");
    assertThat(jinjava.render("<< numbers|select('equalto', 3) >>", new HashMap<>())).isEqualTo("[3]");
    assertThat(jinjava.render("<< users|map(attribute='username')|join(', ') >>",
        ImmutableMap.of("users", (Object) Lists.newArrayList(new User("foo"), new User("bar")))))
        .isEqualTo("foo, bar");

  }

  class CustomTokens extends TokenScannerSymbols {

    @Override
    public char TOKEN_PREFIX_CHAR() {
      return '<';
    }

    @Override
    public char TOKEN_POSTFIX_CHAR() {
      return '>';
    }

    @Override
    public int TOKEN_FIXED_CHAR() {
      return 0;
    }

    @Override
    public int TOKEN_NOTE_CHAR() {
      return '#';
    }

    @Override
    public int TOKEN_TAG_CHAR() {
      return '%';
    }

    @Override
    public int TOKEN_EXPR_START_CHAR() {
      return '<';
    }

    @Override
    public int TOKEN_EXPR_END_CHAR() {
      return '>';
    }

    @Override
    public int TOKEN_NEWLINE_CHAR() {
      return '\n';
    }

    @Override
    public int TOKEN_TRIM_CHAR() {
      return '-';
    }
    
  }
  
}
