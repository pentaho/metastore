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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreDependenciesExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreElementTypeExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

public class MemoryMetaStoreNamespace {

  private final ReadLock readLock;
  private final WriteLock writeLock;

  private final String namespace;
  private final Map<String, MemoryMetaStoreElementType> typeMap;

  public MemoryMetaStoreNamespace( String namespace ) {
    this.namespace = namespace;
    this.typeMap = new HashMap<String, MemoryMetaStoreElementType>();

    ReentrantReadWriteLock lock = new ReentrantReadWriteLock(  );
    readLock = lock.readLock();
    writeLock = lock.writeLock();

  }

  public String getNamespace() {
    return namespace;
  }

  public Map<String, MemoryMetaStoreElementType> getTypeMap() {
    readLock.lock();
    try {
      return new HashMap<String, MemoryMetaStoreElementType>( this.typeMap );
    } finally {
      readLock.unlock();
    }
  }

  private MemoryMetaStoreElementType getElementTypeByNameInternal( String elementTypeName ) {
    for ( MemoryMetaStoreElementType elementType : typeMap.values() ) {
      if ( elementType.getName().equalsIgnoreCase( elementTypeName ) ) {
        return elementType;
      }
    }
    return null;
  }

  public MemoryMetaStoreElementType getElementTypeByName( String elementTypeName ) {
    readLock.lock();
    try {
      return getElementTypeByNameInternal( elementTypeName );
    } finally {
      readLock.unlock();
    }
  }

  public List<String> getElementTypeIds() {
    readLock.lock();
    try {
      ArrayList<String> list = new ArrayList<String>( typeMap.size() );
      for ( MemoryMetaStoreElementType elementType : typeMap.values() ) {
        list.add( elementType.getId() );
      }
      return list;
    } finally {
      readLock.unlock();
    }
  }

  public void createElementType( String metaStoreName, IMetaStoreElementType elementType )
    throws MetaStoreElementTypeExistsException {
    // For the memory store, the ID is the same as the name if empty
    if ( elementType.getId() == null ) {
      elementType.setId( elementType.getName() );
    }

    writeLock.lock();
    try {
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
    } finally {
      writeLock.unlock();
    }
  }

  public void updateElementType( String metaStoreName, IMetaStoreElementType elementType )
    throws MetaStoreElementTypeExistsException {
    writeLock.lock();
    try {
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
    } finally {
      writeLock.unlock();
    }
  }

  public void deleteElementType( IMetaStoreElementType elementType ) throws MetaStoreElementTypeExistsException,
    MetaStoreDependenciesExistsException {
    writeLock.lock();
    try {
      MemoryMetaStoreElementType verifyType = typeMap.get( elementType.getId() );
      if ( verifyType == null ) {
        throw new MetaStoreElementTypeExistsException( new ArrayList<IMetaStoreElementType>( typeMap.values() ),
            "Element type to delete, with ID '" + elementType.getId() + "', does not exist" );
      } else {
        // See if there are elements in there...
        //
        verifyType.getReadLock().lock();
        try {
          if ( !verifyType.isElementMapEmpty() ) {
            MemoryMetaStoreElementType foundElementType = getElementTypeByNameInternal( elementType.getName() );
            throw new MetaStoreDependenciesExistsException( foundElementType.getElementIds(), "Element type with ID '"
                + elementType.getId() + "' could not be deleted as it still contains elements." );
          }
          typeMap.remove( elementType.getId() );
        } finally {
          verifyType.getReadLock().unlock();
        }
      }
    } finally {
      writeLock.unlock();
    }
  }

  public IMetaStoreElementType getElementTypeById( String elementTypeId ) {
    readLock.lock();
    try {
      return typeMap.get( elementTypeId );
    } finally {
      readLock.unlock();
    }
  }

  protected ReadLock getReadLock() {
    return readLock;
  }

  public List<IMetaStoreElement> getElementsByElementTypeName( String elementTypeName ) {
    readLock.lock();
    try {
      MemoryMetaStoreElementType elementType = getElementTypeByNameInternal( elementTypeName );
      if ( elementType != null ) {
        return elementType.getElements();
      }
      return Collections.emptyList();
    } finally {
      readLock.unlock();
    }
  }

  public List<String> getElementIdsByElementTypeName( String elementTypeName ) {
    readLock.lock();
    try {
      MemoryMetaStoreElementType elementType = getElementTypeByNameInternal( elementTypeName );
      if ( elementType != null ) {
        return elementType.getElementIds();
      }
      return Collections.emptyList();
    } finally {
      readLock.unlock();
    }
  }

  public IMetaStoreElement getElementByTypeNameId( String elementTypeName, String elementId ) {
    readLock.lock();
    try {
      MemoryMetaStoreElementType elementType = getElementTypeByNameInternal( elementTypeName );
      if ( elementType != null ) {
        return elementType.getElement( elementId );
      }
      return null;
    } finally {
      readLock.unlock();
    }
  }

  public List<IMetaStoreElementType> getElementTypes() {
    readLock.lock();
    try {
      return new ArrayList<IMetaStoreElementType>( typeMap.values() );
    } finally {
      readLock.unlock();
    }
  }

  public IMetaStoreElement getElementByNameTypeName( String elementTypeName, String elementName ) {
    readLock.lock();
    try {
      MemoryMetaStoreElementType elementType = getElementTypeByNameInternal( elementTypeName );
      if ( elementType != null ) {
        return elementType.getElementByName( elementName );
      }
      return null;
    } finally {
      readLock.unlock();
    }
  }

  public void createElement( IMetaStoreElementType elementType, IMetaStoreElement element ) throws MetaStoreException {
    readLock.lock();
    try {
      MemoryMetaStoreElementType foundElementType = getElementTypeByNameInternal( elementType.getName() );
      if ( foundElementType != null ) {
        foundElementType.createElement( element );
      } else {
        throw new MetaStoreException( "Element type '" + elementType.getName() + "' couldn't be found" );
      }
    } finally {
      readLock.unlock();
    }
  }

  public void updateElement( IMetaStoreElementType elementType, String elementId, IMetaStoreElement element )
    throws MetaStoreException {
    readLock.lock();
    try {
      MemoryMetaStoreElementType foundElementType = getElementTypeByNameInternal( elementType.getName() );
      if ( foundElementType != null ) {
        foundElementType.updateElement( elementId, element );
      } else {
        throw new MetaStoreException( "Element type '" + elementType.getName() + "' couldn't be found" );
      }
    } finally {
      readLock.unlock();
    }

  }

  public void deleteElement( IMetaStoreElementType elementType, String elementId ) throws MetaStoreException {
    readLock.lock();
    try {
      MemoryMetaStoreElementType foundElementType = getElementTypeByNameInternal( elementType.getName() );
      if ( foundElementType != null ) {
        foundElementType.deleteElement( elementId );
      } else {
        throw new MetaStoreException( "Element type '" + elementType.getName() + "' couldn't be found" );
      }
    } finally {
      readLock.unlock();
    }

  }

}
