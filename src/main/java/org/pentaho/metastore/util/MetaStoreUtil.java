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
 */
public class MetaStoreUtil {

  /**
    * Creates the specified namespace when it does not exist.
    *
    * @param metaStore the metastore
    * @param namespace the namespace to create
    * @throws MetaStoreException if the metastore cannot check or create the namespace
   */
  public static void verifyNamespaceCreated( IMetaStore metaStore, String namespace ) throws MetaStoreException {
    if ( !metaStore.namespaceExists( namespace ) ) {
      metaStore.createNamespace( namespace );
    }
  }

  /**
   * Gets the string value of a child attribute.
   *
   * @param attribute the parent attribute
   * @param id the child attribute ID
   * @return the child value, or {@code null} when the child does not exist
   */
  public static String getChildString( IMetaStoreAttribute attribute, String id ) {
    IMetaStoreAttribute child = attribute.getChild( id );
    if ( child == null ) {
      return null;
    }

    return getAttributeString( child );
  }

  /**
   * Gets an attribute value as a string.
   *
   * @param attribute the attribute
   * @return the attribute value, or {@code null} when it has no value
   */
  public static String getAttributeString( IMetaStoreAttribute attribute ) {
    if ( attribute.getValue() == null ) {
      return null;
    }
    return attribute.getValue().toString();
  }

  /**
   * Gets a child attribute value as a boolean.
   *
   * @param attribute the parent attribute
   * @param id the child attribute ID
   * @return {@code true} for a value of {@code true} or {@code y}; otherwise {@code false}
   */
  public static boolean getAttributeBoolean( IMetaStoreAttribute attribute, String id ) {
    String b = getChildString( attribute, id );
    if ( b == null ) {
      return false;
    }
    return "true".equalsIgnoreCase( b ) || "y".equalsIgnoreCase( b );
  }

  /**
   * Runs an operation while holding a lock.
   *
   * @param lock the lock to hold
   * @param callee the operation to run
   * @param <T> the operation result type
   * @return the operation result, or {@code null} when the operation is null
   * @throws MetaStoreException if the operation fails
   */
  public static <T> T executeLockedOperation( Lock lock, Callable<T> callee ) throws MetaStoreException {
    lock.lock();
    try {
      if ( callee != null ) {
        return callee.call();
      }
    } catch ( Exception e ) {
      if ( e instanceof MetaStoreException metaStoreException ) {
        throw metaStoreException;
      }
      throw new MetaStoreException( e );
    } finally {
      lock.unlock();
    }
    return null;
  }

  /**
   * Runs an operation while holding a lock and returns null when it fails.
   *
   * @param lock the lock to hold
   * @param callee the operation to run
   * @param <T> the operation result type
   * @return the operation result, or {@code null} when the operation fails or is null
   */
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
    * Gets sorted element names for an element type in a namespace.
    *
    * @param namespace the namespace
    * @param metaStore the metastore
    * @param elementType the element type
    * @return sorted element names
    * @throws MetaStoreException if the metastore cannot load the elements
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

  /**
   * Copies all namespaces, element types, and elements to another metastore.
   * Existing elements remain unchanged.
   *
   * @param from the source metastore
   * @param to the target metastore
   * @throws MetaStoreException if a copy operation fails
   */
  public static void copy( IMetaStore from, IMetaStore to ) throws MetaStoreException {
    copyNamespaces( from, to, MetaStoreUtil::copyElementType );
  }

  /**
   * Copies all namespaces, element types, and elements to another metastore.
   *
   * @param from the source metastore
   * @param to the target metastore
   * @param overwrite whether existing element types and elements are overwritten
   * @throws MetaStoreException if a copy operation fails
   */
  public static void copy( IMetaStore from, IMetaStore to, boolean overwrite ) throws MetaStoreException {
    if ( overwrite ) {
      copyWithOverwrite( from, to );
    } else {
      copy( from, to );
    }
  }

  /**
   * Copies all namespaces, element types, and elements to another metastore.
   * Existing elements are overwritten.
   *
   * @param from the source metastore
   * @param to the target metastore
   * @throws MetaStoreException if a copy operation fails
   */
  public static void copyWithOverwrite( IMetaStore from, IMetaStore to ) throws MetaStoreException {
    copyNamespaces( from, to, MetaStoreUtil::copyElementTypeWithOverwrite );
  }

  private static void copyNamespaces( IMetaStore from, IMetaStore to, ElementTypeCopier elementTypeCopier )
    throws MetaStoreException {

    // get all of the namespace in the "from" metastore
    List<String> namespaces = from.getNamespaces();
    for ( String namespace : namespaces ) {
      // create the sme namespace in the "to" metastore
      createNamespace( to, namespace );
      // get all of the element types defined in this namespace
      List<IMetaStoreElementType> elementTypes = from.getElementTypes( namespace );
      for ( IMetaStoreElementType elementType : elementTypes ) {
        elementTypeCopier.copy( from, to, namespace, elementType );
      }
    }
  }

  private static void createNamespace( IMetaStore metaStore, String namespace ) throws MetaStoreException {
    try {
      metaStore.createNamespace( namespace );
    } catch ( MetaStoreNamespaceExistsException e ) {
      // already there
    }
  }

  private static void copyElementType( IMetaStore from, IMetaStore to, String namespace,
                                       IMetaStoreElementType elementType ) throws MetaStoreException {
    IMetaStoreElementType targetElementType = to.getElementTypeByName( namespace, elementType.getName() );
    if ( targetElementType == null ) {
      to.createElementType( namespace, elementType );
      targetElementType = elementType;
    }

    copyElements( from, to, namespace, elementType, targetElementType );
  }

  private static void copyElementTypeWithOverwrite( IMetaStore from, IMetaStore to, String namespace,
                                                    IMetaStoreElementType elementType ) throws MetaStoreException {
    IMetaStoreElementType existingType = to.getElementTypeByName( namespace, elementType.getName() );
    if ( existingType != null ) {
      elementType.setId( existingType.getId() );
      to.updateElementType( namespace, elementType );
    } else {
      to.createElementType( namespace, elementType );
    }

    copyElementsWithOverwrite( from, to, namespace, elementType );
  }

  private static void copyElements( IMetaStore from, IMetaStore to, String namespace,
                                    IMetaStoreElementType elementType, IMetaStoreElementType targetElementType )
    throws MetaStoreException {
    List<IMetaStoreElement> elements = from.getElements( namespace, elementType );
    for ( IMetaStoreElement element : elements ) {
      IMetaStoreElement existingElement = to.getElementByName( namespace, targetElementType, element.getName() );
      if ( existingElement == null ) {
        to.createElement( namespace, targetElementType, element );
      }
    }
  }

  private static void copyElementsWithOverwrite( IMetaStore from, IMetaStore to, String namespace,
                                                 IMetaStoreElementType elementType ) throws MetaStoreException {
    List<IMetaStoreElement> elements = from.getElements( namespace, elementType );
    for ( IMetaStoreElement element : elements ) {
      IMetaStoreElement existingElement = to.getElementByName( namespace, elementType, element.getName() );
      if ( existingElement == null ) {
        to.createElement( namespace, elementType, element );
      } else {
        element.setId( existingElement.getId() );
        to.updateElement( namespace, elementType, existingElement.getId(), element );
      }
    }
  }

  @FunctionalInterface
  private interface ElementTypeCopier {
    void copy( IMetaStore from, IMetaStore to, String namespace, IMetaStoreElementType elementType )
      throws MetaStoreException;
  }
}
