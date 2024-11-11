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


package org.pentaho.metastore.api.listeners;

import org.pentaho.metastore.api.IMetaStoreElement;

/**
 * Convenience listener adapter for listening to meta store entity changes.
 * 
 * @author matt
 * 
 */
public class MetaStoreEntityAdapter implements MetaStoreElementListener {

  @Override
  public void elementUpdated( String namespace, String dataTypeId, IMetaStoreElement oldEntity,
      IMetaStoreElement newEntity ) {
  }

  @Override
  public void elementCreated( String namespace, String dataTypeId, IMetaStoreElement entity ) {
  }

  @Override
  public void elementDeleted( String namespace, String dataTypeId, IMetaStoreElement entity ) {
  }

}
