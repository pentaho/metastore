package org.pentaho.metastore.stores.memory;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.security.IMetaStoreElementOwner;
import org.pentaho.metastore.api.security.MetaStoreOwnerPermissions;
import org.pentaho.metastore.stores.xml.XmlMetaStoreElementOwner;

public class MemoryMetaStoreElement extends MemoryMetaStoreAttribute implements IMetaStoreElement {

  protected String name;
  protected IMetaStoreElementOwner owner;
  protected List<MetaStoreOwnerPermissions> ownerPermissionsList;
  
  public MemoryMetaStoreElement() {
    this(null, null);
  }

  public MemoryMetaStoreElement(String id, Object value) {
    super(id, value);
    
    this.ownerPermissionsList = new ArrayList<MetaStoreOwnerPermissions>();
  }

  public MemoryMetaStoreElement(IMetaStoreElement element) {
    super(element);
    this.name = element.getName();
    this.ownerPermissionsList = new ArrayList<MetaStoreOwnerPermissions>();
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
    return ownerPermissionsList;
  }

  public void setOwnerPermissionsList(List<MetaStoreOwnerPermissions> ownerPermissions) {
    this.ownerPermissionsList = ownerPermissions;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  
}
