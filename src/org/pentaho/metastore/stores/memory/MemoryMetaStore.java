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
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.pentaho.metastore.api.BaseMetaStore;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreDependenciesExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreElementExistException;
import org.pentaho.metastore.api.exceptions.MetaStoreElementTypeExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.exceptions.MetaStoreNamespaceExistsException;
import org.pentaho.metastore.api.security.IMetaStoreElementOwner;
import org.pentaho.metastore.api.security.MetaStoreElementOwnerType;

public class MemoryMetaStore extends BaseMetaStore implements IMetaStore {

  private final Map<String, MemoryMetaStoreNamespace> namespacesMap;

  private final ReadLock readLock;
  private final WriteLock writeLock;

  public MemoryMetaStore() {
    namespacesMap = new HashMap<String, MemoryMetaStoreNamespace>();

    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    readLock = lock.readLock();
    writeLock = lock.writeLock();
  }

  @Override
  public List<String> getNamespaces() throws MetaStoreException {
    readLock.lock();
    try {
      return new ArrayList<String>( namespacesMap.keySet() );
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public boolean namespaceExists( String namespace ) throws MetaStoreException {
    readLock.lock();
    try {
      return namespacesMap.get( namespace ) != null;
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( !( obj instanceof MemoryMetaStore ) ) {
      return false;
    }
    return ( (MemoryMetaStore) obj ).name.equalsIgnoreCase( name );
  }

  @Override
  public void createNamespace( String namespace ) throws MetaStoreException, MetaStoreNamespaceExistsException {
    writeLock.lock();
    try {
      if ( namespacesMap.containsKey( namespace ) ) {
        throw new MetaStoreNamespaceExistsException( "Unable to create namespace '" + namespace
            + "' as it already exists!" );
      } else {
        MemoryMetaStoreNamespace storeNamespace = new MemoryMetaStoreNamespace( namespace );
        namespacesMap.put( namespace, storeNamespace );
      }
    } finally {
      writeLock.unlock();
    }

  }

  @Override
  public void deleteNamespace( String namespace ) throws MetaStoreException, MetaStoreDependenciesExistsException {
    writeLock.lock();
    try {
      MemoryMetaStoreNamespace storeNamespace = namespacesMap.get( namespace );

      if ( storeNamespace == null ) {
        throw new MetaStoreException( "Unable to delete namespace '" + namespace + "' as it doesn't exist" );
      }

      storeNamespace.getReadLock().lock();
      try {
        List<String> elementTypeIds = storeNamespace.getElementTypeIds();
        if ( elementTypeIds.isEmpty() ) {
          namespacesMap.remove( namespace );
        } else {
          throw new MetaStoreDependenciesExistsException( elementTypeIds, "Namespace '" + namespace
            + "' is not empty!" );
        }
      } finally {
        storeNamespace.getReadLock().unlock();
      }
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public List<IMetaStoreElementType> getElementTypes( String namespace ) throws MetaStoreException {
    readLock.lock();
    try {
      MemoryMetaStoreNamespace storeNamespace = namespacesMap.get( namespace );
      if ( storeNamespace != null ) {
        return storeNamespace.getElementTypes();
      }
      return Collections.emptyList();
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public IMetaStoreElementType getElementType( String namespace, String elementTypeId ) throws MetaStoreException {
    readLock.lock();
    try {
      MemoryMetaStoreNamespace storeNamespace = namespacesMap.get( namespace );
      if ( storeNamespace != null ) {
        return storeNamespace.getElementTypeById( elementTypeId );
      }
      return null;
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public IMetaStoreElementType getElementTypeByName( String namespace, String elementTypeName )
    throws MetaStoreException {
    readLock.lock();
    try {
      MemoryMetaStoreNamespace storeNamespace = namespacesMap.get( namespace );
      if ( storeNamespace != null ) {
        return storeNamespace.getElementTypeByName( elementTypeName );
      }
      return null;
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public List<String> getElementTypeIds( String namespace ) throws MetaStoreException {
    readLock.lock();
    try {
      MemoryMetaStoreNamespace storeNamespace = namespacesMap.get( namespace );
      if ( storeNamespace != null ) {
        return storeNamespace.getElementTypeIds();
      }
      return Collections.emptyList();
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public void createElementType( String namespace, IMetaStoreElementType elementType ) throws MetaStoreException,
    MetaStoreElementTypeExistsException {
    readLock.lock();
    try {
      MemoryMetaStoreNamespace storeNamespace = namespacesMap.get( namespace );
      if ( storeNamespace != null ) {
        storeNamespace.createElementType( getName(), elementType );
      } else {
        throw new MetaStoreException( "Namespace '" + namespace + "' doesn't exist!" );
      }
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public void updateElementType( String namespace, IMetaStoreElementType elementType ) throws MetaStoreException {
    readLock.lock();
    try {
      MemoryMetaStoreNamespace storeNamespace = namespacesMap.get( namespace );
      if ( storeNamespace != null ) {
        storeNamespace.updateElementType( getName(), elementType );
      } else {
        throw new MetaStoreException( "Namespace '" + namespace + "' doesn't exist!" );
      }
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public void deleteElementType( String namespace, IMetaStoreElementType elementType ) throws MetaStoreException,
    MetaStoreDependenciesExistsException {
    readLock.lock();
    try {
      MemoryMetaStoreNamespace storeNamespace = namespacesMap.get( namespace );
      if ( storeNamespace != null ) {
        storeNamespace.deleteElementType( elementType );
      } else {
        throw new MetaStoreException( "Namespace '" + namespace + "' doesn't exist!" );
      }
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public List<IMetaStoreElement> getElements( String namespace, IMetaStoreElementType elementType )
    throws MetaStoreException {
    readLock.lock();
    try {
      MemoryMetaStoreNamespace storeNamespace = namespacesMap.get( namespace );
      if ( storeNamespace != null ) {
        return storeNamespace.getElementsByElementTypeName( elementType.getName() );
      }
      return Collections.emptyList();
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public List<String> getElementIds( String namespace, IMetaStoreElementType elementType ) throws MetaStoreException {
    readLock.lock();
    try {
      MemoryMetaStoreNamespace storeNamespace = namespacesMap.get( namespace );
      if ( storeNamespace != null ) {
        return storeNamespace.getElementIdsByElementTypeName( elementType.getName() );
      }
      return Collections.emptyList();
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public IMetaStoreElement getElement( String namespace, IMetaStoreElementType elementType, String elementId )
    throws MetaStoreException {
    readLock.lock();
    try {
      MemoryMetaStoreNamespace storeNamespace = namespacesMap.get( namespace );
      if ( storeNamespace != null ) {
        return storeNamespace.getElementByTypeNameId( elementType.getName(), elementId );
      }
      return null;
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public IMetaStoreElement getElementByName( String namespace, IMetaStoreElementType elementType, String name )
    throws MetaStoreException {
    readLock.lock();
    try {
      MemoryMetaStoreNamespace storeNamespace = namespacesMap.get( namespace );
      if ( storeNamespace != null ) {
        return storeNamespace.getElementByNameTypeName( elementType.getName(), name );
      }
      return null;
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public void createElement( String namespace, IMetaStoreElementType elementType, IMetaStoreElement element )
    throws MetaStoreException, MetaStoreElementExistException {
    readLock.lock();
    try {
      MemoryMetaStoreNamespace storeNamespace = namespacesMap.get( namespace );
      if ( storeNamespace != null ) {
        storeNamespace.createElement( elementType, element );
      } else {
        throw new MetaStoreException( "Namespace '" + namespace + "' doesn't exist!" );
      }
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public void updateElement( String namespace, IMetaStoreElementType elementType, String elementId,
      IMetaStoreElement element ) throws MetaStoreException {

    // verify that the element type belongs to this meta store
    //
    if ( elementType.getMetaStoreName() == null || !elementType.getMetaStoreName().equals( getName() ) ) {
      throw new MetaStoreException( "The element type '" + elementType.getName()
        + "' needs to explicitly belong to the meta store in which you are updating." );
    }

    readLock.lock();
    try {
      MemoryMetaStoreNamespace storeNamespace = namespacesMap.get( namespace );
      if ( storeNamespace != null ) {
        storeNamespace.updateElement( elementType, elementId, element );
      } else {
        throw new MetaStoreException( "Namespace '" + namespace + "' doesn't exist!" );
      }
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public void deleteElement( String namespace, IMetaStoreElementType elementType, String elementId )
    throws MetaStoreException {
    readLock.lock();
    try {
      MemoryMetaStoreNamespace storeNamespace = namespacesMap.get( namespace );
      if ( storeNamespace != null ) {
        storeNamespace.deleteElement( elementType, elementId );
      } else {
        throw new MetaStoreException( "Namespace '" + namespace + "' doesn't exist!" );
      }
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public IMetaStoreElementType newElementType( String namespace ) throws MetaStoreException {
    return new MemoryMetaStoreElementType( namespace );
  }

  @Override
  public IMetaStoreElement newElement() throws MetaStoreException {
    return new MemoryMetaStoreElement();
  }

  @Override
  public IMetaStoreElement newElement( IMetaStoreElementType elementType, String id, Object value )
    throws MetaStoreException {
    return new MemoryMetaStoreElement( elementType, id, value );
  }

  public IMetaStoreAttribute newAttribute( String id, Object value ) throws MetaStoreException {
    return new MemoryMetaStoreAttribute( id, value );
  }

  @Override
  public IMetaStoreElementOwner newElementOwner( String name, MetaStoreElementOwnerType ownerType )
    throws MetaStoreException {
    return new MemoryMetaStoreElementOwner( name, ownerType );
  }

}
