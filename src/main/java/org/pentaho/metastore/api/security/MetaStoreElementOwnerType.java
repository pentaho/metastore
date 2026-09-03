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

/**
 * Identifies the type of owner for a metastore element.
 */
public enum MetaStoreElementOwnerType {
  USER, ROLE, SYSTEM_ROLE;

  /**
   * Converts a name to an owner type.
   *
   * @param string the owner type name
   * @return the matching owner type, or {@code null} for an empty name
   */
  public static MetaStoreElementOwnerType getOwnerType( String string ) {
    if ( string == null || string.length() == 0 ) {
      return null;
    }
    return valueOf( string );
  }
}
