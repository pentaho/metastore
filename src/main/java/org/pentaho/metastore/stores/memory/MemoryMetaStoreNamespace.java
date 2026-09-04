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



package org.pentaho.metastore.stores.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreDependenciesExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreElementTypeExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.util.MetaStoreUtil;

/**
 * Stores the element types for one memory metastore namespace.
 */
public class MemoryMetaStoreNamespace {

  private final ReadLock readLock;
  private final WriteLock writeLock;

  private final String namespace;
  private final Map<String, MemoryMetaStoreElementType> typeMap;

  /**
   * Creates an empty namespace.
   *
   * @param namespace the namespace name
   */
  public MemoryMetaStoreNamespace( String namespace ) {
    this.namespace = namespace;
    this.typeMap = new HashMap<String, MemoryMetaStoreElementType>();

    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    readLock = lock.readLock();
    writeLock = lock.writeLock();

  }

  /**
   * Gets the namespace name.
   *
   * @return the namespace name
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * Gets a copy of the element type map.
   *
   * @return a map of element type IDs to element types
   */
  public Map<String, MemoryMetaStoreElementType> getTypeMap() {
    return MetaStoreUtil.executeLockedOperationQuietly( readLock,
        new Callable<Map<String, MemoryMetaStoreElementType>>() {

          @Override
          public Map<String, MemoryMetaStoreElementType> call() throws Exception {
            return new HashMap<String, MemoryMetaStoreElementType>( typeMap );
          }
        } );
  }

  private MemoryMetaStoreElementType getElementTypeByNameInternal( String elementTypeName ) {
    for ( MemoryMetaStoreElementType elementType : typeMap.values() ) {
      if ( elementType.getName().equalsIgnoreCase( elementTypeName ) ) {
        return elementType;
      }
    }
    return null;
  }

  /**
   * Gets an element type by name without case sensitivity.
   *
   * @param elementTypeName the element type name
   * @return the element type, or {@code null} when it does not exist
   */
  public MemoryMetaStoreElementType getElementTypeByName( final String elementTypeName ) {
    return MetaStoreUtil.executeLockedOperationQuietly( readLock, new Callable<MemoryMetaStoreElementType>() {

      @Override
      public MemoryMetaStoreElementType call() throws Exception {
        return getElementTypeByNameInternal( elementTypeName );
      }
    } );
  }

  /**
   * Gets all element type IDs.
   *
   * @return the element type IDs
   */
  public List<String> getElementTypeIds() {
    return MetaStoreUtil.executeLockedOperationQuietly( readLock, new Callable<List<String>>() {

      @Override
      public List<String> call() throws Exception {
        ArrayList<String> list = new ArrayList<String>( typeMap.size() );
        for ( MemoryMetaStoreElementType elementType : typeMap.values() ) {
          list.add( elementType.getId() );
        }
        return list;
      }
    } );
  }

  /**
   * Creates an element type in this namespace.
   *
   * @param metaStoreName the owning metastore name
   * @param elementType the element type to create
   * @throws MetaStoreElementTypeExistsException if the element type ID already exists
   */
  public void createElementType( final String metaStoreName, final IMetaStoreElementType elementType )
    throws MetaStoreElementTypeExistsException {
    // For the memory store, the ID is the same as the name if empty
    if ( elementType.getId() == null ) {
      elementType.setId( elementType.getName() );
    }
    try {
      MetaStoreUtil.executeLockedOperation( writeLock, new Callable<Void>() {

        @Override
        public Void call() throws Exception {
          MemoryMetaStoreElementType verifyType = typeMap.get( elementType.getId() );
          if ( verifyType != null ) {
            throw new MetaStoreElementTypeExistsException( new ArrayList<IMetaStoreElementType>( typeMap.values() ),
                "Element type with ID '" + elementType.getId() + "' already exists" );
          } else {
            MemoryMetaStoreElementType copiedType = new MemoryMetaStoreElementType( elementType );
            typeMap.put( elementType.getId(), copiedType );
            copiedType.setMetaStoreName( metaStoreName );
            elementType.setMetaStoreName( metaStoreName );
          }
          return null;
        }
      } );
    } catch ( MetaStoreException e ) {
      if ( e instanceof MetaStoreElementTypeExistsException ) {
        throw (MetaStoreElementTypeExistsException) e;
      } else {
        throw new RuntimeException( e );
      }
    }
  }

