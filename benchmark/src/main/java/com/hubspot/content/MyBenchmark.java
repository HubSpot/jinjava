/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.hubspot.content;

import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.FileUtils.readFileToString;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.yaml.snakeyaml.Yaml;

import com.google.common.base.Charsets;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.Node;

@State(Scope.Benchmark)
public class MyBenchmark {

  public List<String> templates;
  public Map<String, ?> bindings;
  
  public Jinjava jinjava;
  
  @SuppressWarnings("unchecked")
  @Setup
  public void setup() throws IOException {
    jinjava = new Jinjava();

    jinjava.getGlobalContext().registerClasses(
        Filters.OverrideDateFilter.class,
        
        Filters.JsonFilter.class,
        Filters.LinkToAddTagFilter.class,
        Filters.LinkToRemoveTagFilter.class,
        Filters.LinkToTagFilter.class,
        Filters.HighlightActiveTagFilter.class,
        Filters.MoneyFilter.class,
        Filters.MoneyWithCurrencyFilter.class,
        Filters.WeightFilter.class,
        Filters.WeightWithUnitFilter.class,
        
        Tags.CommentFormTag.class,
        Tags.PaginateTag.class
    );
    
    templates = new ArrayList<>();
    
    Map<String, ?> db = (Map<String, ?>) new Yaml().load(readFileToString(new File("liquid/performance/shopify/vision.database.yml"), Charsets.UTF_8));
    bindings = new HashMap<>(db);
    
    File baseDir = new File("liquid/performance/tests");
    for(File tmpl : listFiles(baseDir, new String[]{"liquid"}, true)){
      
      String template = readFileToString(tmpl, Charsets.UTF_8);
      // convert filter syntax from ':' to '()'
      template = template.replaceAll("\\| ([\\w_]+): (.*?)(\\||})", "| $1($2)$3");
      // jinjava doesn't have the '?' postfix binary operator
      template = template.replaceAll("if (.*?)\\?", "if $1");
      // assign --> set
      template = template.replaceAll("assign", "set");
      // no support for offset:n
      template = template.replaceAll("offset:\\s*\\d*", "");
      // no support for limit:n
      template = template.replaceAll("limit:\\s*\\d*", "");
      // no support for cols:n
      template = template.replaceAll("cols:\\s*\\d*", "");
      
      System.out.println("Adding template: " + tmpl.getAbsolutePath());
      System.out.println(template);

      templates.add(template);
    }
  }
  
  @Benchmark
  public void parse(Blackhole blackhole) {
    JinjavaInterpreter interpreter = new JinjavaInterpreter(jinjava, jinjava.getGlobalContext(), jinjava.getGlobalConfig());

    for(String template : templates) {
      Node parsed = interpreter.parse(template);
      if(blackhole != null) {
        blackhole.consume(parsed);
      }
    }
  }
  
  @Benchmark
  public void parseAndRender(Blackhole blackhole) {
    for(String template : templates) {
      String result = jinjava.render(template, bindings);
      if(blackhole != null) {
        blackhole.consume(result);
      }
    }
  }
  
  public static void main(String[] args) throws Exception {
    MyBenchmark b = new MyBenchmark();
    b.setup();
    b.parse(null);
    b.parseAndRender(null);
  }

}
