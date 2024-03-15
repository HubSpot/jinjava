package com.hubspot.jinjava.el;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.OutputTooBigException;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class ExtendedSyntaxBuilderTest {

  private static final long MAX_STRING_LENGTH = 10_000;

  private Context context;
  private JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    interpreter =
      new Jinjava(JinjavaConfig.newBuilder().withMaxOutputSize(MAX_STRING_LENGTH).build())
        .newInterpreter();
    JinjavaInterpreter.pushCurrent(interpreter);

    context = interpreter.getContext();
  }

  @After
  public void cleanup() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itHandlesNonBreakingSpaceProperly() {
    context.put("foo", "bar");
    assertThat(val("\u00A0foo\u00A0")).isEqualTo("bar");
  }

  @Test
  public void nestedFilters() {
    context.put("content", "mycontent");
    assertThat(val("content|striptags|truncate(100, false)")).isEqualTo("mycontent");
  }

  @Test
  public void positivePrefixOp() {
    assertThat(val("+20")).isEqualTo(20L);
    assertThat(val("2 + 5")).isEqualTo(7L);
  }

  @Test
  public void expTestOp() {
    context.put("foo", "myfoo");
    assertThat(val("foo is defined")).isEqualTo(true);
    assertThat(val("bar is defined")).isEqualTo(false);
    assertThat(val("12 is divisibleby 3")).isEqualTo(true);
    assertThat(val("'foo' is string")).isEqualTo(true);
    assertThat(val("49 is odd")).isEqualTo(true);
  }

  @Test
  public void stringExpTestOps() {
    assertThat(val("'football' is string_startingwith 'foot'")).isEqualTo(true);
    assertThat(val("'football' is string_startingwith 'ball'")).isEqualTo(false);

    assertThat(val("'football' is string_containing 'tb'")).isEqualTo(true);
    assertThat(val("'football' is string_containing 'golf'")).isEqualTo(false);
  }

  @Test
  public void namedFnArgs() {
    context.put("path", "/page");
    assertThat(val("path=='/' or truncate(path,length=5,killwords=true,end='')=='/page'"))
      .isEqualTo(true);
    assertThat(val("truncate('foobar', length = 3)")).isEqualTo("f...");
  }

  @Test
  public void stringConcat() {
    context.put("foo", "xx");
    assertThat(val("foo + 'bar' + foo")).isEqualTo("xxbarxx");
    assertThat(val("123 + 'xx'")).isEqualTo("123xx");
  }

  @Test
  public void stringConcatOperator() {
    context.put("foo", 123);
    assertThat(val("foo ~ 456")).isEqualTo("123456");
    assertThat(val("'foo' ~ 'bar'")).isEqualTo("foobar");
  }

  @Test
  public void itLimitsLengthInStringConcatOperator() {
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < MAX_STRING_LENGTH - 1; i++) {
      stringBuilder.append("0");
    }

    String longString = stringBuilder.toString();

    context.put("longString", longString);
    assertThat(val("longString ~ ''")).isEqualTo(longString);
    assertThat(interpreter.getErrors()).isEmpty();

    assertThat(val("longString ~ 'OVER'")).isNull();

    assertThat(interpreter.getErrors().get(0).getMessage())
      .contains(OutputTooBigException.class.getSimpleName());
  }

  @Test
  public void stringInStringOperator() {
    assertThat(val("'foo' in 'foobar'")).isEqualTo(true);
    assertThat(val("'gg' in 'foobar'")).isEqualTo(false);
  }

  @Test
  public void objInCollectionOperator() {
    assertThat(val("12 in [1, 2, 3]")).isEqualTo(false);
    assertThat(val("12 in [1, 12, 3]")).isEqualTo(true);
  }

  // TODO: support negated collection membership. See CollectionMembershipOperator
  //  @Test
  //  public void stringNotInStringOperator() {
  //    assertThat(val("'foo' not in 'foobar'")).isEqualTo(false);
  //    assertThat(val("'gg' not in 'foobar'")).isEqualTo(true);
  //  }

  //  @Test
  //  public void objNotInCollectionOperator() {
  //    assertThat(val("12 not in [1, 2, 3]")).isEqualTo(true);
  //    assertThat(val("12 not in [1, 12, 3]")).isEqualTo(false);
  //  }

  @Test
  public void conditionalExprWithNoElse() {
    context.put("foo", "bar");
    assertThat(val("'hello' if foo=='bar'")).isEqualTo("hello");
    assertThat(val("'hello' if foo=='barf'")).isNull();
  }

  @Test
  public void conditionalExprWithElse() {
    context.put("foo", "bar");
    assertThat(val("'hello' if foo=='bar' else 'barf'")).isEqualTo("hello");
    assertThat(val("'hello' if foo=='barf' else 'hi'")).isEqualTo("hi");
  }

  @Test
  public void conditionalExprWithOrConditionalAndCustomExpression() {
    assertThat(val("'a' is equalto 'b' or 'a' is equalto 'a'")).isEqualTo(true);
  }

  @Test
  public void newlineEscChar() {
    context.put("comment", "foo\nbar");
    assertThat(val("comment|replace('\\n', '<br/>')")).isEqualTo("foo<br/>bar");
  }

  @Test
  public void literalList() {
    context.put("foo", "bar");
    assertThat((List<Object>) val("[1, foo, 'foo']")).containsExactly(1L, "bar", "foo");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void literalTuple() {
    context.put("foo", "bar");
    List<Object> list = (List<Object>) val("(1, foo, 'foo')");
    assertThat(list).containsExactly(1L, "bar", "foo");
    list.add("xx");
  }

  @Test
  public void mapLiteral() {
    context.put("foo", "bar");
    assertThat((Map<String, Object>) val("{}")).isEmpty();
    Map<String, Object> map = (Map<String, Object>) val(
      "{foo: foo, \"foo2\": foo, foo3: 123, foo4: 'string', foo5: {}, foo6: [1, 2]}"
    );
    assertThat(map)
      .contains(
        entry("foo", "bar"),
        entry("foo2", "bar"),
        entry("foo3", 123L),
        entry("foo4", "string"),
        entry("foo6", Arrays.asList(1L, 2L))
      );

    assertThat((Map<String, Object>) val("{\"address\":\"123 Main - Boston, MA 02111\"}"))
      .contains(entry("address", "123 Main - Boston, MA 02111"));
  }

  @Test
  public void complexMapLiteral() {
    Map<String, Object> map = (Map<String, Object>) val(fixture("complex"));
    assertThat(map).hasSize(11);
    assertThat((Map<String, Object>) map.get("Boston")).contains(entry("city", "Boston"));
  }

  @Test
  public void itParsesDictWithVariableRefs() {
    List<?> theList = Lists.newArrayList(1L, 2L, 3L);
    context.put("the_list", theList);
    context.put("i_am_seven", 7L);
    context.put("False", false);

    Map<String, Object> map = (Map<String, Object>) val(fixture("dict-with-var-refs"));
    assertThat(map).contains(entry("seven", 7L), entry("the_list", theList));

    List<Object> innerList = (List<Object>) map.get("inner_list_literal");
    assertThat(innerList).containsExactly("heya", false);

    Map<String, Object> innerDict = (Map<String, Object>) map.get("inner_dict");
    assertThat(innerDict).contains(entry("car keys", "valuable"));
  }

  @Test
  public void itReturnsLeftResultForOrExpr() {
    context.put("left", "foo");
    context.put("right", "bar");

    assertThat(val("left or right")).isEqualTo("foo");
  }

  @Test
  public void itReturnsRightResultForOrExpr() {
    context.put("right", "bar");

    assertThat(val("left or right")).isEqualTo("bar");
  }

  private String fixture(String name) {
    try {
      return Resources.toString(
        Resources.getResource(String.format("el/dict/%s.fixture", name)),
        StandardCharsets.UTF_8
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testParseExp() {
    context.put("foo", "fff");
    context.put("a", "aaa");
    context.put("b", "bbb");

    assertThat(val("foo|length > 5")).isEqualTo(Boolean.FALSE);
  }

  @Test
  public void listRangeSyntax() {
    List<?> theList = Lists.newArrayList(1, 2, 3, 4, 5);
    context.put("mylist", theList);
    assertThat(val("mylist[0:3]")).isEqualTo(Lists.newArrayList(1, 2, 3));
    assertThat(val("mylist[5:15]")).isEqualTo(Lists.newArrayList());
    assertThat(val("mylist[2]")).isEqualTo(3);
  }

  @Test
  public void outOfRange() {
    List<?> emptyList = Lists.newArrayList();
    context.put("emptyList", emptyList);
    assertThat(val("emptyList.get(0)")).isNull();
    assertThat(val("emptyList.get(-1)")).isNull();

    List<?> theList = Lists.newArrayList(1, 2, 3);
    context.put("mylist", theList);
    assertThat(val("myList.get(3)")).isNull();
  }

  @Test
  public void listRangeSyntaxNegativeIndices() {
    List<?> theList = Lists.newArrayList(1, 2, 3, 4, 5);
    context.put("mylist", theList);

    // assorted valid negative starts and ends
    assertThat(val("mylist[0:-1]")).isEqualTo(Lists.newArrayList(1, 2, 3, 4));
    assertThat(val("mylist[0:-2]")).isEqualTo(Lists.newArrayList(1, 2, 3));
    assertThat(val("mylist[-3:-1]")).isEqualTo(Lists.newArrayList(3, 4));
    assertThat(val("mylist[-5:-4]")).isEqualTo(Lists.newArrayList(1));

    // same start and end -- negative
    assertThat(val("mylist[-3:-3]")).isEqualTo(Lists.newArrayList());

    // negative start, positive end
    assertThat(val("mylist[-3:5]")).isEqualTo(Lists.newArrayList(3, 4, 5));

    // positive start, negative end
    assertThat(val("mylist[2:-2]")).isEqualTo(Lists.newArrayList(3));

    // 6 is out of range, but this is what python does
    assertThat(val("mylist[-3:6]")).isEqualTo(Lists.newArrayList(3, 4, 5));

    // -6 is out of range, but this is what python does
    assertThat(val("mylist[-6:3]")).isEqualTo(Lists.newArrayList(1, 2, 3));

    // start after end
    assertThat(val("mylist[-2:-3]")).isEqualTo(Lists.newArrayList());
  }

  @Test
  public void arrayWithNegativeIndices() {
    String stringToSplit = "one-two-three-four-five";
    context.put("stringToSplit", stringToSplit);

    // Negative index handling on lists happens elsewhere, so make sure we're
    // dealing with an array of Strings.
    assertThat(val("stringToSplit.split('-')"))
      .isEqualTo(new String[] { "one", "two", "three", "four", "five" });

    assertThat(val("stringToSplit.split('-')[-1]")).isEqualTo("five");
    assertThat(val("stringToSplit.split('-')[1.5]")).isEqualTo(null);
    assertThat(val("stringToSplit.split('-')[-1.5]")).isEqualTo(null);

    // out of range returns null, as -6 + the length of the array is still
    // negative, and java doesn't support negative array indices.
    assertThat(val("stringToSplit.split('-')[-6]")).isEqualTo(null);

    assertThat(val("stringToSplit.split('-')[0:2]"))
      .isEqualTo(Lists.newArrayList("one", "two"));
    assertThat(val("stringToSplit.split('-')[0:-2]"))
      .isEqualTo(Lists.newArrayList("one", "two", "three"));
  }

  @Test
  public void arrayWithImplicitIndices() {
    assertThat(val("[1, 2, 3][1:]")).isEqualTo(Lists.newArrayList(2L, 3L));
    assertThat(val("[1, 2, 3][:2]")).isEqualTo(Lists.newArrayList(1L, 2L));
  }

  @Test
  public void invalidNestedAssignmentExpr() {
    assertThat(val("content.template_path = 'Custom/Email/Responsive/testing.html'"))
      .isEqualTo(null);
    assertThat(interpreter.getErrorsCopy()).isNotEmpty();
    assertThat(interpreter.getErrorsCopy().get(0).getReason())
      .isEqualTo(ErrorReason.SYNTAX_ERROR);
    assertThat(interpreter.getErrorsCopy().get(0).getMessage())
      .containsIgnoringCase("identifier");
  }

  @Test
  public void invalidIdentifierAssignmentExpr() {
    assertThat(val("content = 'Custom/Email/Responsive/testing.html'")).isEqualTo(null);
    assertThat(interpreter.getErrorsCopy()).isNotEmpty();
    assertThat(interpreter.getErrorsCopy().get(0).getReason())
      .isEqualTo(ErrorReason.SYNTAX_ERROR);
    assertThat(interpreter.getErrorsCopy().get(0).getMessage())
      .containsIgnoringCase("'='");
  }

  @Test
  public void invalidPipeOperatorExpr() {
    assertThat(val("topics|1")).isEqualTo(null);
    assertThat(interpreter.getErrorsCopy()).isNotEmpty();
    assertThat(interpreter.getErrorsCopy().get(0).getReason())
      .isEqualTo(ErrorReason.SYNTAX_ERROR);
  }

  @Test
  public void itReturnsCorrectSyntaxErrorPositions() {
    assertThat(
      interpreter.render(
        "hi {{ missing thing }}{{ missing thing }}\nI am {{ blah blabbity }} too"
      )
    )
      .isEqualTo("hi \nI am  too");
    assertThat(interpreter.getErrorsCopy().size()).isEqualTo(3);
    assertThat(interpreter.getErrorsCopy().get(0).getLineno()).isEqualTo(1);
    assertThat(interpreter.getErrorsCopy().get(0).getMessage()).contains("position 14");
    assertThat(interpreter.getErrorsCopy().get(0).getStartPosition()).isEqualTo(14);
    assertThat(interpreter.getErrorsCopy().get(0).getFieldName()).isEqualTo("thing");
    assertThat(interpreter.getErrorsCopy().get(1).getLineno()).isEqualTo(1);
    assertThat(interpreter.getErrorsCopy().get(1).getMessage()).contains("position 33");
    assertThat(interpreter.getErrorsCopy().get(1).getStartPosition()).isEqualTo(33);
    assertThat(interpreter.getErrorsCopy().get(1).getFieldName()).isEqualTo("thing");
    assertThat(interpreter.getErrorsCopy().get(2).getLineno()).isEqualTo(2);
    assertThat(interpreter.getErrorsCopy().get(2).getMessage()).contains("position 13");
    assertThat(interpreter.getErrorsCopy().get(2).getStartPosition()).isEqualTo(13);
    assertThat(interpreter.getErrorsCopy().get(2).getFieldName()).isEqualTo("blabbity");
  }

  private Object val(String expr) {
    return interpreter.resolveELExpression(expr, -1);
  }
}
