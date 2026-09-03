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

import org.pentaho.metastore.api.IMetaStoreElement;

/**
 * Convenience listener adapter for listening to meta store entity changes.
 */
public class MetaStoreEntityAdapter implements MetaStoreElementListener {

  /**
   * Receives an element update without taking action.
   *
   * @param namespace the element namespace
   * @param dataTypeId the element type ID
   * @param oldEntity the element before the update
   * @param newEntity the element after the update
   */
  @Override
  public void elementUpdated( String namespace, String dataTypeId, IMetaStoreElement oldEntity,
      IMetaStoreElement newEntity ) {
  }

  /**
   * Receives an element creation without taking action.
   *
   * @param namespace the element namespace
   * @param dataTypeId the element type ID
   * @param entity the created element
   */
  @Override
  public void elementCreated( String namespace, String dataTypeId, IMetaStoreElement entity ) {
  }

  /**
   * Receives an element deletion without taking action.
   *
   * @param namespace the element namespace
   * @param dataTypeId the element type ID
   * @param entity the deleted element
   */
  @Override
  public void elementDeleted( String namespace, String dataTypeId, IMetaStoreElement entity ) {
  }

}
