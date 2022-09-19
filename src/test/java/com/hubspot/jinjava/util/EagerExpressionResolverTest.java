package com.hubspot.jinjava.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.el.ext.AbstractCallableMethod;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.objects.collections.PyList;
import com.hubspot.jinjava.objects.collections.PyMap;
import com.hubspot.jinjava.objects.date.PyishDate;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import com.hubspot.jinjava.random.RandomNumberGeneratorStrategy;
import com.hubspot.jinjava.tree.parse.DefaultTokenScannerSymbols;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.tree.parse.TokenScannerSymbols;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EagerExpressionResolverTest {
  private static final TokenScannerSymbols SYMBOLS = new DefaultTokenScannerSymbols();

  private JinjavaInterpreter interpreter;
  private TagToken tagToken;
  private Context context;

  @Before
  public void setUp() throws Exception {
    JinjavaInterpreter.pushCurrent(getInterpreter(false));
  }

  private JinjavaInterpreter getInterpreter(boolean evaluateMapKeys) throws Exception {
    Jinjava jinjava = new Jinjava(
      JinjavaConfig
        .newBuilder()
        .withExecutionMode(EagerExecutionMode.instance())
        .withRandomNumberGeneratorStrategy(RandomNumberGeneratorStrategy.DEFERRED)
        .withLegacyOverrides(
          LegacyOverrides.newBuilder().withEvaluateMapKeys(evaluateMapKeys).build()
        )
        .build()
    );
    jinjava
      .getGlobalContext()
      .registerFunction(
        new ELFunctionDefinition(
          "",
          "void_function",
          this.getClass().getDeclaredMethod("voidFunction", int.class)
        )
      );
    jinjava
      .getGlobalContext()
      .registerFunction(
        new ELFunctionDefinition(
          "",
          "is_null",
          this.getClass().getDeclaredMethod("isNull", Object.class, Object.class)
        )
      );
    jinjava
      .getGlobalContext()
      .registerFunction(
        new ELFunctionDefinition(
          "",
          "sleeper",
          this.getClass().getDeclaredMethod("sleeper")
        )
      );
    interpreter = new JinjavaInterpreter(jinjava.newInterpreter());
    context = interpreter.getContext();
    context.put("deferred", DeferredValue.instance());
    tagToken = new TagToken("{% foo %}", 1, 2, SYMBOLS);
    return interpreter;
  }

  @After
  public void cleanup() {
    JinjavaInterpreter.popCurrent();
  }

  private EagerExpressionResult eagerResolveExpression(String string) {
    return EagerExpressionResolver.resolveExpression(string, interpreter);
  }

  @Test
  public void itResolvesDeferredBoolean() {
    context.put("foo", "foo_val");
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
      "(111 == 112) or (foo == deferred)"
    );
    String partiallyResolved = eagerExpressionResult.toString();
    assertThat(partiallyResolved).isEqualTo("false || ('foo_val' == deferred)");
    assertThat(eagerExpressionResult.getDeferredWords()).containsExactly("deferred");

    context.put("deferred", "foo_val");
    assertThat(eagerResolveExpression(partiallyResolved).toString()).isEqualTo("true");
    assertThat(interpreter.resolveELExpression(partiallyResolved, 1)).isEqualTo(true);
  }

  @Test
  public void itResolvesDeferredList() {
    context.put("foo", "foo_val");
    context.put("bar", "bar_val");
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
      "[foo == bar, deferred, bar]"
    );
    assertThat(eagerExpressionResult.toString())
      .isEqualTo("[false, deferred, 'bar_val']");
    assertThat(eagerExpressionResult.getDeferredWords())
      .containsExactlyInAnyOrder("deferred");
    context.put("bar", "foo_val");
    eagerExpressionResult = eagerResolveExpression("[foo == bar, deferred, bar]");
    assertThat(eagerExpressionResult.toString()).isEqualTo("[true, deferred, 'foo_val']");
  }

  @Test
  public void itResolvesSimpleBoolean() {
    context.put("foo", true);
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
      "[false || (foo), 'bar']"
    );
    String partiallyResolved = eagerExpressionResult.toString();
    assertThat(partiallyResolved).isEqualTo("[true, 'bar']");
    assertThat(eagerExpressionResult.getDeferredWords()).isEmpty();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void itResolvesRange() {
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression("range(0,2)");
    String partiallyResolved = eagerExpressionResult.toString();
    assertThat(partiallyResolved).isEqualTo("[0, 1]");
    assertThat(eagerExpressionResult.getDeferredWords()).isEmpty();
    // I don't know why this is a list of longs?
    assertThat((List<Long>) interpreter.resolveELExpression(partiallyResolved, 1))
      .contains(0L, 1L);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void itResolvesDeferredRange() throws Exception {
    List<Integer> expectedList = ImmutableList.of(1, 2, 3);
    context.put("foo", 1);
    context.put("bar", 3);
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
      "range(deferred, foo + bar)"
    );
    String partiallyResolved = eagerExpressionResult.toString();
    assertThat(partiallyResolved).isEqualTo("range(deferred, 4)");
    assertThat(eagerExpressionResult.getDeferredWords())
      .containsExactlyInAnyOrder("deferred", "range");

    context.put("deferred", 1);
    assertThat(eagerResolveExpression(partiallyResolved).toString())
      .isEqualTo(PyishObjectMapper.getAsPyishString(expectedList));
    // But this is a list of integers
    assertThat((List<Integer>) interpreter.resolveELExpression(partiallyResolved, 1))
      .isEqualTo(expectedList);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void itResolvesDictionary() {
    Map<String, Object> dict = ImmutableMap.of("foo", "one", "bar", 2L);
    context.put("the_dictionary", dict);

    EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
      "[the_dictionary, 1]"
    );
    String partiallyResolved = eagerExpressionResult.toString();
    assertThat(eagerExpressionResult.getDeferredWords()).isEmpty();
    assertThat(interpreter.resolveELExpression(partiallyResolved, 1))
      .isEqualTo(ImmutableList.of(dict, 1L));
  }

  @Test
  public void itResolvesNested() {
    context.put("foo", 1);
    context.put("bar", 3);
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
      "[foo, range(deferred, bar), range(foo, bar)][0:2]"
    );
    String partiallyResolved = eagerExpressionResult.toString();
    assertThat(partiallyResolved).isEqualTo("[1, range(deferred, 3), [1, 2]][0:2]");
    assertThat(eagerExpressionResult.getDeferredWords())
      .containsExactlyInAnyOrder("deferred", "range");

    context.put("deferred", 2);
    assertThat(eagerResolveExpression(partiallyResolved).toString())
      .isEqualTo("[1, [2]]");
    assertThat(interpreter.resolveELExpression(partiallyResolved, 1))
      .isEqualTo(ImmutableList.of(1L, ImmutableList.of(2)));
  }

  @Test
  public void itSplitsOnNonWords() {
    context.put("foo", 1);
    context.put("bar", 4);
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
      "range(0,foo) + -deferred/bar"
    );
    String partiallyResolved = eagerExpressionResult.toString();
    assertThat(partiallyResolved).isEqualTo("[0] + -deferred / 4");
    assertThat(eagerExpressionResult.getDeferredWords()).containsExactly("deferred");

    context.put("deferred", 2);
    assertThat(eagerResolveExpression(partiallyResolved).toString())
      .isEqualTo("[0, -0.5]");
    assertThat(interpreter.resolveELExpression(partiallyResolved, 1))
      .isEqualTo(ImmutableList.of(0L, -0.5));
  }

  @Test
  public void itSplitsAndIndexesOnNonWords() {
    context.put("foo", 3);
    context.put("bar", 4);
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
      "range(-2,foo)[-1] + -deferred/bar"
    );
    String partiallyResolved = eagerExpressionResult.toString();
    assertThat(partiallyResolved).isEqualTo("2 + -deferred / 4");
    assertThat(eagerExpressionResult.getDeferredWords()).containsExactly("deferred");

    context.put("deferred", 2);
    assertThat(eagerResolveExpression(partiallyResolved).toString()).isEqualTo("1.5");
    assertThat(interpreter.resolveELExpression(partiallyResolved, 1)).isEqualTo(1.5);
  }

  @Test
  public void itSupportsOrderOfOperations() {
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
      "[0,1]|reverse + deferred"
    );
    String partiallyResolved = eagerExpressionResult.toString();
    assertThat(partiallyResolved).isEqualTo("[1, 0] + deferred");
    assertThat(eagerExpressionResult.getDeferredWords()).containsExactly("deferred");

    context.put("deferred", 2L);
    assertThat(eagerResolveExpression(partiallyResolved).toString())
      .isEqualTo("[1, 0, 2]");
    assertThat(interpreter.resolveELExpression(partiallyResolved, 1))
      .isEqualTo(ImmutableList.of(1L, 0L, 2L));
  }

  @Test
  public void itCatchesDeferredVariables() {
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
      "range(0, deferred)"
    );
    String partiallyResolved = eagerExpressionResult.toString();
    assertThat(partiallyResolved).isEqualTo("range(0, deferred)");
    // Since the range function is deferred, it is added to deferredWords.
    assertThat(eagerExpressionResult.getDeferredWords())
      .containsExactlyInAnyOrder("range", "deferred");
  }

  @Test
  public void itDoesntDeferReservedWords() {
    context.put("foo", 0);
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
      "[(foo > 1) || deferred, deferred].append(1)"
    );
    String partiallyResolved = eagerExpressionResult.toString();
    assertThat(partiallyResolved).isEqualTo("[false || deferred, deferred].append(1)");
    assertThat(eagerExpressionResult.getDeferredWords()).doesNotContain("false", "or");
    assertThat(eagerExpressionResult.getDeferredWords()).contains("deferred", ".append");
  }

  @Test
  public void itEvaluatesDict() {
    context.put("foo", new PyMap(ImmutableMap.of("bar", 99)));
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
      "foo.bar == deferred.bar"
    );
    String partiallyResolved = eagerExpressionResult.toString();
    assertThat(partiallyResolved).isEqualTo("99 == deferred.bar");
    assertThat(eagerExpressionResult.getDeferredWords())
      .containsExactlyInAnyOrder("deferred.bar");
  }

  @Test
  public void itSerializesDateProperly() {
    PyishDate date = new PyishDate(
      ZonedDateTime.ofInstant(Instant.ofEpochMilli(1234567890L), ZoneId.systemDefault())
    );
    context.put("date", date);
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression("date");

    assertThat(WhitespaceUtils.unquoteAndUnescape(eagerExpressionResult.toString()))
      .isEqualTo(date.toPyishString().replace("'", "\\'").replace('"', '\''));
  }

  @Test
  public void itHandlesSingleQuotes() {
    context.put("foo", "'");
    context.put("bar", '\'');
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
      "foo ~ ' & ' ~ bar ~ ' & ' ~ '\\'\\\"'"
    );
    assertThat(WhitespaceUtils.unquoteAndUnescape(eagerExpressionResult.toString()))
      .isEqualTo("' & ' & '\"");
  }

  @Test
  public void itHandlesNewlines() {
    context.put("foo", "\n");
    context.put("bar", "\\" + "n"); // Jinja doesn't see this as a newline.
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
      "foo ~ ' & ' ~ bar ~ ' & ' ~ '\\\\' ~ 'n' ~ ' & \\\\n'"
    );
    assertThat(WhitespaceUtils.unquoteAndUnescape(eagerExpressionResult.toString()))
      .isEqualTo("\n & \\n & \\n & \\n");
  }

  @Test
  public void itOutputsUnknownVariablesAsEmpty() {
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
      "contact.some_odd_property"
    );
    assertThat(WhitespaceUtils.unquoteAndUnescape(eagerExpressionResult.toString()))
      .isEqualTo("");
  }

  @Test
  public void itHandlesCancellingSlashes() {
    context.put("foo", "bar");
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
      "foo ~ 'foo\\\\' ~ foo ~ 'foo'"
    );
    assertThat(WhitespaceUtils.unquoteAndUnescape(eagerExpressionResult.toString()))
      .isEqualTo("barfoo\\barfoo");
  }

  @Test
  public void itOutputsEmptyForVoidFunctions() throws Exception {
    assertThat(
        WhitespaceUtils.unquoteAndUnescape(interpreter.render("{{ void_function(2) }}"))
      )
      .isEmpty();
    assertThat(
        WhitespaceUtils.unquoteAndUnescape(
          eagerResolveExpression("void_function(2)").toString()
        )
      )
      .isEmpty();
  }

  @Test
  public void itOutputsNullAsEmptyString() {
    assertThat(eagerResolveExpression("void_function(2)").toString()).isEqualTo("''");
    assertThat(eagerResolveExpression("nothing").toString()).isEqualTo("''");
  }

  @Test
  public void itInterpretsNullAsNull() {
    assertThat(eagerResolveExpression("is_null(nothing, null)").toString())
      .isEqualTo("true");
    assertThat(eagerResolveExpression("is_null(void_function(2), nothing)").toString())
      .isEqualTo("true");
    assertThat(eagerResolveExpression("is_null('', nothing)").toString())
      .isEqualTo("false");
  }

  @Test
  public void itDoesntDeferNull() {
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
      "range(deferred, nothing)"
    );
    assertThat(eagerExpressionResult.toString()).isEqualTo("range(deferred, null)");
    assertThat(eagerExpressionResult.getDeferredWords())
      .containsExactlyInAnyOrder("range", "deferred");
  }

  @Test
  public void itDoesntSplitOnBar() {
    context.put("date", new PyishDate(0L));
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
      "date|datetimeformat('%Y')"
    );
    assertThat(eagerExpressionResult.toString()).isEqualTo("'1970'");
  }

  @Test
  public void itDoesntResolveNonPyishSerializable() {
    PyMap dict = new PyMap(new HashMap<>());
    context.put("dict", dict);
    context.put("foo", new Foo("bar"));
    context.put("mark", "!");
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
      "dict.update({'foo': foo})"
    );
    assertThat(WhitespaceUtils.unquoteAndUnescape(eagerExpressionResult.toString()))
      .isEqualTo("");
    assertThat(dict.get("foo")).isInstanceOf(Foo.class);
    assertThat(((Foo) dict.get("foo")).bar()).isEqualTo("bar");
  }

  @Test
  public void itLeavesPaddedZeros() {
    context.put(
      "zero_date",
      ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault())
    );
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
      "zero_date.strftime('%d')"
    );
    assertThat(WhitespaceUtils.unquoteAndUnescape(eagerExpressionResult.toString()))
      .isEqualTo("01");
  }

  @Test
  public void itPreservesLengthyDoubleStrings() {
    // does not convert to scientific notation
    context.put("small", "0.0000000001");
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression("small");
    assertThat(WhitespaceUtils.unquoteAndUnescape(eagerExpressionResult.toString()))
      .isEqualTo("0.0000000001");
  }

  @Test
  public void itConvertsDoubles() {
    // does convert to scientific notation
    context.put("small", 0.0000000001);
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression("small");
    assertThat(WhitespaceUtils.unquoteAndUnescape(eagerExpressionResult.toString()))
      .isEqualTo("1.0E-10");
  }

  @Test
  public void itDoesntQuoteFloats() {
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression("0.4 + 0.1");
    assertThat(eagerExpressionResult.toString()).isEqualTo("0.5");
  }

  @Test
  public void itHandlesWhitespaceAroundPipe() {
    String lowerFilterString =
      "'AB' | truncate(1) ~ 'BC' |truncate(1) ~ 'CD'| truncate(1)";
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
      lowerFilterString
    );
    assertThat(WhitespaceUtils.unquoteAndUnescape(eagerExpressionResult.toString()))
      .isEqualTo(interpreter.resolveELExpression(lowerFilterString, 0));
  }

  @Test
  public void itHandlesMultipleWhitespaceAroundPipe() {
    String lowerFilterString = "'AB'   |   truncate(1)";
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
      lowerFilterString
    );
    assertThat(WhitespaceUtils.unquoteAndUnescape(eagerExpressionResult.toString()))
      .isEqualTo(interpreter.resolveELExpression(lowerFilterString, 0));
  }

  @Test
  public void itEscapesFormFeed() {
    context.put("foo", "Form feed\f");
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression("foo");
    assertThat(WhitespaceUtils.unquoteAndUnescape(eagerExpressionResult.toString()))
      .isEqualTo("Form feed\f");
  }

  @Test
  public void itHandlesUnconventionalSpacing() {
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
      "(  range (0 , 3 ) [ 1] + deferred) ~ 'YES'| lower"
    );
    String result = WhitespaceUtils.unquoteAndUnescape(eagerExpressionResult.toString());
    assertThat(result).isEqualTo("(1 + deferred) ~ 'yes'");
    context.put("deferred", 2);
    assertThat(interpreter.resolveELExpression(result, 0)).isEqualTo("3yes");
  }

  @Test
  public void itHandlesDotSpacing() {
    context.put("bar", "fake");
    context.put("foo", ImmutableMap.of("bar", "foobar"));
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression("foo . bar");
    String result = WhitespaceUtils.unquoteAndUnescape(eagerExpressionResult.toString());
    assertThat(result).isEqualTo("foobar");
  }

  @Test
  public void itPreservesLegacyDictionaryCreation() {
    context.put("foo", "not_foo");
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression("{foo: 'bar'}");
    String result = WhitespaceUtils.unquoteAndUnescape(eagerExpressionResult.toString());
    assertThat(result).isEqualTo("{'foo': 'bar'}");
  }

  @Test
  public void itHandlesPythonicDictionaryCreation() throws Exception {
    JinjavaInterpreter.pushCurrent(getInterpreter(true));
    try {
      context.put("foo", "not_foo");
      EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
        "{foo: 'bar'}"
      );
      String result = WhitespaceUtils.unquoteAndUnescape(
        eagerExpressionResult.toString()
      );
      assertThat(result).isEqualTo("{'not_foo': 'bar'}");
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }

  @Test
  public void itKeepsPlusSignPrefix() {
    context.put("foo", "+12223334444");
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression("foo");
    assertThat(WhitespaceUtils.unquoteAndUnescape(eagerExpressionResult.toString()))
      .isEqualTo("+12223334444");
  }

  @Test
  public void itHandlesPhoneNumbers() {
    context.put("foo", "+1(123)456-7890");
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression("foo");
    assertThat(WhitespaceUtils.unquoteAndUnescape(eagerExpressionResult.toString()))
      .isEqualTo("+1(123)456-7890");
  }

  @Test
  public void itHandlesNegativeZero() {
    context.put("foo", "-0");
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression("foo");
    assertThat(WhitespaceUtils.unquoteAndUnescape(eagerExpressionResult.toString()))
      .isEqualTo("-0");
  }

  @Test
  public void itHandlesPyishSerializable() {
    context.put("foo", new SomethingPyish("yes"));
    assertThat(
        interpreter.render(
          String.format("{{ %s.name }}", eagerResolveExpression("foo").toString())
        )
      )
      .isEqualTo("yes");
  }

  @Test
  public void itHandlesPyishSerializableWithProcessingException() {
    context.put("foo", new SomethingExceptionallyPyish("yes"));
    context.getMetaContextVariables().add("foo");
    assertThat(interpreter.render("{{ deferred && (1 == 2 || foo) }}"))
      .isEqualTo("{{ deferred && (false || foo) }}");
  }

  @Test
  public void itFinishesResolvingList() {
    assertThat(eagerResolveExpression("[0 + 1, deferred, 2 + 1]").toString())
      .isEqualTo("[1, deferred, 3]");
  }

  @Test
  public void itHandlesExtraSapces() {
    context.put("foo", " foo");
    assertThat(eagerResolveExpression("foo").toString()).isEqualTo("' foo'");
  }

  @Test
  public void itHandlesDeferredExpTests() {
    context.put("foo", 4);
    EagerExpressionResult eagerExpressionResult = eagerResolveExpression(
      "foo is not equalto deferred"
    );
    interpreter.getContext().setThrowInterpreterErrors(true);
    String partiallyResolved = eagerExpressionResult.toString();
    assertThat(partiallyResolved)
      .isEqualTo("exptest:equalto.evaluateNegated(4, ____int3rpr3t3r____, deferred)");
    assertThat(eagerExpressionResult.getDeferredWords())
      .containsExactlyInAnyOrder("deferred", "equalto.evaluateNegated");
    context.put("deferred", 4);
    assertThat(eagerResolveExpression(partiallyResolved).toString()).isEqualTo("false");
    context.put("deferred", 1);
    assertThat(eagerResolveExpression(partiallyResolved).toString()).isEqualTo("true");
  }

  @Test
  public void itHandlesDeferredChoice() {
    context.put("foo", "foo");
    context.put("bar", "bar");
    assertThat(eagerResolveExpression("deferred ? foo : bar").toString())
      .isEqualTo("deferred ? 'foo' : 'bar'");
    assertThat(eagerResolveExpression("true ? deferred : bar").toString())
      .isEqualTo("deferred");
    assertThat(eagerResolveExpression("false ? foo : deferred").toString())
      .isEqualTo("deferred");
    assertThat(eagerResolveExpression("null ? foo : deferred").toString())
      .isEqualTo("deferred");
  }

  @Test
  public void itHandlesDeferredNamedParameter() {
    context.put("foo", "foo");
    EagerExpressionResult result = eagerResolveExpression("[x=foo, y=deferred]");
    assertThat(result.toString()).isEqualTo("[x='foo', y=deferred]");
    assertThat(result.getDeferredWords()).containsExactly("deferred");
  }

  @Test
  public void itHandlesDeferredValueInList() {
    context.put("foo", "foo");
    assertThat(eagerResolveExpression("[foo, deferred, foo ~ '!']").toString())
      .isEqualTo("['foo', deferred, 'foo!']");
  }

  @Test
  public void itHandlesDeferredValueInTuple() {
    context.put("foo", "foo");
    assertThat(eagerResolveExpression("(foo, deferred, foo ~ '!')").toString())
      .isEqualTo("('foo', deferred, 'foo!')");
  }

  @Test
  public void itHandlesDeferredMethod() {
    context.put("foo", "foo");
    context.put("my_list", new PyList(new ArrayList<>()));
    assertThat(eagerResolveExpression("my_list.append(deferred ~ foo)").toString())
      .isEqualTo("my_list.append(deferred ~ 'foo')");
    assertThat(eagerResolveExpression("deferred.append(foo)").toString())
      .isEqualTo("deferred.append('foo')");
    assertThat(eagerResolveExpression("deferred[1 + 1] | length").toString())
      .isEqualTo("filter:length.filter(deferred[2], ____int3rpr3t3r____)");
  }

  @Test
  public void itHandlesDeferredBracketMethod() throws NoSuchMethodException {
    context.put("zero", 0);
    context.put("foo", "foo");
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    map.put("string", null);
    context.put(
      "my_list",
      new PyList(
        Collections.singletonList(
          new AbstractCallableMethod("echo", map) {

            @Override
            public Object doEvaluate(
              Map<String, Object> argMap,
              Map<String, Object> kwargMap,
              List<Object> varArgs
            ) {
              return argMap.get("string");
            }
          }
        )
      )
    );
    assertThat(eagerResolveExpression("my_list[zero](foo)").toString())
      .isEqualTo("'foo'");
    assertThat(eagerResolveExpression("my_list[zero](deferred ~ foo)").toString())
      .isEqualTo("my_list[0](deferred ~ 'foo')");
  }

  @Test
  public void itHandlesOrOperator() {
    assertThat(
        WhitespaceUtils.unquoteAndUnescape(
          eagerResolveExpression("false == true || (true) ? 'yes' : 'no'").toString()
        )
      )
      .isEqualTo("yes");
  }

  @Test
  public void itSplitsResolvedExpression() {
    eagerResolveExpression("['a', 'b']");
    assertThat(context.getResolvedExpressions())
      .containsExactlyInAnyOrder("['a', 'b']", "'a'", "'b'");
  }

  @Test
  public void itHandlesToday() {
    context.put("foo", "bar");
    assertThat(eagerResolveExpression("foo ~ today()").toString())
      .isEqualTo("'bar' ~ today()");
  }

  @Test
  public void itHandlesRandom() {
    assertThat(eagerResolveExpression("range(1)|random").toString())
      .isEqualTo("filter:random.filter([0], ____int3rpr3t3r____)");
  }

  @Test
  public void itDoesntMarkNamedParamsAsDeferredWords() {
    EagerExpressionResult result = eagerResolveExpression("range(end=deferred)");
    assertThat(result.toString()).isEqualTo("range(end=deferred)");
    assertThat(result.getDeferredWords()).containsExactlyInAnyOrder("range", "deferred");
  }

  @Test
  public void itDoesntMarkDictionaryKeysAsDeferredWords() {
    context.put("foo", "foo val");
    EagerExpressionResult result = eagerResolveExpression("{foo: foo, bar:deferred}");
    assertThat(result.toString()).isEqualTo("{foo: 'foo val', bar: deferred}");
    assertThat(result.getDeferredWords()).doesNotContain("foo", "bar");
  }

  @Test
  public void itMarksDictionaryKeysAsDeferredWordsIfEvaluated() throws Exception {
    JinjavaInterpreter.pushCurrent(getInterpreter(true));
    try {
      context.put("foo", "foo val");
      EagerExpressionResult result = eagerResolveExpression("{deferred: foo}");
      assertThat(result.toString()).isEqualTo("{deferred: 'foo val'}");
      assertThat(result.getDeferredWords()).containsExactly("deferred");
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }

  @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
  @Test
  public void itIsThreadSafe() throws InterruptedException {
    Map<String, String> map = new HashMap<>();
    map.put("bar", "first");
    AtomicLong sleepTime = new AtomicLong(500L);
    context.put("map", map);
    context.put("sleep_time", sleepTime);
    CompletableFuture<EagerExpressionResult> first;
    synchronized (sleepTime) {
      first = CompletableFuture.supplyAsync(this::appendAndSleep);
      sleepTime.wait();
    }
    sleepTime.set(1L);
    map.put("bar", "second");
    CompletableFuture<EagerExpressionResult> second = CompletableFuture.supplyAsync(
      this::appendAndSleep
    );
    CompletableFuture.allOf(first, second).join();
    assertThat(first.join().toString())
      .describedAs("First result should say 'first' and sleep for 500ms")
      .isEqualTo("deferred && 'first' && 500");
    assertThat(second.join().toString()) // caching would make this say 'first'
      .describedAs("Second result should say 'second' and sleep for 1ms")
      .isEqualTo("deferred && 'second' && 1");
  }

  private EagerExpressionResult appendAndSleep() {
    JinjavaInterpreter.pushCurrent(interpreter);
    try {
      return eagerResolveExpression("deferred && map.bar && sleeper()");
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }

  public static void voidFunction(int nothing) {}

  public static boolean isNull(Object foo, Object bar) {
    return foo == null && bar == null;
  }

  @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
  public static long sleeper() throws InterruptedException {
    AtomicLong atomicSleepTime = (AtomicLong) JinjavaInterpreter
      .getCurrent()
      .getContext()
      .get("sleep_time");
    long sleepTime = atomicSleepTime.get();
    synchronized (atomicSleepTime) {
      atomicSleepTime.notify();
    }
    Thread.sleep(sleepTime);
    return sleepTime;
  }

  private static class Foo {
    private final String bar;

    Foo(String bar) {
      this.bar = bar;
    }

    String bar() {
      return bar;
    }

    String echo(String toEcho) {
      return toEcho;
    }
  }

  public class SomethingPyish implements PyishSerializable {
    private String name;

    public SomethingPyish(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  public class SomethingExceptionallyPyish implements PyishSerializable {
    private String name;

    public SomethingExceptionallyPyish(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    @Override
    public String toPyishString() {
      throw new DeferredValueException("Can't serialize");
    }
  }
}
