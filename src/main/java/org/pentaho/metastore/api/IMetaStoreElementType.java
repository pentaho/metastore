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

import org.pentaho.metastore.api.exceptions.MetaStoreException;

/**
 * This interface is used to describe objects of this type that are stored in the metastore.
 * 
 * @author matt
 * 
 */
public interface IMetaStoreElementType {

  /**
   * Gets the namespace associated with the element type.
   * 
   * @return The namespace to which the element type belongs.
   */
  public String getNamespace();

  /**
   * Associates the specified namespace for the element type.
   * 
   * @param namespace
   *          The namespace to set.
   */
  public void setNamespace( String namespace );

  /**
   * @return The name of the IMetaStore this element type belongs to.
   */
  public String getMetaStoreName();

  /**
   * @param metaStoreName
   *          The name of the IMetaStore this element type belongs to.
   */
  public void setMetaStoreName( String metaStoreName );

  /**
   * Gets the identifier of the element type. This identifier is unique in a namespace.
   * 
   * @return The ID of the element type, unique in a namespace
   */
  public String getId();

  /**
   * Set the identifier for this element type.
   * 
   * @param id
   *          the id to set
   */
  public void setId( String id );

  /**
   * Gets the name of the element type.
   * 
   * @return The name of the element type
   */
  public String getName();

  /**
   * Sets the name for the element type.
   * 
   * @param name
   *          The element type name to set
   */
  public void setName( String name );

  /**
   * Gets the description of the element type.
   * 
   * @return The description of the element type
   */
  public String getDescription();

  /**
   * Sets the description of the element type.
   * 
   * @param description
   *          the description to set
   */
  public void setDescription( String description );

  /**
   * Persists the element type definition to the underlying metastore.
   * 
   * @throws MetaStoreException
   *           In case there is an error in the underlying store.
   */
  public void save() throws MetaStoreException;
}
