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

import org.pentaho.metastore.api.IMetaStoreElementType;

/**
 * Convenience class to facilitate creating data type listeners.
 * 
 * @author matt
 * 
 */
public class MetaStoreDataTypeAdapter implements MetaStoreElementTypeListener {

  @Override
  public void dataTypeCreated( String namespace, IMetaStoreElementType dataType ) {
  }

  @Override
  public void dataTypeDeleted( String namespace, IMetaStoreElementType dataType ) {
  }

  @Override
  public void dataTypeUpdated( String namespace, IMetaStoreElementType oldDataType, IMetaStoreElementType newDataType ) {
  }

}
