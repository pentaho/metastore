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



package org.pentaho.metastore.api.exceptions;

import java.util.List;

import org.pentaho.metastore.api.IMetaStoreElement;

/**
 * Signals that an element already exists in a metastore.
 */

public class MetaStoreElementExistException extends MetaStoreException {

  private static final long serialVersionUID = -1658192841342866261L;

  private List<IMetaStoreElement> entities;

  /**
   * Creates an exception with existing elements.
   *
   * @param entities the existing elements
   */
  public MetaStoreElementExistException( List<IMetaStoreElement> entities ) {
    super();
    this.entities = entities;
  }

  /**
   * Creates an exception with existing elements and a message.
   *
   * @param entities the existing elements
   * @param message the exception message
   */
  public MetaStoreElementExistException( List<IMetaStoreElement> entities, String message ) {
    super( message );
    this.entities = entities;
  }

  /**
   * Creates an exception with existing elements and a cause.
   *
   * @param entities the existing elements
   * @param cause the cause
   */
  public MetaStoreElementExistException( List<IMetaStoreElement> entities, Throwable cause ) {
    super( cause );
    this.entities = entities;
  }

  /**
   * Creates an exception with existing elements, a message, and a cause.
   *
   * @param entities the existing elements
   * @param message the exception message
   * @param cause the cause
   */
  public MetaStoreElementExistException( List<IMetaStoreElement> entities, String message, Throwable cause ) {
    super( message, cause );
    this.entities = entities;
  }

  /**
   * @return the entities
   */
  public List<IMetaStoreElement> getEntities() {
    return entities;
  }

  /**
   * @param entities
   *          the entities to set
   */
  public void setEntities( List<IMetaStoreElement> entities ) {
    this.entities = entities;
  }
}
