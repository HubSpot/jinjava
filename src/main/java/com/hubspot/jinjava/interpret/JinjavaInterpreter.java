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
package com.hubspot.jinjava.interpret;

import static com.hubspot.jinjava.util.Logging.ENGINE_LOG;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.el.ExpressionResolver;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TreeParser;
import com.hubspot.jinjava.tree.output.BlockPlaceholderOutputNode;
import com.hubspot.jinjava.tree.output.OutputList;
import com.hubspot.jinjava.util.Variable;
import com.hubspot.jinjava.util.WhitespaceUtils;

public class JinjavaInterpreter {

  private final Multimap<String, List<? extends Node>> blocks = ArrayListMultimap.create();
  private final LinkedList<Node> extendParentRoots = new LinkedList<>();

  private Context context;
  private final JinjavaConfig config;

  private final ExpressionResolver expressionResolver;
  private final Jinjava application;

  private int lineNumber = -1;
  private final List<TemplateError> errors = new LinkedList<>();

  public JinjavaInterpreter(Jinjava application, Context context, JinjavaConfig renderConfig) {
    this.context = context;
    this.config = renderConfig;
    this.application = application;

    this.expressionResolver = new ExpressionResolver(this, application.getExpressionFactory());
  }

  public JinjavaInterpreter(JinjavaInterpreter orig) {
    this(orig.application, new Context(orig.context), orig.config);
  }

  public JinjavaConfig getConfiguration() {
    return config;
  }

  public void addExtendParentRoot(Node root) {
    extendParentRoots.add(root);
  }

  public void addBlock(String name, LinkedList<? extends Node> value) {
    blocks.put(name, value);
  }

  /**
   * Creates a new variable scope, extending from the current scope. Allows you to create a nested
   * contextual scope which can override variables from higher levels.
   *
   * Should be used in a try/finally context, similar to lock-use patterns:
   *
   * <code>
   * interpreter.enterScope();
   * try (interpreter.enterScope()) {
   *   // ...
   * }
   * </code>
   */
  public InterpreterScopeClosable enterScope() {
    context = new Context(context);
    return new InterpreterScopeClosable();
  }

  public void leaveScope() {
    Context parent = context.getParent();
    if (parent != null) {
      context = parent;
    }
  }

  public class InterpreterScopeClosable implements AutoCloseable {

    @Override
    public void close() {
      leaveScope();
    }

  }

  public Node parse(String template) {
    return new TreeParser(this, template).buildTree();
  }

  /**
   * Parse the given string into a root Node, and then render it without processing any extend parents.
   * This method should be used when the template is known to not have any extends or block tags.
   *
   * @param template
   *          string to parse
   * @return rendered result
   */
  public String renderFlat(String template) {
    int depth = context.getRenderDepth();

    try {
      if (depth > config.getMaxRenderDepth()) {
        ENGINE_LOG.warn("Max render depth exceeded: {}", Integer.toString(depth));
        return template;
      } else {
        context.setRenderDepth(depth + 1);
        return render(parse(template), false);
      }
    } finally {
      context.setRenderDepth(depth);
    }
  }

  /**
   * Parse the given string into a root Node, and then renders it processing extend parents.
   *
   * @param template
   *          string to parse
   * @return rendered result
   */
  public String render(String template) {
    ENGINE_LOG.debug(template);
    return render(parse(template), true);
  }

  /**
   * Render the given root node, processing extend parents. Equivalent to render(root, true)
   *
   * @param root
   *          node to render
   * @return rendered result
   */
  public String render(Node root) {
    return render(root, true);
  }

  /**
   * Render the given root node using this interpreter's current context
   *
   * @param root
   *          node to render
   * @param processExtendRoots
   *          if true, also render all extend parents
   * @return rendered result
   */
  public String render(Node root, boolean processExtendRoots) {
    OutputList output = new OutputList();

    for (Node node : root.getChildren()) {
      lineNumber = node.getLineNumber();
      output.addNode(node.render(this));
    }

    // render all extend parents, keeping the last as the root output
    if (processExtendRoots) {
      while (!extendParentRoots.isEmpty()) {
        Node parentRoot = extendParentRoots.removeFirst();
        output = new OutputList();

        for (Node node : parentRoot.getChildren()) {
          output.addNode(node.render(this));
        }

        context.getExtendPathStack().pop();
      }
    }

    resolveBlockStubs(output);

    return output.getValue();
  }

  private void resolveBlockStubs(OutputList output) {
    resolveBlockStubs(output, new Stack<>());
  }

