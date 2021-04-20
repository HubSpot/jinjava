package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.interpret.RenderResult;
import java.nio.charset.StandardCharsets;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

public class SliceFilterTest extends BaseJinjavaTest {

  @Test
  public void itSlicesLists() throws Exception {
    Document dom = Jsoup.parseBodyFragment(
      jinjava.render(
        Resources.toString(
          Resources.getResource("filter/slice-filter.jinja"),
          StandardCharsets.UTF_8
        ),
        ImmutableMap.of(
          "items",
          (Object) Lists.newArrayList("a", "b", "c", "d", "e", "f", "g")
        )
      )
    );

    assertThat(dom.select(".columwrapper ul")).hasSize(3);
    assertThat(dom.select(".columwrapper .column-1 li")).hasSize(3);
    assertThat(dom.select(".columwrapper .column-2 li")).hasSize(3);
    assertThat(dom.select(".columwrapper .column-3 li")).hasSize(1);
  }

  @Test
  public void itSlicesListWithReplacement() throws Exception {
    String result = jinjava.render(
      Resources.toString(
        Resources.getResource("filter/slice-filter-replacement.jinja"),
        StandardCharsets.UTF_8
      ),
      ImmutableMap.of("items", (Object) Lists.newArrayList("a", "b", "c", "d", "e"))
    );

    assertThat(result)
      .isEqualTo(
        "\n" +
        "  1\n" +
        "    a\n" +
        "    b\n" +
        "  2\n" +
        "    c\n" +
        "    d\n" +
        "  3\n" +
        "    e\n" +
        "    hello\n" +
        ""
      );
  }

  @Test
  public void itSlicesListWithReplacementButDivisibleSlices() throws Exception {
    String result = jinjava.render(
      Resources.toString(
        Resources.getResource("filter/slice-filter-replacement.jinja"),
        StandardCharsets.UTF_8
      ),
      ImmutableMap.of("items", (Object) Lists.newArrayList("a", "b", "c", "d", "e", "f"))
    );

    assertThat(result)
      .isEqualTo(
        "\n" +
        "  1\n" +
        "    a\n" +
        "    b\n" +
        "  2\n" +
        "    c\n" +
        "    d\n" +
        "  3\n" +
        "    e\n" +
        "    f\n" +
        ""
      );
  }

  @Test
  public void itSlicesEmptyList() throws Exception {
    String result = jinjava.render(
      Resources.toString(
        Resources.getResource("filter/slice-filter-empty.jinja"),
        StandardCharsets.UTF_8
      ),
      ImmutableMap.of("items", (Object) Lists.newArrayList())
    );

    assertThat(result).isEqualTo("\n");
  }

  @Test
  public void itAddsErrorOnNegativeSlice() throws Exception {
    RenderResult result = jinjava.renderForResult(
      Resources.toString(
        Resources.getResource("filter/slice-filter-negative.jinja"),
        StandardCharsets.UTF_8
      ),
      ImmutableMap.of("items", (Object) Lists.newArrayList())
    );

    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getMessage())
      .contains("with value -1 must be a positive number");
  }

  @Test
  public void itAddsErrorOnZeroSlice() throws Exception {
    RenderResult result = jinjava.renderForResult(
      Resources.toString(
        Resources.getResource("filter/slice-filter-zero.jinja"),
        StandardCharsets.UTF_8
      ),
      ImmutableMap.of("items", (Object) Lists.newArrayList())
    );

    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getMessage())
      .contains("with value 0 must be a positive number");
  }
}
