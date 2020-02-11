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

import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.tree.output.OutputNode;
import com.hubspot.jinjava.tree.output.RenderedOutputNode;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.tree.parse.TokenScannerSymbols;

public class TagNode extends Node {

  private static final long serialVersionUID = -6971280448795354252L;

  private final Tag tag;
  private final TagToken master;
  private final String endName;

  public TagNode(Tag tag, TagToken token) {
    super(token, token.getLineNumber(), token.getStartPosition());

    this.master = token;
    this.tag = tag;
    this.endName = tag.getEndTagName();
  }

  private TagNode(TagNode n) {
    super(n.master, n.getLineNumber(), n.getStartPosition());

    tag = n.tag;
    master = n.master;
    endName = n.endName;
  }

  @Override
  public OutputNode render(JinjavaInterpreter interpreter) {
    if (interpreter.getContext().isValidationMode() && !tag.isRenderedInValidationMode()) {
      return new RenderedOutputNode("");
    }

    try {
      return tag.interpretOutput(this, interpreter);
    } catch (DeferredValueException e) {
      interpreter.getContext().addDeferredNode(this);
      return new RenderedOutputNode(reconstructImage());
    } catch (InterpretException | InvalidInputException | InvalidArgumentException e) {
      throw e;
    } catch (Exception e) {
      throw new InterpretException("Error rendering tag", e, master.getLineNumber(), master.getStartPosition());
    }
  }

  @Override
  public String toString() {
    return master.toString();
  }

  @Override
  public String getName() {
    return master.getTagName();
  }

  public String getEndName() {
    return endName;
  }

  public String getHelpers() {
    return master.getHelpers();
  }

  public Tag getTag() {
    return tag;
  }

  public String reconstructImage() {
    StringBuilder builder = new StringBuilder().append(master.getImage());
    for (Node n : getChildren()) {
      builder.append(n.reconstructImage());
    }

    if (getEndName() != null) {
      builder.append(reconstructEnd());
    }

    return builder.toString();
  }

  public String reconstructEnd() {
    return String.format(
        "%s%s %s %s%s",
        TokenScannerSymbols.TOKEN_EXPR_START_CHAR,
        TokenScannerSymbols.TOKEN_TAG_CHAR,
        getEndName(),
        TokenScannerSymbols.TOKEN_TAG_CHAR,
        TokenScannerSymbols.TOKEN_EXPR_END_CHAR
    );
  }

}
