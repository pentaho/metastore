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

import org.pentaho.metastore.api.IMetaStoreElementType;

/**
 * Signals that an element type already exists in a metastore.
 */

public class MetaStoreElementTypeExistsException extends MetaStoreException {

  private static final long serialVersionUID = -1658192841342866261L;

  private List<IMetaStoreElementType> dataTypes;

  /**
   * Creates an exception with existing element types.
   *
   * @param dataTypes the existing element types
   */
  public MetaStoreElementTypeExistsException( List<IMetaStoreElementType> dataTypes ) {
    super();
    this.dataTypes = dataTypes;
  }

  /**
   * Creates an exception with existing element types and a message.
   *
   * @param dataTypes the existing element types
   * @param message the exception message
   */
  public MetaStoreElementTypeExistsException( List<IMetaStoreElementType> dataTypes, String message ) {
    super( message );
    this.dataTypes = dataTypes;
  }

  /**
   * Creates an exception with existing element types and a cause.
   *
   * @param dataTypes the existing element types
   * @param cause the cause
   */
  public MetaStoreElementTypeExistsException( List<IMetaStoreElementType> dataTypes, Throwable cause ) {
    super( cause );
    this.dataTypes = dataTypes;
  }

  /**
   * Creates an exception with existing element types, a message, and a cause.
   *
   * @param dataTypes the existing element types
   * @param message the exception message
   * @param cause the cause
   */
  public MetaStoreElementTypeExistsException( List<IMetaStoreElementType> dataTypes, String message, Throwable cause ) {
    super( message, cause );
    this.dataTypes = dataTypes;
  }

  /**
   * Sets the existing element types.
   *
   * @param dataTypes the existing element types
   */
  public void setDataTypes( List<IMetaStoreElementType> dataTypes ) {
    this.dataTypes = dataTypes;
  }

  /**
   * Gets the existing element types.
   *
   * @return the existing element types
   */
  public List<IMetaStoreElementType> getDataTypes() {
    return dataTypes;
  }
}
