package com.hubspot.jinjava.lib.tag.eager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.OutputTooBigException;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.objects.collections.PyList;
import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.TagToken;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EagerTagDecoratorTest extends BaseInterpretingTest {

  private static final long MAX_OUTPUT_SIZE = 50L;
  private Tag mockTag;
  private EagerGenericTag<Tag> eagerTagDecorator;

  @Before
  public void eagerSetup() throws Exception {
    jinjava
      .getGlobalContext()
      .registerFunction(
        new ELFunctionDefinition(
          "",
          "add_to_context",
          this.getClass().getDeclaredMethod("addToContext", String.class, Object.class)
        )
      );
    jinjava
      .getGlobalContext()
      .registerFunction(
        new ELFunctionDefinition(
          "",
          "modify_context",
          this.getClass().getDeclaredMethod("modifyContext", String.class, Object.class)
        )
      );
    interpreter =
      new JinjavaInterpreter(
        jinjava,
        context,
        JinjavaConfig
          .newBuilder()
          .withMaxOutputSize(MAX_OUTPUT_SIZE)
          .withExecutionMode(EagerExecutionMode.instance())
          .build()
      );
    mockTag = mock(Tag.class);
    eagerTagDecorator = new EagerGenericTag<>(mockTag);

    JinjavaInterpreter.pushCurrent(interpreter);
    context.put("deferred", DeferredValue.instance());
  }

  @After
  public void teardown() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itDoesntLimitShortString() {
    String template = "{% if true %}abc{% endif %}";

    TagNode tagNode = (TagNode) (interpreter.parse(template).getChildren().get(0));
    assertThat(eagerTagDecorator.eagerInterpret(tagNode, interpreter, null))
      .isEqualTo(template);
    assertThat(interpreter.getErrors()).hasSize(0);
  }

  @Test
  public void itLimitsEagerInterpretLength() {
    StringBuilder tooLong = new StringBuilder();
    for (int i = 0; i < MAX_OUTPUT_SIZE; i++) {
      tooLong.append(i);
    }
    TagNode tagNode = (TagNode) (
      interpreter
        .parse(String.format("{%% raw %%}%s{%% endraw %%}", tooLong.toString()))
        .getChildren()
        .get(0)
    );
    assertThatThrownBy(() -> eagerTagDecorator.eagerInterpret(tagNode, interpreter, null))
      .isInstanceOf(OutputTooBigException.class);
  }

  @Test
  public void itLimitsInterpretLength() {
    when(mockTag.interpret(any(), any())).thenThrow(new DeferredValueException(""));
    StringBuilder tooLong = new StringBuilder();
    for (int i = 0; i < MAX_OUTPUT_SIZE; i++) {
      tooLong.append(i);
    }
    TagNode tagNode = (TagNode) (
      interpreter
        .parse(String.format("{%% raw %%}%s{%% endraw %%}", tooLong.toString()))
        .getChildren()
        .get(0)
    );
    assertThatThrownBy(() -> eagerTagDecorator.interpret(tagNode, interpreter))
      .isInstanceOf(DeferredValueException.class);
  }

  @Test
  public void itLimitsTagLength() {
    TagNode tagNode = (TagNode) (
      interpreter.parse("{% print range(0, 50) %}").getChildren().get(0)
    );
    assertThatThrownBy(() ->
        eagerTagDecorator.getEagerTagImage((TagToken) tagNode.getMaster(), interpreter)
      )
      .isInstanceOf(OutputTooBigException.class);
  }

  @Test
  public void itPutsOnContextInChildContext() {
    assertThat(interpreter.render("{{ add_to_context('foo', 'bar') }}{{ foo }}"))
      .isEqualTo("bar");
  }

  @Test
  public void itModifiesContextInChildContext() {
    context.put("foo", new ArrayList<>());
    assertThat(interpreter.render("{{ modify_context('foo', 'bar') }}{{ foo }}"))
      .isEqualTo("[bar]");
  }

  @Test
  public void itDoesntModifyContextWhenResultIsDeferred() {
    context.put("foo", new ArrayList<>());
    assertThat(
      interpreter.render("{{ modify_context('foo', 'bar') ~ deferred }}{{ foo }}")
    )
      .isEqualTo("{{ null ~ deferred }}[bar]");
  }

  public static void addToContext(String key, Object value) {
    JinjavaInterpreter.getCurrent().getContext().put(key, value);
  }

  public static void modifyContext(String key, Object value) {
    ((List<Object>) JinjavaInterpreter.getCurrent().getContext().get(key)).add(value);
  }

  static class TooBig extends PyList implements PyishSerializable {

    public TooBig(List<Object> list) {
      super(list);
    }

    @Override
    public <T extends Appendable & CharSequence> T appendPyishString(T appendable)
      throws IOException {
      throw new OutputTooBigException(1, 1);
    }
  }

  @Test
  public void itDefersNodeWhenOutputTooBigIsThrownWithinInnerInterpret() {
    TooBig tooBig = new TooBig(new ArrayList<>());
    interpreter =
      new JinjavaInterpreter(
        jinjava,
        context,
        JinjavaConfig
          .newBuilder()
          .withExecutionMode(EagerExecutionMode.instance())
          .build()
      );
    interpreter.getContext().put("too_big", tooBig);
    interpreter.render(
      "{% for i in range(2) %}{% do too_big.append(deferred) %}{% endfor %}"
    );
    assertThat(interpreter.getContext().getDeferredNodes()).isNotEmpty();
  }
}
