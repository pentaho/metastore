package org.pentaho.metastore.api.security;

public enum MetaStoreElementOwnerType {
  USER, 
  ROLE, 
  SYSTEM_ROLE,
  ;  
  
  public static MetaStoreElementOwnerType getOwnerType(String string) {
    if (string==null || string.length()==0) {
      return null;
    }
    return valueOf(string);
  }
}
