package org.pentaho.metastore.stores.memory;

import java.util.List;

import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.security.IMetaStoreElementOwner;
import org.pentaho.metastore.api.security.IMetaStoreOwnerPermissions;

public class MemoryMetaStoreElement extends MemoryMetaStoreAttribute implements IMetaStoreElement {

  protected IMetaStoreElementOwner owner;
  protected List<IMetaStoreOwnerPermissions> ownerPermissions;
  
  public MemoryMetaStoreElement() {
    super();
  }

  public IMetaStoreElementOwner getOwner() {
    return owner;
  }

  public void setOwner(IMetaStoreElementOwner owner) {
    this.owner = owner;
  }

  public List<IMetaStoreOwnerPermissions> getOwnerPermissionsList() {
    return ownerPermissions;
  }

  public void setOwnerPermissionsList(List<IMetaStoreOwnerPermissions> ownerPermissions) {
    this.ownerPermissions = ownerPermissions;
  }
  
}
