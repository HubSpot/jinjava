package com.hubspot.jinjava.interpret;

/**
 * Container exception thrown when fatal errors are encountered while rendering a template.
 *
 * @author jstehler
 */
public class FatalTemplateErrorsException extends InterpretException {
  private static final long serialVersionUID = 1L;

  private final String template;
  private final Iterable<TemplateError> errors;

  public FatalTemplateErrorsException(String template, Iterable<TemplateError> errors) {
    super(generateMessage(errors));
    this.template = template;
    this.errors = errors;
  }

  private static String generateMessage(Iterable<TemplateError> errors) {
    StringBuilder msg = new StringBuilder();

    for (TemplateError error : errors) {
      msg.append(error.toString()).append('\n');
    }

    return msg.toString();
  }

  public String getTemplate() {
    return template;
  }

  public Iterable<TemplateError> getErrors() {
    return errors;
  }

}
