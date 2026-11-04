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
package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.tag.WithTag;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.util.EagerContextWatcher;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult.ResolutionState;
import com.hubspot.jinjava.util.EagerReconstructionUtils;

/**
 * Eager version of {@link WithTag}. When everything inside the with block resolves,
 * the block is replaced by its output. When something is deferred, the output keeps
 * the deferred assignments (as a set tag) and the body inside a child scope, so the
 * names assigned by the with tag do not leak once the template is rendered later.
 */
@Beta
public class EagerWithTag extends EagerTagDecorator<WithTag> {

  public EagerWithTag() {
    super(new WithTag());
  }

  public EagerWithTag(WithTag withTag) {
    super(withTag);
  }

  @Override
  public String innerInterpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    // Surface syntax errors directly instead of trying to reconstruct the tag.
    WithTag.parseAssignments(tagNode);
    EagerExecutionResult result = renderInChildContext(tagNode, interpreter, false);
    if (result.getResult().getResolutionState() == ResolutionState.NONE) {
      EagerReconstructionUtils.resetSpeculativeBindings(interpreter, result);
      throw new DeferredValueException(result.getResult().toString(true));
    }
    if (result.getResult().isFullyResolved()) {
      return result.getResult().toString(true);
    }
    return (
      result.getPrefixToPreserveState() +
      EagerReconstructionUtils.wrapInChildScope(
        result.getResult().toString(true),
        interpreter
      )
    );
  }

  @Override
  public String eagerInterpret(
    TagNode tagNode,
    JinjavaInterpreter interpreter,
    InterpretException e
  ) {
    if (e instanceof TemplateSyntaxException) {
      throw e;
    }
    EagerExecutionResult result = renderInChildContext(tagNode, interpreter, true);
    if (result.getResult().getResolutionState() == ResolutionState.NONE) {
      throw new DeferredValueException(result.getResult().toString(true));
    }
    return (
      result.getPrefixToPreserveState() +
      EagerReconstructionUtils.wrapInChildScope(
        result.getResult().toString(true),
        interpreter
      )
    );
  }

  private EagerExecutionResult renderInChildContext(
    TagNode tagNode,
    JinjavaInterpreter interpreter,
    boolean forceDeferredExecutionMode
  ) {
    return EagerContextWatcher.executeInChildContext(
      eagerInterpreter ->
        EagerExpressionResult.fromSupplier(
          () -> getTag().interpret(tagNode, eagerInterpreter),
          eagerInterpreter
        ),
      interpreter,
      EagerContextWatcher.EagerChildContextConfig
        .newBuilder()
        .withForceDeferredExecutionMode(forceDeferredExecutionMode)
        .build()
    );
  }
}
