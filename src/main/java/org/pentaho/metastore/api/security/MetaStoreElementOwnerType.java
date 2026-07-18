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

public enum MetaStoreElementOwnerType {
  USER, ROLE, SYSTEM_ROLE;

  public static MetaStoreElementOwnerType getOwnerType( String string ) {
    if ( string == null || string.length() == 0 ) {
      return null;
    }
    return valueOf( string );
  }
}