  /**
   * Updates an element type in this namespace.
   *
   * @param metaStoreName the owning metastore name
   * @param elementType the replacement element type
   * @throws MetaStoreElementTypeExistsException if the element type ID does not exist
   */
  public void updateElementType( final String metaStoreName, final IMetaStoreElementType elementType )
    throws MetaStoreElementTypeExistsException {
    try {
      MetaStoreUtil.executeLockedOperation( writeLock, new Callable<Void>() {

        @Override
        public Void call() throws Exception {
          MemoryMetaStoreElementType verifyType = typeMap.get( elementType.getId() );
          if ( verifyType == null ) {
            throw new MetaStoreElementTypeExistsException( new ArrayList<IMetaStoreElementType>( typeMap.values() ),
                "Element type to update, with ID '" + elementType.getId() + "', does not exist" );
          } else {
            MemoryMetaStoreElementType copiedType = new MemoryMetaStoreElementType( elementType );
            typeMap.put( elementType.getId(), copiedType );
            copiedType.setMetaStoreName( metaStoreName );
            elementType.setMetaStoreName( metaStoreName );
          }
          return null;
        }
      } );
    } catch ( MetaStoreException e ) {
      if ( e instanceof MetaStoreElementTypeExistsException ) {
        throw (MetaStoreElementTypeExistsException) e;
      } else {
        throw new RuntimeException( e );
      }
    }
  }

  /**
   * Removes an empty element type from this namespace.
   *
   * @param elementType the element type to remove
   * @throws MetaStoreElementTypeExistsException if the element type does not exist
   * @throws MetaStoreDependenciesExistsException if the element type contains elements
   */
  public void deleteElementType( final IMetaStoreElementType elementType ) throws MetaStoreElementTypeExistsException,
    MetaStoreDependenciesExistsException {
    try {
      MetaStoreUtil.executeLockedOperation( writeLock, new Callable<Void>() {

        @Override
        public Void call() throws Exception {
          final MemoryMetaStoreElementType verifyType = typeMap.get( elementType.getId() );
          if ( verifyType == null ) {
            throw new MetaStoreElementTypeExistsException( new ArrayList<IMetaStoreElementType>( typeMap.values() ),
                "Element type to delete, with ID '" + elementType.getId() + "', does not exist" );
          } else {
            // See if there are elements in there...
            //

            MetaStoreUtil.executeLockedOperation( verifyType.getReadLock(), new Callable<Void>() {

              @Override
              public Void call() throws Exception {
                if ( !verifyType.isElementMapEmpty() ) {
                  MemoryMetaStoreElementType foundElementType = getElementTypeByNameInternal( elementType.getName() );
                  throw new MetaStoreDependenciesExistsException( foundElementType.getElementIds(),
                      "Element type with ID '" + elementType.getId()
                          + "' could not be deleted as it still contains elements." );
                }
                typeMap.remove( elementType.getId() );
                return null;
              }
            } );
          }
          return null;
        }
      } );
    } catch ( MetaStoreException e ) {
      if ( e instanceof MetaStoreElementTypeExistsException ) {
        throw (MetaStoreElementTypeExistsException) e;
      } else if ( e instanceof MetaStoreDependenciesExistsException ) {
        throw (MetaStoreDependenciesExistsException) e;
      } else {
        throw new RuntimeException( e );
      }
    }

  }

  /**
   * Gets an element type by ID.
   *
   * @param elementTypeId the element type ID
   * @return the element type, or {@code null} when it does not exist
   */
  public IMetaStoreElementType getElementTypeById( final String elementTypeId ) {
    return MetaStoreUtil.executeLockedOperationQuietly( readLock, new Callable<IMetaStoreElementType>() {

      @Override
      public IMetaStoreElementType call() throws Exception {
        return typeMap.get( elementTypeId );
      }
    } );
  }

  protected ReadLock getReadLock() {
    return readLock;
  }

  /**
   * Gets all elements for an element type name.
   *
   * @param elementTypeName the element type name
   * @return the elements, or an empty list when the type does not exist
   */
  public List<IMetaStoreElement> getElementsByElementTypeName( final String elementTypeName ) {
    return MetaStoreUtil.executeLockedOperationQuietly( readLock, new Callable<List<IMetaStoreElement>>() {

      @Override
      public List<IMetaStoreElement> call() throws Exception {
        MemoryMetaStoreElementType elementType = getElementTypeByNameInternal( elementTypeName );
        if ( elementType != null ) {
          return elementType.getElements();
        }
        return Collections.emptyList();
      }
    } );
  }

