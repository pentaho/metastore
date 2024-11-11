/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.metastore.stores.xml;

import java.util.HashMap;
import java.util.Map;

/**
 * This implementation provides a simple XmlMetaStoreCache.
 * It uses strong references thus client should clear it manually.
 *
 */
public class PersistentXmlMetaStoreCache extends BaseXmlMetaStoreCache implements XmlMetaStoreCache {

  @Override
  protected <K, V> Map<K, V> createStorage() {
    return new HashMap<K, V>();
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
