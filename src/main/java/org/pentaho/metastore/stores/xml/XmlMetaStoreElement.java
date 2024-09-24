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
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.metastore.stores.xml;

import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class XmlMetaStoreElement extends BaseXmlMetaStoreElement {

  public XmlMetaStoreElement() {
    super();
  }

  public XmlMetaStoreElement( IMetaStoreElementType elementType, String id, Object value ) {
    super( elementType, id, value );
  }

  /**
   * Load element data recursively from an XML file...
   * 
   * @param filename
   *          The absolute path to the file to load the element (with children) from.
   * @throws MetaStoreException
   *           In case there is a problem reading the file.
   */
  public XmlMetaStoreElement( String filename ) throws MetaStoreException {
    this();
    setIdWithFilename( filename );

    FileInputStream in = null;

    try {
      in = new FileInputStream( filename );
      loadFromStream( in );

    } catch ( FileNotFoundException ex ) {
      throw new MetaStoreException( "Unable to load XML metastore attribute from file '" + filename + "'", ex );
    } finally {
      try {
        in.close();
      } catch ( Throwable ignored ) {
      }
    }
  }

  public XmlMetaStoreElement( IMetaStoreElement element ) {
    super( element );
  }

  @Override
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( !( obj instanceof XmlMetaStoreElement ) ) {
      return false;
    }
    return ( (XmlMetaStoreElement) obj ).id.equals( id );
  }

  @Override
  public void save() throws MetaStoreException {
    FileOutputStream out = null;

    try {
      out = new FileOutputStream( filename );

      save( out );
    } catch ( FileNotFoundException ex ) {
      throw new MetaStoreException( "The Annotation Group name is too long. Please try something shorter.", ex );
    } finally {
      try {
        out.close();
      } catch ( Throwable ignored ) {
      }
    }
  }
}
