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

import java.util.ArrayList;
import java.util.List;

import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.stores.xml.XmlMetaStoreElementOwner;
import org.pentaho.metastore.stores.xml.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Associates a metastore element owner with object permissions.
 */
public class MetaStoreOwnerPermissions {

  private IMetaStoreElementOwner owner;
  private List<MetaStoreObjectPermission> permissions;

  /**
   * Creates an empty permissions object.
   */
  public MetaStoreOwnerPermissions() {
    this( (IMetaStoreElementOwner) null );
  }

  /**
   * Creates a permissions object for an owner without permissions.
   *
   * @param owner the element owner
   */
  public MetaStoreOwnerPermissions( IMetaStoreElementOwner owner ) {
    this( owner, new ArrayList<MetaStoreObjectPermission>() );
  }

  /**
   * Creates a permissions object for an owner.
   *
   * @param owner the element owner
   * @param permissions the permissions to assign
   */
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

  /**
   * Creates a permissions object for an owner.
   *
   * @param owner the element owner
   * @param permissions the permissions to assign
   */
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

  /**
   * Loads permissions from an XML node.
   *
   * @param opNode the XML node that contains owner and permission data
   * @throws MetaStoreException if a permission value is not recognized
   */
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

  /**
   * Appends this owner and its permissions to an XML element.
   *
   * @param doc the XML document that creates the child nodes
   * @param element the XML element that receives the owner and permissions
   */
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

  /**
   * Gets the owner.
   *
   * @return the owner, or {@code null} when no owner exists
   */
  public IMetaStoreElementOwner getOwner() {
    return owner;
  }

  /**
   * Gets the assigned permissions.
   *
   * @return the assigned permissions
   */
  public List<MetaStoreObjectPermission> getPermissions() {
    return permissions;
  }

  /**
   * Sets the owner.
   *
   * @param owner the element owner
   */
  public void setOwner( IMetaStoreElementOwner owner ) {
    this.owner = owner;
  }

  /**
   * Sets the assigned permissions.
   *
   * @param permissions the permissions to assign
   */
  public void setPermissions( List<MetaStoreObjectPermission> permissions ) {
    this.permissions = permissions;
  }

}
