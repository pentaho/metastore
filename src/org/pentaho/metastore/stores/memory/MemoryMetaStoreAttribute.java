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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.pentaho.metastore.api.IMetaStoreAttribute;

public class MemoryMetaStoreAttribute implements IMetaStoreAttribute {

  private final ReadLock readLock;
  private final WriteLock writeLock;

  protected final Map<String, IMetaStoreAttribute> children;

  protected String id;
  protected Object value;

  public MemoryMetaStoreAttribute() {
    this( null, null );
  }

  public MemoryMetaStoreAttribute( String id, Object value ) {
    this.id = id;
    this.value = value;
    this.children = new HashMap<String, IMetaStoreAttribute>();

    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    readLock = lock.readLock();
    writeLock = lock.writeLock();
  }

  public MemoryMetaStoreAttribute( IMetaStoreAttribute attribute ) {
    this( attribute.getId(), attribute.getValue() );

    for ( IMetaStoreAttribute childElement : attribute.getChildren() ) {
      addChild( new MemoryMetaStoreAttribute( childElement ) );
    }
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id
   *          the id to set
   */
  public void setId( String id ) {
    this.id = id;
  }

  /**
   * @return the value
   */
  public Object getValue() {
    return value;
  }

  /**
   * @param value
   *          the value to set
   */
  public void setValue( Object value ) {
    this.value = value;
  }

  /**
   * @return the children
   */
  public List<IMetaStoreAttribute> getChildren() {
    readLock.lock();
    try {
      return new ArrayList<IMetaStoreAttribute>( children.values() );
    } finally {
      readLock.unlock();
    }
  }

  /**
   * @param children
   *          the children to set
   */
  public void setChildren( List<IMetaStoreAttribute> children ) {
    writeLock.lock();
    try {
      this.children.clear();
      for ( IMetaStoreAttribute child : children ) {
        this.children.put( child.getId(), child );
      }
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public void addChild( IMetaStoreAttribute attribute ) {
    writeLock.lock();
    try {
      children.put( attribute.getId(), attribute );
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public void deleteChild( String attributeId ) {
    writeLock.lock();
    try {
      Iterator<IMetaStoreAttribute> iterator = children.values().iterator();
      while ( iterator.hasNext() ) {
        IMetaStoreAttribute next = iterator.next();
        if ( next.getId().equals( attributeId ) ) {
          iterator.remove();
          break;
        }
      }
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Remove all child attributes
   */
  public void clearChildren() {
    writeLock.lock();
    try {
      children.clear();
    } finally {
      writeLock.unlock();
    }
  }

  public IMetaStoreAttribute getChild( String id ) {
    readLock.lock();
    try {
      return children.get( id );
    } finally {
      readLock.unlock();
    }
  }
}
