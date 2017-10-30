package com.hubspot.jinjava.lib.tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.TreeParser;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class SetTagTest {

  @InjectMocks
  SetTag tag;

  Context context;
  JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
    context = interpreter.getContext();
  }

  @Test
  public void itReturnsBlankString() throws Exception {
    TagNode tagNode = (TagNode) fixture("set-val");
    assertThat(tag.interpret(tagNode, interpreter)).isEqualTo("");
  }

  @Test
  public void itAssignsValToVar() throws Exception {
    TagNode tagNode = (TagNode) fixture("set-val");
    tag.interpret(tagNode, interpreter);
    assertThat(interpreter.resolveObject("primary_line_height", -1, -1)).isEqualTo(42L);
  }

  @Test
  public void itAssignsVarToVar() throws Exception {
    context.put("primary_font_size_num", 10);
    TagNode tagNode = (TagNode) fixture("set-var-exp");
    tag.interpret(tagNode, interpreter);
    assertThat(interpreter.resolveObject("primary_line_height", -1, -1)).isEqualTo(15.0);
  }

  @Test
  public void itHandlesComplexDictWithConcats() throws Exception {
    Map<String, Object> colors = new HashMap<>();
    colors.put("base", "basecolor");
    colors.put("blue", "bluecolor");
    Map<String, Object> fonts = new HashMap<>();
    fonts.put("size", 42);
    fonts.put("family", "fontfam");

    context.put("colors", colors);
    context.put("fonts", fonts);
    context.put("padding", 123);

    TagNode tagNode = (TagNode) fixture("set-complex-dict");
    tag.interpret(tagNode, interpreter);

    Map<String, Object> dict = (Map<String, Object>) interpreter.resolveObject("styles", -1, -1);
    assertThat(dict).contains(
        entry("heading", "color:bluecolor;text-shadow:2px 2px 1px rgba(0,0,0,.1);font-family:fontfam"),
        entry("module", "background:#ffffff;padding:123px;border:1px solid 10"));
  }

  @Test
  public void itConcatsStringsInExprs() throws Exception {
    context.put("lw_font_size_base", 42);
    TagNode tagNode = (TagNode) fixture("set-string-concat");
    tag.interpret(tagNode, interpreter);
    assertThat(interpreter.resolveObject("lw_font_size", -1 ,-1)).isEqualTo("font-size: 42px; ");
  }

  @Test
  public void itConcatsStringsWithNestedExprs() throws Exception {
    context.put("lw_font_size_base", 10);
    TagNode tagNode = (TagNode) fixture("set-expr-concat");
    tag.interpret(tagNode, interpreter);
    assertThat(interpreter.resolveObject("lw_secondary_font_size", -1, -1)).isEqualTo("font-size: 8px; ");
  }

  @Test
  public void itSupportsExpressionsWithFilters() throws Exception {
    context.put("max_size", "470");
    TagNode tagNode = (TagNode) fixture("set-filter-expr");
    tag.interpret(tagNode, interpreter);
    assertThat(interpreter.resolveObject("ie_max_size", -1 ,-1)).isEqualTo(440L);
  }

  @Test
  public void itSupportsArraySyntax() throws Exception {
    context.put("i_am_seven", 7L);
    TagNode tagNode = (TagNode) fixture("set-array");
    tag.interpret(tagNode, interpreter);

    List<Object> result = (List<Object>) interpreter.resolveObject("the_list", -1,-1);
    assertThat(result).containsExactly(1L, 2L, 3L, 7L);
  }

  @Test
  public void itSupportsDictionarySyntax() throws Exception {
    context.put("i_am_seven", 7L);
    context.put("the_list", Lists.newArrayList(1L, 2L, 3L));
    TagNode tagNode = (TagNode) fixture("set-dictionary");
    tag.interpret(tagNode, interpreter);

    Map<String, Object> result = (Map<String, Object>) interpreter.resolveObject("the_dictionary", -1,-1);
    assertThat(result).contains(entry("seven", 7L));
  }

  @Test
  public void itSupportsListAppendFunc() throws Exception {
    context.put("show_count", Lists.newArrayList("foo"));
    context.put("show", "bar");
    TagNode tagNode = (TagNode) fixture("set-list-append");
    tag.interpret(tagNode, interpreter);

    List<String> thelist = (List<String>) context.get("show_count");
    assertThat(thelist).containsExactly("foo", "bar");
  }

  @Test
  public void itSupportsListSetFunc() throws Exception {
    context.put("show_count", Lists.newArrayList("foo"));
    context.put("show", "bar");
    TagNode tagNode = (TagNode) fixture("set-list-modify");
    tag.interpret(tagNode, interpreter);

    List<String> thelist = (List<String>) context.get("show_count");
    assertThat(thelist).containsExactly("bar");
  }

  @Test
  public void itSupportsMultiVar() throws Exception {
    context.put("bar", "mybar");

    TagNode tagNode = (TagNode) fixture("set-multivar");
    tag.interpret(tagNode, interpreter);

    assertThat(context).contains(
        entry("myvar1", "foo"),
        entry("myvar2", "mybar"),
        entry("myvar3", Lists.newArrayList(1L, 2L, 3L, 4L)),
        entry("myvar4", "yoooooo"));
  }

  @Test(expected = TemplateSyntaxException.class)
  public void itThrowsErrorWhenMultiVarIsUnbalancedForVars() throws Exception {
    context.put("bar", "mybar");

    TagNode tagNode = (TagNode) fixture("set-multivar-unbalanced-vars");
    tag.interpret(tagNode, interpreter);

    assertThat(context).contains(
        entry("myvar1", "foo"),
        entry("myvar2", "mybar"),
        entry("myvar3", Lists.newArrayList(1L, 2L, 3L, 4L)),
        entry("myvar4", "yoooooo"));
  }

  @Test(expected = TemplateSyntaxException.class)
  public void itThrowsErrorWhenMultiVarIsUnbalancedForVals() throws Exception {
    context.put("bar", "mybar");

    TagNode tagNode = (TagNode) fixture("set-multivar-unbalanced-vals");
    tag.interpret(tagNode, interpreter);

    assertThat(context).contains(
        entry("myvar1", "foo"),
        entry("myvar2", "mybar"),
        entry("myvar3", Lists.newArrayList(1L, 2L, 3L, 4L)),
        entry("myvar4", "yoooooo"));
  }

  private Node fixture(String name) {
    try {
      return new TreeParser(interpreter, Resources.toString(
          Resources.getResource(String.format("tags/settag/%s.jinja", name)), StandardCharsets.UTF_8))
              .buildTree().getChildren().getFirst();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

}
