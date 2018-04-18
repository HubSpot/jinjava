package com.hubspot.jinjava.interpret;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class RenderResult {

  private final String output;
  private final Context context;
  private final List<TemplateError> errors;

  public RenderResult(String output, Context context, List<TemplateError> errors) {
    this.output = output;
    this.context = context;
    this.errors = errors;
  }

  public RenderResult(TemplateError fromException, Context context, List<TemplateError> errors) {
    this.output = "";
    this.context = context;
    this.errors = ImmutableList.<TemplateError>builder().add(fromException).addAll(errors).build();
  }

  public RenderResult(String result) {
    this.output = result;
    this.context = null;
    this.errors = Collections.emptyList();
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  public List<TemplateError> getErrors() {
    return errors;
  }

  public Context getContext() {
    return context;
  }

  public String getOutput() {
    return output;
  }

  public RenderResult withOutput(String newOutput) {
    return new RenderResult(newOutput, getContext(), getErrors());
  }

}
