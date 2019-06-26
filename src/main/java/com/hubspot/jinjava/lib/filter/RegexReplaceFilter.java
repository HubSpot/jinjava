package com.hubspot.jinjava.lib.filter;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import com.google.re2j.PatternSyntaxException;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;

@JinjavaDoc(
        value = "Return a copy of the value with all occurrences of a matched regular expression (Java RE2 syntax) " +
                "replaced with a new one. The first argument is the regular expression to be matched, the second " +
                "is the replacement string",
        input = @JinjavaParam(value = "s", desc = "Base string to find and replace within", required = true),
        params = {
                @JinjavaParam(value = "regex", desc = "The regular expression that you want to match and replace", required = true),
                @JinjavaParam(value = "new", desc = "The new string that you replace the matched substring", required = true)
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
                         Object... args) {

        if (args.length < 2) {
            throw new TemplateSyntaxException(interpreter, getName(), "requires 2 arguments (regex string, replacement string)");
        }

        if (var == null) {
            return null;
        }

        if (var instanceof String) {
            String s = (String) var;
            String toReplace = args[0].toString();
            String replaceWith = args[1].toString();

            try {
                Pattern p = Pattern.compile(toReplace);
                Matcher matcher = p.matcher(s);

                return matcher.replaceAll(replaceWith);
            } catch (PatternSyntaxException e) {
                throw new InvalidArgumentException(interpreter, this, InvalidReason.REGEX, 0, toReplace);
            }
        } else {
            throw new InvalidInputException(interpreter, this, InvalidReason.STRING);
        }
    }

}
