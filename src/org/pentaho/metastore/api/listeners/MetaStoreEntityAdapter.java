package org.pentaho.metastore.api.listeners;

import org.pentaho.metastore.api.IMetaStoreElement;

/**
 * Convenience listener adapter for listening to meta store entity changes.
 * 
 * @author matt
 *
 */
public class MetaStoreEntityAdapter implements MetaStoreElementListener {

  @Override
  public void entityUpdated(String namespace, String dataTypeId, IMetaStoreElement oldEntity, IMetaStoreElement newEntity) {
  }

  @Override
  public void entityCreated(String namespace, String dataTypeId, IMetaStoreElement entity) {
  }

  @Override
  public void entityDeleted(String namespace, String dataTypeId, IMetaStoreElement entity) {
  }

}
