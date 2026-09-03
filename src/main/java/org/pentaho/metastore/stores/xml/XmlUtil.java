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

import java.nio.file.Path;
import java.nio.file.Paths;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Provides XML metastore path and parser utility methods.
 */
public class XmlUtil {

  /** The folder that contains metastore data. */
  public static final String META_FOLDER_NAME = "metastore";
  /** The file name that stores an element type definition. */
  public static final String ELEMENT_TYPE_FILE_NAME = ".type.xml";

  private XmlUtil() {
    // hidden
  }

  /**
   * Gets the text value of the first text child of a node.
   *
   * @param node the XML node
   * @return the text value, or {@code null} when the node has no text child
   */
  public static String getNodeValue( Node node ) {
    if ( node == null ) {
      return null;
    }

    NodeList children = node.getChildNodes();
    for ( int i = 0; i < children.getLength(); i++ ) {
      Node child = children.item( i );
      if ( child.getNodeType() == Node.TEXT_NODE ) {
        return child.getNodeValue();
      }
    }
    return null;
  }

  /**
   * Gets the namespace folder path.
   *
   * @param rootFolder the metastore root folder
   * @param namespace the namespace
   * @return the namespace folder path
   */
  public static String getNamespaceFolder( String rootFolder, String namespace ) {
    return getNamespaceFolderPath( Paths.get( rootFolder ), namespace ).toString();
  }

  /**
   * Gets the element type folder path.
   *
   * @param rootFolder the metastore root folder
   * @param namespace the namespace
   * @param elementTypeId the element type ID
   * @return the element type folder path
   */
  public static String getElementTypeFolder( String rootFolder, String namespace, String elementTypeId ) {
    return getElementTypeFolderPath( Paths.get( rootFolder ), namespace, elementTypeId ).toString();
  }

  /**
   * Gets the element type definition file path.
   *
   * @param rootFolder the metastore root folder
   * @param namespace the namespace
   * @param elementTypeId the element type ID
   * @return the element type definition file path
   */
  public static String getElementTypeFile( String rootFolder, String namespace, String elementTypeId ) {
    return getElementTypeFilePath( Paths.get( rootFolder ), namespace, elementTypeId ).toString();
  }

  /**
   * Gets an element file path.
   *
   * @param rootFolder the metastore root folder
   * @param namespace the namespace
   * @param elementTypeId the element type ID
   * @param elementId the element ID
   * @return the element file path
   */
  public static String getElementFile( String rootFolder, String namespace, String elementTypeId, String elementId ) {
    return getElementFilePath( Paths.get( rootFolder ), namespace, elementTypeId, elementId ).toString();
  }

  static Path getNamespaceFolderPath( Path rootFolder, String namespace ) {
    return resolvePath( rootFolder, namespace );
  }

  static Path getElementTypeFolderPath( Path rootFolder, String namespace, String elementTypeId ) {
    return resolvePath( rootFolder, namespace, elementTypeId );
  }

  static Path getElementTypeFilePath( Path rootFolder, String namespace, String elementTypeId ) {
    return resolvePath( rootFolder, namespace, elementTypeId, ELEMENT_TYPE_FILE_NAME );
  }

  static Path getElementFilePath( Path rootFolder, String namespace, String elementTypeId, String elementId ) {
    return resolvePath( rootFolder, namespace, elementTypeId, elementId + ".xml" );
  }

  private static Path resolvePath( Path rootFolder, String... pathSegments ) {
    Path normalizedRoot = rootFolder.normalize();
    Path resolvedPath = normalizedRoot;
    Path absoluteRoot = normalizedRoot.toAbsolutePath().normalize();
    for ( String pathSegment : pathSegments ) {
      Path segmentPath = Paths.get( pathSegment );
      if ( segmentPath.isAbsolute() ) {
        throw new IllegalArgumentException( "Path segment must be relative: " + pathSegment );
      }
      resolvedPath = resolvedPath.resolve( segmentPath ).normalize();
      if ( !resolvedPath.toAbsolutePath().normalize().startsWith( absoluteRoot ) ) {
        throw new IllegalArgumentException( "Path segment escapes root folder: " + pathSegment );
      }
    }
    return resolvedPath;
  }

  /**
   * Creates a secure XML document builder factory.
   *
   * @return the configured document builder factory
   * @throws ParserConfigurationException if the XML parser does not support a required feature
   */
  public static DocumentBuilderFactory createSafeDocumentBuilderFactory() throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
    factory.setFeature( "http://apache.org/xml/features/disallow-doctype-decl", true );
    return factory;
  }
}
