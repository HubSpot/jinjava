package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

@JinjavaDoc(
        value = "Return a copy of the value with all occurrences of a matched regular expression replaced with a new one. " +
                "The first argument is the regular expression to be matched, the second is the replacement string",
        params = {
                @JinjavaParam(value = "s", desc = "Base string to find and replace within"),
                @JinjavaParam(value = "regex", desc = "The regular expression that you want to match and replace"),
                @JinjavaParam(value = "new", desc = "The new string that you replace the matched substring")
        },
        snippets = {
                @JinjavaSnippet(
                        code = "{{ \"It costs $300\"|regex_replace(\"[^a-zA-Z]\", \"\") }}",
                        output = "Itcosts")
        })
public class RegexReplaceFilter implements Filter {

    @Override
    public String getName() {
        return "regex_replace";
    }

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter,
                         String... args) {

        if (var == null) {
            return null;
        }
        if (args.length < 2) {
            throw new InterpretException("filter " + getName() + " requires two string args");
        }

        if (var instanceof String) {
            String toReplace = args[0];
            String replaceWith = args[1];
            String s = (String) var;

            Pattern p = Pattern.compile(toReplace);
            Matcher matcher = p.matcher(s);
            return matcher.replaceAll(replaceWith);
        } else {
            throw new InterpretException(getName() + " filter requires a string parameter");
        }
    }
}
