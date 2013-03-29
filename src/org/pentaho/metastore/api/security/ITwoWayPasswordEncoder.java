package org.pentaho.metastore.api.security;

public interface ITwoWayPasswordEncoder {
  /**
   * Encode the raw password.
   */
  String encode(CharSequence rawPassword);
  
  /**
   * Decode the password.
   */
  String decode(CharSequence encodedPassword);
  
  /**
   * Verify the encoded password matches raw password.
   */
  boolean matches(CharSequence rawPassword, String encodedPassword);
}
