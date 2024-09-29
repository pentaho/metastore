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


package org.pentaho.metastore.api.security;

public interface IMetaStoreElementOwner {

  public String getName();

  public void setName( String name );

  public MetaStoreElementOwnerType getOwnerType();

  public void setOwnerType( MetaStoreElementOwnerType type );

}
