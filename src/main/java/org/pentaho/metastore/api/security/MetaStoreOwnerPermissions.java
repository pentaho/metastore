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


package org.pentaho.metastore.api.security;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.stores.xml.XmlMetaStoreElementOwner;
import org.pentaho.metastore.stores.xml.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MetaStoreOwnerPermissions {

  private IMetaStoreElementOwner owner;
  private List<MetaStoreObjectPermission> permissions;

  public MetaStoreOwnerPermissions() {
    this( (IMetaStoreElementOwner) null );
  }

  public MetaStoreOwnerPermissions( IMetaStoreElementOwner owner ) {
    this( owner, new ArrayList<MetaStoreObjectPermission>() );
  }

  public MetaStoreOwnerPermissions( IMetaStoreElementOwner owner, MetaStoreObjectPermission... permissions ) {
    super();
    this.permissions = new ArrayList<MetaStoreObjectPermission>();
    if ( owner != null ) {
      this.owner = new XmlMetaStoreElementOwner( owner );
    }
    for ( MetaStoreObjectPermission permission : permissions ) {
      this.permissions.add( permission );
    }
  }

  public MetaStoreOwnerPermissions( IMetaStoreElementOwner owner, List<MetaStoreObjectPermission> permissions ) {
    super();
    this.permissions = new ArrayList<MetaStoreObjectPermission>();
    if ( owner != null ) {
      this.owner = new XmlMetaStoreElementOwner( owner );
    }
    for ( MetaStoreObjectPermission permission : permissions ) {
      this.permissions.add( permission );
    }
  }

  public MetaStoreOwnerPermissions( Node opNode ) throws MetaStoreException {
    this();
    NodeList childNodes = opNode.getChildNodes();
    for ( int c = 0; c < childNodes.getLength(); c++ ) {
      Node childNode = childNodes.item( c );
      if ( "owner".equals( childNode.getNodeName() ) ) {
        owner = new XmlMetaStoreElementOwner( childNode );
        if ( owner.getName() == null || owner.getOwnerType() == null ) {
          owner = null;
        }
      }
      if ( "permissions".equals( childNode.getNodeName() ) ) {
        NodeList pNodes = childNode.getChildNodes();
        for ( int p = 0; p < pNodes.getLength(); p++ ) {
          Node pNode = pNodes.item( p );
          if ( "permission".equals( pNode.getNodeName() ) ) {
            String permissionString = XmlUtil.getNodeValue( pNode );
            try {
              permissions.add( MetaStoreObjectPermission.valueOf( permissionString ) );
            } catch ( Exception e ) {
              throw new MetaStoreException( "Unable to recognize permission '" + permissionString
                  + "' as one of CREATE, READ, UPDATE or DELETE", e );
            }
          }
        }
      }
    }
  }

  public void append( Document doc, Element element ) {
    Element ownerElement = doc.createElement( "owner" );
    if ( owner != null ) {
      ( (XmlMetaStoreElementOwner) owner ).append( doc, ownerElement );
    }
    element.appendChild( ownerElement );

    Element permissionsElement = doc.createElement( "permissions" );
    for ( MetaStoreObjectPermission permission : permissions ) {
      Element permissionElement = doc.createElement( "permission" );
      permissionElement.appendChild( doc.createTextNode( permission.name() ) );
      permissionsElement.appendChild( permissionElement );
    }
    element.appendChild( permissionsElement );
  }

  public IMetaStoreElementOwner getOwner() {
    return owner;
  }

  public List<MetaStoreObjectPermission> getPermissions() {
    return permissions;
  }

  public void setOwner( IMetaStoreElementOwner owner ) {
    this.owner = owner;
  }

  public void setPermissions( List<MetaStoreObjectPermission> permissions ) {
    this.permissions = permissions;
  }

}
