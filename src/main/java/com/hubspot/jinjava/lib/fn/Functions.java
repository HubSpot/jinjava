package com.hubspot.jinjava.lib.fn;

import static com.hubspot.jinjava.util.Logging.ENGINE_LOG;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.BooleanUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.common.collect.Lists;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.date.PyishDate;
import com.hubspot.jinjava.objects.date.StrftimeFormatter;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.NodeList;

public class Functions {

  public static String renderSuperBlock() {
    JinjavaInterpreter interpreter = JinjavaInterpreter.getCurrent();
    StringBuilder result = new StringBuilder();
    
    NodeList superBlock = (NodeList) interpreter.getContext().get("__superbl0ck__");
    if(superBlock != null) {
      for(Node n : superBlock) {
        result.append(n.render(interpreter));
      }
    }
    
    return result.toString();
  }
  
  public static List<Object> immutableListOf(Object... items) {
    return Collections.unmodifiableList(Lists.newArrayList(items));
  }
  
  public static String dateTimeFormat(Object var, String... format) {
    DateTime d = null;
    
    if(var == null) {
      d = DateTime.now(DateTimeZone.UTC);
    }
    else if(var instanceof Long) {
      d = new DateTime((Long) var, DateTimeZone.UTC);
    }
    else if(var instanceof PyishDate) {
      d = ((PyishDate) var).toDateTime();
    }
    else if(var instanceof DateTime) {
      d = (DateTime) var;
    }
    else if(!DateTime.class.isAssignableFrom(var.getClass())) {
      throw new InterpretException("Input to datetimeformat function must be a date object, was: " + var.getClass());
    }
    
    if(d == null) {
      return "";
    }
    
    if(format.length > 0) {
      return StrftimeFormatter.format(d, format[0]);
    }
    else {
      return StrftimeFormatter.format(d);
    }
  }
  
  private static final int DEFAULT_TRUNCATE_LENGTH = 255;
  private static final String DEFAULT_END = "...";

  public static Object truncate(Object var, Object... arg) {
    if (var instanceof String) {
      int length = DEFAULT_TRUNCATE_LENGTH;
      boolean killwords = false;
      String ends = DEFAULT_END;
      
      if (arg.length > 0) {
        try {
          length = Integer.valueOf(Objects.toString(arg[0]));
        } catch (Exception e) {
          ENGINE_LOG.warn("truncate(): error setting length for {}, using default {}", arg[0], DEFAULT_TRUNCATE_LENGTH);
        }
      }

      if (arg.length > 1) {
        try {
          killwords = BooleanUtils.toBoolean(Objects.toString(arg[1]));
        }
        catch(Exception e) {
          ENGINE_LOG.warn("truncate(); error setting killwords for {}", arg[1]);
        }
      }
      
      if (arg.length > 2) {
        ends = Objects.toString(arg[2]);
      }
      
      String string = (String) var;

      if (string.length() > length) {
        if(!killwords) {
          length = movePointerToJustBeforeLastWord(length, string);
        }
        
        return string.substring(0, length) + ends;
      } else {
        return string;
      }
    }

    return var;
  }

  private static int movePointerToJustBeforeLastWord(int pointer, String s) {
    while(pointer > 0 && pointer < s.length()) {
      if(Character.isWhitespace(s.charAt(--pointer))) {
        break;
      }
    }
    
    return pointer + 1;
  }

}
