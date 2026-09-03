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


import org.pentaho.metastore.api.BaseElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import java.io.InputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

/**
 * Provides XML persistence for metastore element types.
 */
public abstract class BaseXmlMetaStoreElementType extends BaseElementType {

  /** The XML tag for an element type. */
  public static final String XML_TAG = "data-type";

  /**
   * @param namespace
   * @param id
   * @param name
   * @param description
   */
  protected BaseXmlMetaStoreElementType( String namespace, String id, String name, String description ) {
    super( namespace );
    setId( id );
    setName( name );
    setDescription( description );
  }

  /**
   * Initialize with separate call to loadFromStream
   * 
   * @param namespace
   *          the namespace
   */
  protected BaseXmlMetaStoreElementType( String namespace ) throws MetaStoreException {
    super( namespace );

  }

  // filename is used only for the error message and normalized display; the caller already opened the stream.
  protected void loadFromStream( String filename, InputStream input ) throws MetaStoreException {
    Path path;
    try {
      path = Paths.get( filename ).normalize();
    } catch ( InvalidPathException e ) {
      throw new MetaStoreException( "Invalid file path '" + filename + "'", e );
    }
    loadFromPath( path, input );
  }

  void loadFromPath( Path filename, InputStream input ) throws MetaStoreException {
    try {
      DocumentBuilderFactory documentBuilderFactory = XmlUtil.createSafeDocumentBuilderFactory();
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      Document document = documentBuilder.parse( input );
      Element elementTypeElement = document.getDocumentElement();

      loadElementType( elementTypeElement );
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to load XML metastore element type from file '" + filename + "'", e );
    }
  }

  protected void loadElementType( Node elementTypeNode ) {
    NodeList childNodes = elementTypeNode.getChildNodes();
    for ( int e = 0; e < childNodes.getLength(); e++ ) {
      Node childNode = childNodes.item( e );
      if ( "name".equals( childNode.getNodeName() ) ) {
        setName( XmlUtil.getNodeValue( childNode ) );
      }
      if ( "description".equals( childNode.getNodeName() ) ) {
        setDescription( XmlUtil.getNodeValue( childNode ) );
      }
    }
  }

  /**
   * Gets the XML representation of this element type.
   *
   * @return the XML representation
   * @throws MetaStoreException if the XML cannot be created
   */
  public String getXml() throws MetaStoreException {
    try {
      StringWriter stringWriter = new StringWriter();
      StreamResult result = new StreamResult( stringWriter );
      saveToStreamResult( result );

      return result.toString();
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to get XML form of meta store.", e );
    }
  }

  /**
   * Saves this element type to a transformation stream result.
   *
   * @param streamResult the output stream result
   * @throws MetaStoreException if the XML cannot be written
   */
  public void saveToStreamResult( StreamResult streamResult ) throws MetaStoreException {
    try {
      DocumentBuilderFactory factory = XmlUtil.createSafeDocumentBuilderFactory();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.newDocument();

      Element elementTypeElement = doc.createElement( XML_TAG );
      doc.appendChild( elementTypeElement );

      appendElementType( doc, elementTypeElement );

      // Write the document content into the data type XML file
      //
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformerFactory.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
      transformerFactory.setAttribute( XMLConstants.ACCESS_EXTERNAL_DTD, "" );
      transformerFactory.setAttribute( XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "" );
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2" );
      transformer.setOutputProperty( OutputKeys.INDENT, "yes" );

      DOMSource source = new DOMSource( doc );

      // Do the actual saving...
      transformer.transform( source, streamResult );
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to serialize XML meta store to stream result", e );
    }
  }

  protected void appendElementType( Document doc, Element elementTypeElement ) {
    Element nameElement = doc.createElement( "name" );
    nameElement.appendChild( doc.createTextNode( getName() ) );
    elementTypeElement.appendChild( nameElement );

    Element descriptionElement = doc.createElement( "description" );
    descriptionElement.appendChild( doc.createTextNode( getDescription() ) );
    elementTypeElement.appendChild( descriptionElement );

  }

}
