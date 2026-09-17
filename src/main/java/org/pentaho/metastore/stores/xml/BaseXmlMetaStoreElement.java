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



package org.pentaho.metastore.stores.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provides XML persistence for metastore elements.
 */
public abstract class BaseXmlMetaStoreElement extends XmlMetaStoreAttribute implements IMetaStoreElement {

  /** The XML tag for an element. */
  public static final String XML_TAG = "element";

  protected String name;

  protected IMetaStoreElementType elementType;

  protected XmlMetaStoreElementOwner owner;
  protected List<MetaStoreOwnerPermissions> ownerPermissionsList;

  protected BaseXmlMetaStoreElement() {
    super();
    this.ownerPermissionsList = new ArrayList<MetaStoreOwnerPermissions>();
  }

  protected BaseXmlMetaStoreElement( IMetaStoreElementType elementType, String id, Object value ) {
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

  /**
   * Sets the element ID from an XML file name.
   *
   * @param filename the XML file name
   */
  public void setIdWithFilename( String filename ) {
    Path filePath = Paths.get( filename ).normalize();
    id = filePath.getFileName().toString();
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
      transformerFactory.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
      transformerFactory.setAttribute( XMLConstants.ACCESS_EXTERNAL_DTD, "" );
      transformerFactory.setAttribute( XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "" );
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
        loadSecurityNode( childNode );
      }
    }
  }

  private void loadSecurityNode( Node securityNode ) throws MetaStoreException {
    NodeList securityNodes = securityNode.getChildNodes();
    for ( int s = 0; s < securityNodes.getLength(); s++ ) {
      Node childNode = securityNodes.item( s );
      if ( "owner".equals( childNode.getNodeName() ) ) {
        owner = new XmlMetaStoreElementOwner( childNode );
      } else if ( "owner-permissions-list".equals( childNode.getNodeName() ) ) {
        loadOwnerPermissions( childNode );
      }
    }
  }

  private void loadOwnerPermissions( Node ownerPermissionsListNode ) throws MetaStoreException {
    NodeList permissionNodes = ownerPermissionsListNode.getChildNodes();
    for ( int p = 0; p < permissionNodes.getLength(); p++ ) {
      Node permissionNode = permissionNodes.item( p );
      if ( "owner-permissions".equals( permissionNode.getNodeName() ) ) {
        ownerPermissionsList.add( new MetaStoreOwnerPermissions( permissionNode ) );
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

  /**
   * Sets the owner using the XML owner representation.
   *
   * @param owner the element owner
   */
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
   * Saves this element to its configured XML file.
   *
   * @throws MetaStoreException if the element cannot be saved
   */
  public abstract void save() throws MetaStoreException;

}
