package com.hubspot.jinjava.el.ext.eager;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.el.JinjavaELContext;
import com.hubspot.jinjava.el.JinjavaInterpreterResolver;
import de.odysseus.el.tree.Bindings;
import java.lang.reflect.Method;
import javax.el.ValueExpression;
import org.junit.Before;
import org.junit.Test;

public class EagerAstIdentifierTest extends BaseInterpretingTest {

  private JinjavaELContext elContext;

  @Before
  public void setup() {
    elContext =
      new JinjavaELContext(interpreter, new JinjavaInterpreterResolver(interpreter));
  }

  @Test
  public void itSavesNullEvalResult() {
    EagerAstIdentifier identifier = new EagerAstIdentifier("foo", 0, true);
    identifier.eval(
      new Bindings(
        new Method[] {},
        new ValueExpression[] {
          jinjava
            .getEagerExpressionFactory()
            .createValueExpression(elContext, "#{foo}", Object.class),
        }
      ),
      elContext
    );
    assertThat(identifier.hasEvalResult()).isTrue();
  }
}
