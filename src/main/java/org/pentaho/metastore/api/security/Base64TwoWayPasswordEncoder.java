/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.metastore.api.security;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

/**
 * Encodes passwords with Base64.
 */
public class Base64TwoWayPasswordEncoder implements ITwoWayPasswordEncoder {

  private String ENCODING = "UTF-8";

  /**
   * Encodes a raw password with Base64.
   *
   * @param rawPassword the raw password
   * @return the encoded password, or {@code null} for a null input
   */
  @Override
  public String encode( CharSequence rawPassword ) {
    try {
      if ( rawPassword == null ) {
        return null;
      }
      if ( rawPassword.length() == 0 ) {
        return "";
      }
      return new String( Base64.encodeBase64( rawPassword.toString().getBytes( ENCODING ) ), ENCODING );
    } catch ( UnsupportedEncodingException e ) {
      throw new RuntimeException( ENCODING + " is not a supported encoding: fatal error", e );
    }
  }

  /**
   * Decodes a Base64 password.
   *
   * @param encodedPassword the encoded password
   * @return the decoded password, or {@code null} for a null input
   */
  @Override
  public String decode( CharSequence encodedPassword ) {
    try {
      if ( encodedPassword == null ) {
        return null;
      }
      if ( encodedPassword.length() == 0 ) {
        return "";
      }
      return new String( Base64.decodeBase64( encodedPassword.toString() ), ENCODING );
    } catch ( UnsupportedEncodingException e ) {
      throw new RuntimeException( ENCODING + " is not a supported encoding: fatal error", e );
    }
  }

  /**
   * Checks whether a raw password matches an encoded password.
   *
   * @param rawPassword the raw password
   * @param encodedPassword the encoded password
   * @return {@code true} when the values match
   */
  @Override
  public boolean matches( CharSequence rawPassword, String encodedPassword ) {
    if ( rawPassword == null || rawPassword.length() == 0 ) {
      return ( encodedPassword == null || encodedPassword.length() == 0 );
    } else {
      if ( encodedPassword == null ) {
        return false;
      } else {
        return encode( rawPassword ).equals( encodedPassword );
      }
    }
  }
}
