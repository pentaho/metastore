package org.pentaho.metastore.stores.memory;

import java.util.HashMap;
import java.util.Map;

public class MemoryMetaStoreNamespace {
  
  private String namespace;
  private Map<String, MemoryMetaStoreElementType> typeMap;
  
  public MemoryMetaStoreNamespace(String namespace) {
    this.namespace = namespace;
    this.typeMap = new HashMap<String, MemoryMetaStoreElementType>();
  }
  
  public String getNamespace() {
    return namespace;
  }
  
  public Map<String, MemoryMetaStoreElementType> getTypeMap() {
    return typeMap;
  }
}
