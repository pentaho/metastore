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

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.pentaho.metastore.api.IMetaStoreElementType;

/**
 * This implementation of XmlMetaStoreCache stores the cache using soft references.
 * But client is still able to clear it manually.
 *
 */
public class AutomaticXmlMetaStoreCache implements XmlMetaStoreCache {
  
  private final Map<String, Long> processedFiles = new HashMap<String, Long>();
  
  private final Map<String, Map<String, SoftReference<ElementType>>> elementTypesMap = new HashMap<String, Map<String, SoftReference<ElementType>>>();
  
  @Override
  public synchronized void registerElementTypeIdForName( String namespace, String elementTypeName, String elementId ) {
    Map<String, SoftReference<ElementType>> elementTypeNameToId = elementTypesMap.get( namespace );
    if ( elementTypeNameToId == null ) {
      elementTypeNameToId = new HashMap<String, SoftReference<ElementType>>();
      elementTypesMap.put( namespace, elementTypeNameToId );
    }
    SoftReference<ElementType> softReferenceToElementType = elementTypeNameToId.get( elementTypeName );
    ElementType elementType = ( softReferenceToElementType == null ) ? null : softReferenceToElementType.get();
    if ( elementType == null ) {
      elementType = new ElementType( elementId );
      elementTypeNameToId.put( elementTypeName, new SoftReference<ElementType>( elementType ) );
    } else if (!elementType.getId().equals( elementId )) {
      elementType.unregisterElements();
      elementType.setId( elementId );
    }
  }

  @Override
  public synchronized String getElementTypeIdByName( String namespace, String elementTypeName ) {
    Map<String, SoftReference<ElementType>> elementTypeNameToId = elementTypesMap.get( namespace );
    if ( elementTypeNameToId == null) {
      return null;
    }
    SoftReference<ElementType> softReferenceToElementType = elementTypeNameToId.get( elementTypeName );
    if (softReferenceToElementType != null) {
      ElementType element = softReferenceToElementType.get();
      return element == null ? null : element.getId();
    }
    return null;
  }

  @Override
  public synchronized void unregisterElementTypeId( String namespace, String elementTypeId ) {
    Map<String, SoftReference<ElementType>> elementTypeNameToId = elementTypesMap.get( namespace );
    if (elementTypeNameToId == null) {
      return;
    }
    Iterator<Entry<String, SoftReference<ElementType>>> iterator = elementTypeNameToId.entrySet().iterator();
    while ( iterator.hasNext() ) {
      Entry<String, SoftReference<ElementType>> elementTypeEntry = iterator.next();
      SoftReference<ElementType> softReferenceToElementType = elementTypeEntry.getValue();
      if ( softReferenceToElementType != null ) {
        ElementType elementType = softReferenceToElementType.get();
        if ( elementType != null && elementType.getId().equals( elementTypeId ) ) {
          iterator.remove();
          return;
        }
      }
    }
  }

  @Override
  public synchronized void registerElementIdForName( String namespace, IMetaStoreElementType elementType, String elementName,
      String elementId ) {
    Map<String, SoftReference<ElementType>> nameToElementType = elementTypesMap.get( namespace );
    if (nameToElementType == null) {
      registerElementTypeIdForName( namespace, elementType.getName(), elementType.getId() );
      nameToElementType = elementTypesMap.get( namespace );
    }
    SoftReference<ElementType> softReferenceToType = nameToElementType.get( elementType.getName() );
    ElementType type =softReferenceToType.get();
    if (type != null) {
      type.registerElementIdForName( elementName, elementId );
    }
  }

  @Override
  public synchronized String getElementIdByName( String namespace, IMetaStoreElementType elementType, String elementName ) {
    Map<String, SoftReference<ElementType>> elementTypeNameToId = elementTypesMap.get( namespace );
    if ( elementTypeNameToId == null) {
      return null;
    }
    SoftReference<ElementType> softReferenceToElementType = elementTypeNameToId.get( elementType.getName() );
    ElementType type = ( softReferenceToElementType == null ) ? null : softReferenceToElementType.get();
    return type == null ? null : type.getElementIdByName( elementName );
  }

  @Override
  public synchronized void unregisterElementId( String namespace, IMetaStoreElementType elementType, String elementId ) {
    Map<String, SoftReference<ElementType>> elementTypeNameToId = elementTypesMap.get( namespace );
    if ( elementTypeNameToId == null) {
      return;
    }
    SoftReference<ElementType> softReferenceToElementType = elementTypeNameToId.get( elementType.getName() );
    ElementType type = ( softReferenceToElementType == null ) ? null : softReferenceToElementType.get();
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
  
  @Override
  public synchronized void clear() {
    processedFiles.clear();
    for ( Map<String, SoftReference<ElementType>> namespaceElementType : elementTypesMap.values() ) {
      for ( SoftReference<ElementType> softReferenceToElementType : namespaceElementType.values() ) {
        if (softReferenceToElementType != null) {
          ElementType elementType = softReferenceToElementType.get();
          if ( elementType != null ) {
            elementType.unregisterElements();
          }
        }
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
