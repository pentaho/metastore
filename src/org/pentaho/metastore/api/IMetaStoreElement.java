package org.pentaho.metastore.api;

import java.util.List;

import org.pentaho.metastore.api.security.IMetaStoreOwnerPermissions;
import org.pentaho.metastore.api.security.IMetaStoreElementOwner;


/**
 * This interface describes the element as an attribute (with children) with security on top of it.
 * @author matt
 *
 */
public interface IMetaStoreElement extends IMetaStoreAttribute {

  
  public IMetaStoreElementOwner getOwner();
  public void setOwner(IMetaStoreElementOwner owner);
  
  public List<IMetaStoreOwnerPermissions> getOwnerPermissionsList(); 
  public void setOwnerPermissionsList(List<IMetaStoreOwnerPermissions> ownerPermissions);
  
}
