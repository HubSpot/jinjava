package com.hubspot.jinjava.interpret;

import java.util.Collection;

/**
 * Container exception thrown when fatal errors are encountered while rendering a template.
 *
 * @author jstehler
 */
public class FatalTemplateErrorsException extends InterpretException {
  private static final long serialVersionUID = 1L;

  private final String template;
  private final Iterable<TemplateError> errors;

  public FatalTemplateErrorsException(String template, Collection<TemplateError> errors) {
    super(generateMessage(errors));
    this.template = template;
    this.errors = errors;
  }

  private static String generateMessage(Collection<TemplateError> errors) {
    if (errors.isEmpty()) {
      throw new IllegalArgumentException("FatalTemplateErrorsException should have at least one error");
    }

    return errors.iterator().next().getMessage();
  }

  public String getTemplate() {
    return template;
  }

  public Iterable<TemplateError> getErrors() {
    return errors;
  }

}
