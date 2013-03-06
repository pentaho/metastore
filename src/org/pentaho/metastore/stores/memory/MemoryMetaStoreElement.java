package org.pentaho.metastore.stores.memory;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.security.IMetaStoreElementOwner;
import org.pentaho.metastore.api.security.MetaStoreOwnerPermissions;
import org.pentaho.metastore.stores.xml.XmlMetaStoreElementOwner;

public class MemoryMetaStoreElement extends MemoryMetaStoreAttribute implements IMetaStoreElement {

  protected IMetaStoreElementOwner owner;
  protected List<MetaStoreOwnerPermissions> ownerPermissions;
  
  public MemoryMetaStoreElement() {
    this(null, null);
  }

  public MemoryMetaStoreElement(String id, Object value) {
    super(id, value);
    
    this.ownerPermissions = new ArrayList<MetaStoreOwnerPermissions>();
  }

  public MemoryMetaStoreElement(IMetaStoreElement element) {
    super(element);
    this.ownerPermissions = new ArrayList<MetaStoreOwnerPermissions>();
    if (element.getOwner()!=null) {
      this.owner = new XmlMetaStoreElementOwner(element.getOwner());
    }
    for (MetaStoreOwnerPermissions ownerPermissions : element.getOwnerPermissionsList()) {
      this.getOwnerPermissionsList().add( new MetaStoreOwnerPermissions(ownerPermissions.getOwner(), ownerPermissions.getPermissions()) );
    }
  }

  public IMetaStoreElementOwner getOwner() {
    return owner;
  }

  public void setOwner(IMetaStoreElementOwner owner) {
    this.owner = owner;
  }

  public List<MetaStoreOwnerPermissions> getOwnerPermissionsList() {
    return ownerPermissions;
  }

  public void setOwnerPermissionsList(List<MetaStoreOwnerPermissions> ownerPermissions) {
    this.ownerPermissions = ownerPermissions;
  }
  
}
