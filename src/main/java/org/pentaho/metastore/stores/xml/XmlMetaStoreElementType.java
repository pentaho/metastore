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

import org.pentaho.metastore.api.exceptions.MetaStoreException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.transform.stream.StreamResult;

/**
 * Stores a metastore element type in an XML file.
 */
public class XmlMetaStoreElementType extends BaseXmlMetaStoreElementType {

  // full path
  private String filename;

  /**
   * Creates an XML element type.
   *
   * @param namespace the element type namespace
   * @param id the element type ID
   * @param name the element type name
   * @param description the element type description
   */
  public XmlMetaStoreElementType( String namespace, String id, String name, String description ) {
    super( namespace, id, name, description );
  }

  /**
   * Load an XML meta data store data type from file.
   * 
   * @param namespace
   *          the namespace
   * @param filename
   *          the full path of the file to load from
   */
  public XmlMetaStoreElementType( String namespace, String filename ) throws MetaStoreException {
    super( namespace );

    Path filePath = Paths.get( filename ).normalize();
    this.setId( filePath.getParent().getFileName().toString() );
    try ( InputStream input = Files.newInputStream( filePath ) ) {
      loadFromPath( filePath, input );
    } catch ( IOException ex ) {
      throw new MetaStoreException( ex );
    }
    setFilename( filename );
  }

  /**
   * Saves this element type to its configured XML file.
   *
   * @throws MetaStoreException if the element type cannot be saved
   */
  @Override
  public void save() throws MetaStoreException {
    try ( OutputStream output = Files.newOutputStream( getFilenamePath() ) ) {
      StreamResult result = new StreamResult( output );
      saveToStreamResult( result );
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to save XML meta store data type with file '" + filename + "'", e );
    }
  }

  /**
   * @return the filename, which is the full path to the type file
   */
  public String getFilename() {
    return filename;
  }

  /**
   * @param filename
   *          the full path to the type file.
   */
  public void setFilename( String filename ) {
    this.filename = filename;
  }

  private Path getFilenamePath() {
    return filename == null ? null : Paths.get( filename ).normalize();
  }

}
