/**********************************************************************
 * Copyright (c) 2014 HubSpot Inc.
 *
 * Licensed under the Apache License Version 2.0 (the "License");
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

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.el.ExpressionResolver;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.ext.ExtendedParser;
import com.hubspot.jinjava.interpret.TemplateError.ErrorItem;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import com.hubspot.jinjava.interpret.errorcategory.BasicTemplateErrorCategory;
import com.hubspot.jinjava.lib.tag.ExtendsTag;
import com.hubspot.jinjava.lib.tag.SetTag;
import com.hubspot.jinjava.lib.tag.eager.EagerGenericTag;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import com.hubspot.jinjava.random.ConstantZeroRandomNumberGenerator;
import com.hubspot.jinjava.random.DeferredRandomNumberGenerator;
import com.hubspot.jinjava.tree.ExpressionNode;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.TreeParser;
import com.hubspot.jinjava.tree.output.BlockInfo;
import com.hubspot.jinjava.tree.output.BlockPlaceholderOutputNode;
import com.hubspot.jinjava.tree.output.OutputList;
import com.hubspot.jinjava.tree.output.OutputNode;
import com.hubspot.jinjava.tree.output.RenderedOutputNode;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import com.hubspot.jinjava.util.Variable;
import com.hubspot.jinjava.util.WhitespaceUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class JinjavaInterpreter implements PyishSerializable {
  private final Multimap<String, BlockInfo> blocks = ArrayListMultimap.create();
  private final LinkedList<Node> extendParentRoots = new LinkedList<>();
  private final Map<String, RevertibleObject> revertibleObjects = new HashMap<>();

  private Context context;
  private final JinjavaConfig config;

  private final ExpressionResolver expressionResolver;
  private final Jinjava application;
  private final Random random;

  private int lineNumber = -1;
  private int position = 0;
  private int scopeDepth = 1;
  private BlockInfo currentBlock;
  private final List<TemplateError> errors = new LinkedList<>();
  private static final int MAX_ERROR_SIZE = 100;

  public JinjavaInterpreter(
    Jinjava application,
    Context context,
    JinjavaConfig renderConfig
  ) {
    this.context = context;
    this.config = renderConfig;
    this.application = application;

    this.config.getExecutionMode().prepareContext(this.context);

    switch (config.getRandomNumberGeneratorStrategy()) {
      case THREAD_LOCAL:
        random = ThreadLocalRandom.current();
        break;
      case CONSTANT_ZERO:
        random = new ConstantZeroRandomNumberGenerator();
        break;
      case DEFERRED:
        random = new DeferredRandomNumberGenerator();
        break;
      default:
        throw new IllegalStateException(
          "No random number generator with strategy " +
          config.getRandomNumberGeneratorStrategy()
        );
    }

    this.expressionResolver = new ExpressionResolver(this, application);
  }

  public JinjavaInterpreter(JinjavaInterpreter orig) {
    this(orig.application, new Context(orig.context), orig.config);
    scopeDepth = orig.getScopeDepth() + 1;
  }

  /**
   * @deprecated use {{@link #getConfig()}}
   */
  @Deprecated
  public JinjavaConfig getConfiguration() {
    return config;
  }

  public void addExtendParentRoot(Node root) {
    extendParentRoots.add(root);
  }

  public void addBlock(String name, BlockInfo blockInfo) {
    blocks.put(name, blockInfo);
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
    return enterScope(null);
  }

  public InterpreterScopeClosable enterScope(Map<Context.Library, Set<String>> disabled) {
    context = new Context(context, null, disabled);
    scopeDepth++;
    return new InterpreterScopeClosable();
  }

  public InterpreterScopeClosable enterNonStackingScope() {
    context = new Context(context, null, null, false);
    scopeDepth++;
    return new InterpreterScopeClosable();
  }

  public void leaveScope() {
    Context parent = context.getParent();
    scopeDepth--;
    if (parent != null) {
      parent.addDependencies(context.getDependencies());
      context = parent;
    }
  }

  public Random getRandom() {
    return random;
  }

  public boolean isValidationMode() {
    return config.isValidationMode();
  }

  public Map<String, RevertibleObject> getRevertibleObjects() {
    return revertibleObjects;
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
    OutputList output = new OutputList(config.getMaxOutputSize());

    for (Node node : root.getChildren()) {
      lineNumber = node.getLineNumber();
      position = node.getStartPosition();
      String renderStr = node.getMaster().getImage();
      try {
        if (node instanceof ExpressionNode && context.doesRenderStackContain(renderStr)) {
          // This is a circular rendering. Stop rendering it here.
          addError(
            new TemplateError(
              ErrorType.WARNING,
              ErrorReason.EXCEPTION,
              ErrorItem.TAG,
              "Rendering cycle detected: '" + renderStr + "'",
              null,
              getLineNumber(),
              node.getStartPosition(),
              null,
              BasicTemplateErrorCategory.IMPORT_CYCLE_DETECTED,
              ImmutableMap.of("string", renderStr)
            )
          );
          output.addNode(new RenderedOutputNode(renderStr));
        } else {
          OutputNode out;
          context.pushRenderStack(renderStr);
          try {
            out = node.render(this);
          } catch (DeferredValueException e) {
            context.handleDeferredNode(node);
            out = new RenderedOutputNode(node.getMaster().getImage());
          }
          context.popRenderStack();
          output.addNode(out);
        }
      } catch (OutputTooBigException e) {
        addError(TemplateError.fromOutputTooBigException(e));
        return output.getValue();
      } catch (CollectionTooBigException e) {
        addError(
          new TemplateError(
            ErrorType.FATAL,
            ErrorReason.COLLECTION_TOO_BIG,
            ErrorItem.OTHER,
            ExceptionUtils.getMessage(e),
            null,
            -1,
            -1,
            e,
            BasicTemplateErrorCategory.UNKNOWN,
            ImmutableMap.of()
          )
        );
        return output.getValue();
      }
    }
    StringBuilder ignoredOutput = new StringBuilder();

    // render all extend parents, keeping the last as the root output
    if (processExtendRoots) {
      Set<String> extendPaths = new HashSet<>();
      Optional<String> extendPath = context.getExtendPathStack().peek();
      int numDeferredTokensBefore = 0;
      while (!extendParentRoots.isEmpty()) {
        if (extendPaths.contains(extendPath.orElse(""))) {
          addError(
            TemplateError.fromException(
              new ExtendsTagCycleException(
                extendPath.orElse(""),
                context.getExtendPathStack().getTopLineNumber(),
                context.getExtendPathStack().getTopStartPosition()
              )
            )
          );
          break;
        }
        extendPaths.add(extendPath.orElse(""));
        context
          .getCurrentPathStack()
          .push(
            extendPath.orElse(""),
            context.getExtendPathStack().getTopLineNumber(),
            context.getExtendPathStack().getTopStartPosition()
          );
        Node parentRoot = extendParentRoots.removeFirst();
        if (context.getDeferredTokens().size() > numDeferredTokensBefore) {
          ignoredOutput.append(
            output
              .getNodes()
              .stream()
              .filter(node -> node instanceof RenderedOutputNode)
              .map(OutputNode::getValue)
              .collect(Collectors.joining())
          );
        }
        numDeferredTokensBefore = context.getDeferredTokens().size();
        output = new OutputList(config.getMaxOutputSize());

        boolean hasNestedExtends = false;
        for (Node node : parentRoot.getChildren()) {
          lineNumber = node.getLineNumber() - 1; // The line number is off by one when rendering the extend parent
          position = node.getStartPosition();
          try {
            OutputNode out = node.render(this);
            output.addNode(out);
            if (isExtendsTag(node)) {
              hasNestedExtends = true;
            }
          } catch (OutputTooBigException e) {
            addError(TemplateError.fromOutputTooBigException(e));
            return output.getValue();
          }
        }

        Optional<String> currentExtendPath = context.getExtendPathStack().pop();
        extendPath =
          hasNestedExtends ? currentExtendPath : context.getExtendPathStack().peek();
        context.getCurrentPathStack().pop();
      }
    }

    resolveBlockStubs(output);
    if (ignoredOutput.length() > 0) {
      return (
        EagerReconstructionUtils.buildBlockSetTag(
          SetTag.IGNORED_VARIABLE_NAME,
          ignoredOutput.toString(),
          this,
          false
        ) +
        output.getValue()
      );
    }

    return output.getValue();
  }

  private void resolveBlockStubs(OutputList output) {
    resolveBlockStubs(output, new Stack<>());
  }

  private boolean isExtendsTag(Node node) {
    return (
      node instanceof TagNode &&
      (
        ((TagNode) node).getTag() instanceof ExtendsTag ||
        isEagerExtendsTag((TagNode) node)
      )
    );
  }

  private boolean isEagerExtendsTag(TagNode node) {
    return (
      node.getTag() instanceof EagerGenericTag &&
      ((EagerGenericTag) node.getTag()).getTag() instanceof ExtendsTag
    );
  }

  @SuppressFBWarnings(
    justification = "Iterables#getFirst DOES allow null for default value",
    value = "NP_NONNULL_PARAM_VIOLATION"
  )
  private void resolveBlockStubs(OutputList output, Stack<String> blockNames) {
    for (BlockPlaceholderOutputNode blockPlaceholder : output.getBlocks()) {
      if (!blockNames.contains(blockPlaceholder.getBlockName())) {
        Collection<BlockInfo> blockChain = blocks.get(blockPlaceholder.getBlockName());
        BlockInfo block = Iterables.getFirst(blockChain, null);

        if (block != null && block.getNodes() != null) {
          List<? extends Node> superBlock = Optional
            .ofNullable(Iterables.get(blockChain, 1, null))
            .map(BlockInfo::getNodes)
            .orElse(null);
          context.setSuperBlock(superBlock);
          currentBlock = block;

          OutputList blockValueBuilder = new OutputList(config.getMaxOutputSize());

          for (Node child : block.getNodes()) {
            lineNumber = child.getLineNumber();
            position = child.getStartPosition();

            boolean pushedParentPathOntoStack = false;
            if (
              block.getParentPath().isPresent() &&
              !getContext().getCurrentPathStack().contains(block.getParentPath().get())
            ) {
              getContext()
                .getCurrentPathStack()
                .push(
                  block.getParentPath().get(),
                  block.getParentLineNo(),
                  block.getParentPosition()
                );
              pushedParentPathOntoStack = true;
              lineNumber--; // The line number is off by one when rendering the block from the parent template
            }

            blockValueBuilder.addNode(child.render(this));

            if (pushedParentPathOntoStack) {
              getContext().getCurrentPathStack().pop();
            }
          }

          blockNames.push(blockPlaceholder.getBlockName());
          resolveBlockStubs(blockValueBuilder, blockNames);
          blockNames.pop();

          context.removeSuperBlock();
          currentBlock = null;

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
   * @param startPosition
   *          current line position, for error reporting
   * @return resolved value for variable
   */
  public Object retraceVariable(String variable, int lineNumber, int startPosition) {
    if (StringUtils.isBlank(variable)) {
      return "";
    }
    Variable var = new Variable(this, variable);
    String varName = var.getName();
    Object obj = context.get(varName);
    if (obj == null && context.getDynamicVariableResolver() != null) {
      obj = context.getDynamicVariableResolver().apply(varName);
    }
    if (obj != null) {
      if (obj instanceof DeferredValue && !(obj instanceof PartiallyDeferredValue)) {
        if (config.getExecutionMode().useEagerParser()) {
          throw new DeferredParsingException(this, variable);
        } else {
          throw new DeferredValueException(variable, lineNumber, startPosition);
        }
      }
      obj = var.resolve(obj);
    }
    return obj;
  }

  public Object retraceVariable(String variable, int lineNumber) {
    return retraceVariable(variable, lineNumber, -1);
  }

  /**
   * Resolve a variable into an object value. If given a string literal (e.g. 'foo' or "foo"), this method returns the literal unquoted. If the variable is undefined in the context, this method returns the given variable string.
   *
   * @param variable
   *          name of variable in context
   * @param lineNumber
   *          current line number, for error reporting
   * @param startPosition
   *          current line position, for error reporting
   * @return resolved value for variable
   */
  public Object resolveObject(String variable, int lineNumber, int startPosition) {
    if (StringUtils.isBlank(variable)) {
      return "";
    }
    if (WhitespaceUtils.isQuoted(variable)) {
      return WhitespaceUtils.unquote(variable);
    } else {
      Object val = retraceVariable(variable, lineNumber, startPosition);
      if (val == null) {
        return variable;
      }
      return val;
    }
  }

  public Object resolveObject(String variable, int lineNumber) {
    return resolveObject(variable, lineNumber, -1);
  }

  /**
   * Resolve a variable into a string value. If given a string literal (e.g. 'foo' or "foo"), this method returns the literal unquoted. If the variable is undefined in the context, this method returns the given variable string.
   *
   * @param variable
   *          name of variable in context
   * @param lineNumber
   *          current line number, for error reporting
   * @param startPosition
   *          current line position, for error reporting
   * @return resolved value for variable
   */
  public String resolveString(String variable, int lineNumber, int startPosition) {
    Object object = resolveObject(variable, lineNumber, startPosition);
    return getAsString(object);
  }

  public String getAsString(Object object) {
    if (config.getLegacyOverrides().isUsePyishObjectMapper()) {
      return PyishObjectMapper.getAsUnquotedPyishString(object);
    }
    return Objects.toString(object, "");
  }

  public String resolveString(String variable, int lineNumber) {
    return resolveString(variable, lineNumber, -1);
  }

  public Context getContext() {
    return context;
  }

  public String resolveResourceLocation(String location) {
    return application
      .getResourceLocator()
      .getLocationResolver()
      .map(resolver -> resolver.resolve(location, this))
      .orElse(location);
  }

  public String getResource(String resource) throws IOException {
    return application
      .getResourceLocator()
      .getString(resource, config.getCharset(), this);
  }

  public JinjavaConfig getConfig() {
    return config;
  }

  /**
   * Resolve expression against current context, but does not add the expression to the set of resolved expressions.
   *
   * @param expression
   *          Jinja expression.
   * @return Value of expression.
   */
  public Object resolveELExpressionSilently(String expression) {
    return expressionResolver.resolveExpression(expression, false);
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
   * Resolve expression against current context. Also set the interpreter's position,
   * useful for nodes that resolve multiple expressions such as a node using an IfTag and ElseTags.
   * @param expression Jinja expression.
   * @param lineNumber Line number of expression.
   * @param position Start position of expression.
   * @return Value of expression.
   */
  public Object resolveELExpression(String expression, int lineNumber, int position) {
    this.position = position;
    return resolveELExpression(expression, lineNumber);
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

  /**
   * Wrap an object in it's PyIsh equivalent
   *
   * @param object
   *          Bean.
   * @return Wrapped bean.
   */
  public Object wrap(Object object) {
    return expressionResolver.wrap(object);
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public void setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public BlockInfo getCurrentBlock() {
    return currentBlock;
  }

  public void addError(TemplateError templateError) {
    if (context.getThrowInterpreterErrors()) {
      if (templateError.getSeverity() == ErrorType.FATAL) {
        // Throw fatal errors when locating deferred words.
        throw new TemplateSyntaxException(
          this,
          templateError.getFieldName(),
          templateError.getMessage()
        );
      } else {
        // Hide warning errors when locating deferred words.
        return;
      }
    }
    // fix line numbers not matching up with source template
    if (!context.getCurrentPathStack().isEmpty()) {
      if (
        !templateError.getSourceTemplate().isPresent() &&
        context.getCurrentPathStack().peek().isPresent()
      ) {
        templateError.setMessage(
          getWrappedErrorMessage(
            context.getCurrentPathStack().peek().get(),
            templateError
          )
        );
        templateError.setSourceTemplate(context.getCurrentPathStack().peek().get());
      }
      templateError.setStartPosition(context.getCurrentPathStack().getTopStartPosition());
      templateError.setLineno(context.getCurrentPathStack().getTopLineNumber());
    }

    // Limit the number of error.
    if (errors.size() < MAX_ERROR_SIZE) {
      this.errors.add(templateError.withScopeDepth(scopeDepth));
    }
  }

  public void removeLastError() {
    if (!errors.isEmpty()) {
      errors.remove(errors.size() - 1);
    }
  }

  public Optional<TemplateError> getLastError() {
    return errors.isEmpty()
      ? Optional.empty()
      : Optional.of(errors.get(errors.size() - 1));
  }

  public int getScopeDepth() {
    return scopeDepth;
  }

  /**
    Use {@link #addAllChildErrors(String, Collection)} instead to fix error line numbers
   */
  @Deprecated
  public void addAllErrors(Collection<TemplateError> other) {
    if (errors.size() >= MAX_ERROR_SIZE) {
      return;
    }
    other.stream().limit(MAX_ERROR_SIZE - errors.size()).forEach(this::addError);
  }

  public void addAllChildErrors(
    String childTemplateName,
    Collection<TemplateError> childErrors
  ) {
    if (errors.size() >= MAX_ERROR_SIZE) {
      return;
    }

    childErrors
      .stream()
      .limit(MAX_ERROR_SIZE - errors.size())
      .forEach(
        error -> {
          if (!error.getSourceTemplate().isPresent()) {
            error.setMessage(getWrappedErrorMessage(childTemplateName, error));
            error.setSourceTemplate(childTemplateName);
          }
          error.setStartPosition(this.getPosition());
          error.setLineno(this.getLineNumber());
          this.addError(error);
        }
      );
  }

  // We cannot just remove this, other projects may depend on it.
  public List<TemplateError> getErrors() {
    return getErrorsCopy();
  }

  // Explicitly indicate this returns a copy of the errors list.
  public List<TemplateError> getErrorsCopy() {
    return Lists.newArrayList(errors);
  }

  private static final ThreadLocal<Stack<JinjavaInterpreter>> CURRENT_INTERPRETER = ThreadLocal.withInitial(
    Stack::new
  );

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

  public void startRender(String name) {
    RenderTimings renderTimings = (RenderTimings) getContext().get("request");
    if (renderTimings != null) {
      renderTimings.start(this, name);
    }
  }

  public void endRender(String name) {
    RenderTimings renderTimings = (RenderTimings) getContext().get("request");
    if (renderTimings != null) {
      renderTimings.end(this, name);
    }
  }

  public void endRender(String name, Map<String, Object> data) {
    RenderTimings renderTimings = (RenderTimings) getContext().get("request");
    if (renderTimings != null) {
      renderTimings.end(this, name, data);
    }
  }

  private String getWrappedErrorMessage(
    String childTemplateName,
    TemplateError templateError
  ) {
    String severity = templateError.getSeverity() == ErrorType.WARNING
      ? "Warning"
      : "Error";
    String lineNumber = templateError.getLineno() > 0
      ? String.format(" on line %d", templateError.getLineno())
      : "";

    if (Strings.isNullOrEmpty(templateError.getMessage())) {
      return String.format(
        "Unknown %s in file `%s`%s",
        severity.toLowerCase(),
        childTemplateName,
        lineNumber
      );
    } else {
      return String.format(
        "%s in `%s`%s: %s",
        severity,
        childTemplateName,
        lineNumber,
        templateError.getMessage()
      );
    }
  }

  @Override
  public String toPyishString() {
    return ExtendedParser.INTERPRETER;
  }
}
