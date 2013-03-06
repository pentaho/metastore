package org.pentaho.metastore.stores.memory;

import org.pentaho.metastore.api.security.IMetaStoreElementOwner;
import org.pentaho.metastore.api.security.MetaStoreElementOwnerType;

public class MemoryMetaStoreElementOwner implements IMetaStoreElementOwner {

  private String name;

  private MetaStoreElementOwnerType ownerType;

  public MemoryMetaStoreElementOwner(String name, MetaStoreElementOwnerType ownerType) {
    super();
    this.name = name;
    this.ownerType = ownerType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public MetaStoreElementOwnerType getOwnerType() {
    return ownerType;
  }

  public void setOwnerType(MetaStoreElementOwnerType ownerType) {
    this.ownerType = ownerType;
  }

}
