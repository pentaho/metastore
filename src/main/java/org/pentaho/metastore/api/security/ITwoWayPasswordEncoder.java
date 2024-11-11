/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.metastore.api.security;

public interface ITwoWayPasswordEncoder {
  /**
   * Encode the raw password.
   */
  String encode( CharSequence rawPassword );

  /**
   * Decode the password.
   */
  String decode( CharSequence encodedPassword );

  /**
   * Verify the encoded password matches raw password.
   */
  boolean matches( CharSequence rawPassword, String encodedPassword );
}
