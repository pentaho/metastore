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

package org.pentaho.metastore.stores.xml;

import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.security.IMetaStoreElementOwner;
import org.pentaho.metastore.api.security.MetaStoreElementOwnerType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlMetaStoreElementOwner implements IMetaStoreElementOwner {

  private String name;
  private MetaStoreElementOwnerType type;

  public XmlMetaStoreElementOwner( String name, MetaStoreElementOwnerType type ) {
    super();
    this.name = name;
    this.type = type;
  }

  /**
   * Load an element owner from an XML node
   * 
   * @param elementNode
   *          The node to load the element owner data from
   * @throws MetaStoreException
   *           In case there was an error loading the data, if data was incomplete, ...
   */
  public XmlMetaStoreElementOwner( Node elementNode ) throws MetaStoreException {

    NodeList ownerNodes = elementNode.getChildNodes();
    for ( int o = 0; o < ownerNodes.getLength(); o++ ) {
      Node ownerNode = ownerNodes.item( o );
      if ( "name".equals( ownerNode.getNodeName() ) ) {
        name = XmlUtil.getNodeValue( ownerNode );
      }
      if ( "type".equals( ownerNode.getNodeName() ) ) {
        String typeString = XmlUtil.getNodeValue( ownerNode );
        try {
          type = MetaStoreElementOwnerType.getOwnerType( typeString );
        } catch ( Exception ex ) {
          throw new MetaStoreException( "Unable to convert owner type [" + typeString
              + "] to one of USER, ROLE or SYSTEM_ROLE", ex );
        }
      }
    }

    /*
     * if (name==null) { throw new
     * MetaStoreException("An owner needs to have a name in the <security><owner><name> element"); } if (type==null) {
     * throw new MetaStoreException("An owner needs to have a type in the <security><owner><type> element"); }
     */
  }

  public XmlMetaStoreElementOwner( IMetaStoreElementOwner owner ) {
    this.name = owner.getName();
    this.type = owner.getOwnerType();
  }

  public void append( Document doc, Element ownerElement ) {
    Element nameElement = doc.createElement( "name" );
    nameElement.appendChild( doc.createTextNode( name == null ? "" : name ) );
    ownerElement.appendChild( nameElement );

    Element typeElement = doc.createElement( "type" );
    typeElement.appendChild( doc.createTextNode( type == null ? "" : type.name() ) );
    ownerElement.appendChild( typeElement );
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName( String name ) {
    this.name = name;
  }

  @Override
  public MetaStoreElementOwnerType getOwnerType() {
    return type;
  }

  @Override
  public void setOwnerType( MetaStoreElementOwnerType type ) {
    this.type = type;
  }

}
