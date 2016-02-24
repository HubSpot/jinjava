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
