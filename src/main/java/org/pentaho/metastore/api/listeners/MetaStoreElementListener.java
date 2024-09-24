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
 * Set of methods that are called in various parts of the meta store element life-cycle.
 * 
 * @author matt
 * 
 */
public interface MetaStoreElementListener {

  /**
   * This method is called after an element was created in the store
   * 
   * @param namespace
   *          The namespace of the element
   * @param elementTypeId
   *          The element type ID of the element
   * @param element
   *          The element that was created
   */
  public void elementCreated( String namespace, String elementTypeId, IMetaStoreElement element );

  /**
   * This method is called when an element is changed
   * 
   * @param namespace
   *          The namespace of the element
   * @param dataType
   *          The element type of the element
   * @param oldElement
   *          The element before the change
   * @param newElement
   *          The element after the change
   */
  public void elementUpdated( String namespace, String elementTypeId, IMetaStoreElement oldElement,
      IMetaStoreElement newElement );

  /**
   * This method is called after an element was deleted from the store
   * 
   * @param namespace
   *          The namespace of the element
   * @param dataType
   *          The element type ID of the element
   * @param element
   *          The element that was deleted
   */
  public void elementDeleted( String namespace, String elementTypeId, IMetaStoreElement element );
}
