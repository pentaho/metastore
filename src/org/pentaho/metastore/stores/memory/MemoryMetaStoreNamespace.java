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

public class MemoryMetaStoreNamespace {

  private final ReadLock readLock;
  private final WriteLock writeLock;

  private final String namespace;
  private final Map<String, MemoryMetaStoreElementType> typeMap;

  public MemoryMetaStoreNamespace( String namespace ) {
    this.namespace = namespace;
    this.typeMap = new HashMap<String, MemoryMetaStoreElementType>();

    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    readLock = lock.readLock();
    writeLock = lock.writeLock();

  }

  public String getNamespace() {
    return namespace;
  }

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

  public MemoryMetaStoreElementType getElementTypeByName( final String elementTypeName ) {
    return MetaStoreUtil.executeLockedOperationQuietly( readLock, new Callable<MemoryMetaStoreElementType>() {

      @Override
      public MemoryMetaStoreElementType call() throws Exception {
        return getElementTypeByNameInternal( elementTypeName );
      }
    } );
  }

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

  public List<IMetaStoreElementType> getElementTypes() {
    return MetaStoreUtil.executeLockedOperationQuietly( readLock, new Callable<List<IMetaStoreElementType>>() {

      @Override
      public List<IMetaStoreElementType> call() throws Exception {
        return new ArrayList<IMetaStoreElementType>( typeMap.values() );
      }
    } );
  }

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
