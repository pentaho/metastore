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

/**
 * The IMetaStoreAttribute interface specifies methods related to metastore attributes. A metastore attribute has an
 * identifier/key, value, and possibly child attributes, which allows for hierarchical attribute relationships.
 */
public interface IMetaStoreAttribute {

  /**
   * Gets the identifier/key of this attribute.
   * 
   * @return The ID or key of the metastore attribute
   */
  public String getId();

  /**
   * Sets the identifier/key for this attribute.
   * 
   * @param id
   *          The ID or key of the attribute to set.
   */
  public void setId( String id );

  /**
   * Gets the value object for this attribute.
   * 
   * @return The value of the attribute
   */
  public Object getValue();

  /**
   * Sets the value object for this attribute.
   * 
   * @param value
   *          The attribute value to set.
   */
  public void setValue( Object value );

  /**
   * Gets the child attributes of this attribute.
   * 
   * @return A list of the child attributes
   */
  public List<IMetaStoreAttribute> getChildren();

  /**
   * Adds a child attribute to this attribute.
   * 
   * @param attribute
   *          The attribute to add
   */
  public void addChild( IMetaStoreAttribute attribute );

  /**
   * Deletes the specified child attribute from this attribute.
   * 
   * @param attributeId
   *          The ID or key of the attribute to delete
   */
  public void deleteChild( String attributeId );

  /**
   * Removes all child attributes.
   */
  public void clearChildren();

  /**
   * Retrieves the child attribute with the specified identifier/key.
   * 
   * @param id
   *          The id of the child attribute to retrieve
   * @return The attribute value or null if the attribute doesn't exist.
   */
  public IMetaStoreAttribute getChild( String id );
}
