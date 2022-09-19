package com.hubspot.jinjava.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.lib.tag.eager.DeferredToken;
import com.hubspot.jinjava.tree.ExpressionNode;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.DefaultTokenScannerSymbols;
import com.hubspot.jinjava.tree.parse.ExpressionToken;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.tree.parse.Token;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeferredValueUtilsTest {

  @Test
  public void itFindsGlobalProperties() {
    Context context = new Context();
    context.put("java_bean", getPopulatedJavaBean());

    context =
      getContext(
        Lists.newArrayList(getNodeForClass(TagNode.class, "{% if java_bean %}")),
        Optional.of(context)
      );

    Set<String> deferredProperties = DeferredValueUtils.findAndMarkDeferredProperties(
      context
    );

    assertThat(deferredProperties).contains("java_bean");
  }

  @Test
  public void itDefersWholePropertyOnArrayAccess() {
    Context context = getContext(
      Lists.newArrayList(getNodeForClass(TagNode.class, "{{ array[0] }}"))
    );
    context.put("array", Lists.newArrayList("a", "b", "c"));

    Set<String> deferredProperties = DeferredValueUtils.findAndMarkDeferredProperties(
      context
    );
    assertThat(deferredProperties).contains("array");
  }

  @Test
  public void itDefersWholePropertyOnDictAccess() {
    Context context = getContext(
      Lists.newArrayList(getNodeForClass(TagNode.class, "{{ dict['a'] }}"))
    );
    context.put("dict", Collections.singletonMap("a", "x"));

    Set<String> deferredProperties = DeferredValueUtils.findAndMarkDeferredProperties(
      context
    );
    assertThat(deferredProperties).contains("dict");
  }

  @Test
  public void itDefersTheCompleteObjectWhenAtLeastOnePropertyIsUsed() {
    Context context = new Context();
    context.put("java_bean", getPopulatedJavaBean());

    context =
      getContext(
        Lists.newArrayList(
          getNodeForClass(
            TagNode.class,
            "{% if java_bean.property_one %}",
            Optional.empty(),
            Optional.empty()
          )
        ),
        Optional.of(context)
      );

    DeferredValueUtils.findAndMarkDeferredProperties(context);
    assertThat(context.containsKey("java_bean")).isTrue();
    assertThat(context.get("java_bean")).isInstanceOf(DeferredValue.class);
    DeferredValue deferredValue = (DeferredValue) context.get("java_bean");
    JavaBean originalValue = (JavaBean) deferredValue.getOriginalValue();
    assertThat(originalValue).hasFieldOrPropertyWithValue("propertyOne", "propertyOne");
    assertThat(originalValue).hasFieldOrPropertyWithValue("propertyTwo", "propertyTwo");
  }

  @Test
  public void itHandlesCaseWhereValueIsNull() {
    Context context = getContext(
      Lists.newArrayList(
        getNodeForClass(
          TagNode.class,
          "{% if property.id %}",
          Optional.empty(),
          Optional.empty()
        )
      )
    );
    context.put("property", null);
    DeferredValueUtils.findAndMarkDeferredProperties(context);

    assertThat(context.get("property")).isNull();
  }

  @Test
  public void itPreservesNonDeferredProperties() {
    Context context = getContext(
      Lists.newArrayList(
        getNodeForClass(
          TagNode.class,
          "{% if deferred %}",
          Optional.empty(),
          Optional.empty()
        )
      )
    );
    context.put("deferred", "deferred");
    context.put("not_deferred", "test_value");

    DeferredValueUtils.findAndMarkDeferredProperties(context);
    assertThat(context.get("not_deferred")).isEqualTo("test_value");
  }

  @Test
  public void itRestoresContextSuccessfully() {
    Context context = new Context();
    ImmutableMap<String, String> simpleMap = ImmutableMap.of("a", "x", "b", "y");
    ImmutableMap<String, Object> nestedMap = ImmutableMap.of("nested", simpleMap);
    Integer[] simpleArray = { 1, 2, 3, 4, 5, 6 };
    JavaBean javaBean = getPopulatedJavaBean();
    context.put("simple_var", DeferredValue.instance("SimpleVar"));
    context.put("java_bean", DeferredValue.instance(javaBean));
    context.put("simple_bool", DeferredValue.instance(true));
    context.put("simple_array", DeferredValue.instance(simpleArray));
    context.put("simple_map", DeferredValue.instance(simpleMap));
    context.put("nested_map", DeferredValue.instance(nestedMap));

    context.put("simple_var_undeferred", "SimpleVarUnDeferred");
    context.put("java_bean_undeferred", javaBean);
    context.put("nested_map_undeferred", nestedMap);

    HashMap<String, Object> result = DeferredValueUtils.getDeferredContextWithOriginalValues(
      context
    );
    assertThat(result).contains(entry("simple_var", "SimpleVar"));
    assertThat(result).contains(entry("java_bean", javaBean));
    assertThat(result).contains(entry("simple_bool", true));
    assertThat(result).contains(entry("simple_array", simpleArray));
    assertThat(result).contains(entry("simple_map", simpleMap));
    assertThat(result).contains(entry("nested_map", nestedMap));

    assertThat(result)
      .doesNotContain(entry("simple_var_undeferred", "SimpleVarUnDeferred"));
    assertThat(result).doesNotContain(entry("java_bean_undeferred", javaBean));
    assertThat(result).doesNotContain(entry("nested_map_undeferred", nestedMap));
  }

  @Test
  public void itIgnoresUnrestorableValuesFromDeferredContext() {
    Context context = new Context();
    context.put("simple_var", DeferredValue.instance());
    context.put("java_bean", DeferredValue.instance());

    HashMap<String, Object> result = DeferredValueUtils.getDeferredContextWithOriginalValues(
      context
    );
    assertThat(result).isEmpty();
  }

  @Test
  public void itDefersSetWordsInDeferredTokens() {
    Context context = new Context();
    context.put("var_a", "a");
    DeferredToken deferredToken = new DeferredToken(
      new TagToken(
        "{% set var_a, var_b = deferred, deferred %}",
        1,
        1,
        new DefaultTokenScannerSymbols()
      ),
      ImmutableSet.of(),
      ImmutableSet.of("var_a", "var_b")
    );
    context.handleDeferredToken(deferredToken);
    assertThat(context.get("var_a")).isInstanceOf(DeferredValue.class);
    assertThat(context.get("var_b")).isInstanceOf(DeferredValue.class);
  }

  @Test
  public void itDefersUsedWordsInDeferredTokens() {
    Context context = new Context();
    context.put("var_a", "a");
    DeferredToken deferredToken = new DeferredToken(
      new ExpressionToken(
        "{{ var_a.append(deferred|int)}}",
        1,
        1,
        new DefaultTokenScannerSymbols()
      ),
      ImmutableSet.of("var_a", "int")
    );
    context.handleDeferredToken(deferredToken);
    assertThat(context.get("var_a")).isInstanceOf(DeferredValue.class);
    assertThat(context.containsKey("int")).isFalse();
  }

  private Context getContext(List<? extends Node> nodes) {
    return getContext(nodes, Optional.empty());
  }

  private Context getContext(
    List<? extends Node> nodes,
    Optional<Context> initialContext
  ) {
    Context context = new Context();

    if (initialContext.isPresent()) {
      context = initialContext.get();
    }
    for (Node node : nodes) {
      context.handleDeferredNode(node);
    }
    return context;
  }

  private <T extends Node> T getNodeForClass(Class<T> clazz, String image) {
    return getNodeForClass(clazz, image, Optional.empty(), Optional.empty());
  }

  private <T extends Node> T getNodeForClass(
    Class<T> clazz,
    String image,
    Optional<List<Node>> childNodes,
    Optional<String> endName
  ) {
    T node = mock(clazz);
    Token token = mock(Token.class);
    if (childNodes.isPresent()) {
      LinkedList<Node> children = new LinkedList<>();
      children.addAll(childNodes.get());
      when(node.getChildren()).thenReturn(children);
    }
    when(token.getImage()).thenReturn(image);
    when(node.getMaster()).thenReturn(token);
    if (node instanceof ExpressionNode) {
      when(node.toString()).thenReturn(image);
    }
    if (node instanceof TagNode && endName.isPresent()) {
      TagNode tagNode = (TagNode) node;
      when(tagNode.getEndName()).thenReturn(endName.get());
      when(tagNode.reconstructEnd()).thenReturn("{% " + endName.get() + " %}");
    }
    return node;
  }

  private JavaBean getPopulatedJavaBean() {
    JavaBean javaBean = new JavaBean();
    javaBean.setPropertyOne("propertyOne");
    javaBean.setPropertyTwo("propertyTwo");
    return javaBean;
  }

  private class JavaBean {
    String propertyOne;
    String propertyTwo;

    public String getPropertyOne() {
      return propertyOne;
    }

    public JavaBean setPropertyOne(String propertyOne) {
      this.propertyOne = propertyOne;
      return this;
    }

    public String getPropertyTwo() {
      return propertyTwo;
    }

    public JavaBean setPropertyTwo(String propertyTwo) {
      this.propertyTwo = propertyTwo;
      return this;
    }
  }
}
