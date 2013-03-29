package org.pentaho.metastore.api.security;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

public class Base64TwoWayPasswordEncoder implements ITwoWayPasswordEncoder {

  private String ENCODING = "UTF-8";
  
  @Override
  public String encode(CharSequence rawPassword) {
    try {
      if (rawPassword==null) {
        return null;
      }
      if (rawPassword.length()==0) {
        return "";
      }
      return new String(Base64.encodeBase64(rawPassword.toString().getBytes(ENCODING)), ENCODING);
    } catch(UnsupportedEncodingException e) {
      throw new RuntimeException(ENCODING+" is not a supported encoding: fatal error", e);
    }
  }

  @Override
  public String decode(CharSequence encodedPassword) {
    try {
      if (encodedPassword==null) {
        return null;
      }
      if (encodedPassword.length()==0) {
        return "";
      }
      return new String(Base64.decodeBase64(encodedPassword.toString()), ENCODING);
    } catch(UnsupportedEncodingException e) {
      throw new RuntimeException(ENCODING+" is not a supported encoding: fatal error", e);
    }
  }

  @Override
  public boolean matches(CharSequence rawPassword, String encodedPassword) {
    if (rawPassword==null || rawPassword.length()==0) {
      return (encodedPassword==null || encodedPassword.length()==0);
    } else {
      if (encodedPassword==null) {
        return false;
      } else {
        return encode(rawPassword).equals(encodedPassword);
      }
    }
  }
}
