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
 * Set of methods that are called in various parts of the meta store data type life-cycle.
 * 
 * @author matt
 * 
 */
public interface MetaStoreElementTypeListener {

  /**
   * This method will inform you of the creation of a data type.
   * 
   * @param namespace
   *          The namespace the data type is created in.
   * @param dataType
   *          the data type that was created.
   */
  public void dataTypeCreated( String namespace, IMetaStoreElementType dataType );

  /**
   * This method will is called when a data type is updated.
   * 
   * @param namespace
   *          The namespace the data type was updated in
   * @param oldDataType
   *          The old data type.
   * @param newDataType
   *          The new data type.
   */
  public void dataTypeUpdated( String namespace, IMetaStoreElementType oldDataType, IMetaStoreElementType newDataType );

  /**
   * This method will is called when a data type is deleted.
   * 
   * @param namespace
   *          The namespace the data type was deleted from
   * @param dataType
   *          The deleted data type.
   */
  public void dataTypeDeleted( String namespace, IMetaStoreElementType dataType );
}
