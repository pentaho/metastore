package org.pentaho.metastore.api;

import java.util.List;

import org.pentaho.metastore.api.security.IMetaStoreElementOwner;
import org.pentaho.metastore.api.security.MetaStoreOwnerPermissions;


/**
 * This interface describes the element as an attribute (with children) with security on top of it.
 * @author matt
 *
 */
public interface IMetaStoreElement extends IMetaStoreAttribute {

  
  public IMetaStoreElementOwner getOwner();
  public void setOwner(IMetaStoreElementOwner owner);
  
  public List<MetaStoreOwnerPermissions> getOwnerPermissionsList(); 
  public void setOwnerPermissionsList(List<MetaStoreOwnerPermissions> ownerPermissions);
  
}
