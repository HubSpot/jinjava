package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Stopwatch;
import com.google.common.io.Resources;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.objects.SafeString;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.Before;
import org.junit.Test;

public class EscapeFilterTest extends BaseInterpretingTest {
  EscapeFilter f;

  @Before
  public void setup() {
    f = new EscapeFilter();
  }

  @Test
  public void testEscape() {
    assertThat(f.filter("", interpreter)).isEqualTo("");
    assertThat(f.filter("me & you", interpreter)).isEqualTo("me &amp; you");
    assertThat(f.filter("jared's & ted's bogus journey", interpreter))
      .isEqualTo("jared&#39;s &amp; ted&#39;s bogus journey");
    assertThat(f.filter(1, interpreter)).isEqualTo("1");
  }

  @Test
  public void testSafeStringCanBeEscaped() {
    assertThat(
        f
          .filter(new SafeString("<a>Previously marked as safe<a/>"), interpreter)
          .toString()
      )
      .isEqualTo("&lt;a&gt;Previously marked as safe&lt;a/&gt;");
    assertThat(f.filter(new SafeString("<a>Previously marked as safe<a/>"), interpreter))
      .isInstanceOf(SafeString.class);
  }

  @Test
  public void testNewStringReplaceIsFaster() {
    String html = fixture("filter/blog.html").substring(0, 100_000);
    Stopwatch oldStopWatch = Stopwatch.createStarted();
    String oldResult = EscapeFilter.oldEscapeHtmlEntities(html);
    Duration oldTime = oldStopWatch.elapsed();

    Stopwatch newStopWatch = Stopwatch.createStarted();
    String newResult = EscapeFilter.escapeHtmlEntities(html);
    Duration newTime = newStopWatch.elapsed();

    assertThat(newResult).isEqualTo(oldResult);
    System.out.printf("New: %d Old:%d\n", newTime.toMillis(), oldTime.toMillis());
    int speedUpFactor = getVersion() < 17 ? 2 : 1; // On M1, it is between 50 and 100 times faster. Difference is much smaller on java 17
    assertThat(newTime.toMillis()).isLessThan(oldTime.toMillis() / speedUpFactor);
  }

  private static String fixture(String name) {
    try {
      return Resources.toString(Resources.getResource(name), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static int getVersion() {
    String version = System.getProperty("java.version");
    if (version.startsWith("1.")) {
      version = version.substring(2, 3);
    } else {
      int dotIndex = version.indexOf(".");
      if (dotIndex != -1) {
        version = version.substring(0, dotIndex);
      }
    }
    return Integer.parseInt(version);
  }
}
