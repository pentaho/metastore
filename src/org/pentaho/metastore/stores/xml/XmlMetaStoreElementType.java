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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.metastore.stores.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlMetaStoreElementType implements IMetaStoreElementType {

  public static final String XML_TAG = "data-type";

  private String namespace;
  private String id;
  private String name;
  private String description;
  private String metaStoreName;

  private String filename;

  /**
   * @param namespace
   * @param id
   * @param name
   * @param description
   */
  public XmlMetaStoreElementType( String namespace, String id, String name, String description ) {
    this.namespace = namespace;
    this.id = id;
    this.name = name;
    this.description = description;
  }

  /**
   * Load an XML meta data store data type from file.
   * 
   * @param namespace
   *          the namespace
   * @param filename
   *          the file to load from
   */
  public XmlMetaStoreElementType( String namespace, String filename ) throws MetaStoreException {
    this.namespace = namespace;

    File file = new File( filename );
    this.id = file.getParentFile().getName();

    try {

      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      Document document = documentBuilder.parse( file );
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
        name = XmlUtil.getNodeValue( childNode );
      }
      if ( "description".equals( childNode.getNodeName() ) ) {
        description = XmlUtil.getNodeValue( childNode );
      }
    }
  }

  public void save() throws MetaStoreException {
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream( filename );
      StreamResult result = new StreamResult( fos );
      saveToStreamResult( result );
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to save XML meta store data type with file '" + filename + "'", e );
    } finally {
      if ( fos != null ) {
        try {
          fos.close();
        } catch ( Exception e ) {
          throw new MetaStoreException( "Unable to save XML meta store data type with file '" + filename
              + "' (close failed)", e );
        }
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

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
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
    nameElement.appendChild( doc.createTextNode( name ) );
    elementTypeElement.appendChild( nameElement );

    Element descriptionElement = doc.createElement( "description" );
    descriptionElement.appendChild( doc.createTextNode( description ) );
    elementTypeElement.appendChild( descriptionElement );

  }

  /**
   * @return the namespace
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * @param namespace
   *          the namespace to set
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
   * @param id
   *          the id to set
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
   * @param name
   *          the name to set
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
   * @param description
   *          the description to set
   */
  public void setDescription( String description ) {
    this.description = description;
  }

  /**
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }

  /**
   * @param filename
   *          the filename to set
   */
  public void setFilename( String filename ) {
    this.filename = filename;
  }

  public String getMetaStoreName() {
    return metaStoreName;
  }

  public void setMetaStoreName( String metaStoreName ) {
    this.metaStoreName = metaStoreName;
  }

}
