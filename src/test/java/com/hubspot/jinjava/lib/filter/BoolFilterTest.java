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
package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BoolFilterTest {
    BoolFilter filter;
    JinjavaInterpreter interpreter;

    @Before
    public void setup() {
        interpreter = new Jinjava().newInterpreter();
        filter = new BoolFilter();
    }

    @Test
    public void itReturnsStringAsBool() {
        assertThat(filter.filter("true", interpreter)).isEqualTo(true);
        assertThat(filter.filter("false", interpreter)).isEqualTo(false);
    }

    @Test
    public void itReturnsIntAsBool() {
        assertThat(filter.filter(Integer.valueOf(0), interpreter)).isEqualTo(false);
        assertThat(filter.filter(Integer.valueOf(1), interpreter)).isEqualTo(true);
        assertThat(filter.filter(Integer.valueOf(2), interpreter)).isEqualTo(false);
    }

    @Test
    public void itReturnsFalseWhenVarIsNull() {
        assertThat(filter.filter(null, interpreter)).isEqualTo(false);
    }

    @Test
    public void itReturnsFalseWhenVarIsString() {
        assertThat(filter.filter("foobar", interpreter)).isEqualTo(false);
    }

    @Test
    public void itReturnsSameWhenVarIsBool() {
        assertThat(filter.filter(false, interpreter)).isEqualTo(false);
        assertThat(filter.filter(true, interpreter)).isEqualTo(true);
    }

}
