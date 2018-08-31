package com.hubspot.jinjava.el;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.Context.Library;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorItem;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.objects.PyWrapper;
import com.hubspot.jinjava.objects.date.PyishDate;

@SuppressWarnings("unchecked")
public class ExpressionResolverTest {

  private JinjavaInterpreter interpreter;
  private Context context;
  private Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
    interpreter = jinjava.newInterpreter();
    context = interpreter.getContext();
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
    assertThat(interpreter.resolveELExpression("'2013-12-08 16:00:00+00:00' > '2013-12-08 13:00:00+00:00'",
                                               -1)).isEqualTo(Boolean.TRUE);
    assertThat(interpreter.resolveELExpression("foo == \"white\"", -1)).isEqualTo(Boolean.TRUE);
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

    MyCustomMap dict = new MyCustomMap();
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

    MyCustomMap dict = new MyCustomMap();
    context.put("thedict", dict);

    Object val = interpreter.resolveELExpression("thedict.size", -1);
    assertThat(val).isEqualTo("777");

    Object val1 = interpreter.resolveELExpression("thedict.size()", -1);
    assertThat(val1).isEqualTo(3);

    Object val2 = interpreter.resolveELExpression("thedict.items()", -1);
    assertThat(val2.toString()).isEqualTo("[foo=bar, two=2, size=777]");
  }

  public static final class MyCustomMap implements Map<String, String> {

    Map<String, String> data = ImmutableMap.of("foo", "bar", "two", "2", "size", "777");

    @Override
    public int size() {
      return data.size();
    }

    @Override
    public boolean isEmpty() {
      return data.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
      return data.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
      return data.containsValue(value);
    }

    @Override
    public String get(Object key) {
      return data.get(key);
    }

    @Override
    public String put(String key, String value) {
      return null;
    }

    @Override
    public String remove(Object key) {
      return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set<String> keySet() {
      return data.keySet();
    }

    @Override
    public Collection<String> values() {
      return data.values();
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
      return data.entrySet();
    }
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

  public static class MyCustomList<T> extends ForwardingList<T> implements PyWrapper {
    private final List<T> list;

    public MyCustomList(List<T> list) {
      this.list = list;
    }

    @Override
    protected List<T> delegate() {
      return list;
    }

    public int getTotalCount() {
      return list.size();
    }
  }

  @Test
  public void itRecordsFilterNames() {
    Object val = interpreter.resolveELExpression("2.3 | round", -1);
    assertThat(val).isEqualTo(new BigDecimal(2));
    assertThat(interpreter.getContext().wasValueResolved("filter:round")).isTrue();
  }

  @Test
  public void callCustomListProperty() {
    List<Integer> myList = new MyCustomList<>(Lists.newArrayList(1, 2, 3, 4));

    context.put("mylist", myList);
    Object val = interpreter.resolveELExpression("mylist.total_count", -1);
    assertThat(val).isEqualTo(4);
  }

  @Test
  public void complexInWithOrCondition() {
    context.put("foo", "this is<hr>something");
    context.put("bar", "this is<hr/>something");

    assertThat(interpreter.resolveELExpression("\"<hr>\" in foo or \"<hr/>\" in foo", -1)).isEqualTo(true);
    assertThat(interpreter.resolveELExpression("\"<hr>\" in bar or \"<hr/>\" in bar", -1)).isEqualTo(true);
    assertThat(interpreter.resolveELExpression("\"<har>\" in foo or \"<har/>\" in foo", -1)).isEqualTo(false);
  }

  @Test
  public void unknownProperty() {
    interpreter.resolveELExpression("foo", 23);
    assertThat(interpreter.getErrorsCopy()).isEmpty();

    context.put("foo", new Object());
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
    context.put("myobj", new MyClass(new Date(0)));
    Object result = interpreter.resolveELExpression("myobj.date", -1);
    assertThat(result).isInstanceOf(PyishDate.class);
    assertThat(result.toString()).isEqualTo("1970-01-01 00:00:00");
  }

  @Test
  public void blackListedProperties() {
    context.put("myobj", new MyClass(new Date(0)));
    interpreter.resolveELExpression("myobj.class.methods[0]", -1);

    assertThat(interpreter.getErrorsCopy()).isNotEmpty();
    TemplateError e = interpreter.getErrorsCopy().get(0);
    assertThat(e.getReason()).isEqualTo(ErrorReason.UNKNOWN);
    assertThat(e.getFieldName()).isEqualTo("class");
    assertThat(e.getMessage()).contains("Cannot resolve property 'class'");
  }

  @Test
  public void itWillNotReturnClassObjectProperties() {
    context.put("myobj", new MyClass(new Date(0)));
    Object clazz = interpreter.resolveELExpression("myobj.clazz", -1);
    assertThat(clazz).isNull();
  }

  @Test
  public void blackListedMethods() {
    context.put("myobj", new MyClass(new Date(0)));
    interpreter.resolveELExpression("myobj.wait()", -1);

    assertThat(interpreter.getErrorsCopy()).isNotEmpty();
    TemplateError e = interpreter.getErrorsCopy().get(0);
    assertThat(e.getMessage()).contains("Cannot find method 'wait'");
  }

  @Test
  public void itWillNotReturnClassObjects() {
    context.put("myobj", new MyClass(new Date(0)));
    interpreter.resolveELExpression("myobj.getClass()", -1);

    assertThat(interpreter.getErrorsCopy()).isNotEmpty();
    TemplateError e = interpreter.getErrorsCopy().get(0);
    assertThat(e.getMessage()).contains("Cannot find method 'getClass'");
  }


  @Test
  public void itBlocksDisabledTags() {

    Map<Context.Library, Set<String>> disabled = ImmutableMap.of(Context.Library.TAG, ImmutableSet.of("raw"));
    assertThat(interpreter.render("{% raw %}foo{% endraw %}")).isEqualTo("foo");

    try (JinjavaInterpreter.InterpreterScopeClosable c = interpreter.enterScope(disabled)) {
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

    Map<Context.Library, Set<String>> disabled = ImmutableMap.of(Context.Library.TAG, ImmutableSet.of("raw"));
    assertThat(interpreter.render(jinja)).isEqualTo("top before raw after\n");

    try (JinjavaInterpreter.InterpreterScopeClosable c = interpreter.enterScope(disabled)) {
      interpreter.render(jinja);
    }
    TemplateError e = interpreter.getErrorsCopy().get(0);
    assertThat(e.getItem()).isEqualTo(ErrorItem.TAG);
    assertThat(e.getReason()).isEqualTo(ErrorReason.DISABLED);
    assertThat(e.getMessage()).contains("'raw' is disabled in this context");
  }

  @Test
  public void itBlocksDisabledFilters() {

    Map<Context.Library, Set<String>> disabled = ImmutableMap.of(Context.Library.FILTER, ImmutableSet.of("truncate"));
    assertThat(interpreter.resolveELExpression("\"hey\"|truncate(2)", -1)).isEqualTo("h...");

    try (JinjavaInterpreter.InterpreterScopeClosable c = interpreter.enterScope(disabled)) {
      interpreter.resolveELExpression("\"hey\"|truncate(2)", -1);
      TemplateError e = interpreter.getErrorsCopy().get(0);
      assertThat(e.getItem()).isEqualTo(ErrorItem.FILTER);
      assertThat(e.getReason()).isEqualTo(ErrorReason.DISABLED);
      assertThat(e.getMessage()).contains("truncate' is disabled in this context");
    }
  }

  @Test
  public void itBlocksDisabledFunctions() {

    Map<Context.Library, Set<String>> disabled = ImmutableMap.of(Library.FUNCTION, ImmutableSet.of(":range"));

    String template = "hi {% for i in range(1, 3) %}{{i}} {% endfor %}";

    String rendered = jinjava.render(template, context);
    assertEquals("hi 1 2 ", rendered);

    final JinjavaConfig config = JinjavaConfig.newBuilder().withDisabled(disabled).build();

    final RenderResult renderResult = jinjava.renderForResult(template, context, config);
    assertEquals("hi  ", renderResult.getOutput());
    TemplateError e = renderResult.getErrors().get(0);
    assertThat(e.getItem()).isEqualTo(ErrorItem.FUNCTION);
    assertThat(e.getReason()).isEqualTo(ErrorReason.DISABLED);
    assertThat(e.getMessage()).contains("':range' is disabled in this context");
  }

  @Test
  public void itBlocksDisabledExpTests() {

    Map<Context.Library, Set<String>> disabled = ImmutableMap.of(Context.Library.EXP_TEST, ImmutableSet.of("even"));
    assertThat(interpreter.render("{% if 2 is even %}yes{% endif %}")).isEqualTo("yes");

    try (JinjavaInterpreter.InterpreterScopeClosable c = interpreter.enterScope(disabled)) {
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
    final JinjavaConfig config = JinjavaConfig.newBuilder().build();
    String template = "{% for i in range(1, 5) %}{{i}} {% endfor %}\n{{ unixtimestamp(datetime) }}";
    final RenderResult renderResult = jinjava.renderForResult(template, context, config);
    assertThat(renderResult.getOutput()).isEqualTo("1 2 3 4 \n12000");
    assertThat(renderResult.getContext().getResolvedFunctions()).hasSameElementsAs(ImmutableSet.of(":range", ":unixtimestamp"));
  }

  @Test
  public void presentOptionalProperty() {
    context.put("myobj", new OptionalProperty(null, "foo"));
    assertThat(interpreter.resolveELExpression("myobj.val", -1)).isEqualTo("foo");
    assertThat(interpreter.getErrorsCopy()).isEmpty();
  }

  @Test
  public void emptyOptionalProperty() {
    context.put("myobj", new OptionalProperty(null, null));
    assertThat(interpreter.resolveELExpression("myobj.val", -1)).isNull();
    assertThat(interpreter.getErrorsCopy()).isEmpty();
  }

  @Test
  public void presentNestedOptionalProperty() {
    context.put("myobj", new OptionalProperty(new MyClass(new Date(0)), "foo"));
    assertThat(Objects.toString(interpreter.resolveELExpression("myobj.nested.date", -1))).isEqualTo(
        "1970-01-01 00:00:00");
    assertThat(interpreter.getErrorsCopy()).isEmpty();
  }

  @Test
  public void emptyNestedOptionalProperty() {
    context.put("myobj", new OptionalProperty(null, null));
    assertThat(interpreter.resolveELExpression("myobj.nested.date", -1)).isNull();
    assertThat(interpreter.getErrorsCopy()).isEmpty();
  }

  @Test
  public void presentNestedNestedOptionalProperty() {
    context.put("myobj", new NestedOptionalProperty(new OptionalProperty(new MyClass(new Date(0)), "foo")));
    assertThat(Objects.toString(interpreter.resolveELExpression("myobj.nested.nested.date", -1))).isEqualTo(
        "1970-01-01 00:00:00");
    assertThat(interpreter.getErrorsCopy()).isEmpty();
  }

  public static final class MyClass {
    private Date date;

    MyClass(Date date) {
      this.date = date;
    }

    public Class getClazz() { return this.getClass(); }

    public Date getDate() {
      return date;
    }
  }

  public static final class OptionalProperty {
    private MyClass nested;
    private String val;

    OptionalProperty(MyClass nested, String val) {
      this.nested = nested;
      this.val = val;
    }

    public Optional<MyClass> getNested() {
      return Optional.ofNullable(nested);
    }

    public Optional<String> getVal() {
      return Optional.ofNullable(val);
    }
  }

  public static final class NestedOptionalProperty {
    private OptionalProperty nested;

    public NestedOptionalProperty(OptionalProperty nested) {
      this.nested = nested;
    }

    public Optional<OptionalProperty> getNested() {
      return Optional.ofNullable(nested);
    }
  }
}
