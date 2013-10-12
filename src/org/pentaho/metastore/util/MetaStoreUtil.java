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

package org.pentaho.metastore.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

/**
 * Generally useful methods for extracting data
 * 
 * @author matt
 * 
 */
public class MetaStoreUtil {

  /**
   * Create the specified namespace if it doesn't exist.
   * 
   * @param metaStore
   * @param namespace
   * @throws MetaStoreException
   */
  public static void verifyNamespaceCreated( IMetaStore metaStore, String namespace ) throws MetaStoreException {
    if ( !metaStore.namespaceExists( namespace ) ) {
      metaStore.createNamespace( namespace );
    }
  }

  public static String getChildString( IMetaStoreAttribute attribute, String id ) {
    IMetaStoreAttribute child = attribute.getChild( id );
    if ( child == null ) {
      return null;
    }

    return getAttributeString( child );
  }

  public static String getAttributeString( IMetaStoreAttribute attribute ) {
    if ( attribute.getValue() == null ) {
      return null;
    }
    return attribute.getValue().toString();
  }

  /**
   * Get a sorted list of element names for the specified element type in the given namespace.
   * 
   * @param namespace
   * @param metaStore
   * @param elementType
   * @return
   * @throws MetaStoreException
   */
  public String[] getElementNames( String namespace, IMetaStore metaStore, IMetaStoreElementType elementType )
    throws MetaStoreException {
    List<String> names = new ArrayList<String>();

    List<IMetaStoreElement> elements = metaStore.getElements( namespace, elementType );
    for ( IMetaStoreElement element : elements ) {
      names.add( element.getName() );
    }

    // Alphabetical sort
    //
    Collections.sort( names );

    return names.toArray( new String[names.size()] );
  }
}
