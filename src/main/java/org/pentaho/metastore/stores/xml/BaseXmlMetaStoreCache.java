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

package org.pentaho.metastore.stores.xml;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.pentaho.metastore.api.IMetaStoreElementType;

/**
 * Provides common cache behavior for XML metastores.
 */
public abstract class BaseXmlMetaStoreCache implements XmlMetaStoreCache {

  private final Map<String, Long> processedFiles = new HashMap<String, Long>();

  private final Map<String, Map<String, ElementType>> elementTypesMap = new HashMap<String, Map<String, ElementType>>();

  @Override
  public synchronized void registerElementTypeIdForName( String namespace, String elementTypeName, String elementId ) {
    Map<String, ElementType> elementTypeNameToId =
        elementTypesMap.computeIfAbsent( namespace, key -> createStorage() );
    ElementType elementType =
        elementTypeNameToId.computeIfAbsent( elementTypeName, key -> createElementType( elementId ) );
    if ( !elementType.getId().equals( elementId ) ) {
      elementType.unregisterElements();
      elementType.setId( elementId );
    }
  }

  @Override
  public synchronized String getElementTypeIdByName( String namespace, String elementTypeName ) {
    Map<String, ElementType> elementTypeNameToId = elementTypesMap.get( namespace );
    if ( elementTypeNameToId == null ) {
      return null;
    }

    ElementType element = elementTypeNameToId.get( elementTypeName );
    return element == null ? null : element.getId();
  }

  @Override
  public synchronized void unregisterElementTypeId( String namespace, String elementTypeId ) {
    Map<String, ElementType> elementTypeNameToId = elementTypesMap.get( namespace );
    if ( elementTypeNameToId == null ) {
      return;
    }
    Iterator<Entry<String, ElementType>> iterator = elementTypeNameToId.entrySet().iterator();
    while ( iterator.hasNext() ) {
      Entry<String, ElementType> elementType = iterator.next();
      if ( elementType.getValue().getId().equals( elementTypeId ) ) {
        iterator.remove();
        return;
      }
    }
  }

  @Override
  public synchronized void registerElementIdForName( String namespace, IMetaStoreElementType elementType, String elementName,
      String elementId ) {
    Map<String, ElementType> nameToElementType = elementTypesMap.get( namespace );
    if ( nameToElementType == null ) {
      registerElementTypeIdForName( namespace, elementType.getName(), elementType.getId() );
      nameToElementType = elementTypesMap.get( namespace );
    }
    ElementType type = nameToElementType.get( elementType.getName() );
    if ( type == null ) {
      registerElementTypeIdForName( namespace, elementType.getName(), elementType.getId() );
      type = nameToElementType.get( elementType.getName() );
    }
    type.registerElementIdForName( elementName, elementId );
  }

  @Override
  public synchronized String getElementIdByName( String namespace, IMetaStoreElementType elementType, String elementName ) {
    Map<String, ElementType> elementTypeNameToId = elementTypesMap.get( namespace );
    if ( elementTypeNameToId == null ) {
      return null;
    }
    ElementType type = elementTypeNameToId.get( elementType.getName() );
    return type == null ? null : type.getElementIdByName( elementName );
  }

  @Override
  public synchronized void unregisterElementId( String namespace, IMetaStoreElementType elementType, String elementId ) {
    Map<String, ElementType> elementTypeNameToId = elementTypesMap.get( namespace );
    if ( elementTypeNameToId == null ) {
      return;
    }
    ElementType type = elementTypeNameToId.get( elementType.getName() );
    if ( type == null ) {
      return;
    }
    type.unregisterElementId( elementId );
  }

  @Override
  public synchronized void registerProcessedFile( String fullPath, long lastUpdate ) {
    processedFiles.put( normalizePath( fullPath ), lastUpdate );
  }

  @Override
  public synchronized Map<String, Long> getProcessedFiles() {
    return Collections.unmodifiableMap( processedFiles );
  }

  @Override
  public synchronized void unregisterProcessedFile( String fullPath ) {
    processedFiles.remove( normalizePath( fullPath ) );
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

  protected abstract <K, V> Map<K, V> createStorage();

  // This is a cache-key normalizer only; it does not open, read, or write any file.
  private String normalizePath( String path ) {
    if ( path == null ) {
      return null;
    }
    try {
      return Paths.get( path ).normalize().toString();
    } catch ( InvalidPathException e ) {
      return path;
    }
  }

  protected abstract ElementType createElementType( String elementId );

  protected abstract static class ElementType {

    private String id;

    protected ElementType( String id ) {
      this.id = id;

    }

    /**
     * Gets the element type ID.
     *
     * @return the element type ID
     */
    public String getId() {
      return id;
    }

    /**
     * Sets the element type ID.
     *
     * @param id the element type ID
     */
    public void setId( String id ) {
      this.id = id;
    }

    protected abstract Map<String, String> getElementNameToIdMap();

    /**
     * Registers an element ID by name.
     *
     * @param elementName the element name
     * @param elementId the element ID
     */
    public void registerElementIdForName( String elementName, String elementId ) {
      if ( elementId == null ) {
        return;
      }
      Map<String, String> elementNameToIdMap = getElementNameToIdMap();
      elementNameToIdMap.put( elementName, elementId );
    }

    /**
     * Gets an element ID by name.
     *
     * @param elementName the element name
     * @return the element ID, or {@code null} when no mapping exists
     */
    public String getElementIdByName( String elementName ) {
      Map<String, String> elementNameToIdMap = getElementNameToIdMap();
      return elementNameToIdMap.get( elementName );
    }

    /**
     * Removes an element ID mapping.
     *
     * @param elementId the element ID
     */
    public void unregisterElementId( String elementId ) {
      Map<String, String> elementNameToIdMap = getElementNameToIdMap();
      Iterator<Entry<String, String>> iterator = elementNameToIdMap.entrySet().iterator();
      while ( iterator.hasNext() ) {
        Entry<String, String> element = iterator.next();
        if ( element.getValue().equals( elementId ) ) {
          iterator.remove();
          return;
        }
      }
    }

    /**
     * Removes all element ID mappings.
     */
    public void unregisterElements() {
      Map<String, String> elementNameToIdMap = getElementNameToIdMap();
      elementNameToIdMap.clear();
    }
  }
}
