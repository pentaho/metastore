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

package org.pentaho.metastore.stores.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.util.MetaStoreUtil;

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
    return MetaStoreUtil.executeLockedOperationQuietly( readLock, new Callable<List<IMetaStoreAttribute>>() {

      @Override
      public List<IMetaStoreAttribute> call() throws Exception {
        return new ArrayList<IMetaStoreAttribute>( children.values() );
      }
    } );
  }

  /**
   * @param children
   *          the children to set
   */
  public void setChildren( final List<IMetaStoreAttribute> childrenList ) {
    MetaStoreUtil.executeLockedOperationQuietly( writeLock, new Callable<Void>() {

      @Override
      public Void call() throws Exception {

        children.clear();
        for ( IMetaStoreAttribute child : childrenList ) {
          children.put( child.getId(), child );
        }
        return null;
      }
    } );
  }

  @Override
  public void addChild( final IMetaStoreAttribute attribute ) {
    MetaStoreUtil.executeLockedOperationQuietly( writeLock, new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        children.put( attribute.getId(), attribute );
        return null;
      }
    } );
  }

  @Override
  public void deleteChild( final String attributeId ) {
    MetaStoreUtil.executeLockedOperationQuietly( writeLock, new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        Iterator<IMetaStoreAttribute> iterator = children.values().iterator();
        while ( iterator.hasNext() ) {
          IMetaStoreAttribute next = iterator.next();
          if ( next.getId().equals( attributeId ) ) {
            iterator.remove();
            break;
          }
        }
        return null;
      }
    } );
  }

  /**
   * Remove all child attributes
   */
  public void clearChildren() {
    MetaStoreUtil.executeLockedOperationQuietly( writeLock, new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        children.clear();
        return null;
      }
    } );
  }

  public IMetaStoreAttribute getChild( final String id ) {
    return MetaStoreUtil.executeLockedOperationQuietly( readLock, new Callable<IMetaStoreAttribute>() {

      @Override
      public IMetaStoreAttribute call() throws Exception {
        return children.get( id );
      }
    } );
  }
}
