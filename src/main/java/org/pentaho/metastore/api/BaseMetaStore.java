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



package org.pentaho.metastore.api;

import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.security.Base64TwoWayPasswordEncoder;
import org.pentaho.metastore.api.security.ITwoWayPasswordEncoder;

/**
 * This class implements common and/or default functionality between IMetaStore instances
 */
public abstract class BaseMetaStore implements IMetaStore {

  /** The name of this metastore. */
  protected String name;

  /** The description of this metastore. */
  protected String description;

  protected ITwoWayPasswordEncoder passwordEncoder;

  /**
   * Instantiates a new base meta store.
   */
  protected BaseMetaStore() {
    passwordEncoder = new Base64TwoWayPasswordEncoder();
  }

  /**
   * Gets the name of this metastore.
   * 
   * @return the name
   */
  public String getName() throws MetaStoreException {
    return name;
  }

  /**
   * Sets the name of this metastore.
   * 
   * @param name
   *          the name to set
   */
  public void setName( String name ) {
    this.name = name;
  }

  /**
   * Gets the description of this metastore.
   * 
   * @return the description of this metastore
   */
  public String getDescription() throws MetaStoreException {
    return description;
  }

  /**
   * Sets the description for this metastore.
   * 
   * @param description
   *          the description to set
   */
  public void setDescription( String description ) {
    this.description = description;
  }

  /**
   * Gets the password encoder for this metastore.
   *
   * @return the password encoder
   */
  public ITwoWayPasswordEncoder getTwoWayPasswordEncoder() {
    return passwordEncoder;
  }

  /**
   * Sets the password encoder for this metastore.
   *
   * @param passwordEncoder the password encoder
   */
  public void setTwoWayPasswordEncoder( ITwoWayPasswordEncoder passwordEncoder ) {
    this.passwordEncoder = passwordEncoder;
  }

}
