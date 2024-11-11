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

package org.pentaho.metastore.stores.xml;

import com.google.common.cache.CacheBuilder;

import java.util.Map;

/**
 * This implementation of XmlMetaStoreCache stores the cache using soft references. But client is still able to clear it
 * manually.
 */
public class AutomaticXmlMetaStoreCache extends BaseXmlMetaStoreCache implements XmlMetaStoreCache {

  @Override
  protected <K, V> Map<K, V> createStorage() {
    return CacheBuilder.newBuilder().softValues().<K, V>build().asMap();
  }

  @Override
  protected ElementType createElementType( String elementId ) {
    return new ElementType( elementId, this.<String, String>createStorage() );
  }

  protected static class ElementType extends BaseXmlMetaStoreCache.ElementType {

    private final Map<String, String> elementNameToIdMap;

    public ElementType( String id, Map<String, String> elementNameToIdMap ) {
      super( id );
      this.elementNameToIdMap = elementNameToIdMap;
    }

    @Override
    protected Map<String, String> getElementNameToIdMap() {
      return elementNameToIdMap;
    }

  }

}