  /**
   * Gets all element IDs for an element type name.
   *
   * @param elementTypeName the element type name
   * @return the element IDs, or an empty list when the type does not exist
   */
  public List<String> getElementIdsByElementTypeName( final String elementTypeName ) {
    return MetaStoreUtil.executeLockedOperationQuietly( readLock, new Callable<List<String>>() {

      @Override
      public List<String> call() throws Exception {
        MemoryMetaStoreElementType elementType = getElementTypeByNameInternal( elementTypeName );
        if ( elementType != null ) {
          return elementType.getElementIds();
        }
        return Collections.emptyList();
      }
    } );
  }

  /**
   * Gets an element by type name and element ID.
   *
   * @param elementTypeName the element type name
   * @param elementId the element ID
   * @return the element, or {@code null} when it does not exist
   */
  public IMetaStoreElement getElementByTypeNameId( final String elementTypeName, final String elementId ) {
    return MetaStoreUtil.executeLockedOperationQuietly( readLock, new Callable<IMetaStoreElement>() {

      @Override
      public IMetaStoreElement call() throws Exception {
        MemoryMetaStoreElementType elementType = getElementTypeByNameInternal( elementTypeName );
        if ( elementType != null ) {
          return elementType.getElement( elementId );
        }
        return null;
      }
    } );
  }

  /**
   * Gets all element types in this namespace.
   *
   * @return the element types
   */
  public List<IMetaStoreElementType> getElementTypes() {
    return MetaStoreUtil.executeLockedOperationQuietly( readLock, new Callable<List<IMetaStoreElementType>>() {

      @Override
      public List<IMetaStoreElementType> call() throws Exception {
        return new ArrayList<IMetaStoreElementType>( typeMap.values() );
      }
    } );
  }

  /**
   * Gets an element by type name and element name.
   *
   * @param elementTypeName the element type name
   * @param elementName the element name
   * @return the element, or {@code null} when it does not exist
   */
  public IMetaStoreElement getElementByNameTypeName( final String elementTypeName, final String elementName ) {
    return MetaStoreUtil.executeLockedOperationQuietly( readLock, new Callable<IMetaStoreElement>() {

      @Override
      public IMetaStoreElement call() throws Exception {
        MemoryMetaStoreElementType elementType = getElementTypeByNameInternal( elementTypeName );
        if ( elementType != null ) {
          return elementType.getElementByName( elementName );
        }
        return null;
      }
    } );
  }

  /**
   * Creates an element in an element type.
   *
   * @param elementType the element type
   * @param element the element to create
   * @throws MetaStoreException if the element type does not exist or the element cannot be created
   */
  public void createElement( final IMetaStoreElementType elementType, final IMetaStoreElement element )
    throws MetaStoreException {
    MetaStoreUtil.executeLockedOperation( readLock, new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        MemoryMetaStoreElementType foundElementType = getElementTypeByNameInternal( elementType.getName() );
        if ( foundElementType != null ) {
          foundElementType.createElement( element );
        } else {
          throw new MetaStoreException( "Element type '" + elementType.getName() + "' couldn't be found" );
        }
        return null;
      }
    } );
  }

  /**
   * Updates an element by ID.
   *
   * @param elementType the element type
   * @param elementId the element ID
   * @param element the replacement element
   * @throws MetaStoreException if the element type does not exist or the element cannot be updated
   */
  public void updateElement( final IMetaStoreElementType elementType, final String elementId,
      final IMetaStoreElement element ) throws MetaStoreException {
    MetaStoreUtil.executeLockedOperation( readLock, new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        MemoryMetaStoreElementType foundElementType = getElementTypeByNameInternal( elementType.getName() );
        if ( foundElementType != null ) {
          foundElementType.updateElement( elementId, element );
        } else {
          throw new MetaStoreException( "Element type '" + elementType.getName() + "' couldn't be found" );
        }
        return null;
      }
    } );
  }

  /**
   * Removes an element by ID.
   *
   * @param elementType the element type
   * @param elementId the element ID
   * @throws MetaStoreException if the element type does not exist or the element cannot be removed
   */
  public void deleteElement( final IMetaStoreElementType elementType, final String elementId )
    throws MetaStoreException {
    MetaStoreUtil.executeLockedOperation( readLock, new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        MemoryMetaStoreElementType foundElementType = getElementTypeByNameInternal( elementType.getName() );
        if ( foundElementType != null ) {
          foundElementType.deleteElement( elementId );
        } else {
          throw new MetaStoreException( "Element type '" + elementType.getName() + "' couldn't be found" );
        }
        return null;
      }
    } );
  }

}
