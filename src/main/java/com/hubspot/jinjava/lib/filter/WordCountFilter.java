package com.hubspot.jinjava.lib.filter;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;


@JinjavaDoc("Counts the words in the given string")
public class WordCountFilter implements Filter {

  @Override
  public String getName() {
    return "wordcount";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    Matcher matcher = WORD_RE.matcher(Objects.toString(var, ""));

    int count = 0;
    
    while(matcher.find()) {
      count++;
    }
    
    return count;
  }

  private static final Pattern WORD_RE = Pattern.compile("\\w+", Pattern.UNICODE_CHARACTER_CLASS | Pattern.MULTILINE);

}
