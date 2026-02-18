package com.hubspot.jinjava.el.ext;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.interpret.RenderResult;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class AstFilterChainParityTest {

  private Jinjava jinjavaOptimized;
  private Jinjava jinjavaUnoptimized;
  private Map<String, Object> context;

  @Before
  public void setup() {
    LegacyOverrides legacyOverrides = LegacyOverrides
      .newBuilder()
      .withUsePyishObjectMapper(true)
      .withKeepNullableLoopValues(true)
      .build();

    jinjavaOptimized =
      new Jinjava(
        BaseJinjavaTest
          .newConfigBuilder()
          .withEnableFilterChainOptimization(true)
          .withLegacyOverrides(legacyOverrides)
          .build()
      );

    jinjavaUnoptimized =
      new Jinjava(
        BaseJinjavaTest
          .newConfigBuilder()
          .withEnableFilterChainOptimization(false)
          .withLegacyOverrides(legacyOverrides)
          .build()
      );

    context = new HashMap<>();
    context.put("name", "  Hello World  ");
    context.put("text", "the quick brown fox jumps over the lazy dog");
    context.put("number", 12345);
    context.put("float_num", 3.14159);
    context.put("negative", -42);
    context.put("items", Arrays.asList("apple", "banana", "cherry"));
    context.put("empty_list", ImmutableList.of());
    context.put("numbers", Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6));
    context.put("html", "<script>alert('xss')</script>");
    context.put("null_value", null);
    context.put(
      "nested",
      ImmutableMap.of("key", "value", "num", 100, "list", Arrays.asList(1, 2, 3))
    );
    context.put(
      "objects",
      Arrays.asList(
        ImmutableMap.of("name", "Alice", "age", 30),
        ImmutableMap.of("name", "Bob", "age", 25),
        ImmutableMap.of("name", "Charlie", "age", 35)
      )
    );
    context.put("mixed_case", "HeLLo WoRLd");
    context.put("whitespace", "   lots   of   spaces   ");
    context.put("unicode", "héllo wörld 你好");
    context.put("special_chars", "a&b<c>d\"e'f");
    context.put("json_string", "{\"key\": \"value\", \"num\": 42}");
    context.put("long_text", "word ".repeat(100));
    context.put("arg_value", 10);
  }

  @Test
  public void itProducesSameResultsForSingleFilters() {
    List<String> templates = ImmutableList.of(
      "{{ name|trim }}",
      "{{ name|lower }}",
      "{{ name|upper }}",
      "{{ name|length }}",
      "{{ number|string }}",
      "{{ number|abs }}",
      "{{ float_num|round }}",
      "{{ float_num|int }}",
      "{{ items|first }}",
      "{{ items|last }}",
      "{{ items|length }}",
      "{{ items|reverse }}",
      "{{ items|sort }}",
      "{{ html|escape }}",
      "{{ html|e }}",
      "{{ text|capitalize }}",
      "{{ text|title }}",
      "{{ text|wordcount }}",
      "{{ negative|abs }}",
      "{{ mixed_case|lower }}",
      "{{ mixed_case|upper }}",
      "{{ whitespace|trim }}",
      "{{ unicode|upper }}",
      "{{ unicode|lower }}"
    );

    assertParityForTemplates(templates);
  }

  @Test
  public void itProducesSameResultsForChainedFilters() {
    List<String> templates = ImmutableList.of(
      "{{ name|trim|lower }}",
      "{{ name|trim|upper }}",
      "{{ name|trim|lower|capitalize }}",
      "{{ name|trim|lower|upper }}",
      "{{ text|upper|lower|capitalize }}",
      "{{ text|capitalize|lower|upper }}",
      "{{ number|string|length }}",
      "{{ number|string|upper }}",
      "{{ items|first|upper }}",
      "{{ items|last|lower }}",
      "{{ items|reverse|first }}",
      "{{ items|sort|last }}",
      "{{ items|sort|reverse|first }}",
      "{{ html|escape|upper }}",
      "{{ float_num|round|string|length }}",
      "{{ whitespace|trim|lower|capitalize }}"
    );

    assertParityForTemplates(templates);
  }

  @Test
  public void itProducesSameResultsForFiltersWithPositionalArgs() {
    List<String> templates = ImmutableList.of(
      "{{ text|truncate(20) }}",
      "{{ text|truncate(20, True) }}",
      "{{ text|truncate(20, True, '...') }}",
      "{{ text|truncate(10, False) }}",
      "{{ items|join(', ') }}",
      "{{ items|join(' - ') }}",
      "{{ items|join('') }}",
      "{{ text|replace('the', 'a') }}",
      "{{ text|replace('o', '0') }}",
      "{{ text|split(' ') }}",
      "{{ text|split(' ', 3) }}",
      "{{ number|default(0) }}",
      "{{ null_value|default('fallback') }}",
      "{{ null_value|default(42) }}",
      "{{ float_num|round(2) }}",
      "{{ float_num|round(0) }}",
      "{{ text|center(50) }}",
      "{{ text|center(50, '-') }}",
      "{{ numbers|batch(3) }}",
      "{{ numbers|slice(3) }}"
    );

    assertParityForTemplates(templates);
  }

  @Test
  public void itProducesSameResultsForFiltersWithNamedParams() {
    List<String> templates = ImmutableList.of(
      "{{ text|truncate(length=20) }}",
      "{{ text|truncate(length=20, killwords=True) }}",
      "{{ text|truncate(length=20, end='!!!') }}",
      "{{ text|truncate(length=15, killwords=False, end='...') }}"
    );

    assertParityForTemplates(templates);
  }

  @Test
  public void itProducesSameResultsForMixedPositionalAndNamedParams() {
    List<String> templates = ImmutableList.of(
      "{{ text|truncate(20, killwords=True) }}",
      "{{ text|truncate(20, end='!') }}",
      "{{ items|join(', ')|truncate(length=15) }}"
    );

    assertParityForTemplates(templates);
  }

  @Test
  public void itProducesSameResultsForChainedFiltersWithArgs() {
    List<String> templates = ImmutableList.of(
      "{{ text|truncate(20)|upper }}",
      "{{ text|upper|truncate(20) }}",
      "{{ text|replace('the', 'a')|upper }}",
      "{{ text|upper|replace('THE', 'a') }}",
      "{{ text|truncate(30)|replace('...', '!')|upper }}",
      "{{ items|join(', ')|upper }}",
      "{{ items|join(', ')|truncate(10) }}",
      "{{ items|sort|join(' - ')|upper }}",
      "{{ items|reverse|join(', ')|lower }}",
      "{{ numbers|sort|join('-') }}",
      "{{ numbers|reverse|join(', ')|length }}"
    );

    assertParityForTemplates(templates);
  }

  @Test
  public void itProducesSameResultsForFilterArgsWithExpressions() {
    List<String> templates = ImmutableList.of(
      "{{ text|truncate(arg_value) }}",
      "{{ text|truncate(arg_value + 5) }}",
      "{{ text|truncate(arg_value * 2) }}",
      "{{ items|join(name|trim) }}",
      "{{ text|replace(items|first, items|last) }}"
    );

    assertParityForTemplates(templates);
  }

  @Test
  public void itProducesSameResultsForNullAndUndefinedHandling() {
    List<String> templates = ImmutableList.of(
      "{{ null_value|default('fallback') }}",
      "{{ null_value|default('fallback')|upper }}",
      "{{ undefined_var|default('missing') }}",
      "{{ undefined_var|default('missing')|lower }}",
      "{{ null_value|string }}",
      "{{ null_value|e }}",
      "{{ nested.missing|default('not found') }}",
      "{{ nested.missing|default('')|length }}"
    );

    assertParityForTemplates(templates);
  }

  @Test
  public void itProducesSameResultsForSafeStringHandling() {
    context.put("safe_html", "<b>Bold</b>");

    List<String> templates = ImmutableList.of(
      "{{ safe_html|safe }}",
      "{{ safe_html|safe|upper }}",
      "{{ safe_html|upper|safe }}",
      "{{ safe_html|safe|length }}",
      "{{ safe_html|safe|trim }}",
      "{{ safe_html|safe|lower|capitalize }}"
    );

    assertParityForTemplates(templates);
  }

  @Test
  public void itProducesSameResultsForCollectionFilters() {
    List<String> templates = ImmutableList.of(
      "{{ items|list }}",
      "{{ items|unique }}",
      "{{ numbers|sum }}",
      "{{ numbers|sort }}",
      "{{ numbers|sort|reverse }}",
      "{{ objects|map(attribute='name') }}",
      "{{ objects|map(attribute='name')|join(', ') }}",
      "{{ objects|selectattr('age', '>', 28) }}",
      "{{ objects|rejectattr('age', '<', 30) }}",
      "{{ numbers|select('>', 3) }}",
      "{{ numbers|reject('==', 1) }}",
      "{{ items|batch(2)|list }}",
      "{{ numbers|slice(3)|list }}"
    );

    assertParityForTemplates(templates);
  }

  @Test
  public void itProducesSameResultsForStringManipulationFilters() {
    List<String> templates = ImmutableList.of(
      "{{ text|format }}",
      "{{ text|striptags }}",
      "{{ html|striptags }}",
      "{{ text|urlize }}",
      "{{ special_chars|escape }}",
      "{{ special_chars|urlencode }}",
      "{{ text|regex_replace('\\\\s+', '_') }}",
      "{{ text|replace(' ', '_') }}",
      "{{ name|trim|replace(' ', '-')|lower }}"
    );

    assertParityForTemplates(templates);
  }

  @Test
  public void itProducesSameResultsForNumericFilters() {
    List<String> templates = ImmutableList.of(
      "{{ number|filesizeformat }}",
      "{{ float_num|round }}",
      "{{ float_num|round(2) }}",
      "{{ float_num|round(2, 'floor') }}",
      "{{ float_num|round(2, 'ceil') }}",
      "{{ negative|abs }}",
      "{{ number|float }}",
      "{{ float_num|int }}",
      "{{ number|divide(100) }}",
      "{{ number|multiply(2) }}",
      "{{ float_num|log }}",
      "{{ number|root }}"
    );

    assertParityForTemplates(templates);
  }

  @Test
  public void itProducesSameResultsForDateTimeFilters() {
    context.put("timestamp", 1609459200000L);
    context.put("date_string", "2021-01-01");

    List<String> templates = ImmutableList.of(
      "{{ timestamp|datetimeformat }}",
      "{{ timestamp|unixtimestamp }}"
    );

    assertParityForTemplates(templates);
  }

  @Test
  public void itProducesSameResultsForJsonFilters() {
    List<String> templates = ImmutableList.of(
      "{{ nested|tojson }}",
      "{{ items|tojson }}",
      "{{ json_string|fromjson }}",
      "{{ json_string|fromjson|tojson }}"
    );

    assertParityForTemplates(templates);
  }

  @Test
  public void itProducesSameResultsForMultipleFilterChainsInTemplate() {
    List<String> templates = ImmutableList.of(
      "{{ name|trim|lower }} and {{ text|upper|truncate(10) }}",
      "Hello {{ name|trim }}, you have {{ items|length }} items",
      "{{ items|first|upper }} - {{ items|last|lower }}",
      "{{ number|string }} is {{ number|string|length }} digits",
      "Name: {{ name|trim|lower|capitalize }}, Count: {{ items|length }}"
    );

    assertParityForTemplates(templates);
  }

  @Test
  public void itProducesSameResultsForNestedPropertyAccess() {
    List<String> templates = ImmutableList.of(
      "{{ nested.key|upper }}",
      "{{ nested.num|string }}",
      "{{ nested.list|first }}",
      "{{ nested.list|join('-') }}",
      "{{ nested.key|upper|lower|capitalize }}",
      "{{ objects[0].name|upper }}",
      "{{ objects[0].name|upper|truncate(3) }}"
    );

    assertParityForTemplates(templates);
  }

  @Test
  public void itProducesSameResultsForFilterChainInConditions() {
    List<String> templates = ImmutableList.of(
      "{% if name|trim|length > 5 %}long{% else %}short{% endif %}",
      "{% if items|length > 2 %}many{% else %}few{% endif %}",
      "{% if name|trim|lower == 'hello world' %}match{% else %}no match{% endif %}"
    );

    assertParityForTemplates(templates);
  }

  @Test
  public void itProducesSameResultsForFilterChainInLoops() {
    List<String> templates = ImmutableList.of(
      "{% for item in items|sort %}{{ item|upper }}{% endfor %}",
      "{% for item in items|reverse %}{{ item|capitalize }}{% endfor %}",
      "{% for n in numbers|sort|unique %}{{ n }}{% endfor %}"
    );

    assertParityForTemplates(templates);
  }

  @Test
  public void itProducesSameResultsForLongFilterChains() {
    List<String> templates = ImmutableList.of(
      "{{ text|upper|lower|capitalize|trim }}",
      "{{ text|trim|lower|upper|lower|capitalize }}",
      "{{ name|trim|lower|upper|lower|upper|lower }}",
      "{{ text|replace('the', 'a')|upper|lower|capitalize|trim }}",
      "{{ items|sort|reverse|join(', ')|upper|truncate(20) }}"
    );

    assertParityForTemplates(templates);
  }

  @Test
  public void itTracksResolvedValuesConsistently() {
    String template = "{{ name|trim|lower|upper }}";

    RenderResult optimizedResult = jinjavaOptimized.renderForResult(template, context);
    RenderResult unoptimizedResult = jinjavaUnoptimized.renderForResult(
      template,
      context
    );

    assertThat(optimizedResult.getOutput())
      .as("Output should match")
      .isEqualTo(unoptimizedResult.getOutput());

    Set<String> optimizedResolved = optimizedResult.getContext().getResolvedValues();
    Set<String> unoptimizedResolved = unoptimizedResult.getContext().getResolvedValues();

    assertThat(optimizedResolved).as("Resolved filter:trim").contains("filter:trim");
    assertThat(optimizedResolved).as("Resolved filter:lower").contains("filter:lower");
    assertThat(optimizedResolved).as("Resolved filter:upper").contains("filter:upper");

    assertThat(unoptimizedResolved)
      .as("Unoptimized resolved filter:trim")
      .contains("filter:trim");
    assertThat(unoptimizedResolved)
      .as("Unoptimized resolved filter:lower")
      .contains("filter:lower");
    assertThat(unoptimizedResolved)
      .as("Unoptimized resolved filter:upper")
      .contains("filter:upper");
  }

  @Test
  public void itHandlesUnknownFiltersConsistently() {
    String template = "{{ name|unknownfilter }}";

    RenderResult optimizedResult = jinjavaOptimized.renderForResult(template, context);
    RenderResult unoptimizedResult = jinjavaUnoptimized.renderForResult(
      template,
      context
    );

    assertThat(optimizedResult.getOutput())
      .as("Both paths should return empty for unknown filter")
      .isEqualTo(unoptimizedResult.getOutput());
  }

  @Test
  public void itProducesSameResultsForEmptyInputs() {
    context.put("empty_string", "");

    List<String> templates = ImmutableList.of(
      "{{ empty_string|upper }}",
      "{{ empty_string|trim }}",
      "{{ empty_string|default('fallback') }}",
      "{{ empty_string|length }}",
      "{{ empty_list|join(', ') }}",
      "{{ empty_list|first }}",
      "{{ empty_list|last }}",
      "{{ empty_list|length }}"
    );

    assertParityForTemplates(templates);
  }

  @Test
  public void itProducesSameResultsForSpecialCharacters() {
    List<String> templates = ImmutableList.of(
      "{{ special_chars|escape }}",
      "{{ special_chars|escape|upper }}",
      "{{ special_chars|urlencode }}",
      "{{ special_chars|replace('&', 'and') }}",
      "{{ unicode|upper }}",
      "{{ unicode|lower }}",
      "{{ unicode|length }}"
    );

    assertParityForTemplates(templates);
  }

  @Test
  public void itProducesSameResultsForBase64Filters() {
    context.put("plain_text", "Hello, World!");
    context.put("base64_text", "SGVsbG8sIFdvcmxkIQ==");

    List<String> templates = ImmutableList.of(
      "{{ plain_text|b64encode }}",
      "{{ base64_text|b64decode }}",
      "{{ plain_text|b64encode|b64decode }}"
    );

    assertParityForTemplates(templates);
  }

  @Test
  public void itProducesSameResultsForSelectAndRejectFilters() {
    List<String> templates = ImmutableList.of(
      "{{ numbers|select('even')|list }}",
      "{{ numbers|select('odd')|list }}",
      "{{ numbers|reject('even')|list }}",
      "{{ numbers|select('>', 3)|list }}",
      "{{ numbers|select('>=', 4)|list }}",
      "{{ numbers|reject('>', 5)|list }}"
    );

    assertParityForTemplates(templates);
  }

  @Test
  public void itProducesSameResultsForAttrFilters() {
    List<String> templates = ImmutableList.of(
      "{{ objects|map(attribute='name')|list }}",
      "{{ objects|map(attribute='age')|list }}",
      "{{ objects|selectattr('age', '>', 28)|map(attribute='name')|list }}",
      "{{ objects|rejectattr('age', '<', 30)|map(attribute='name')|list }}",
      "{{ objects|groupby('age') }}"
    );

    assertParityForTemplates(templates);
  }

  private void assertParityForTemplates(List<String> templates) {
    for (String template : templates) {
      String optimizedResult = jinjavaOptimized.render(template, context);
      String unoptimizedResult = jinjavaUnoptimized.render(template, context);
      assertThat(optimizedResult)
        .as("Template: %s", template)
        .isEqualTo(unoptimizedResult);
    }
  }
}
