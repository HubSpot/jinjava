package com.hubspot.jinjava.loader;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class FileLocatorTest {

  JinjavaInterpreter interpreter;
  
  FileLocator locatorWorkingDir;
  FileLocator locatorTmpDir;
  
  File first;
  File second;
  
  @Before
  public void setUp() throws Exception {
    Jinjava jinjava = new Jinjava();
    Context context = new Context();
    interpreter = new JinjavaInterpreter(jinjava, context, jinjava.getGlobalConfig());
    
    locatorWorkingDir = new FileLocator();
    
    File tmpDir = java.nio.file.Files.createTempDirectory(getClass().getSimpleName()).toFile();
    locatorTmpDir = new FileLocator(tmpDir);
    
    first = new File(tmpDir, "foo/first.jinja");
    second =  new File("target/loader-test-data/second.jinja");
    
    first.getParentFile().mkdirs();
    second.getParentFile().mkdirs();
    
    Files.write("first", first, Charsets.UTF_8);
    Files.write("second", second, Charsets.UTF_8);
  }

  @Test
  public void testWorkingDirRelative() throws Exception {
    assertThat(locatorWorkingDir.getString("target/loader-test-data/second.jinja", Charsets.UTF_8, interpreter)).isEqualTo("second");
  }
  
  @Test
  public void testWorkingDirAbs() throws Exception {
    assertThat(locatorWorkingDir.getString(second.getAbsolutePath(), Charsets.UTF_8, interpreter)).isEqualTo("second");
  }
  
  @Test
  public void testTmpDirRel() throws Exception {
    assertThat(locatorTmpDir.getString("foo/first.jinja", Charsets.UTF_8, interpreter)).isEqualTo("first");
  }
  
  @Test
  public void testTmpDirAbs() throws Exception {
    assertThat(locatorTmpDir.getString(first.getAbsolutePath(), Charsets.UTF_8, interpreter)).isEqualTo("first");
  }
  
  @Test(expected=FileNotFoundException.class)
  public void testInvalidBaseDir() throws Exception {
    new FileLocator(new File("/blarghhh"));
  }

  @Test(expected=ResourceNotFoundException.class)
  public void testNotFoundCauseItsADir() throws Exception {
    locatorTmpDir.getString("foo", Charsets.UTF_8, interpreter);
  }
  
  @Test(expected=ResourceNotFoundException.class)
  public void testNotFoundRel() throws Exception {
    locatorWorkingDir.getString("blargh", Charsets.UTF_8, interpreter);
  }
  
  @Test(expected=ResourceNotFoundException.class)
  public void testNotFoundAbs() throws Exception {
    locatorWorkingDir.getString("/blargh", Charsets.UTF_8, interpreter);
  }
  
}
