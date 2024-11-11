/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.metastore.api;

/**
 * @author nhudak
 */
public abstract class BaseElementType implements IMetaStoreElementType {
  private String namespace;
  private String id;
  private String name;
  private String description;
  private String metaStoreName;

  public BaseElementType( String namespace ) {
    this.namespace = namespace;
  }

  public void copyFrom( IMetaStoreElementType elementType ) {
    this.id = elementType.getId();
    this.name = elementType.getName();
    this.description = elementType.getDescription();
    this.metaStoreName = elementType.getMetaStoreName();
  }

  /**
   * @return the namespace
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * @param namespace the namespace to set
   */
  public void setNamespace( String namespace ) {
    this.namespace = namespace;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId( String id ) {
    this.id = id;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName( String name ) {
    this.name = name;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription( String description ) {
    this.description = description;
  }

  public String getMetaStoreName() {
    return metaStoreName;
  }

  public void setMetaStoreName( String metaStoreName ) {
    this.metaStoreName = metaStoreName;
  }
}
