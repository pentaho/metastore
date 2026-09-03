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



package org.pentaho.metastore.api.listeners;

import org.pentaho.metastore.api.IMetaStoreElementType;

/**
 * Convenience class to facilitate creating data type listeners.
 */
public class MetaStoreDataTypeAdapter implements MetaStoreElementTypeListener {

  /**
   * Receives a data type creation without taking action.
   *
   * @param namespace the data type namespace
   * @param dataType the created data type
   */
  @Override
  public void dataTypeCreated( String namespace, IMetaStoreElementType dataType ) {
  }

  /**
   * Receives a data type deletion without taking action.
   *
   * @param namespace the data type namespace
   * @param dataType the deleted data type
   */
  @Override
  public void dataTypeDeleted( String namespace, IMetaStoreElementType dataType ) {
  }

  /**
   * Receives a data type update without taking action.
   *
   * @param namespace the data type namespace
   * @param oldDataType the data type before the update
   * @param newDataType the data type after the update
   */
  @Override
  public void dataTypeUpdated( String namespace, IMetaStoreElementType oldDataType, IMetaStoreElementType newDataType ) {
  }

}
