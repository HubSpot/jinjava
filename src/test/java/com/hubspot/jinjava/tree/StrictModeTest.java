package com.hubspot.jinjava.tree;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.FatalTemplateErrorsException;

public class StrictModeTest {
    private static Jinjava jinjava;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        JinjavaConfig.Builder builder = JinjavaConfig.newBuilder();
        builder.withStrictUndefined(true);
        JinjavaConfig config = builder.build();
        jinjava = new Jinjava(config);

    }

    @Test
    public void checkExceptiontest() {
        try {
            Map<String, String> context = new HashMap<String, String>();
            context.put("token1", "test");
            String template = "hello {{ token1 }} and {{ token2 }}";
            String str = jinjava.render(template, context);
            fail();
        } catch (FatalTemplateErrorsException e) {
            assertTrue(e instanceof FatalTemplateErrorsException);
        }
    }

    @Test
    public void noException() {
        Map<String, String> context = new HashMap<String, String>();
        context.put("token1", "test");
        context.put("token2", "test1");
        String template = "hello {{ token1 }} and {{ token2 }}";
        String renderedTemplate = jinjava.render(template, context);
        assertEquals("hello test and test1", renderedTemplate);

    }

}