  private void resolveBlockStubs(OutputList output, Stack<String> blockNames) {
    for (BlockPlaceholderOutputNode blockPlaceholder : output.getBlocks()) {

      if (!blockNames.contains(blockPlaceholder.getBlockName())) {
        Collection<List<? extends Node>> blockChain = blocks.get(blockPlaceholder.getBlockName());
        List<? extends Node> block = Iterables.getFirst(blockChain, null);

        if (block != null) {
          List<? extends Node> superBlock = Iterables.get(blockChain, 1, null);
          context.setSuperBlock(superBlock);

          OutputList blockValueBuilder = new OutputList();

          for (Node child : block) {
            blockValueBuilder.addNode(child.render(this));
          }

          blockNames.push(blockPlaceholder.getBlockName());
          resolveBlockStubs(blockValueBuilder, blockNames);
          blockNames.pop();

          context.removeSuperBlock();

          blockPlaceholder.resolve(blockValueBuilder.getValue());
        }
      }

      if (!blockPlaceholder.isResolved()) {
        blockPlaceholder.resolve("");
      }
    }
  }

  /**
   * Resolve a variable from the interpreter context, returning null if not found. This method updates the template error accumulators when a variable is not found.
   *
   * @param variable
   *          name of variable in context
   * @param lineNumber
   *          current line number, for error reporting
   * @return resolved value for variable
   */
  public Object retraceVariable(String variable, int lineNumber) {
    if (StringUtils.isBlank(variable)) {
      return "";
    }
    Variable var = new Variable(this, variable);
    String varName = var.getName();
    Object obj = context.get(varName);
    if (obj != null) {
      obj = var.resolve(obj);
    }
    return obj;
  }

  /**
   * Resolve a variable into an object value. If given a string literal (e.g. 'foo' or "foo"), this method returns the literal unquoted. If the variable is undefined in the context, this method returns the given variable string.
   *
   * @param variable
   *          name of variable in context
   * @param lineNumber
   *          current line number, for error reporting
   * @return resolved value for variable
   */
  public Object resolveObject(String variable, int lineNumber) {
    if (StringUtils.isBlank(variable)) {
      return "";
    }
    if (WhitespaceUtils.isQuoted(variable)) {
      return WhitespaceUtils.unquote(variable);
    } else {
      Object val = retraceVariable(variable, lineNumber);
      if (val == null) {
        return variable;
      }
      return val;
    }
  }

  /**
   * Resolve a variable into a string value. If given a string literal (e.g. 'foo' or "foo"), this method returns the literal unquoted. If the variable is undefined in the context, this method returns the given variable string.
   *
   * @param variable
   *          name of variable in context
   * @param lineNumber
   *          current line number, for error reporting
   * @return resolved value for variable
   */
  public String resolveString(String variable, int lineNumber) {
    return Objects.toString(resolveObject(variable, lineNumber), "");
  }

  public Context getContext() {
    return context;
  }

  public String getResource(String resource) throws IOException {
    return application.getResourceLocator().getString(resource, config.getCharset(), this);
  }

  public JinjavaConfig getConfig() {
    return config;
  }

  /**
   * Resolve expression against current context.
   *
   * @param expression
   *          Jinja expression.
   * @param lineNumber
   *          Line number of expression.
   * @return Value of expression.
   */
  public Object resolveELExpression(String expression, int lineNumber) {
    this.lineNumber = lineNumber;

    return expressionResolver.resolveExpression(expression);
  }

  /**
   * Resolve property of bean.
   *
   * @param object
   *          Bean.
   * @param propertyName
   *          Name of property to resolve.
   * @return Value of property.
   */
  public Object resolveProperty(Object object, String propertyName) {
    return resolveProperty(object, Collections.singletonList(propertyName));
  }

  /**
   * Resolve property of bean.
   *
   * @param object
   *          Bean.
   * @param propertyNames
   *          Names of properties to resolve recursively.
   * @return Value of property.
   */
  public Object resolveProperty(Object object, List<String> propertyNames) {
    return expressionResolver.resolveProperty(object, propertyNames);
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public void addError(TemplateError templateError) {
    this.errors.add(templateError);
  }

  public List<TemplateError> getErrors() {
    return errors;
  }

  private static final ThreadLocal<Stack<JinjavaInterpreter>> CURRENT_INTERPRETER = ThreadLocal.withInitial(Stack::new);

  public static JinjavaInterpreter getCurrent() {
    if (CURRENT_INTERPRETER.get().isEmpty()) {
      return null;
    }

    return CURRENT_INTERPRETER.get().peek();
  }

  public static Optional<JinjavaInterpreter> getCurrentMaybe() {
    return Optional.ofNullable(getCurrent());
  }

  public static void pushCurrent(JinjavaInterpreter interpreter) {
    CURRENT_INTERPRETER.get().push(interpreter);
  }

  public static void popCurrent() {
    if (!CURRENT_INTERPRETER.get().isEmpty()) {
      CURRENT_INTERPRETER.get().pop();
    }
  }

}
