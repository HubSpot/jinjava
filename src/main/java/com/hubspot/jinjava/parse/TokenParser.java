/**********************************************************************
Copyright (c) 2014 HubSpot Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 **********************************************************************/
package com.hubspot.jinjava.parse;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;

public class TokenParser implements Iterator<Token> {

  private JinjavaInterpreter interpreter;
  private Tokenizer tm = new Tokenizer();
  private Token token;
  private boolean proceeding = true;

  public TokenParser(JinjavaInterpreter interpreter, String text) {
    this.interpreter = interpreter;
    tm.init(text);
  }

  @Override
  public boolean hasNext() {
    if (proceeding) {
      try {
        token = tm.getNextToken();
        if (token != null) {
          return true;
        } else {
          proceeding = false;
          return false;
        }
      } catch (TemplateSyntaxException e) {
        interpreter.addError(TemplateError.fromException(e));
        token = null;
      }
    }
    return false;
  }

  @Override
  public Token next() {
    if (proceeding) {
      if (token == null) {
        try {
          Token tk = tm.getNextToken();
          if (tk == null) {
            proceeding = false;
            throw new NoSuchElementException();
          }
          return tk;
        } catch (TemplateSyntaxException e) {
          interpreter.addError(TemplateError.fromException(e));
          throw new NoSuchElementException();
        }
      } else {
        Token last = token;
        token = null;
        return last;
      }
    }
    throw new NoSuchElementException();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
  
  public JinjavaInterpreter getInterpreter() {
    return interpreter;
  }
  
}
