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

package org.pentaho.metastore.util;

import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.exceptions.MetaStoreNamespaceExistsException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;

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

  public static boolean getAttributeBoolean( IMetaStoreAttribute attribute, String id ) {
    String b = getChildString( attribute, id );
    if ( b == null ) {
      return false;
    }
    return "true".equalsIgnoreCase( b ) || "y".equalsIgnoreCase( b );
  }

  public static <T> T executeLockedOperation( Lock lock, Callable<T> callee ) throws MetaStoreException {
    lock.lock();
    try {
      if ( callee != null ) {
        return callee.call();
      }
    } catch ( Exception e ) {
      if ( e instanceof MetaStoreException ) {
        throw (MetaStoreException) e;
      }
      throw new MetaStoreException( e );
    } finally {
      lock.unlock();
    }
    return null;
  }

  public static <T> T executeLockedOperationQuietly( Lock lock, Callable<T> callee ) {
    T result = null;
    try {
      result = executeLockedOperation( lock, callee );
    } catch ( Exception e ) {
      // ignore
    }
    return result;
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

  public static void copy( IMetaStore from, IMetaStore to ) throws MetaStoreException {
    copy( from, to, false );
  }

  public static void copy( IMetaStore from, IMetaStore to, boolean overwrite ) throws MetaStoreException {

    // get all of the namespace in the "from" metastore
    List<String> namespaces = from.getNamespaces();
    for ( String namespace : namespaces ) {
      // create the sme namespace in the "to" metastore
      try {
        to.createNamespace( namespace );
      } catch ( MetaStoreNamespaceExistsException e ) {
        // already there
      }
      // get all of the element types defined in this namespace
      List<IMetaStoreElementType> elementTypes = from.getElementTypes( namespace );
      for ( IMetaStoreElementType elementType : elementTypes ) {
        // See if it's already there
        IMetaStoreElementType existingType = to.getElementTypeByName( namespace, elementType.getName() );
        if ( existingType != null ) {
          if ( overwrite ) {
            // we looked it up by name, but need to update it by id
            elementType.setId( existingType.getId() );
            to.updateElementType( namespace, elementType );
          } else {
            elementType = existingType;
          }
        } else {
          // create the elementType in the "to" metastore
          to.createElementType( namespace, elementType );
        }

        // get all of the elements defined for this type
        List<IMetaStoreElement> elements = from.getElements( namespace, elementType );
        for ( IMetaStoreElement element : elements ) {
          IMetaStoreElement existingElement = to.getElementByName( namespace, elementType, element.getName() );
          if ( existingElement != null ) {
            element.setId( existingElement.getId() );
            if ( overwrite ) {
              to.updateElement( namespace, elementType, existingElement.getId(), element );
            }
          } else {
            to.createElement( namespace, elementType, element );
          }
        }
      }
    }

  }
}
