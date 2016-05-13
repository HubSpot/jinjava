/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hubspot.jinjava.el.ext;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.Jinjava;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by anev on 11/05/16.
 */
public class RangeStringTest {

    private Jinjava jinjava;
    private Map<String, Object> context;

    @Before
    public void setup() {
        jinjava = new Jinjava();
        context = ImmutableMap.of("theString", "theSimpleString");
    }

    @Test
    public void testStringRangeSimple() {
        assertThat(jinjava.render("{{ theString[0:4] }}", context)).isEqualTo("theS");
    }

    @Test
    public void testStringRangeOutOfRange() {
        assertThat(jinjava.render("{{ theString[0:400] }}", context)).isEqualTo("theSimpleString");
    }

    @Test
    public void testStringRangeInvalidRange() {
        assertThat(jinjava.render("{{ theString[4:2] }}", context)).isEmpty();
    }

    @Test
    public void testStringRangeNegative() {
        assertThat(jinjava.render("{{ theString[-7:-4] }}", context)).isEqualTo("eSt");
    }

    @Test
    public void testStringRangeRightOnly() {
        assertThat(jinjava.render("{{ theString[3:] }}", context)).isEqualTo("SimpleString");
    }

    @Test
    public void testStringRangeLeftOnly() {
        assertThat(jinjava.render("{{ theString[:3] }}", context)).isEqualTo("the");
    }

    @Test
    public void testStringRangeNoRange() {
        assertThat(jinjava.render("{{ theString[:] }}", context)).isEqualTo("theSimpleString");
    }

    @Test
    public void testStringRangeMultiLine() {
        Map<String, Object> localContext = ImmutableMap.of("theString", "multi\nline\nstring");
        assertThat(jinjava.render("{{ theString[3:8] }}", localContext)).isEqualTo("ti\nli");
    }

    @Test
    public void testStringRangeCyrillic() {
        Map<String, Object> localContext = ImmutableMap.of("theString", "Строка с non ascii символами");
        assertThat(jinjava.render("{{ theString[1:4] }}", localContext)).isEqualTo("тро");
    }
}
