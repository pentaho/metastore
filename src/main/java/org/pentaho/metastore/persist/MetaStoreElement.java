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

package org.pentaho.metastore.persist;

/**
 * Marks a field as a metastore element reference.
 */
public @interface MetaStoreElement {
  /**
   * Gets the element type for the reference.
   *
   * @return the element type
   */
  MetaStoreElementType elementType();

  /**
   * Gets the reference name.
   *
   * @return the reference name
   */
  String name();
}
