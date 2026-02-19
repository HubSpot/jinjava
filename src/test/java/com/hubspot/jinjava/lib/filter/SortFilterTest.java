package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import com.hubspot.jinjava.testobjects.SortFilterTestObjects;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class SortFilterTest extends BaseJinjavaTest {

  @Test
  public void sortAsc() {
    assertThat(render(4, 2, 1, 3)).isEqualTo("1234");
  }

  @Test
  public void sortDesc() {
    assertThat(render("(true)", 4, 2, 1, 3)).isEqualTo("4321");
  }

  @Test
  public void sortStringsCaseSensitive() {
    assertThat(render("(false, true)", "foo", "Foo", "bar")).isEqualTo("Foobarfoo");
  }

  @Test
  public void sortWithNamedAttributes() throws Exception {
    // even if named attributes were never supported for this filter, ensure parameters are passed in order and it works
    assertThat(
      render(
        "(reverse=false, case_sensitive=false, attribute='foo.date')",
        new SortFilterTestObjects.MyBar(new SortFilterTestObjects.MyFoo(new Date(250L))),
        new SortFilterTestObjects.MyBar(new SortFilterTestObjects.MyFoo(new Date(0L))),
        new SortFilterTestObjects.MyBar(
          new SortFilterTestObjects.MyFoo(new Date(100000000L))
        )
      )
    )
      .isEqualTo("0250100000000");
  }

  @Test
  public void sortStringsCaseInsensitive() {
    assertThat(render("()", "foo", "Foo", "bar")).isEqualTo("barfooFoo");
  }

  @Test
  public void sortWithAttr() {
    assertThat(
      render(
        "(false, false, 'date')",
        new SortFilterTestObjects.MyFoo(new Date(250L)),
        new SortFilterTestObjects.MyFoo(new Date(0L)),
        new SortFilterTestObjects.MyFoo(new Date(100000000L))
      )
    )
      .isEqualTo("0250100000000");
  }

  @Test
  public void sortWithNestedAttr() {
    assertThat(
      render(
        "(false, false, 'foo.date')",
        new SortFilterTestObjects.MyBar(new SortFilterTestObjects.MyFoo(new Date(250L))),
        new SortFilterTestObjects.MyBar(new SortFilterTestObjects.MyFoo(new Date(0L))),
        new SortFilterTestObjects.MyBar(
          new SortFilterTestObjects.MyFoo(new Date(100000000L))
        )
      )
    )
      .isEqualTo("0250100000000");
  }

  @Test
  public void itThrowsInvalidArgumentExceptionOnNullAttribute() {
    RenderResult result = renderForResult(
      "(false, false, null)",
      new SortFilterTestObjects.MyFoo(new Date(250L)),
      new SortFilterTestObjects.MyFoo(new Date(0L)),
      new SortFilterTestObjects.MyFoo(new Date(100000000L))
    );
    assertThat(result.getOutput()).isEmpty();
    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getSeverity()).isEqualTo(ErrorType.FATAL);
    assertThat(result.getErrors().get(0).getMessage()).contains("cannot be null");
  }

  @Test
  public void itThrowsInvalidArgumentWhenObjectAttributeIsNull() {
    RenderResult result = renderForResult(
      "(false, false, 'doesNotResolve')",
      new SortFilterTestObjects.MyFoo(new Date(250L)),
      new SortFilterTestObjects.MyFoo(new Date(0L)),
      new SortFilterTestObjects.MyFoo(new Date(100000000L))
    );
    assertThat(result.getOutput()).isEmpty();
    assertThat(result.getErrors()).hasSize(2);
    assertThat(result.getErrors().get(1).getSeverity()).isEqualTo(ErrorType.FATAL);
    assertThat(result.getErrors().get(1).getMessage())
      .contains("must be a valid attribute of every item in the list");
  }

  @Test
  public void itThrowsInvalidInputWhenListContainsNull() {
    RenderResult result = renderForResult(
      "(false, false)",
      new SortFilterTestObjects.MyFoo(new Date(250L)),
      new SortFilterTestObjects.MyFoo(new Date(0L)),
      null,
      new SortFilterTestObjects.MyFoo(new Date(100000000L))
    );
    assertThat(result.getOutput()).isEmpty();
    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getSeverity()).isEqualTo(ErrorType.FATAL);
    assertThat(result.getErrors().get(0).getMessage())
      .contains("cannot contain a null item");
  }

  String render(Object... items) {
    return render("", items);
  }

  String render(String sortExtra, Object... items) {
    return renderForResult(sortExtra, items).getOutput();
  }

  RenderResult renderForResult(String sortExtra, Object... items) {
    Map<String, Object> context = new HashMap<>();
    context.put("iterable", items);

    return jinjava.renderForResult(
      "{% for item in iterable|sort" + sortExtra + " %}{{ item }}{% endfor %}",
      context
    );
  }
}
