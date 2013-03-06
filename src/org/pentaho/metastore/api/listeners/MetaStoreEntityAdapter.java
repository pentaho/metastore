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
  public void elementUpdated(String namespace, String dataTypeId, IMetaStoreElement oldEntity, IMetaStoreElement newEntity) {
  }

  @Override
  public void elementCreated(String namespace, String dataTypeId, IMetaStoreElement entity) {
  }

  @Override
  public void elementDeleted(String namespace, String dataTypeId, IMetaStoreElement entity) {
  }

}
