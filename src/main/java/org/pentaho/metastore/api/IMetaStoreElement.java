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


package org.pentaho.metastore.api;

import java.util.List;

import org.pentaho.metastore.api.security.IMetaStoreElementOwner;
import org.pentaho.metastore.api.security.MetaStoreOwnerPermissions;

/**
 * This interface describes the element as an attribute (with children) with security on top of it.
 * 
 * @author matt
 * 
 */
public interface IMetaStoreElement extends IMetaStoreAttribute {

  /**
   * Gets the name of this element.
   * 
   * @return the name of the element
   */
  public String getName();

  /**
   * Sets the name for this element.
   * 
   * @param name
   *          the new name
   */
  public void setName( String name );

  /**
   * Gets the element type.
   * 
   * @return the element type
   */
  public IMetaStoreElementType getElementType();

  /**
   * Sets the element type.
   * 
   * @param elementType
   *          the new element type
   */
  public void setElementType( IMetaStoreElementType elementType );

  /**
   * Gets the owner of this element.
   * 
   * @return the owner
   */
  public IMetaStoreElementOwner getOwner();

  /**
   * Sets the owner for this element.
   * 
   * @param owner
   *          the new owner
   */
  public void setOwner( IMetaStoreElementOwner owner );

  /**
   * Gets the owner permissions list for this element.
   * 
   * @return the owner permissions list
   */
  public List<MetaStoreOwnerPermissions> getOwnerPermissionsList();

  /**
   * Sets the owner permissions list for this element.
   * 
   * @param ownerPermissions
   *          the new owner permissions list
   */
  public void setOwnerPermissionsList( List<MetaStoreOwnerPermissions> ownerPermissions );

}
