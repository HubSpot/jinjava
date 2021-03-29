package com.hubspot.jinjava.doc;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.doc.annotations.JinjavaHasCodeBody;
import com.hubspot.jinjava.doc.annotations.JinjavaMetaValue;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.doc.annotations.JinjavaTextMateSnippet;
import com.hubspot.jinjava.lib.exptest.ExpTest;
import com.hubspot.jinjava.lib.filter.Filter;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import com.hubspot.jinjava.lib.fn.InjectedContextFunctionProxy;
import com.hubspot.jinjava.lib.tag.EndTag;
import com.hubspot.jinjava.lib.tag.Tag;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JinjavaDocFactory {
  private static final Logger LOG = LoggerFactory.getLogger(JinjavaDocFactory.class);

  private static final Class JINJAVA_DOC_CLASS =
    com.hubspot.jinjava.doc.annotations.JinjavaDoc.class;

  private static final String GUICE_CLASS_INDICATOR = "$$EnhancerByGuice$$";

  private final Jinjava jinjava;

  public JinjavaDocFactory(Jinjava jinjava) {
    this.jinjava = jinjava;
  }

  public JinjavaDoc get() {
    JinjavaDoc doc = new JinjavaDoc();

    addExpTests(doc);
    addFilterDocs(doc);
    addFnDocs(doc);
    addTagDocs(doc);

    return doc;
  }

  public String getCodeEditorTagSnippets() {
    StringBuffer snippets = new StringBuffer();
    for (Tag tag : jinjava.getGlobalContextCopy().getAllTags()) {
      if (tag instanceof EndTag) {
        continue;
      }
      snippets.append(getTagSnippet(tag));
      snippets.append("\n\n");
    }
    return snippets.toString();
  }

  private void addExpTests(JinjavaDoc doc) {
    for (ExpTest t : jinjava.getGlobalContextCopy().getAllExpTests()) {
      com.hubspot.jinjava.doc.annotations.JinjavaDoc docAnnotation = getJinjavaDocAnnotation(
        t.getClass()
      );

      if (docAnnotation == null) {
        LOG.warn(
          "Expression Test {} doesn't have a @{} annotation",
          t.getName(),
          JINJAVA_DOC_CLASS.getName()
        );
        doc.addExpTest(
          new JinjavaDocExpTest(
            t.getName(),
            "",
            "",
            false,
            new JinjavaDocParam[] {},
            new JinjavaDocParam[] {},
            new JinjavaDocSnippet[] {},
            Collections.emptyMap()
          )
        );
      } else if (!docAnnotation.hidden()) {
        doc.addExpTest(
          new JinjavaDocExpTest(
            t.getName(),
            docAnnotation.value(),
            docAnnotation.aliasOf(),
            docAnnotation.deprecated(),
            extractParams(docAnnotation.input()),
            extractParams(docAnnotation.params()),
            extractSnippets(docAnnotation.snippets()),
            extractMeta(docAnnotation.meta())
          )
        );
      }
    }
  }

  private void addFilterDocs(JinjavaDoc doc) {
    for (Filter f : jinjava.getGlobalContextCopy().getAllFilters()) {
      com.hubspot.jinjava.doc.annotations.JinjavaDoc docAnnotation = getJinjavaDocAnnotation(
        f.getClass()
      );

      if (docAnnotation == null) {
        LOG.warn(
          "Filter {} doesn't have a @{} annotation",
          f.getClass(),
          JINJAVA_DOC_CLASS.getName()
        );
        doc.addFilter(
          new JinjavaDocFilter(
            f.getName(),
            "",
            "",
            false,
            new JinjavaDocParam[] {},
            new JinjavaDocParam[] {},
            new JinjavaDocSnippet[] {},
            Collections.emptyMap()
          )
        );
      } else if (!docAnnotation.hidden()) {
        doc.addFilter(
          new JinjavaDocFilter(
            f.getName(),
            docAnnotation.value(),
            docAnnotation.aliasOf(),
            docAnnotation.deprecated(),
            extractParams(docAnnotation.input()),
            extractParams(docAnnotation.params()),
            extractSnippets(docAnnotation.snippets()),
            extractMeta(docAnnotation.meta())
          )
        );
      }
    }
  }

  private void addFnDocs(JinjavaDoc doc) {
    for (ELFunctionDefinition fn : jinjava.getGlobalContextCopy().getAllFunctions()) {
      if (StringUtils.isBlank(fn.getNamespace())) {
        Method realMethod = fn.getMethod();
        if (
          realMethod
            .getDeclaringClass()
            .getName()
            .contains(InjectedContextFunctionProxy.class.getSimpleName())
        ) {
          try {
            realMethod =
              (Method) realMethod.getDeclaringClass().getField("delegate").get(null);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }

        com.hubspot.jinjava.doc.annotations.JinjavaDoc docAnnotation = realMethod.getAnnotation(
          com.hubspot.jinjava.doc.annotations.JinjavaDoc.class
        );

        if (docAnnotation == null) {
          LOG.warn(
            "Function {} doesn't have a @{} annotation",
            fn.getName(),
            JINJAVA_DOC_CLASS.getName()
          );
          doc.addFunction(
            new JinjavaDocFunction(
              fn.getLocalName(),
              "",
              "",
              false,
              new JinjavaDocParam[] {},
              new JinjavaDocParam[] {},
              new JinjavaDocSnippet[] {},
              Collections.emptyMap()
            )
          );
        } else if (!docAnnotation.hidden()) {
          doc.addFunction(
            new JinjavaDocFunction(
              fn.getLocalName(),
              docAnnotation.value(),
              docAnnotation.aliasOf(),
              docAnnotation.deprecated(),
              extractParams(docAnnotation.input()),
              extractParams(docAnnotation.params()),
              extractSnippets(docAnnotation.snippets()),
              extractMeta(docAnnotation.meta())
            )
          );
        }
      }
    }
  }

  private void addTagDocs(JinjavaDoc doc) {
    for (Tag t : jinjava.getGlobalContextCopy().getAllTags()) {
      if (t instanceof EndTag) {
        continue;
      }
      com.hubspot.jinjava.doc.annotations.JinjavaDoc docAnnotation = getJinjavaDocAnnotation(
        t.getClass()
      );

      if (docAnnotation == null) {
        LOG.warn(
          "Tag {} doesn't have a @{} annotation",
          t.getName(),
          JINJAVA_DOC_CLASS.getName()
        );
        doc.addTag(
          new JinjavaDocTag(
            t.getName(),
            StringUtils.isBlank(t.getEndTagName()),
            "",
            "",
            false,
            new JinjavaDocParam[] {},
            new JinjavaDocParam[] {},
            new JinjavaDocSnippet[] {},
            Collections.emptyMap()
          )
        );
      } else if (!docAnnotation.hidden()) {
        doc.addTag(
          new JinjavaDocTag(
            t.getName(),
            StringUtils.isBlank(t.getEndTagName()),
            docAnnotation.value(),
            docAnnotation.aliasOf(),
            docAnnotation.deprecated(),
            extractParams(docAnnotation.input()),
            extractParams(docAnnotation.params()),
            extractSnippets(docAnnotation.snippets()),
            extractMeta(docAnnotation.meta())
          )
        );
      }
    }
  }

  private JinjavaDocParam[] extractParams(JinjavaParam[] params) {
    JinjavaDocParam[] result = new JinjavaDocParam[params.length];

    for (int i = 0; i < params.length; i++) {
      JinjavaParam p = params[i];
      result[i] =
        new JinjavaDocParam(
          p.value(),
          p.type(),
          p.desc(),
          p.defaultValue(),
          p.required()
        );
    }

    return result;
  }

  private JinjavaDocSnippet[] extractSnippets(JinjavaSnippet[] snippets) {
    JinjavaDocSnippet[] result = new JinjavaDocSnippet[snippets.length];

    for (int i = 0; i < snippets.length; i++) {
      JinjavaSnippet s = snippets[i];
      result[i] = new JinjavaDocSnippet(s.desc(), s.code(), s.output());
    }

    return result;
  }

  private Map<String, String> extractMeta(JinjavaMetaValue[] metaValues) {
    Map<String, String> meta = new LinkedHashMap<>();

    for (JinjavaMetaValue metaValue : metaValues) {
      meta.put(metaValue.name(), metaValue.value());
    }

    return meta;
  }

  private com.hubspot.jinjava.doc.annotations.JinjavaDoc getJinjavaDocAnnotation(
    Class<?> clazz
  ) {
    if (
      clazz.getName().contains(GUICE_CLASS_INDICATOR) && clazz.getSuperclass() != null
    ) {
      clazz = clazz.getSuperclass();
    }

    return clazz.getAnnotation(com.hubspot.jinjava.doc.annotations.JinjavaDoc.class);
  }

  private String getTagSnippet(Tag tag) {
    JinjavaTextMateSnippet annotation = tag
      .getClass()
      .getAnnotation(JinjavaTextMateSnippet.class);
    if (annotation != null) {
      return annotation.code();
    }
    com.hubspot.jinjava.doc.annotations.JinjavaDoc docAnnotation = getJinjavaDocAnnotation(
      tag.getClass()
    );
    StringBuilder snippet = new StringBuilder("{% ");
    snippet.append(tag.getName());
    int i = 1;
    for (JinjavaParam param : docAnnotation.input()) {
      String inputValue = "${" + i + ":" + param.value() + "}";
      if (param.value().equalsIgnoreCase("path")) {
        inputValue = "'" + inputValue + "'";
      } else if (param.value().equalsIgnoreCase("argument_names")) {
        inputValue = "(" + inputValue + ")";
      }
      snippet.append(" " + inputValue);
      i++;
    }

    for (JinjavaParam param : docAnnotation.params()) {
      String paramValue = "${" + i + ":" + param.value() + "}";
      if (param.value().equalsIgnoreCase("path")) {
        paramValue = "'" + paramValue + "'";
      } else if (param.value().equalsIgnoreCase("argument_names")) {
        paramValue = "(" + paramValue + ")";
      }
      snippet.append(" " + paramValue);
      i++;
    }

    snippet.append(" %}");

    if (tag.getClass().getAnnotation(JinjavaHasCodeBody.class) != null) {
      snippet.append("\n$0");
    }
    if (tag.getEndTagName() != null) {
      snippet.append("\n{% " + tag.getEndTagName() + " %}");
    }

    return snippet.toString();
  }
}
