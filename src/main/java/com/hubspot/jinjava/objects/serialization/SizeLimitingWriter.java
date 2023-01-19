package com.hubspot.jinjava.objects.serialization;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicInteger;

public class SizeLimitingWriter extends Writer {
  public static final String REMAINING_LENGTH_ATTRIBUTE = "remainingLength";
  private final CharArrayWriter charArrayWriter;
  private final AtomicInteger remainingLength;
  private final int startingLength;

  public SizeLimitingWriter(
    CharArrayWriter charArrayWriter,
    AtomicInteger remainingLength
  ) {
    this.charArrayWriter = charArrayWriter;
    this.remainingLength = remainingLength;
    startingLength = remainingLength.get();
  }

  @Override
  public void write(int c) throws SizeLimitingJsonProcessingException {
    checkMaxSize(1);
    charArrayWriter.write(c);
  }

  @Override
  public void write(char[] c, int off, int len)
    throws SizeLimitingJsonProcessingException {
    checkMaxSize(len);
    charArrayWriter.write(c, off, len);
  }

  @Override
  public void write(String str, int off, int len)
    throws SizeLimitingJsonProcessingException {
    checkMaxSize(len);
    charArrayWriter.write(str, off, len);
  }

  private void checkMaxSize(int extra) throws SizeLimitingJsonProcessingException {
    if (remainingLength.addAndGet(extra * -1) < 0) {
      throw new SizeLimitingJsonProcessingException(
        charArrayWriter.size() + extra,
        startingLength
      );
    }
  }

  public char[] toCharArray() {
    return charArrayWriter.toCharArray();
  }

  public int size() {
    return charArrayWriter.size();
  }

  public String toString() {
    return charArrayWriter.toString();
  }

  @Override
  public void flush() throws IOException {}

  @Override
  public void close() throws IOException {}
}
