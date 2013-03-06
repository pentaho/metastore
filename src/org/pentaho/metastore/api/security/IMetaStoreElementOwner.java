package org.pentaho.metastore.api.security;

public interface IMetaStoreElementOwner {
  
  public String getName();
  public void setName(String name);
  public MetaStoreElementOwnerType getOwnerType();
  public void setOwnerType(MetaStoreElementOwnerType type);

}
