package com.hubspot.jinjava.el.ext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.FatalTemplateErrorsException;

public class PowerOfTest {

    @Before
    public void setUp() {
        jinja = new Jinjava();
    }

    @Test
    public void testPowerOfInteger() {

        Map<String, Object> context = Maps.newHashMap();
        context.put("base",     2);
        context.put("exponent", 8);

        String template = "{% set x = base ** exponent %}{{x}}";
        String rendered = jinja.render(template, context);
        assertEquals("256", rendered);
    }

    @Test
    public void testPowerOfFractional() {

        Map<String, Object> context = Maps.newHashMap();
        context.put("base",     2);
        context.put("exponent", 8.0);

        String template = "{% set x = base ** exponent %}{{x}}";
        String rendered = jinja.render(template, context);
        assertEquals("256.0", rendered);
    }

    @Test
    public void test04PowerOfStringFails() {

        Map<String, Object> context = Maps.newHashMap();
        context.put("base",     "2");
        context.put("exponent", "8");

        String template = "{% set x = base ** exponent %}{{x}}";
        try {
            jinja.render(template, context);
        } catch (FatalTemplateErrorsException e) {
            String msg = e.getMessage();
            assertTrue(msg.contains("Unsupported operand type(s)"));
        }
    }

    private Jinjava jinja;
}
