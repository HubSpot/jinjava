/**
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
 */
package com.hubspot.jinjava.tree.output;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.OutputTooBigException;
import com.hubspot.jinjava.interpret.RenderResult;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;

public class OutputListTest {

  @Test
  public void itDoesNotQueryNodeSizeWhenLimitIsDisabled() {
    for (long limit : new long[] { 0, -1 }) {
      OutputList output = new OutputList(limit);
      OutputNode node = new RenderedOutputNode("<p>Привет 😀</p>") {
        @Override
        public long getSize() {
          throw new AssertionError("Size queried with output limit disabled");
        }
      };

      output.addNode(node);

      assertThat(output.getNodes()).containsExactly(node);
      assertThat(output.getValue()).isEqualTo("<p>Привет 😀</p>");
    }
  }

  @Test
  public void itRegistersBlocksWithoutQueryingSizeWhenLimitIsDisabled() {
    for (long limit : new long[] { 0, -1 }) {
      OutputList output = new OutputList(limit);
      BlockPlaceholderOutputNode block = new BlockPlaceholderOutputNode("content") {
        @Override
        public long getSize() {
          throw new AssertionError("Block size queried with output limit disabled");
        }
      };

      output.addNode(block);

      assertThat(output.getNodes()).containsExactly(block);
      assertThat(output.getBlocks()).containsExactly(block);
      block.resolve("<p>Привет 😀</p>");
      assertThat(output.getValue()).isEqualTo("<p>Привет 😀</p>");
    }
  }

  @Test
  public void itAcceptsOutputExactlyAtUtf8ByteLimit() {
    for (String text : Arrays.asList("hello", "Привет", "😀")) {
      OutputList output = new OutputList(text.getBytes(StandardCharsets.UTF_8).length);

      output.addNode(new RenderedOutputNode(text));

      assertThat(output.getValue()).isEqualTo(text);
    }
  }

  @Test
  public void itRejectsOutputOneByteOverUtf8ByteLimit() {
    for (String text : Arrays.asList("hello", "Привет", "😀")) {
      long size = text.getBytes(StandardCharsets.UTF_8).length;
      OutputList output = new OutputList(size - 1);

      assertThatThrownBy(() -> output.addNode(new RenderedOutputNode(text)))
        .isInstanceOf(OutputTooBigException.class)
        .hasMessage("%d byte output rendered, over limit of %d bytes", size, size - 1);
      assertThat(output.getNodes()).isEmpty();
    }
  }

  @Test
  public void itAccountsForUtf8BytesAcrossNodes() {
    for (String text : Arrays.asList("hello", "Привет", "😀")) {
      long size = text.getBytes(StandardCharsets.UTF_8).length + 1;
      OutputList output = new OutputList(size);
      output.addNode(new RenderedOutputNode("a"));
      output.addNode(new RenderedOutputNode(text));

      assertThat(output.getValue()).isEqualTo("a" + text);
      assertThatThrownBy(() -> output.addNode(new RenderedOutputNode("b")))
        .isInstanceOf(OutputTooBigException.class)
        .hasMessage("%d byte output rendered, over limit of %d bytes", size + 1, size);
      assertThat(output.getValue()).isEqualTo("a" + text);
    }
  }

  @Test
  public void itPreservesResolvedBlockSizeChecksWithPositiveLimit() {
    BlockPlaceholderOutputNode block = new BlockPlaceholderOutputNode("content");
    block.resolve("ab");
    OutputList output = new OutputList(3);

    assertThatThrownBy(() -> output.addNode(block))
      .isInstanceOf(OutputTooBigException.class)
      .hasMessage("4 byte output rendered, over limit of 3 bytes");
    assertThat(output.getNodes()).containsExactly(block);
    assertThat(output.getBlocks()).isEmpty();
  }

  @Test
  public void itRendersHtmlWithVariablesLoopsAndBlocksWhenLimitIsDisabled() {
    String template =
      "<html><body><h1>{{ title }}</h1>" +
      "{% block content %}<ul>{% for item in items %}<li>{{ item }}</li>" +
      "{% endfor %}</ul>{% endblock %}</body></html>";
    Map<String, Object> context = Map.of(
      "title",
      "Привет 😀",
      "items",
      Arrays.asList("one", "два", "😀")
    );

    for (long limit : new long[] { 0, -1 }) {
      Jinjava jinjava = new Jinjava(
        BaseJinjavaTest.newConfigBuilder().withMaxOutputSize(limit).build()
      );
      RenderResult result = jinjava.renderForResult(template, context);

      assertThat(result.getErrors()).isEmpty();
      assertThat(result.getOutput())
        .isEqualTo(
          "<html><body><h1>Привет 😀</h1><ul><li>one</li><li>два</li>" +
          "<li>😀</li></ul></body></html>"
        );
    }
  }

  @Test
  public void itResolvesInheritedBlocksWhenLimitIsDisabled() {
    for (long limit : new long[] { 0, -1 }) {
      Jinjava jinjava = new Jinjava(
        BaseJinjavaTest.newConfigBuilder().withMaxOutputSize(limit).build()
      );
      jinjava.setResourceLocator((name, encoding, interpreter) ->
        "<main>{% block content %}parent{% endblock %}</main>"
      );
      RenderResult result = jinjava.renderForResult(
        "{% extends 'base.html' %}{% block content %}<p>{{ message }}</p>{% endblock %}",
        Collections.singletonMap("message", "Привет 😀")
      );

      assertThat(result.getErrors()).isEmpty();
      assertThat(result.getOutput()).isEqualTo("<main><p>Привет 😀</p></main>");
    }
  }
}
