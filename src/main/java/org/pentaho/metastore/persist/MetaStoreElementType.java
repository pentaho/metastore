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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a class as a metastore element type.
 */
@Retention( RetentionPolicy.RUNTIME )
public @interface MetaStoreElementType {

  /**
   * Gets the element type name.
   *
   * @return the element type name
   */
  String name();

  /**
   * Gets the element type description.
   *
   * @return the element type description
   */
  String description();
}
