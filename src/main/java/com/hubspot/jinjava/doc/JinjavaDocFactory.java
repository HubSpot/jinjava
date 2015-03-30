package com.hubspot.jinjava.doc;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.doc.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.lib.exptest.ExpTest;
import com.hubspot.jinjava.lib.filter.Filter;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import com.hubspot.jinjava.lib.tag.Tag;

public class JinjavaDocFactory {
  private static final Logger LOG = LoggerFactory.getLogger(JinjavaDocFactory.class);
  
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

  private void addExpTests(JinjavaDoc doc) {
    for(ExpTest t : jinjava.getGlobalContext().getAllExpTests()) {
      com.hubspot.jinjava.doc.annotations.JinjavaDoc docAnnotation = t.getClass().getAnnotation(com.hubspot.jinjava.doc.annotations.JinjavaDoc.class);
      
      if(docAnnotation == null) {
        LOG.warn("Expression Test {} doesn't have a @{} annotation", t.getName(), com.hubspot.jinjava.doc.annotations.JinjavaDoc.class);
        doc.addExpTest(new JinjavaDocExpTest(t.getName(), "", ""));
      }
      else {
        doc.addExpTest(new JinjavaDocExpTest(t.getName(), docAnnotation.value(), docAnnotation.aliasOf(), extractParams(docAnnotation.params())));
      }
    }
  }

  private void addFilterDocs(JinjavaDoc doc) {
    for(Filter f : jinjava.getGlobalContext().getAllFilters()) {
      com.hubspot.jinjava.doc.annotations.JinjavaDoc docAnnotation = f.getClass().getAnnotation(com.hubspot.jinjava.doc.annotations.JinjavaDoc.class);
      
      if(docAnnotation == null) {
        LOG.warn("Filter {} doesn't have a @{} annotation", f.getClass(), com.hubspot.jinjava.doc.annotations.JinjavaDoc.class);
        doc.addFilter(new JinjavaDocFilter(f.getName(), "", ""));
      }
      else {
        doc.addFilter(new JinjavaDocFilter(f.getName(), docAnnotation.value(), docAnnotation.aliasOf(), extractParams(docAnnotation.params())));
      }
    }
  }

  private void addFnDocs(JinjavaDoc doc) {
    for(ELFunctionDefinition fn : jinjava.getGlobalContext().getAllFunctions()) {
      if(StringUtils.isBlank(fn.getNamespace())) {
        com.hubspot.jinjava.doc.annotations.JinjavaDoc docAnnotation = fn.getMethod().getAnnotation(com.hubspot.jinjava.doc.annotations.JinjavaDoc.class);
        
        if(docAnnotation == null) {
          LOG.warn("Function {} doesn't have a @{} annotation", fn.getName(), com.hubspot.jinjava.doc.annotations.JinjavaDoc.class);
          doc.addFunction(new JinjavaDocFunction(fn.getLocalName(), "", ""));
        }
        else {
          doc.addFunction(new JinjavaDocFunction(fn.getLocalName(), docAnnotation.value(), docAnnotation.aliasOf(), extractParams(docAnnotation.params())));
        }
      }
    }
  }
  
  private void addTagDocs(JinjavaDoc doc) {
    for(Tag t : jinjava.getGlobalContext().getAllTags()) {
      com.hubspot.jinjava.doc.annotations.JinjavaDoc docAnnotation = t.getClass().getAnnotation(com.hubspot.jinjava.doc.annotations.JinjavaDoc.class);
      
      if(docAnnotation == null) {
        LOG.warn("Tag {} doesn't have a @{} annotation", t.getName(), com.hubspot.jinjava.doc.annotations.JinjavaDoc.class);
        doc.addTag(new JinjavaDocTag(t.getName(), StringUtils.isNotBlank(t.getEndTagName()), "", ""));
      }
      else {
        doc.addTag(new JinjavaDocTag(t.getName(), StringUtils.isNotBlank(t.getEndTagName()), docAnnotation.value(), docAnnotation.aliasOf(), extractParams(docAnnotation.params())));
      }
    }
  }
  
  private JinjavaDocParam[] extractParams(JinjavaParam[] params) {
    JinjavaDocParam[] result = new JinjavaDocParam[params.length];

    for(int i = 0; i < params.length; i++) {
      JinjavaParam p = params[i];
      result[i] = new JinjavaDocParam(p.value(), p.type(), p.desc(), p.defaultValue());
    }

    return result;
  }
  
}
