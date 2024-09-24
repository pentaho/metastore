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
