/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2023 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.metastore.stores.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.security.IMetaStoreElementOwner;
import org.pentaho.metastore.api.security.MetaStoreOwnerPermissions;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class BaseXmlMetaStoreElement extends XmlMetaStoreAttribute implements IMetaStoreElement {

  public static final String XML_TAG = "element";

  protected String name;

  protected IMetaStoreElementType elementType;

  protected XmlMetaStoreElementOwner owner;
  protected List<MetaStoreOwnerPermissions> ownerPermissionsList;

  public BaseXmlMetaStoreElement() {
    super();
    this.ownerPermissionsList = new ArrayList<MetaStoreOwnerPermissions>();
  }

  public BaseXmlMetaStoreElement( IMetaStoreElementType elementType, String id, Object value ) {
    super( id, value );
    this.elementType = elementType;
    this.ownerPermissionsList = new ArrayList<MetaStoreOwnerPermissions>();
  }

  /**
   * Load element data recursively from an XML file...
   * 
   * @param in
   *          The stream to load the element (with children) from.
   * @throws MetaStoreException
   *           In case there is a problem reading the file.
   */
  protected void loadFromStream( InputStream in ) throws MetaStoreException {
    try {
      DocumentBuilderFactory documentBuilderFactory = XmlUtil.createSafeDocumentBuilderFactory();
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      Document document = documentBuilder.parse( in );
      Element dataTypeElement = document.getDocumentElement();

      loadElement( dataTypeElement );
      loadAttribute( dataTypeElement );
      loadSecurity( dataTypeElement );
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to load XML metastore attribute from file '" + filename + "'", e );
    }
  }

  public void setIdWithFilename( String filename ) {
    File file = new File( filename );
    id = file.getName();
    id = id.substring( 0, id.length() - 4 );
  }

  protected void loadElement( Node elementNode ) {
    NodeList childNodes = elementNode.getChildNodes();
    for ( int e = 0; e < childNodes.getLength(); e++ ) {
      Node childNode = childNodes.item( e );
      if ( "name".equals( childNode.getNodeName() ) ) {
        name = XmlUtil.getNodeValue( childNode );
      }
    }
  }

  protected void save( OutputStream out ) throws MetaStoreException {

    try {
      DocumentBuilderFactory factory = XmlUtil.createSafeDocumentBuilderFactory();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.newDocument();

      Element element = doc.createElement( XML_TAG );
      doc.appendChild( element );

      appendAttribute( this, doc, element );
      appendElement( this, doc, element );
      appendSecurity( doc, element );

      // Write the document content into the data type XML file
      //
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
      transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2" );
      DOMSource source = new DOMSource( doc );
      StreamResult result = new StreamResult( out );

      // Do the actual saving...
      transformer.transform( source, result );
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to save XML meta store element to file '" + filename + "'", e );
    }
  }

  protected void appendElement( IMetaStoreElement element, Document doc, Element parentElement ) {
    Element nameElement = doc.createElement( "name" );
    if ( element.getName() != null ) {
      nameElement.appendChild( doc.createTextNode( element.getName() == null ? "" : element.getName() ) );
    }
    parentElement.appendChild( nameElement );
  }

  protected void appendSecurity( Document doc, Element parentElement ) {
    // <security>
    //
    Element securityElement = doc.createElement( "security" );
    parentElement.appendChild( securityElement );

    // <security><owner>
    //
    Element ownerElement = doc.createElement( "owner" );
    securityElement.appendChild( ownerElement );
    if ( owner != null ) {
      // <security><owner><name/><type/>
      //
      owner.append( doc, ownerElement );
    }

    // <security><owner-permissions-list>
    //
    Element oplElement = doc.createElement( "owner-permissions-list" );
    securityElement.appendChild( oplElement );
    for ( MetaStoreOwnerPermissions ownerPermissions : ownerPermissionsList ) {
      // <security><owner-permissions-list><owner-permissions>
      //
      Element opElement = doc.createElement( "owner-permissions" );
      oplElement.appendChild( opElement );
      ownerPermissions.append( doc, opElement );
    }
  }

  protected void loadSecurity( Node elementNode ) throws MetaStoreException {
    NodeList childNodes = elementNode.getChildNodes();
    for ( int c = 0; c < childNodes.getLength(); c++ ) {
      Node childNode = childNodes.item( c );
      if ( "security".equals( childNode.getNodeName() ) ) {
        NodeList securityNodes = childNode.getChildNodes();
        for ( int s = 0; s < securityNodes.getLength(); s++ ) {
          Node securityNode = securityNodes.item( s );

          if ( "owner".equals( securityNode.getNodeName() ) ) {
            // Load security details...
            //
            owner = new XmlMetaStoreElementOwner( securityNode );
          }
          if ( "owner-permissions-list".equals( securityNode.getNodeName() ) ) {
            NodeList opNodes = securityNode.getChildNodes();
            for ( int op = 0; op < opNodes.getLength(); op++ ) {
              Node opNode = opNodes.item( op );
              if ( "owner-permissions".equals( opNode.getNodeName() ) ) {
                MetaStoreOwnerPermissions ownerPermissions = new MetaStoreOwnerPermissions( opNode );
                ownerPermissionsList.add( ownerPermissions );
              }
            }
          }
        }
      }
    }
  }

  /**
   * Duplicate the element data into this structure.
   * 
   * @param element
   */
  protected BaseXmlMetaStoreElement( IMetaStoreElement element ) {
    super( element );
    this.name = element.getName();
    this.ownerPermissionsList = new ArrayList<MetaStoreOwnerPermissions>();
    if ( element.getOwner() != null ) {
      this.owner = new XmlMetaStoreElementOwner( element.getOwner() );
    }
    for ( MetaStoreOwnerPermissions ownerPermissions : element.getOwnerPermissionsList() ) {
      this.getOwnerPermissionsList()
          .add( new MetaStoreOwnerPermissions( ownerPermissions.getOwner(), ownerPermissions.getPermissions() ) );
    }
  }

  @Override
  public IMetaStoreElementOwner getOwner() {
    return owner;
  }

  @Override
  public void setOwner( IMetaStoreElementOwner owner ) {
    // Copy the data first, could come from other storage worlds
    //
    this.owner = new XmlMetaStoreElementOwner( owner );
  }

  @Override
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

  /**
   * This method is only called on object instances that have been constructed from another element, via
   * BaseXmlMetaStore.newElement( IMetaStoreElement )
   *
   */
  public abstract void save() throws MetaStoreException;

}
