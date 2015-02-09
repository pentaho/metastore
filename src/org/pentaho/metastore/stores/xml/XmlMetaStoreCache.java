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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.metastore.stores.xml;

import java.util.Collections;
import java.util.Map;

import org.pentaho.metastore.api.IMetaStoreElementType;

/**
 * This interface describes the cache object for XmlMetaStore.
 *
 */
public interface XmlMetaStoreCache {
  
  /**
   * 
   * 
   * @param namespace
   * @param elementTypeName
   * @param elementTypeId
   */
  void registerElementTypeIdForName( String namespace, String elementTypeName, String elementTypeId );
  
  /**
   * 
   * @param namespace
   * @param elementTypeName
   * @return
   */
  String getElementTypeIdByName( String namespace, String elementTypeName );
  
  /**
   * 
   * @param namespace
   * @param elementTypeId
   */
  void unregisterElementTypeId( String namespace, String elementTypeId );
  
  /**
   * 
   * @param namespace
   * @param elementType
   * @param elementName
   * @param elementId
   */
  void registerElementIdForName( String namespace, IMetaStoreElementType elementType, String elementName, String elementId );
  
  /**
   * 
   * @param namespace
   * @param elementType
   * @param elementName
   * @return
   */
  String getElementIdByName( String namespace, IMetaStoreElementType elementType, String elementName );
  
  /**
   * 
   * @param namespace
   * @param elementType
   * @param elementId
   */
  void unregisterElementId( String namespace, IMetaStoreElementType elementType, String elementId );
  
  /**
   * 
   * @param fullPath
   * @param lastModified
   */
  void registerProcessedFile( String fullPath, long lastModified );
  
  /**
   * 
   * @return
   */
  Map<String, Long> getProcessedFiles();
  
  /**
   * 
   * @param fullPath
   */
  void unregisterProcessedFile( String fullPath );
  
  /**
   * Default non-caching implementation.
   */
  XmlMetaStoreCache NO_CACHE_INSTANCE = new XmlMetaStoreCache() {

    @Override
    public void registerElementTypeIdForName( String namespace, String elementTypeName, String elementTypeId ) {
    }

    @Override
    public String getElementTypeIdByName( String namespace, String elementTypeName ) {
      return null;
    }

    @Override
    public void unregisterElementTypeId( String namespace, String elementTypeId ) {
    }

    @Override
    public void registerElementIdForName( String namespace, IMetaStoreElementType elementType, String elementName, String elementId ) {
    }

    @Override
    public String getElementIdByName( String namespace, IMetaStoreElementType elementType, String elementName ) {
      return null;
    }

    @Override
    public void unregisterElementId( String namespace, IMetaStoreElementType elementType, String elementId ) {
    }

    @Override
    public void registerProcessedFile( String fullPath, long lastModified ) {
    }

    @Override
    public Map<String, Long> getProcessedFiles() {
      return Collections.emptyMap();
    }

    @Override
    public void unregisterProcessedFile( String fullPath ) {
    }
    
  };
  
}
