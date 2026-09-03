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
 * Identifies an operation that a user can perform on a metastore object.
 */
public enum MetaStoreObjectPermission {
  CREATE, READ, UPDATE, DELETE;
}
