package org.pentaho.metastore.api.security;

import java.util.List;

/**
 * Get the permissions for a certain owner.
 * 
 * @author matt
 */
public interface IMetaStoreOwnerPermissions {
  public IMetaStoreElementOwner getOwner();
  public List<MetaStoreObjectPermission> getPermissions();
}
