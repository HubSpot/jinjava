package com.hubspot.jinjava.loader;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseInterpretingTest;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.Before;
import org.junit.Test;

public class FileLocatorTest extends BaseInterpretingTest {

  FileLocator locatorWorkingDir;
  FileLocator locatorTmpDir;
  FileLocator locatorConfined;

  File first;
  File second;
  File inside;
  File secretOutside;

  @Before
  public void setUp() throws Exception {
    locatorWorkingDir = new FileLocator();

    File tmpDir = Files.createTempDirectory(getClass().getSimpleName()).toFile();
    locatorTmpDir = new FileLocator(tmpDir);

    first = new File(tmpDir, "foo/first.jinja");
    second = new File("target/loader-test-data/second.jinja");

    first.getParentFile().mkdirs();
    second.getParentFile().mkdirs();

    Files.writeString(first.toPath(), "first");
    Files.writeString(second.toPath(), "second");

    File parent = Files
      .createTempDirectory(getClass().getSimpleName() + "-confined")
      .toFile();
    File confinedDir = new File(parent, "base");
    confinedDir.mkdirs();
    locatorConfined = new FileLocator(confinedDir);

    inside = new File(confinedDir, "inside.jinja");
    Files.writeString(inside.toPath(), "inside");

    secretOutside = new File(parent, "secret.txt");
    Files.writeString(secretOutside.toPath(), "TOP_SECRET_FROM_OUTSIDE_DIR");
  }

  @Test
  public void testWorkingDirRelative() throws Exception {
    assertThat(
      locatorWorkingDir.getString(
        "target/loader-test-data/second.jinja",
        StandardCharsets.UTF_8,
        interpreter
      )
    )
      .isEqualTo("second");
  }

  @Test
  public void testWorkingDirAbs() throws Exception {
    assertThat(
      locatorWorkingDir.getString(
        second.getAbsolutePath(),
        StandardCharsets.UTF_8,
        interpreter
      )
    )
      .isEqualTo("second");
  }

  @Test
  public void testTmpDirRel() throws Exception {
    assertThat(
      locatorTmpDir.getString("foo/first.jinja", StandardCharsets.UTF_8, interpreter)
    )
      .isEqualTo("first");
  }

  @Test
  public void testTmpDirAbs() throws Exception {
    assertThat(
      locatorTmpDir.getString(
        first.getAbsolutePath(),
        StandardCharsets.UTF_8,
        interpreter
      )
    )
      .isEqualTo("first");
  }

  @Test(expected = FileNotFoundException.class)
  public void testInvalidBaseDir() throws Exception {
    new FileLocator(new File("/blarghhh"));
  }

  @Test(expected = ResourceNotFoundException.class)
  public void testNotFoundCauseItsADir() throws Exception {
    locatorTmpDir.getString("foo", StandardCharsets.UTF_8, interpreter);
  }

  @Test(expected = ResourceNotFoundException.class)
  public void testNotFoundRel() throws Exception {
    locatorWorkingDir.getString("blargh", StandardCharsets.UTF_8, interpreter);
  }

  @Test(expected = ResourceNotFoundException.class)
  public void testNotFoundAbs() throws Exception {
    locatorWorkingDir.getString("/blargh", StandardCharsets.UTF_8, interpreter);
  }

  @Test
  public void itAllowsInRootRelativePath() throws Exception {
    assertThat(
      locatorConfined.getString("inside.jinja", StandardCharsets.UTF_8, interpreter)
    )
      .isEqualTo("inside");
  }

  @Test
  public void itAllowsInRootAbsolutePath() throws Exception {
    assertThat(
      locatorConfined.getString(
        inside.getAbsolutePath(),
        StandardCharsets.UTF_8,
        interpreter
      )
    )
      .isEqualTo("inside");
  }

  @Test(expected = ResourceNotFoundException.class)
  public void itBlocksDotDotTraversalOutsideRoot() throws Exception {
    locatorConfined.getString("../secret.txt", StandardCharsets.UTF_8, interpreter);
  }

  @Test(expected = ResourceNotFoundException.class)
  public void itBlocksAbsolutePathOutsideRoot() throws Exception {
    locatorConfined.getString(
      secretOutside.getAbsolutePath(),
      StandardCharsets.UTF_8,
      interpreter
    );
  }
}
