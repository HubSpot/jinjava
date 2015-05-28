package com.hubspot.jinjava.benchmarks.jinja2;

public class User {

  private String href;
  private String username;

  public User(String username) {
    this.href = "/user/" + username;
    this.username = username;
  }

  public String getHref() {
    return href;
  }

  public String getUsername() {
    return username;
  }

}
