package com.hubspot.jinjava.el;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.Context.Library;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.LazyExpression;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorItem;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.objects.date.PyishDate;
import com.hubspot.jinjava.testobjects.ExpressionResolverTestObjects;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class ExpressionResolverTest {

  private JinjavaInterpreter interpreter;
  private Context context;
  private Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava(BaseJinjavaTest.newConfigBuilder().build());
    interpreter = jinjava.newInterpreter();
    context = interpreter.getContext();
    JinjavaInterpreter.pushCurrent(interpreter);
  }

  @After
  public void teardown() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itResolvesListLiterals() {
    Object val = interpreter.resolveELExpression("['0.5','50']", -1);
    List<Object> list = (List<Object>) val;
    assertThat(list).containsExactly("0.5", "50");
  }

  @Test
  public void itResolvesImmutableListLiterals() {
    Object val = interpreter.resolveELExpression("('0.5','50')", -1);
    List<Object> list = (List<Object>) val;
    assertThat(list).containsExactly("0.5", "50");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testTuplesAreImmutable() {
    Object val = interpreter.resolveELExpression("('0.5','50')", -1);
    List<Object> list = (List<Object>) val;
    list.add("foo");
  }

  @Test
  public void itCanCompareStrings() {
    context.put("foo", "white");
    assertThat(
      interpreter.resolveELExpression(
        "'2013-12-08 16:00:00+00:00' > '2013-12-08 13:00:00+00:00'",
        -1
      )
    )
      .isEqualTo(Boolean.TRUE);
    assertThat(interpreter.resolveELExpression("foo == \"white\"", -1))
      .isEqualTo(Boolean.TRUE);
  }

  @Test
  public void itResolvesUntrimmedExprs() {
    context.put("foo", "bar");
    Object val = interpreter.resolveELExpression("  foo ", -1);
    assertThat(val).isEqualTo("bar");
    assertThat(interpreter.getContext().wasExpressionResolved("foo")).isTrue();
  }

  @Test
  public void itResolvesMathVals() {
    context.put("i_am_seven", 7L);
    Object val = interpreter.resolveELExpression("(i_am_seven * 2 + 1)/3", -1);
    assertThat(val).isEqualTo(5.0);
    assertThat(interpreter.getContext().wasValueResolved("i_am_seven")).isTrue();
  }

  @Test
  public void itResolvesListVal() {
    context.put("thelist", Lists.newArrayList(1L, 2L, 3L));
    Object val = interpreter.resolveELExpression("thelist[1]", -1);
    assertThat(val).isEqualTo(2L);
  }

  @Test
  public void itResolvesListStringNegative() {
    context.put("thelist", Lists.newArrayList("foo", "bar", "blah"));
    Object val = interpreter.resolveELExpression("thelist[-1]", -1);
    assertThat(val).isEqualTo("blah");
  }

  @Test
  public void itResolvesListStringNextToLast() {
    context.put("thelist", Lists.newArrayList("foo", "bar", "blah"));
    Object val = interpreter.resolveELExpression("thelist[-2]", -1);
    assertThat(val).isEqualTo("bar");
  }

  @Test
  public void itResolvesListStringNegativeIndicatingFirst() {
    context.put("thelist", Lists.newArrayList("foo", "bar", "blah"));
    Object val = interpreter.resolveELExpression("thelist[-3]", -1);
    assertThat(val).isEqualTo("foo");
  }

  @Test
  public void itResolvesListStringNegativeZero() {
    context.put("thelist", Lists.newArrayList("foo", "bar", "blah"));
    Object val = interpreter.resolveELExpression("thelist[-0]", -1);
    assertThat(val).isEqualTo("foo");
  }

  @Test
  public void itResolvesListStringNegativeOutOfBounds() {
    context.put("thelist", Lists.newArrayList("foo", "bar", "blah"));
    Object val = interpreter.resolveELExpression("thelist[-4]", -1);
    assertThat(val).isEqualTo(null);
  }

  @Test
  public void itResolvesDictValWithBracket() {
    Map<String, Object> dict = Maps.newHashMap();
    dict.put("foo", "bar");
    context.put("thedict", dict);

    Object val = interpreter.resolveELExpression("thedict['foo']", -1);
    assertThat(val).isEqualTo("bar");
    assertThat(interpreter.getContext().wasExpressionResolved("thedict['foo']")).isTrue();
  }

  @Test
  public void itResolvesDictValWithDotParam() {
    Map<String, Object> dict = Maps.newHashMap();
    dict.put("foo", "bar");
    context.put("thedict", dict);

    Object val = interpreter.resolveELExpression("thedict.foo", -1);
    assertThat(val).isEqualTo("bar");
    assertThat(interpreter.getContext().wasExpressionResolved("thedict.foo")).isTrue();
  }

  @Test
  public void itResolvesMapValOnCustomObject() {
    ExpressionResolverTestObjects.MyCustomMap dict =
      new ExpressionResolverTestObjects.MyCustomMap();
    context.put("thedict", dict);

    Object val = interpreter.resolveELExpression("thedict['foo']", -1);
    assertThat(val).isEqualTo("bar");
    assertThat(interpreter.getContext().wasExpressionResolved("thedict['foo']")).isTrue();

    Object val2 = interpreter.resolveELExpression("thedict.two", -1);
    assertThat(val2).isEqualTo("2");
    assertThat(interpreter.getContext().wasExpressionResolved("thedict.two")).isTrue();
  }

  @Test
  public void itResolvesOtherMethodsOnCustomMapObject() {
    ExpressionResolverTestObjects.MyCustomMap dict =
      new ExpressionResolverTestObjects.MyCustomMap();
    context.put("thedict", dict);

    Object val = interpreter.resolveELExpression("thedict.size", -1);
    assertThat(val).isEqualTo("777");

    Object val1 = interpreter.resolveELExpression("thedict.size()", -1);
    assertThat(val1).isEqualTo(3);

    Object val2 = interpreter.resolveELExpression("thedict.items()", -1);
    assertThat(val2.toString()).isEqualTo("[foo=bar, two=2, size=777]");
  }

  @Test
  public void itResolvesInnerDictVal() {
    Map<String, Object> dict = Maps.newHashMap();
    Map<String, Object> inner = Maps.newHashMap();
    inner.put("test", "val");
    dict.put("inner", inner);
    context.put("thedict", dict);

    Object val = interpreter.resolveELExpression("thedict.inner[\"test\"]", -1);
    assertThat(val).isEqualTo("val");
  }

  @Test
  public void itResolvesInnerListVal() {
    Map<String, Object> dict = Maps.newHashMap();
    List<String> inner = Lists.newArrayList("val");
    dict.put("inner", inner);
    context.put("thedict", dict);

    Object val = interpreter.resolveELExpression("thedict.inner[0]", -1);
    assertThat(val).isEqualTo("val");
  }

  @Test
  public void itRecordsFilterNames() {
    Object val = interpreter.resolveELExpression("2.3 | round", -1);
    assertThat(val).isEqualTo(new BigDecimal(2));
    assertThat(interpreter.getContext().wasValueResolved("filter:round")).isTrue();
  }

  @Test
  public void callCustomListProperty() {
    List<Integer> myList = new ExpressionResolverTestObjects.MyCustomList<>(
      Lists.newArrayList(1, 2, 3, 4)
    );

    context.put("mylist", myList);
    Object val = interpreter.resolveELExpression("mylist.total_count", -1);
    assertThat(val).isEqualTo(4);
  }

  @Test
  public void complexInWithOrCondition() {
    context.put("foo", "this is<hr>something");
    context.put("bar", "this is<hr/>something");

    assertThat(interpreter.resolveELExpression("\"<hr>\" in foo or \"<hr/>\" in foo", -1))
      .isEqualTo(true);
    assertThat(interpreter.resolveELExpression("\"<hr>\" in bar or \"<hr/>\" in bar", -1))
      .isEqualTo(true);
    assertThat(
      interpreter.resolveELExpression("\"<har>\" in foo or \"<har/>\" in foo", -1)
    )
      .isEqualTo(false);
  }

  @Test
  public void unknownProperty() {
    interpreter.resolveELExpression("foo", 23);
    assertThat(interpreter.getErrorsCopy()).isEmpty();

    context.put("foo", "");
    interpreter.resolveELExpression("foo.bar", 23);

    assertThat(interpreter.getErrorsCopy()).hasSize(1);

    TemplateError e = interpreter.getErrorsCopy().get(0);
    assertThat(e.getReason()).isEqualTo(ErrorReason.UNKNOWN);
    assertThat(e.getLineno()).isEqualTo(23);
    assertThat(e.getFieldName()).isEqualTo("bar");
    assertThat(e.getMessage()).contains("Cannot resolve property 'bar'");
  }

  @Test
  public void syntaxError() {
    interpreter.resolveELExpression("(*&W", 123);
    assertThat(interpreter.getErrorsCopy()).hasSize(1);

    TemplateError e = interpreter.getErrorsCopy().get(0);
    assertThat(e.getReason()).isEqualTo(ErrorReason.SYNTAX_ERROR);
    assertThat(e.getLineno()).isEqualTo(123);
    assertThat(e.getMessage()).contains("invalid character");
  }

  @Test
  public void itWrapsDates() {
    context.put("myobj", new ExpressionResolverTestObjects.MyClass(new Date(0)));
    Object result = interpreter.resolveELExpression("myobj.date", -1);
    assertThat(result).isInstanceOf(PyishDate.class);
    assertThat(result.toString()).isEqualTo("1970-01-01 00:00:00");
  }

  @Test
  public void blackListedProperties() {
    context.put("myobj", new ExpressionResolverTestObjects.MyClass(new Date(0)));
    interpreter.resolveELExpression("myobj.class.methods[0]", -1);

    assertThat(interpreter.getErrorsCopy()).isNotEmpty();
    TemplateError e = interpreter.getErrorsCopy().get(0);
    assertThat(e.getReason()).isEqualTo(ErrorReason.UNKNOWN);
    assertThat(e.getFieldName()).isEqualTo("class");
    assertThat(e.getMessage()).contains("Cannot resolve property 'class'");
  }

  @Test
  public void itWillNotReturnClassObjectProperties() {
    context.put("myobj", new ExpressionResolverTestObjects.MyClass(new Date(0)));
    Object clazz = interpreter.resolveELExpression("myobj.clazz", -1);
    assertThat(clazz).isNull();
  }

  @Test
  public void blackListedMethods() {
    context.put("myobj", new ExpressionResolverTestObjects.MyClass(new Date(0)));
    interpreter.resolveELExpression("myobj.wait()", -1);

    assertThat(interpreter.getErrorsCopy()).isNotEmpty();
    TemplateError e = interpreter.getErrorsCopy().get(0);
    assertThat(e.getMessage())
      .contains(
        "Cannot find method wait with 0 parameters in class com.hubspot.jinjava.testobjects.ExpressionResolverTestObjects$MyClass"
      );
  }

  @Test
  public void itWillNotReturnClassObjects() {
    context.put("myobj", new ExpressionResolverTestObjects.MyClass(new Date(0)));
    interpreter.resolveELExpression("myobj.getClass()", -1);

    assertThat(interpreter.getErrorsCopy()).isNotEmpty();
    TemplateError e = interpreter.getErrorsCopy().get(0);
    assertThat(e.getMessage())
      .contains(
        "Cannot find method getClass with 0 parameters in class com.hubspot.jinjava.testobjects.ExpressionResolverTestObjects$MyClass"
      );
  }

  @Test
  public void itBlocksDisabledTags() {
    ImmutableMap<Context.Library, ImmutableSet<String>> disabled = ImmutableMap.of(
      Context.Library.TAG,
      ImmutableSet.of("raw")
    );
    assertThat(interpreter.render("{% raw %}foo{% endraw %}")).isEqualTo("foo");

    try (
      JinjavaInterpreter.InterpreterScopeClosable c = interpreter.enterScope(disabled)
    ) {
      interpreter.render("{% raw %} foo {% endraw %}");
    }

    TemplateError e = interpreter.getErrorsCopy().get(0);
    assertThat(e.getItem()).isEqualTo(ErrorItem.TAG);
    assertThat(e.getReason()).isEqualTo(ErrorReason.DISABLED);
    assertThat(e.getMessage()).contains("'raw' is disabled in this context");
  }

  @Test
  public void itBlocksDisabledTagsInIncludes() {
    final String jinja = "top {% include \"tags/includetag/raw.html\" %}";

    ImmutableMap<Context.Library, ImmutableSet<String>> disabled = ImmutableMap.of(
      Context.Library.TAG,
      ImmutableSet.of("raw")
    );
    assertThat(interpreter.render(jinja)).isEqualTo("top before raw after\n");

    try (
      JinjavaInterpreter.InterpreterScopeClosable c = interpreter.enterScope(disabled)
    ) {
      interpreter.render(jinja);
    }
    TemplateError e = interpreter.getErrorsCopy().get(0);
    assertThat(e.getItem()).isEqualTo(ErrorItem.TAG);
    assertThat(e.getReason()).isEqualTo(ErrorReason.DISABLED);
    assertThat(e.getMessage()).contains("'raw' is disabled in this context");
  }

  @Test
  public void itBlocksDisabledFilters() {
    ImmutableMap<Context.Library, ImmutableSet<String>> disabled = ImmutableMap.of(
      Context.Library.FILTER,
      ImmutableSet.of("truncate")
    );
    assertThat(interpreter.resolveELExpression("\"hey\"|truncate(2)", -1))
      .isEqualTo("h...");

    try (
      JinjavaInterpreter.InterpreterScopeClosable c = interpreter.enterScope(disabled)
    ) {
      interpreter.resolveELExpression("\"hey\"|truncate(2)", -1);
      TemplateError e = interpreter.getErrorsCopy().get(0);
      assertThat(e.getItem()).isEqualTo(ErrorItem.FILTER);
      assertThat(e.getReason()).isEqualTo(ErrorReason.DISABLED);
      assertThat(e.getMessage()).contains("truncate' is disabled in this context");
    }
  }

  @Test
  public void itBlocksDisabledFunctions() {
    ImmutableMap<Context.Library, ImmutableSet<String>> disabled = ImmutableMap.of(
      Library.FUNCTION,
      ImmutableSet.of(":range")
    );

    String template = "hi {% for i in range(1, 3) %}{{i}} {% endfor %}";
    JinjavaInterpreter.popCurrent();

    String rendered = jinjava.render(template, context);
    assertEquals("hi 1 2 ", rendered);

    final JinjavaConfig config = BaseJinjavaTest
      .newConfigBuilder()
      .withDisabled(disabled)
      .build();

    final RenderResult renderResult = jinjava.renderForResult(template, context, config);
    assertEquals("hi ", renderResult.getOutput());
    TemplateError e = renderResult.getErrors().get(0);
    assertThat(e.getItem()).isEqualTo(ErrorItem.FUNCTION);
    assertThat(e.getReason()).isEqualTo(ErrorReason.DISABLED);
    assertThat(e.getMessage()).contains("':range' is disabled in this context");
  }

  @Test
  public void itBlocksDisabledExpTests() {
    ImmutableMap<Context.Library, ImmutableSet<String>> disabled = ImmutableMap.of(
      Context.Library.EXP_TEST,
      ImmutableSet.of("even")
    );
    assertThat(interpreter.render("{% if 2 is even %}yes{% endif %}")).isEqualTo("yes");

    try (
      JinjavaInterpreter.InterpreterScopeClosable c = interpreter.enterScope(disabled)
    ) {
      interpreter.render("{% if 2 is even %}yes{% endif %}");
      TemplateError e = interpreter.getErrorsCopy().get(0);
      assertThat(e.getItem()).isEqualTo(ErrorItem.EXPRESSION_TEST);
      assertThat(e.getReason()).isEqualTo(ErrorReason.DISABLED);
      assertThat(e.getMessage()).contains("even' is disabled in this context");
    }
  }

  @Test
  public void itStoresResolvedFunctions() {
    context.put("datetime", 12345);
    final JinjavaConfig config = BaseJinjavaTest.newConfigBuilder().build();
    String template =
      "{% for i in range(1, 5) %}{{i}} {% endfor %}\n{{ unixtimestamp(datetime) }}";
    final RenderResult renderResult = jinjava.renderForResult(template, context, config);
    assertThat(renderResult.getOutput()).isEqualTo("1 2 3 4 \n12345");
    assertThat(renderResult.getContext().getResolvedFunctions())
      .hasSameElementsAs(ImmutableSet.of(":range", ":unixtimestamp"));
  }

  @Test
  public void presentOptionalProperty() {
    context.put("myobj", new ExpressionResolverTestObjects.OptionalProperty(null, "foo"));
    assertThat(interpreter.resolveELExpression("myobj.val", -1)).isEqualTo("foo");
    assertThat(interpreter.getErrorsCopy()).isEmpty();
  }

  @Test
  public void emptyOptionalProperty() {
    context.put("myobj", new ExpressionResolverTestObjects.OptionalProperty(null, null));
    assertThat(interpreter.resolveELExpression("myobj.val", -1)).isNull();
    assertThat(interpreter.getErrorsCopy()).isEmpty();
  }

  @Test
  public void presentNestedOptionalProperty() {
    context.put(
      "myobj",
      new ExpressionResolverTestObjects.OptionalProperty(
        new ExpressionResolverTestObjects.MyClass(new Date(0)),
        "foo"
      )
    );
    assertThat(Objects.toString(interpreter.resolveELExpression("myobj.nested.date", -1)))
      .isEqualTo("1970-01-01 00:00:00");
    assertThat(interpreter.getErrorsCopy()).isEmpty();
  }

  @Test
  public void emptyNestedOptionalProperty() {
    context.put("myobj", new ExpressionResolverTestObjects.OptionalProperty(null, null));
    assertThat(interpreter.resolveELExpression("myobj.nested.date", -1)).isNull();
    assertThat(interpreter.getErrorsCopy()).isEmpty();
  }

  @Test
  public void presentNestedNestedOptionalProperty() {
    context.put(
      "myobj",
      new ExpressionResolverTestObjects.NestedOptionalProperty(
        new ExpressionResolverTestObjects.OptionalProperty(
          new ExpressionResolverTestObjects.MyClass(new Date(0)),
          "foo"
        )
      )
    );
    assertThat(
      Objects.toString(interpreter.resolveELExpression("myobj.nested.nested.date", -1))
    )
      .isEqualTo("1970-01-01 00:00:00");
    assertThat(interpreter.getErrorsCopy()).isEmpty();
  }

  @Test
  public void itResolvesLazyExpressionsToTheirUnderlyingValue() {
    ExpressionResolverTestObjects.TestClass testClass =
      new ExpressionResolverTestObjects.TestClass();
    Supplier<String> lazyString = () -> result("hallelujah", testClass);

    context.put("myobj", ImmutableMap.of("test", LazyExpression.of(lazyString, "")));

    assertThat(Objects.toString(interpreter.resolveELExpression("myobj.test", -1)))
      .isEqualTo("hallelujah");
    assertThat(interpreter.getErrorsCopy()).isEmpty();
    assertThat(testClass.isTouched()).isTrue();
  }

  @Test
  public void itResolvesNullLazyExpressions() {
    Supplier<Object> lazyNull = () -> null;
    context.put("nullobj", LazyExpression.of(lazyNull, ""));
    assertThat(interpreter.resolveELExpression("nullobj", -1)).isNull();
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itResolvesSuppliersOnlyIfResolved() {
    ExpressionResolverTestObjects.TestClass testClass =
      new ExpressionResolverTestObjects.TestClass();
    Supplier<String> lazyString = () -> result("hallelujah", testClass);

    context.put(
      "myobj",
      ImmutableMap.of("test", LazyExpression.of(lazyString, ""), "nope", "test")
    );

    assertThat(Objects.toString(interpreter.resolveELExpression("myobj.nope", -1)))
      .isEqualTo("test");
    assertThat(interpreter.getErrorsCopy()).isEmpty();
    assertThat(testClass.isTouched()).isFalse();
  }

  @Test
  public void itResolvesLazyExpressionsInNested() {
    Supplier<ExpressionResolverTestObjects.TestClass> lazyObject =
      ExpressionResolverTestObjects.TestClass::new;

    context.put("myobj", ImmutableMap.of("test", LazyExpression.of(lazyObject, "")));

    assertThat(Objects.toString(interpreter.resolveELExpression("myobj.test.name", -1)))
      .isEqualTo("Amazing test class");
    assertThat(interpreter.getErrorsCopy()).isEmpty();
  }

  @Test
  public void itResolvesAlternateExpTestSyntax() {
    assertThat(interpreter.render("{% if 2 is even %}yes{% endif %}")).isEqualTo("yes");

    assertThat(
      interpreter.render("{% if exptest:even.evaluate(2, null) %}yes{% endif %}")
    )
      .isEqualTo("yes");
    assertThat(
      interpreter.render("{% if exptest:false.evaluate(false, null) %}yes{% endif %}")
    )
      .isEqualTo("yes");
  }

  @Test
  public void itResolvesAlternateExpTestSyntaxForTrueAndFalseExpTests() {
    assertThat(
      interpreter.render("{% if exptest:false.evaluate(false, null) %}yes{% endif %}")
    )
      .isEqualTo("yes");
    assertThat(
      interpreter.render("{% if exptest:true.evaluate(true, null) %}yes{% endif %}")
    )
      .isEqualTo("yes");
  }

  @Test
  public void itResolvesAlternateExpTestSyntaxForInExpTests() {
    assertThat(
      interpreter.render("{% if exptest:in.evaluate(1, null, [1]) %}yes{% endif %}")
    )
      .isEqualTo("yes");
    assertThat(
      interpreter.render(
        "{% if exptest:in.evaluate(2, null, [1]) %}yes{% else %}no{% endif %}"
      )
    )
      .isEqualTo("no");
  }

  @Test
  public void itAddsErrorRenderingUnclosedExpression() {
    interpreter.resolveELExpression("{", 1);
    assertThat(interpreter.getErrors().get(0).getMessage())
      .contains(
        "Error parsing '{': syntax error at position 4, encountered 'null', expected '}'"
      );
  }

  @Test
  public void itAddsInvalidInputErrorWhenArithmeticExceptionIsThrown() {
    String render = interpreter.render("{% set n = 12/0|round %}{{n}}");
    assertThat(interpreter.getErrors().get(0).getMessage())
      .contains(
        "ArithmeticException when resolving expression [[ 12/0|round ]]: ArithmeticException: / by zero"
      );
    assertThat(interpreter.getErrors().get(0).getReason())
      .isEqualTo(ErrorReason.INVALID_INPUT);
  }

  public String result(String value, ExpressionResolverTestObjects.TestClass testClass) {
    testClass.touch();
    return value;
  }
}
