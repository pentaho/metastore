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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

import javax.xml.transform.stream.StreamResult;

public class XmlMetaStoreElementType extends BaseXmlMetaStoreElementType {

  // full path
  private String filename;

  /**
   * @param namespace
   * @param id
   * @param name
   * @param description
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

    File file = new File( filename );
    this.setId( file.getParentFile().getName() );
    try ( InputStream input = new FileInputStream( filename ) ) {
      loadFromStream( filename, input );
    } catch ( IOException ex ) {
      throw new MetaStoreException( ex );
    }
    setFilename( filename );
  }

  @Override
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
          throw new MetaStoreException(
              "Unable to save XML meta store data type with file '" + filename + "' (close failed)", e );
        }
      }
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

}
