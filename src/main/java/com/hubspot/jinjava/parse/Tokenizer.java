/**********************************************************************
Copyright (c) 2014 HubSpot Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 **********************************************************************/
package com.hubspot.jinjava.parse;

import static com.hubspot.jinjava.parse.ParserConstants.TOKEN_ECHO;
import static com.hubspot.jinjava.parse.ParserConstants.TOKEN_ECHO2;
import static com.hubspot.jinjava.parse.ParserConstants.TOKEN_FIXED;
import static com.hubspot.jinjava.parse.ParserConstants.TOKEN_NEWLINE;
import static com.hubspot.jinjava.parse.ParserConstants.TOKEN_NOTE;
import static com.hubspot.jinjava.parse.ParserConstants.TOKEN_POSTFIX;
import static com.hubspot.jinjava.parse.ParserConstants.TOKEN_PREFIX;
import static com.hubspot.jinjava.parse.ParserConstants.TOKEN_TAG;

public class Tokenizer {

  private char[] is;
  private int currPost = 0;
  private int tokenStart = 0;
  private int tokenLength = 0;
  private int tokenKind = -1;
  private int length = 0;
  private int lastStart = 0;
  private int inComment = 0;
  private int inRaw = 0;
  private int inBlock = 0;
  private char inQuote = 0;
  private int currLine = 1;

  public void init(String inputstream) {
    is = inputstream.toCharArray();
    length = inputstream.length();
    currPost = 0;
    tokenStart = 0;
    tokenKind = -1;
    lastStart = 0;
    inComment = 0;
    inRaw = 0;
    inBlock = 0;
    inQuote = 0;
    currLine = 1;
  }

  public Token getNextToken() {
    char c = 0;
    while (currPost < length) {
      c = is[currPost++];
      if (currPost == length) {
        return getEndToken();
      }
      
      if(inBlock > 0) {
        if(inQuote != 0) {
          if(inQuote != c) {
            continue;
          }
          else if(is[currPost - 2] == '\\') {
            continue;
          }
          else {
            inQuote = 0;
            continue;
          }
        }
        else if(inQuote == 0 && (c == '\'' || c == '"')){
          inQuote = c;
          continue;
        }
      }
      
      switch (c) {
      case TOKEN_PREFIX:
        if (currPost < length) {
          c = is[currPost];
          switch (c) {
          case TOKEN_NOTE:
            if(inComment == 1 || inRaw == 1) {
              continue;
            }
            inComment = 1;

            tokenLength = currPost - tokenStart - 1;
            if (tokenLength > 0) {
              // start a new token
              lastStart = tokenStart;
              tokenStart = --currPost;
              tokenKind = c;
              inComment = 0;
              return newToken(TOKEN_FIXED);
            } else {
              tokenKind = c;
            }
            break;
          case TOKEN_TAG:
          case TOKEN_ECHO:
            if (inComment > 0) {
              continue;
            }
            if (inRaw > 0 && (c == TOKEN_ECHO || !isEndRaw())) {
              continue;
            }
            // match token two ends
            if (!matchToken(c) && tokenKind > 0) {
              continue;
            }
            if(inBlock++ > 0) {
              continue;
            }

            tokenLength = currPost - tokenStart - 1;
            if (tokenLength > 0) {
              // start a new token
              lastStart = tokenStart;
              tokenStart = --currPost;
              tokenKind = c;
              return newToken(TOKEN_FIXED);
            } else {
              tokenKind = c;
            }
            break;
          }
        }
        // reach the stream end
        else {
          return getEndToken();
        }
        break;
        
      // maybe current token is closing
      case TOKEN_TAG:
      case TOKEN_ECHO2:
        if (inComment > 0) {
          continue;
        }
        if (!matchToken(c)) {
          continue;
        }
        if (currPost < length) {
          c = is[currPost];
          if (c == TOKEN_POSTFIX) {
            inBlock = 0;

            tokenLength = currPost - tokenStart + 1;
            if (tokenLength > 0) {
              // start a new token
              lastStart = tokenStart;
              tokenStart = ++currPost;
              int kind = tokenKind;
              tokenKind = TOKEN_FIXED;
              return newToken(kind);
            }
          }
        } else {
          return getEndToken();
        }
        break;
      case TOKEN_NOTE:
        if (!matchToken(c)) {
          continue;
        }
        if (currPost < length) {
          c = is[currPost];
          if (c == TOKEN_POSTFIX) {
            inComment = 0;

            tokenLength = currPost - tokenStart + 1;
            if (tokenLength > 0) {
              // start a new token
              lastStart = tokenStart;
              tokenStart = ++currPost;
              tokenKind = TOKEN_FIXED;
              return newToken(TOKEN_NOTE);
            }
          }
        } else {
          return getEndToken();
        }
        break;
      case TOKEN_NEWLINE:
        currLine++;

        if(inComment > 0 || inBlock > 0) {
          continue;
        }

        tokenKind = TOKEN_FIXED;
        break;
      default:
        if (tokenKind == -1) {
          tokenKind = TOKEN_FIXED;
        }
      }
    }
    return null;
  }

  private boolean isEndRaw() {
    int pos = currPost + 1;
    while(pos < length) {
      if(!Character.isWhitespace(is[pos++])) {
        break;
      }
    }
    
    if(pos + 5 >= length) {
      return false;
    }
    
    return "endraw".equals(String.valueOf(is, pos - 1, 6));
  }
  
  private Token getEndToken() {
    tokenLength = currPost - tokenStart;
    int type = TOKEN_FIXED;
    if (inComment > 0) {
      type = TOKEN_NOTE;
    }
    return Token.newToken(type, String.valueOf(is, tokenStart, tokenLength), currLine);
  }

  private Token newToken(int kind) {
    Token t = Token.newToken(kind, String.copyValueOf(is, lastStart, tokenLength), currLine);
    
    if(t instanceof TagToken) {
      TagToken tt = (TagToken) t;
      if("raw".equals(tt.getTagName())) {
        inRaw = 1;
        return tt;
      }
      else if("endraw".equals(tt.getTagName())) {
        inRaw = 0;
        return tt;
      }
    }

    if(inRaw > 0 && t.getType() != TOKEN_FIXED) {
      return Token.newToken(TOKEN_FIXED, t.image, currLine);
    }
    
    return t;
  }

  private boolean matchToken(char kind) {
    if (kind == TOKEN_ECHO) {
      return tokenKind == TOKEN_ECHO2;
    } else if (kind == TOKEN_ECHO2) {
      return tokenKind == TOKEN_ECHO;
    } else {
      return kind == tokenKind;
    }
  }

}
