package com.hubspot.jinjava.objects.date;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class StrftimeFormatter {

  /*
   * Mapped from http://strftime.org/, http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
   */
  private static final Map<Character, String> CONVERSIONS = new HashMap<Character, String>();
  static {
    CONVERSIONS.put('a', "EEE");
    CONVERSIONS.put('A', "EEEEEE");
    CONVERSIONS.put('b', "MMM");
    CONVERSIONS.put('B', "MMMMMM");
    CONVERSIONS.put('c', "EEE MMM dd HH:mm:ss yyyy");
    CONVERSIONS.put('d', "dd");
    CONVERSIONS.put('e', "dd"); // The day of the month like with %d, but padded with blank (range 1 through 31).
    CONVERSIONS.put('f', "SSSSSS");
    CONVERSIONS.put('H', "HH");
    CONVERSIONS.put('h', "hh");
    CONVERSIONS.put('I', "hh");
    CONVERSIONS.put('j', "DDD");
    CONVERSIONS.put('k', "HH"); // The hour as a decimal number, using a 24-hour clock like %H, but padded with blank (range 0 through 23).
    CONVERSIONS.put('l', "hh"); // The hour as a decimal number, using a 12-hour clock like %I, but padded with blank (range 1 through 12).
    CONVERSIONS.put('m', "MM");
    CONVERSIONS.put('M', "mm");
    CONVERSIONS.put('p', "aa");
    CONVERSIONS.put('S', "ss");
    CONVERSIONS.put('U', "ww");
    CONVERSIONS.put('w', "uu");
    CONVERSIONS.put('W', "ww");
    CONVERSIONS.put('x', "MM/dd/yy");
    CONVERSIONS.put('X', "HH:mm:ss");
    CONVERSIONS.put('y', "yy");
    CONVERSIONS.put('Y', "yyyy");
    CONVERSIONS.put('z', "Z");
    CONVERSIONS.put('Z', "ZZZ");
    CONVERSIONS.put('%', "%");
  }
  
  /**
   * Parses a string in python strftime format, returning the equivalent string in java date time format.
   * 
   * @param strftime
   * @return date formatted as string
   */
  private static String toJavaDateTimeFormat(String strftime) {
    if(!StringUtils.contains(strftime, '%')) {
      return replaceL(strftime);
    }
    
    StringBuilder result = new StringBuilder();
    
    for(int i = 0; i < strftime.length(); i++) {
      char c = strftime.charAt(i);
      if(c == '%') {
        c = strftime.charAt(++i);
        boolean stripLeadingZero = false;
        
        if(c == '-') {
          stripLeadingZero = true;
          c = strftime.charAt(++i);
        }
        
        if(stripLeadingZero) {
          result.append(CONVERSIONS.get(c).substring(1));
        }
        else {
          result.append(CONVERSIONS.get(c));
        }
      }
      else if(Character.isLetter(c)) {
        result.append("'");
        while(Character.isLetter(c)) {
          result.append(c);
          c = strftime.charAt(++i);
        }
        result.append("'");
        --i;  // re-consume last char
      }
      else {
        result.append(c);
      }
    }
    
    return replaceL(result.toString());
  }

  private static String replaceL(String s) {
    return StringUtils.replaceChars(s, 'L', 'M');
  }

  public static DateTimeFormatter formatter(String strftime) {
    switch (strftime.toLowerCase()) {
      case "short":
        return DateTimeFormat.shortDateTime();
      case "medium":
        return DateTimeFormat.mediumDateTime();
      case "long":
        return DateTimeFormat.longDateTime();
      case "full":
        return DateTimeFormat.fullDateTime();
      default:
        return DateTimeFormat.forPattern(toJavaDateTimeFormat(strftime));
    }
  }

  public static String format(DateTime d) {
    return format(d, "%H:%M / %d-%m-%Y");
  }
  
  public static String format(DateTime d, String strftime) {
    return formatter(strftime).print(d);
  }
  
}
