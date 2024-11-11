/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.metastore.api.security;

import junit.framework.TestCase;

/**
 * Created by saslan on 10/23/2015.
 */
public class Base64TwoWayPasswordEncoderTest extends TestCase {
  private Base64TwoWayPasswordEncoder base64TwoWayPasswordEncoder;

  public void setUp() throws Exception {
    base64TwoWayPasswordEncoder = new Base64TwoWayPasswordEncoder();
  }

  public void testEncode() throws Exception {
    CharSequence charSequence = "password";
    String encoded = base64TwoWayPasswordEncoder.encode( charSequence );
    assertEquals( encoded, "cGFzc3dvcmQ=" );
    encoded = base64TwoWayPasswordEncoder.encode( "" );
    assertEquals( encoded, "" );
    encoded = base64TwoWayPasswordEncoder.encode( null );
    assertEquals( encoded, null );
  }

  public void testDecode() throws Exception {
    CharSequence charSequence = "cGFzc3dvcmQ=";
    String decoded = base64TwoWayPasswordEncoder.decode( charSequence );
    assertEquals( decoded, "password" );
    decoded = base64TwoWayPasswordEncoder.decode( "" );
    assertEquals( decoded, "" );
    decoded = base64TwoWayPasswordEncoder.decode( null );
    assertEquals( decoded, null );

  }

  public void testMatches() throws Exception {
    String encodedPassword = "cGFzc3dvcmQ=";
    CharSequence rawPassword = "password";
    boolean matches = base64TwoWayPasswordEncoder.matches( rawPassword, encodedPassword );
    assertEquals( matches, true );
    matches = base64TwoWayPasswordEncoder.matches( null, encodedPassword );
    assertEquals( matches, false );
    matches = base64TwoWayPasswordEncoder.matches( rawPassword, null );
    assertEquals( matches, false );

  }
}
