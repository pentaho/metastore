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


package org.pentaho.metastore.stores.memory;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.security.IMetaStoreElementOwner;
import org.pentaho.metastore.api.security.MetaStoreOwnerPermissions;
import org.pentaho.metastore.stores.xml.XmlMetaStoreElementOwner;

public class MemoryMetaStoreElement extends MemoryMetaStoreAttribute implements IMetaStoreElement {

  protected String name;

  protected IMetaStoreElementType elementType;

  protected IMetaStoreElementOwner owner;
  protected List<MetaStoreOwnerPermissions> ownerPermissionsList;

  public MemoryMetaStoreElement() {
    this( null, null, null );
  }

  public MemoryMetaStoreElement( IMetaStoreElementType elementType, String id, Object value ) {
    super( id, value );
    this.elementType = elementType;

    this.ownerPermissionsList = new ArrayList<MetaStoreOwnerPermissions>();
  }

  public MemoryMetaStoreElement( IMetaStoreElement element ) {
    super( element );
    this.name = element.getName();
    this.elementType = element.getElementType();
    this.ownerPermissionsList = new ArrayList<MetaStoreOwnerPermissions>();
    if ( element.getOwner() != null ) {
      this.owner = new XmlMetaStoreElementOwner( element.getOwner() );
    }
    for ( MetaStoreOwnerPermissions ownerPermissions : element.getOwnerPermissionsList() ) {
      this.getOwnerPermissionsList().add(
          new MetaStoreOwnerPermissions( ownerPermissions.getOwner(), ownerPermissions.getPermissions() ) );
    }
  }

  public IMetaStoreElementOwner getOwner() {
    return owner;
  }

  public void setOwner( IMetaStoreElementOwner owner ) {
    this.owner = owner;
  }

  public List<MetaStoreOwnerPermissions> getOwnerPermissionsList() {
    return ownerPermissionsList;
  }

  public void setOwnerPermissionsList( List<MetaStoreOwnerPermissions> ownerPermissions ) {
    this.ownerPermissionsList = ownerPermissions;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public IMetaStoreElementType getElementType() {
    return elementType;
  }

  public void setElementType( IMetaStoreElementType elementType ) {
    this.elementType = elementType;
  }
}
