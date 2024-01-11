package com.hubspot.jinjava.lib.tag.eager.importing;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.parse.TagToken;
import java.util.List;

public class ImportingData {

  private final JinjavaInterpreter originalInterpreter;
  private final TagToken tagToken;
  private final List<String> helpers;
  private final String initialPathSetter;

  public ImportingData(
    JinjavaInterpreter originalInterpreter,
    TagToken tagToken,
    List<String> helpers,
    String initialPathSetter
  ) {
    this.originalInterpreter = originalInterpreter;
    this.tagToken = tagToken;
    this.helpers = helpers;
    this.initialPathSetter = initialPathSetter;
  }

  public JinjavaInterpreter getOriginalInterpreter() {
    return originalInterpreter;
  }

  public TagToken getTagToken() {
    return tagToken;
  }

  public List<String> getHelpers() {
    return helpers;
  }

  public String getInitialPathSetter() {
    return initialPathSetter;
  }
}
