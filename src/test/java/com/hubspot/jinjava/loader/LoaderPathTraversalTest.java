package com.hubspot.jinjava.loader;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.RenderResult;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

public class LoaderPathTraversalTest {

  private static final String SECRET = "TOP_SECRET_FROM_OUTSIDE_DIR";

  private Jinjava jinjava;
  private File secretOutside;

  @Before
  public void setUp() throws Exception {
    File parent = Files.createTempDirectory(getClass().getSimpleName()).toFile();
    File baseDir = new File(parent, "base");
    baseDir.mkdirs();

    secretOutside = new File(parent, "secret.txt");
    Files.writeString(secretOutside.toPath(), SECRET);

    jinjava = new Jinjava();
    jinjava.setResourceLocator(new FileLocator(baseDir));
  }

  private String render(String template) {
    RenderResult result = jinjava.renderForResult(template, new HashMap<>());
    return result.getOutput();
  }

  @Test
  public void itDoesNotDiscloseFileViaIncludeTraversal() {
    assertThat(render("{% include \"../secret.txt\" %}")).doesNotContain(SECRET);
  }

  @Test
  public void itDoesNotDiscloseFileViaExtendsTraversal() {
    assertThat(render("{% extends \"../secret.txt\" %}")).doesNotContain(SECRET);
  }

  @Test
  public void itDoesNotDiscloseFileViaImportTraversal() {
    assertThat(render("{% import \"../secret.txt\" as leaked %}{{ leaked }}"))
      .doesNotContain(SECRET);
  }

  @Test
  public void itDoesNotDiscloseFileViaFromTraversal() {
    assertThat(render("{% from \"../secret.txt\" import leaked %}{{ leaked }}"))
      .doesNotContain(SECRET);
  }

  @Test
  public void itDoesNotDiscloseFileViaAbsolutePathOutsideRoot() {
    assertThat(render("{% include \"" + secretOutside.getAbsolutePath() + "\" %}"))
      .doesNotContain(SECRET);
  }
}
