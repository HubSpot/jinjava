package com.hubspot.jinjava.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import com.hubspot.jinjava.objects.collections.PyMap;
import com.hubspot.jinjava.objects.date.PyishDate;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import com.hubspot.jinjava.tree.parse.DefaultTokenScannerSymbols;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.tree.parse.TokenScannerSymbols;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ChunkResolverTest {
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

  private ChunkResolver makeChunkResolver(String string) {
    return new ChunkResolver(string, tagToken, interpreter);
  }

  @Test
  public void itResolvesDeferredBoolean() {
    context.put("foo", "foo_val");
    ChunkResolver chunkResolver = makeChunkResolver("(111 == 112) or (foo == deferred)");
    String partiallyResolved = chunkResolver.resolveChunks();
    assertThat(partiallyResolved).isEqualTo("false or ('foo_val' == deferred)");
    assertThat(chunkResolver.getDeferredWords()).containsExactly("deferred");

    context.put("deferred", "foo_val");
    assertThat(makeChunkResolver(partiallyResolved).resolveChunks()).isEqualTo("true");
    assertThat(interpreter.resolveELExpression(partiallyResolved, 1)).isEqualTo(true);
  }

  @Test
  public void itResolvesDeferredList() {
    context.put("foo", "foo_val");
    context.put("bar", "bar_val");
    ChunkResolver chunkResolver = makeChunkResolver("[foo == bar, deferred, bar]");
    assertThat(chunkResolver.resolveChunks()).isEqualTo("[false, deferred, 'bar_val']");
    assertThat(chunkResolver.getDeferredWords()).containsExactlyInAnyOrder("deferred");
    context.put("bar", "foo_val");
    assertThat(chunkResolver.resolveChunks()).isEqualTo("[true, deferred, 'foo_val']");
  }

  @Test
  public void itResolvesSimpleBoolean() {
    context.put("foo", true);
    ChunkResolver chunkResolver = makeChunkResolver("false || (foo), 'bar'");
    String partiallyResolved = chunkResolver.resolveChunks();
    assertThat(partiallyResolved).isEqualTo("true, 'bar'");
    assertThat(chunkResolver.getDeferredWords()).isEmpty();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void itResolvesRange() {
    ChunkResolver chunkResolver = makeChunkResolver("range(0,2)");
    String partiallyResolved = chunkResolver.resolveChunks();
    assertThat(partiallyResolved).isEqualTo("[0, 1]");
    assertThat(chunkResolver.getDeferredWords()).isEmpty();
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
    ChunkResolver chunkResolver = makeChunkResolver("range(deferred, foo + bar)");
    String partiallyResolved = chunkResolver.resolveChunks();
    assertThat(partiallyResolved).isEqualTo("range(deferred, 4)");
    assertThat(chunkResolver.getDeferredWords())
      .containsExactlyInAnyOrder("deferred", "range");

    context.put("deferred", 1);
    assertThat(makeChunkResolver(partiallyResolved).resolveChunks())
      .isEqualTo(new PyishObjectMapper().getAsPyishString(expectedList));
    // But this is a list of integers
    assertThat((List<Integer>) interpreter.resolveELExpression(partiallyResolved, 1))
      .isEqualTo(expectedList);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void itResolvesDictionary() {
    Map<String, Object> dict = ImmutableMap.of("foo", "one", "bar", 2L);
    context.put("the_dictionary", dict);

    ChunkResolver chunkResolver = makeChunkResolver("[the_dictionary, 1]");
    String partiallyResolved = chunkResolver.resolveChunks();
    assertThat(chunkResolver.getDeferredWords()).isEmpty();
    assertThat(interpreter.resolveELExpression(partiallyResolved, 1))
      .isEqualTo(ImmutableList.of(dict, 1L));
  }

  @Test
  public void itResolvesNested() {
    context.put("foo", 1);
    context.put("bar", 3);
    ChunkResolver chunkResolver = makeChunkResolver(
      "[foo, range(deferred, bar), range(foo, bar)][0:2]"
    );
    String partiallyResolved = chunkResolver.resolveChunks();
    assertThat(partiallyResolved).isEqualTo("[1, range(deferred, 3), [1, 2]][0:2]");
    assertThat(chunkResolver.getDeferredWords())
      .containsExactlyInAnyOrder("deferred", "range");

    context.put("deferred", 2);
    assertThat(makeChunkResolver(partiallyResolved).resolveChunks())
      .isEqualTo("[1, [2]]");
    assertThat(interpreter.resolveELExpression(partiallyResolved, 1))
      .isEqualTo(ImmutableList.of(1L, ImmutableList.of(2)));
  }

  @Test
  public void itSplitsOnNonWords() {
    context.put("foo", 1);
    context.put("bar", 4);
    ChunkResolver chunkResolver = makeChunkResolver("range(0,foo) + -deferred/bar");
    String partiallyResolved = chunkResolver.resolveChunks();
    assertThat(partiallyResolved).isEqualTo("[0] + -deferred/4");
    assertThat(chunkResolver.getDeferredWords()).containsExactly("deferred");

    context.put("deferred", 2);
    assertThat(makeChunkResolver(partiallyResolved).resolveChunks())
      .isEqualTo("[0, -0.5]");
    assertThat(interpreter.resolveELExpression(partiallyResolved, 1))
      .isEqualTo(ImmutableList.of(0L, -0.5));
  }

  @Test
  public void itSplitsAndIndexesOnNonWords() {
    context.put("foo", 3);
    context.put("bar", 4);
    ChunkResolver chunkResolver = makeChunkResolver("range(-2,foo)[-1] + -deferred/bar");
    String partiallyResolved = chunkResolver.resolveChunks();
    assertThat(partiallyResolved).isEqualTo("2 + -deferred/4");
    assertThat(chunkResolver.getDeferredWords()).containsExactly("deferred");

    context.put("deferred", 2);
    assertThat(makeChunkResolver(partiallyResolved).resolveChunks()).isEqualTo("1.5");
    assertThat(interpreter.resolveELExpression(partiallyResolved, 1)).isEqualTo(1.5);
  }

  @Test
  @Ignore
  // TODO support order of operations
  public void itSupportsOrderOfOperations() {
    ChunkResolver chunkResolver = makeChunkResolver("[0,1]|reverse + deferred");
    String partiallyResolved = chunkResolver.resolveChunks();
    assertThat(partiallyResolved).isEqualTo("[1,0] + deferred");
    assertThat(chunkResolver.getDeferredWords()).containsExactly("deferred");

    context.put("deferred", 2);
    assertThat(makeChunkResolver(partiallyResolved).resolveChunks()).isEqualTo("[1,0,2]");
    assertThat(interpreter.resolveELExpression(partiallyResolved, 1))
      .isEqualTo(ImmutableList.of(1L, 0L, 2L));
  }

  @Test
  public void itCatchesDeferredVariables() {
    ChunkResolver chunkResolver = makeChunkResolver("range(0, deferred)");
    String partiallyResolved = chunkResolver.resolveChunks();
    assertThat(partiallyResolved).isEqualTo("range(0, deferred)");
    // Since the range function is deferred, it is added to deferredWords.
    assertThat(chunkResolver.getDeferredWords())
      .containsExactlyInAnyOrder("range", "deferred");
  }

  @Test
  public void itSplitsChunks() {
    ChunkResolver chunkResolver = makeChunkResolver("1, 1 + 1, 1 + 2");
    List<String> miniChunks = chunkResolver.splitChunks();
    assertThat(miniChunks).containsExactly("1", "2", "3");
    assertThat(chunkResolver.getDeferredWords()).isEmpty();
  }

  @Test
  public void itProperlySplitsMultiLevelChunks() {
    ChunkResolver chunkResolver = makeChunkResolver(
      "[5,7], 1 + 1, 1 + range(0 + 1, deferred)"
    );
    List<String> miniChunks = chunkResolver.splitChunks();
    assertThat(miniChunks).containsExactly("[5, 7]", "2", "1 + range(1, deferred)");
    assertThat(chunkResolver.getDeferredWords())
      .containsExactlyInAnyOrder("range", "deferred");
  }

  @Test
  public void itDoesntDeferReservedWords() {
    context.put("foo", 0);
    ChunkResolver chunkResolver = makeChunkResolver(
      "[(foo > 1) or deferred, deferred].append(1)"
    );
    String partiallyResolved = chunkResolver.resolveChunks();
    assertThat(partiallyResolved).isEqualTo("[false or deferred, deferred].append(1)");
    assertThat(chunkResolver.getDeferredWords()).doesNotContain("false", "or");
    assertThat(chunkResolver.getDeferredWords()).contains("deferred", ".append");
  }

  @Test
  public void itEvaluatesDict() {
    context.put("foo", new PyMap(ImmutableMap.of("bar", 99)));
    ChunkResolver chunkResolver = makeChunkResolver("foo.bar == deferred.bar");
    String partiallyResolved = chunkResolver.resolveChunks();
    assertThat(partiallyResolved).isEqualTo("99 == deferred.bar");
    assertThat(chunkResolver.getDeferredWords())
      .containsExactlyInAnyOrder("deferred.bar");
  }

  @Test
  public void itSerializesDateProperly() {
    PyishDate date = new PyishDate(
      ZonedDateTime.ofInstant(Instant.ofEpochMilli(1234567890L), ZoneId.systemDefault())
    );
    context.put("date", date);
    ChunkResolver chunkResolver = makeChunkResolver("date");

    // don't prematurely resolve date because of datetime functions.
    assertThat(WhitespaceUtils.unquoteAndUnescape(chunkResolver.resolveChunks()))
      .isEqualTo("date");
    assertThat(WhitespaceUtils.unquoteAndUnescape(interpreter.resolveString("date", -1)))
      .isEqualTo(date.toString());
  }

  @Test
  public void itHandlesSingleQuotes() {
    context.put("foo", "'");
    context.put("bar", '\'');
    ChunkResolver chunkResolver = makeChunkResolver(
      "foo ~ ' & ' ~ bar ~ ' & ' ~ '\\'\\\"'"
    );
    assertThat(WhitespaceUtils.unquoteAndUnescape(chunkResolver.resolveChunks()))
      .isEqualTo("' & ' & '\"");
  }

  @Test
  public void itHandlesNewlines() {
    context.put("foo", "\n");
    context.put("bar", "\\" + "n"); // Jinja doesn't see this as a newline.
    ChunkResolver chunkResolver = makeChunkResolver(
      "foo ~ ' & ' ~ bar ~ ' & ' ~ '\\\\' ~ 'n' ~ ' & \\\\n'"
    );
    assertThat(WhitespaceUtils.unquoteAndUnescape(chunkResolver.resolveChunks()))
      .isEqualTo("\n & \\n & \\n & \\n");
  }

  @Test
  public void itOutputsUnknownVariablesAsEmpty() {
    ChunkResolver chunkResolver = makeChunkResolver("contact.some_odd_property");
    assertThat(WhitespaceUtils.unquoteAndUnescape(chunkResolver.resolveChunks()))
      .isEqualTo("");
  }

  @Test
  public void itHandlesCancellingSlashes() {
    context.put("foo", "bar");
    ChunkResolver chunkResolver = makeChunkResolver("foo ~ 'foo\\\\' ~ foo ~ 'foo'");
    assertThat(WhitespaceUtils.unquoteAndUnescape(chunkResolver.resolveChunks()))
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
          makeChunkResolver("void_function(2)").resolveChunks()
        )
      )
      .isEmpty();
  }

  @Test
  public void itOutputsNullAsEmptyString() {
    assertThat(makeChunkResolver("void_function(2)").resolveChunks()).isEqualTo("''");
    assertThat(makeChunkResolver("nothing").resolveChunks()).isEqualTo("''");
  }

  @Test
  public void itInterpretsNullAsNull() {
    assertThat(makeChunkResolver("is_null(nothing, null)").resolveChunks())
      .isEqualTo("true");
    assertThat(makeChunkResolver("is_null(void_function(2), nothing)").resolveChunks())
      .isEqualTo("true");
    assertThat(makeChunkResolver("is_null('', nothing)").resolveChunks())
      .isEqualTo("false");
  }

  @Test
  public void itDoesntDeferNull() {
    ChunkResolver chunkResolver = makeChunkResolver("range(deferred, nothing)");
    assertThat(chunkResolver.resolveChunks()).isEqualTo("range(deferred, null)");
    assertThat(chunkResolver.getDeferredWords())
      .containsExactlyInAnyOrder("range", "deferred");
  }

  @Test
  public void itDoesntSplitOnBar() {
    context.put("date", new PyishDate(0L));
    ChunkResolver chunkResolver = makeChunkResolver("date|datetimeformat('%Y')");
    assertThat(chunkResolver.resolveChunks()).isEqualTo("1970");
  }

  @Test
  public void itDoesntResolveNonPyishSerializable() {
    PyMap dict = new PyMap(new HashMap<>());
    context.put("dict", dict);
    context.put("foo", new Foo("bar"));
    context.put("mark", "!");
    ChunkResolver chunkResolver = makeChunkResolver("(dict.update({'foo': foo})");
    assertThat(WhitespaceUtils.unquoteAndUnescape(chunkResolver.resolveChunks()))
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
    ChunkResolver chunkResolver = makeChunkResolver("zero_date.strftime('%d')");
    assertThat(WhitespaceUtils.unquoteAndUnescape(chunkResolver.resolveChunks()))
      .isEqualTo("01");
  }

  @Test
  public void itPreservesLengthyDoubleStrings() {
    // does not convert to scientific notation
    context.put("small", "0.0000000001");
    ChunkResolver chunkResolver = makeChunkResolver("small");
    assertThat(WhitespaceUtils.unquoteAndUnescape(chunkResolver.resolveChunks()))
      .isEqualTo("0.0000000001");
  }

  @Test
  public void itConvertsDoubles() {
    // does convert to scientific notation
    context.put("small", 0.0000000001);
    ChunkResolver chunkResolver = makeChunkResolver("small");
    assertThat(WhitespaceUtils.unquoteAndUnescape(chunkResolver.resolveChunks()))
      .isEqualTo("1.0E-10");
  }

  @Test
  public void itDoesntQuoteFloats() {
    ChunkResolver chunkResolver = makeChunkResolver("0.4 + 0.1");
    assertThat(chunkResolver.resolveChunks()).isEqualTo("0.5");
  }

  @Test
  public void itHandlesWhitespaceAroundPipe() {
    String lowerFilterString =
      "'AB' | truncate(1) ~ 'BC' |truncate(1) ~ 'CD'| truncate(1)";
    ChunkResolver chunkResolver = makeChunkResolver(lowerFilterString);
    assertThat(WhitespaceUtils.unquoteAndUnescape(chunkResolver.resolveChunks()))
      .isEqualTo(interpreter.resolveELExpression(lowerFilterString, 0));
  }

  @Test
  public void itHandlesMultipleWhitespaceAroundPipe() {
    String lowerFilterString = "'AB'   |   truncate(1)";
    ChunkResolver chunkResolver = makeChunkResolver(lowerFilterString);
    assertThat(WhitespaceUtils.unquoteAndUnescape(chunkResolver.resolveChunks()))
      .isEqualTo(interpreter.resolveELExpression(lowerFilterString, 0));
  }

  @Test
  public void itEscapesFormFeed() {
    context.put("foo", "Form feed\f");
    ChunkResolver chunkResolver = makeChunkResolver("foo");
    assertThat(WhitespaceUtils.unquoteAndUnescape(chunkResolver.resolveChunks()))
      .isEqualTo("Form feed\f");
  }

  @Test
  public void itHandlesUnconventionalSpacing() {
    ChunkResolver chunkResolver = makeChunkResolver(
      "(  range (0 , 3 ) [ 1] + deferred) ~ 'YES'| lower"
    );
    String result = WhitespaceUtils.unquoteAndUnescape(chunkResolver.resolveChunks());
    assertThat(result).isEqualTo("( 1 + deferred) ~ 'yes'");
    context.put("deferred", 2);
    assertThat(interpreter.resolveELExpression(result, 0)).isEqualTo("3yes");
  }

  @Test
  public void itHandlesDotSpacing() {
    context.put("bar", "fake");
    context.put("foo", ImmutableMap.of("bar", "foobar"));
    ChunkResolver chunkResolver = makeChunkResolver("foo . bar");
    String result = WhitespaceUtils.unquoteAndUnescape(chunkResolver.resolveChunks());
    assertThat(result).isEqualTo("foobar");
  }

  @Test
  public void itPreservesLegacyDictionaryCreation() {
    context.put("foo", "not_foo");
    ChunkResolver chunkResolver = makeChunkResolver("{foo: 'bar'}");
    String result = WhitespaceUtils.unquoteAndUnescape(chunkResolver.resolveChunks());
    assertThat(result).isEqualTo("{'foo': 'bar'}");
  }

  @Test
  public void itHandlesPythonicDictionaryCreation() throws Exception {
    JinjavaInterpreter.pushCurrent(getInterpreter(true));
    try {
      context.put("foo", "not_foo");
      ChunkResolver chunkResolver = makeChunkResolver("{foo: 'bar'}");
      String result = WhitespaceUtils.unquoteAndUnescape(chunkResolver.resolveChunks());
      assertThat(result).isEqualTo("{'not_foo': 'bar'}");
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }

  @Test
  public void itKeepsPlusSignPrefix() {
    context.put("foo", "+12223334444");
    ChunkResolver chunkResolver = makeChunkResolver("foo");
    assertThat(WhitespaceUtils.unquoteAndUnescape(chunkResolver.resolveChunks()))
      .isEqualTo("+12223334444");
  }

  @Test
  public void itHandlesPhoneNumbers() {
    context.put("foo", "+1(123)456-7890");
    ChunkResolver chunkResolver = makeChunkResolver("foo");
    assertThat(WhitespaceUtils.unquoteAndUnescape(chunkResolver.resolveChunks()))
      .isEqualTo("+1(123)456-7890");
  }

  @Test
  public void itHandlesNegativeZero() {
    context.put("foo", "-0");
    ChunkResolver chunkResolver = makeChunkResolver("foo");
    assertThat(WhitespaceUtils.unquoteAndUnescape(chunkResolver.resolveChunks()))
      .isEqualTo("-0");
  }

  @Test
  public void itHandlesPyishSerializable() {
    context.put("foo", new SomethingPyish("yes"));
    assertThat(
        interpreter.render(
          String.format("{{ %s.name }}", makeChunkResolver("foo").resolveChunks())
        )
      )
      .isEqualTo("yes");
  }

  public static void voidFunction(int nothing) {}

  public static boolean isNull(Object foo, Object bar) {
    return foo == null && bar == null;
  }

  private class Foo {
    private String bar;

    Foo(String bar) {
      this.bar = bar;
    }

    String bar() {
      return bar;
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
}
