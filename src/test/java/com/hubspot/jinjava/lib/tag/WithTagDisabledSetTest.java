/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hubspot.jinjava.lib.tag;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.mode.ExecutionMode;
import java.util.HashMap;
import org.junit.Test;

public class WithTagDisabledSetTest {

  @Test
  public void itDoesNotAssignWhenSetTagIsDisabled() {
    assertSetIsNotBypassed(BaseJinjavaTest.newConfigBuilder().build().getExecutionMode());
  }

  @Test
  public void itDoesNotAssignWhenSetTagIsDisabledInEagerMode() {
    assertSetIsNotBypassed(EagerExecutionMode.instance());
  }

  private void assertSetIsNotBypassed(ExecutionMode executionMode) {
    // with assigns through set, so disabling set must also cover with
    Jinjava jinjava = new Jinjava(
      BaseJinjavaTest
        .newConfigBuilder()
        .withExecutionMode(executionMode)
        .withDisabled(ImmutableMap.of(Context.Library.TAG, ImmutableSet.of("set")))
        .build()
    );
    RenderResult result = jinjava.renderForResult(
      "{% with a = 1 %}[{{ a }}]{% endwith %}{% with %}ok{% endwith %}",
      new HashMap<>()
    );
    assertThat(result.getOutput()).isEqualTo("ok");
    assertThat(result.getErrors())
      .extracting(TemplateError::getReason)
      .containsExactly(ErrorReason.DISABLED);
  }
}
