package com.hubspot.jinjava.lib.expression;

import com.hubspot.jinjava.ExpectedTemplateInterpreter;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.objects.collections.PyList;
import com.hubspot.jinjava.tree.ExpressionNodeTest;
import java.util.ArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EagerExpressionStrategyTest extends ExpressionNodeTest {
  private ExpectedTemplateInterpreter expectedTemplateInterpreter;

  @Before
  public void eagerSetup() {
    interpreter =
      new JinjavaInterpreter(
        jinjava,
        context,
        JinjavaConfig.newBuilder().withExecutionMode(new EagerExecutionMode()).build()
      );
    JinjavaInterpreter.pushCurrent(interpreter);
    expectedTemplateInterpreter =
      new ExpectedTemplateInterpreter(jinjava, interpreter, "expression");
    context.put("deferred", DeferredValue.instance());
  }

  @After
  public void teardown() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itPreservesRawTags() {
    interpreter =
      new JinjavaInterpreter(
        jinjava,
        context,
        JinjavaConfig
          .newBuilder()
          .withNestedInterpretationEnabled(false)
          .withExecutionMode(new EagerExecutionMode())
          .build()
      );
    JinjavaInterpreter.pushCurrent(interpreter);
    try {
      expectedTemplateInterpreter.assertExpectedOutput("preserves-raw-tags");
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }

  @Test
  public void itPreservesRawTagsNestedInterpretation() {
    expectedTemplateInterpreter.assertExpectedOutput(
      "preserves-raw-tags-nested-interpretation"
    );
  }

  @Test
  public void itPrependsMacro() {
    expectedTemplateInterpreter.assertExpectedOutput("prepends-macro");
  }

  @Test
  public void itPrependsSet() {
    context.put("foo", new PyList(new ArrayList<>()));
    expectedTemplateInterpreter.assertExpectedOutput("prepends-set");
  }
}
