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

import org.pentaho.metastore.api.BaseElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import java.io.InputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public abstract class BaseXmlMetaStoreElementType extends BaseElementType {

  public static final String XML_TAG = "data-type";

  private String filename;

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

  protected void loadFromStream( String filename, InputStream input ) throws MetaStoreException {
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
