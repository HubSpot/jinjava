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
package com.hubspot.jinjava;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.el.ExpressionFactory;

import com.hubspot.jinjava.doc.JinjavaDoc;
import com.hubspot.jinjava.doc.JinjavaDocFactory;
import com.hubspot.jinjava.el.ExtendedSyntaxBuilder;
import com.hubspot.jinjava.el.TruthyTypeConverter;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.FatalTemplateErrorsException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.loader.ClasspathResourceLocator;
import com.hubspot.jinjava.loader.ResourceLocator;

import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.misc.TypeConverter;
import de.odysseus.el.tree.TreeBuilder;

/**
 * The main client API for the Jinjava library, instances of this class can be used to render jinja templates with a given map of context values. Example use:
 *
 * <pre>
 * Jinjava jinjava = new Jinjava();
 * Map&lt;String, Object&gt; context = new HashMap&lt;&gt;();
 * context.put(&quot;name&quot;, &quot;Jared&quot;);
 * // ...
 * String template = &quot;Hello, {{ name }}&quot;;
 * String renderedTemplate = jinjava.render(template, context);
 * </pre>
 *
 * @author jstehler
 */
public class Jinjava {

  private ExpressionFactory expressionFactory;
  private ResourceLocator resourceLocator;

  private Context globalContext;
  private JinjavaConfig globalConfig;

  /**
   * Create a new Jinjava processor instance with the default global config
   */
  public Jinjava() {
    this(new JinjavaConfig());
  }

  /**
   * Create a new jinjava processor instance with the specified global config
   *
   * @param globalConfig
   *          used for all render operations performed by this processor instance
   */
  public Jinjava(JinjavaConfig globalConfig) {
    this.globalConfig = globalConfig;
    this.globalContext = new Context();

    Properties expConfig = new Properties();
    expConfig.setProperty(TreeBuilder.class.getName(), ExtendedSyntaxBuilder.class.getName());

    TypeConverter converter = new TruthyTypeConverter();
    this.expressionFactory = new ExpressionFactoryImpl(expConfig, converter);

    this.resourceLocator = new ClasspathResourceLocator();
  }

  /**
   * Set the object responsible for locating templates referenced in other templates
   *
   * @param resourceLocator
   *          the locator to use for loading all templates
   */
  public void setResourceLocator(ResourceLocator resourceLocator) {
    this.resourceLocator = resourceLocator;
  }

  /**
   * @return The EL factory used to process expressions in templates by this instance.
   */
  public ExpressionFactory getExpressionFactory() {
    return expressionFactory;
  }

  /**
   * @return The global config used as a base for all render operations performed by this instance.
   */
  public JinjavaConfig getGlobalConfig() {
    return globalConfig;
  }

  /**
   * The global render context includes such things as the base set of tags, filters, exp tests and functions, used as a base by all render operations performed by this instance
   *
   * @return the global render context
   */
  public Context getGlobalContext() {
    return globalContext;
  }

  public ResourceLocator getResourceLocator() {
    return resourceLocator;
  }

  /**
   * @return a comprehensive descriptor of all available filters, functions, and tags registered on this jinjava instance.
   */
  public JinjavaDoc getJinjavaDoc() {
    return new JinjavaDocFactory(this).get();
  }

  /**
   * Render the given template using the given context bindings.
   *
   * @param template
   *          jinja source template
   * @param bindings
   *          map of objects to put into scope for this rendering action
   * @return the rendered template
   * @throws InterpretException
   *           if any syntax errors were encountered during rendering
   */
  public String render(String template, Map<String, ?> bindings) {
    RenderResult result = renderForResult(template, bindings);

    List<TemplateError> fatalErrors = result.getErrors().stream()
        .filter(error -> error.getSeverity() == ErrorType.FATAL)
        .collect(Collectors.toList());

    if (!fatalErrors.isEmpty()) {
      throw new FatalTemplateErrorsException(template, fatalErrors);
    }

    return result.getOutput();
  }

  /**
   * Render the given template using the given context bindings. This method returns some metadata about the render process, including any errors which may have been encountered such as unknown variables or syntax errors. This method will
   * not throw any exceptions; it is up to the caller to inspect the renderResult.errors collection if necessary / desired.
   *
   * @param template
   *          jinja source template
   * @param bindings
   *          map of objects to put into scope for this rendering action
   * @return result object containing rendered output, render context, and any encountered errors
   */
  public RenderResult renderForResult(String template, Map<String, ?> bindings) {
    return renderForResult(template, bindings, globalConfig);
  }

  /**
   * Render the given template using the given context bindings. This method returns some metadata about the render process, including any errors which may have been encountered such as unknown variables or syntax errors. This method will
   * not throw any exceptions; it is up to the caller to inspect the renderResult.errors collection if necessary / desired.
   *
   * @param template
   *          jinja source template
   * @param bindings
   *          map of objects to put into scope for this rendering action
   * @param renderConfig
   *          used to override specific config values for this render operation
   * @return result object containing rendered output, render context, and any encountered errors
   */
  public RenderResult renderForResult(String template, Map<String, ?> bindings, JinjavaConfig renderConfig) {
    Context context = new Context(globalContext, bindings, renderConfig.getDisabled());

    JinjavaInterpreter parentInterpreter = JinjavaInterpreter.getCurrent();
    if (parentInterpreter != null) {
      renderConfig = parentInterpreter.getConfig();
    }

    JinjavaInterpreter interpreter = new JinjavaInterpreter(this, context, renderConfig);
    JinjavaInterpreter.pushCurrent(interpreter);

    try {
      String result = interpreter.render(template);
      return new RenderResult(result, interpreter.getContext(), interpreter.getErrorsCopy());
    } catch (InterpretException e) {
      if (e instanceof TemplateSyntaxException) {
        return new RenderResult(TemplateError.fromException((TemplateSyntaxException) e), interpreter.getContext(), interpreter.getErrorsCopy());
      }
      return new RenderResult(TemplateError.fromSyntaxError(e), interpreter.getContext(), interpreter.getErrorsCopy());
    } catch (Exception e) {
      return new RenderResult(TemplateError.fromException(e), interpreter.getContext(), interpreter.getErrorsCopy());
    } finally {
      globalContext.reset();
      JinjavaInterpreter.popCurrent();
    }
  }

  /**
   * Creates a new interpreter instance using the global context and global config
   *
   * @return a new interpreter instance
   */
  public JinjavaInterpreter newInterpreter() {
    return new JinjavaInterpreter(this, this.getGlobalContext(), this.getGlobalConfig());
  }

}
