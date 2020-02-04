package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.SafeString;

public class RegexReplaceFilterTest {

    JinjavaInterpreter interpreter;
    RegexReplaceFilter filter;

    @Before
    public void setup() {
        interpreter = new Jinjava().newInterpreter();
        filter = new RegexReplaceFilter();
    }

    @Test(expected = InterpretException.class)
    public void expects2Args() {
        filter.filter("foo", interpreter);
    }

    public void noopOnNullExpr() {
        assertThat(filter.filter(null, interpreter, "foo", "bar")).isNull();
    }

    @Test
    public void itMatchesRegexAndReplacesString() {
        assertThat(filter.filter("It costs $300", interpreter, "[^a-zA-Z]", "")).isEqualTo("Itcosts");
    }

    @Test(expected = InvalidArgumentException.class)
    public void isThrowsExceptionOnInvalidRegex() {
        filter.filter("It costs $300", interpreter, "[", "");
    }

    @Test
    public void itMatchesRegexAndReplacesStringForSafeString() {
        assertThat(filter.filter(new SafeString("It costs $300"), interpreter, "[^a-zA-Z]", "").toString()).isEqualTo("Itcosts");
    }
}
