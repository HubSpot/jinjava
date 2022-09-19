/**********************************************************************
 * Copyright (c) 2014 HubSpot Inc.
 *
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
 **********************************************************************/
package com.hubspot.jinjava.tree;

import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.expression.DefaultExpressionStrategy;
import com.hubspot.jinjava.lib.expression.ExpressionStrategy;
import com.hubspot.jinjava.tree.output.OutputNode;
import com.hubspot.jinjava.tree.output.RenderedOutputNode;
import com.hubspot.jinjava.tree.parse.ExpressionToken;

public class ExpressionNode extends Node {
  private static final long serialVersionUID = -6063173739682221042L;

  private final ExpressionStrategy expressionStrategy;
  private final ExpressionToken master;

  public ExpressionNode(ExpressionToken token) {
    super(token, token.getLineNumber(), token.getStartPosition());
    this.expressionStrategy = new DefaultExpressionStrategy();
    master = token;
  }

  public ExpressionNode(ExpressionStrategy expressionStrategy, ExpressionToken token) {
    super(token, token.getLineNumber(), token.getStartPosition());
    this.expressionStrategy = expressionStrategy;
    master = token;
  }

  @Override
  public OutputNode render(JinjavaInterpreter interpreter) {
    preProcess(interpreter);
    try {
      return expressionStrategy.interpretOutput(master, interpreter);
    } catch (DeferredValueException e) {
      checkForInterrupt();
      interpreter.getContext().handleDeferredNode(this);
      return new RenderedOutputNode(master.getImage());
    }
  }

  @Override
  public String toString() {
    return master.toString();
  }

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }
}
