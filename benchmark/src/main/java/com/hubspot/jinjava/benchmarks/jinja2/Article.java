package com.hubspot.jinjava.benchmarks.jinja2;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import de.svenjacobs.loremipsum.LoremIpsum;

public class Article {

  private int id;
  private String href;
  private String title;
  private User user;
  private String body;
  private Date pubDate;
  private boolean published;

  public Article(int id, User user) throws NoSuchAlgorithmException {
    this.id = id;
    this.href = "/article/" + id;

    LoremIpsum ipsum = new LoremIpsum();
    SecureRandom rnd = SecureRandom.getInstanceStrong();

    this.title = ipsum.getWords(10);
    this.user = user;
    this.body = ipsum.getParagraphs();
    this.pubDate = Date.from(LocalDateTime.now().minusHours(rnd.nextInt(128)).toInstant(ZoneOffset.UTC));
    this.published = true;
  }

  public int getId() {
    return id;
  }

  public String getHref() {
    return href;
  }

  public String getTitle() {
    return title;
  }

  public User getUser() {
    return user;
  }

  public String getBody() {
    return body;
  }

  public Date getPubDate() {
    return pubDate;
  }

  public boolean isPublished() {
    return published;
  }

}
