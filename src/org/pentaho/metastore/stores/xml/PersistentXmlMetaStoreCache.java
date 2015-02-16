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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.pentaho.metastore.api.IMetaStoreElementType;

/**
 * This implementation provides a simple XmlMetaStoreCache.
 * It uses strong references thus client should clear it manually.
 *
 */
public class PersistentXmlMetaStoreCache implements XmlMetaStoreCache {

  private final Map<String, Long> processedFiles = new HashMap<String, Long>();
  
  private final Map<String, Map<String, ElementType>> elementTypesMap = new HashMap<String, Map<String, ElementType>>();
  
  @Override
  public synchronized void registerElementTypeIdForName( String namespace, String elementTypeName, String elementId ) {
    Map<String, ElementType> elementTypeNameToId = elementTypesMap.get( namespace );
    if (elementTypeNameToId == null) {
      elementTypeNameToId = new HashMap<String, ElementType>();
      elementTypesMap.put( namespace, elementTypeNameToId );
    }
    ElementType elementType = elementTypeNameToId.get( elementTypeName );
    if (elementType == null) {
      elementType = new ElementType( elementId );
      elementTypeNameToId.put( elementTypeName, elementType );
    } else if (!elementType.getId().equals( elementId )) {
      elementType.unregisterElements();
      elementType.setId( elementId );
    }
  }

  @Override
  public synchronized String getElementTypeIdByName( String namespace, String elementTypeName ) {
    Map<String, ElementType> elementTypeNameToId = elementTypesMap.get( namespace );
    if ( elementTypeNameToId == null) {
      return null;
    }
    
    ElementType element = elementTypeNameToId.get( elementTypeName );
    return element == null ? null : element.getId();
  }

  @Override
  public synchronized void unregisterElementTypeId( String namespace, String elementTypeId ) {
    Map<String, ElementType> elementTypeNameToId = elementTypesMap.get( namespace );
    if (elementTypeNameToId == null) {
      return;
    }
    Iterator<Entry<String, ElementType>> iterator = elementTypeNameToId.entrySet().iterator();
    while ( iterator.hasNext() ) {
      Entry<String, ElementType> elementType = iterator.next();
      if (elementType.getValue().getId().equals( elementTypeId )) {
        iterator.remove();
        return;
      }
    }
  }

  @Override
  public synchronized void registerElementIdForName( String namespace, IMetaStoreElementType elementType, String elementName,
      String elementId ) {
    Map<String, ElementType> nameToElementType = elementTypesMap.get( namespace );
    if (nameToElementType == null) {
      registerElementTypeIdForName( namespace, elementType.getName(), elementType.getId() );
      nameToElementType = elementTypesMap.get( namespace );
    }
    ElementType type = nameToElementType.get( elementType.getName() );
    if (type != null) {
      type.registerElementIdForName( elementName, elementId );
    }
  }

  @Override
  public synchronized String getElementIdByName( String namespace, IMetaStoreElementType elementType, String elementName ) {
    Map<String, ElementType> elementTypeNameToId = elementTypesMap.get( namespace );
    if ( elementTypeNameToId == null) {
      return null;
    }
    ElementType type = elementTypeNameToId.get( elementType.getName() );
    return type == null ? null : type.getElementIdByName( elementName );
  }

  @Override
  public synchronized void unregisterElementId( String namespace, IMetaStoreElementType elementType, String elementId ) {
    Map<String, ElementType> elementTypeNameToId = elementTypesMap.get( namespace );
    if ( elementTypeNameToId == null) {
      return;
    }
    ElementType type = elementTypeNameToId.get( elementType.getName() );
    if (type == null) {
      return;
    }
    type.unregisterElementId( elementId );
  }

  @Override
  public synchronized void registerProcessedFile( String fullPath, long lastUpdate ) {
    processedFiles.put( fullPath, lastUpdate );
  }

  @Override
  public synchronized Map<String, Long> getProcessedFiles() {
    return Collections.unmodifiableMap( processedFiles );
  }

  @Override
  public synchronized void unregisterProcessedFile( String fullPath ) {
    processedFiles.remove( fullPath );
  }
  
  public synchronized void clear() {
    processedFiles.clear();
    for ( Map<String, ElementType> namespaceElementType : elementTypesMap.values() ) {
      for ( ElementType elementType : namespaceElementType.values() ) {
        elementType.unregisterElements();
      }
      namespaceElementType.clear();
    }
    elementTypesMap.clear();
  }

  private static class ElementType {
    
    private final Map<String, String> elementNameToIdMap = new HashMap<String, String>();
    
    private String id;
    
    public ElementType(String id) {
      this.id = id;
    }
    
    public String getId() {
      return id;
    }

    public void setId( String id ) {
      this.id = id;
    }

    public void registerElementIdForName( String elementName, String elementId ) {
      elementNameToIdMap.put( elementName, elementId );
    }

    public String getElementIdByName( String elementName ) {
      return elementNameToIdMap.get( elementName );
    }

    public void unregisterElementId( String elementId ) {
      Iterator<Entry<String, String>> iterator = elementNameToIdMap.entrySet().iterator();
      while ( iterator.hasNext() ) {
        Entry<String, String> element = iterator.next();
        if (element.getValue().equals( elementId )) {
          iterator.remove();
          return;
        }
      }
    }
    
    public void unregisterElements() {
      elementNameToIdMap.clear();
    }
    
  }
  
}
