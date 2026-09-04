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



package org.pentaho.metastore.api.security;

/**
 * Describes the owner of a metastore element.
 */
public interface IMetaStoreElementOwner {

  /**
   * Gets the owner name.
   *
   * @return the owner name
   */
  public String getName();

  /**
   * Sets the owner name.
   *
   * @param name the owner name
   */
  public void setName( String name );

  /**
   * Gets the owner type.
   *
   * @return the owner type
   */
  public MetaStoreElementOwnerType getOwnerType();

  /**
   * Sets the owner type.
   *
   * @param type the owner type
   */
  public void setOwnerType( MetaStoreElementOwnerType type );

}
