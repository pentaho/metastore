package org.pentaho.metastore.api.listeners;

import org.pentaho.metastore.api.IMetaStoreElement;

/**
 * Set of methods that are called in various parts of the meta store entity life-cycle.
 * @author matt
 *
 */
public interface MetaStoreElementListener {
  
  /**
   * This method is called after an entity was created in the store
   *
   * @param namespace The namespace of the entity
   * @param dataTypeId The data type ID of the entity
   * @param entity The entity that was created
   */
  public void entityCreated(String namespace, String dataTypeId, IMetaStoreElement entity);

  /**
   * This method is called when an entity is changed
   *
   * @param namespace The namespace of the entity
   * @param dataType The data type of the entity
   * @param oldEntity The entity before the change
   * @param newEntity The entity after the change 
   */
  public void entityUpdated(String namespace, String dataTypeId, IMetaStoreElement oldEntity, IMetaStoreElement newEntity);

  /**
   * This method is called after an entity was deleted from the store
   *
   * @param namespace The namespace of the entity
   * @param dataType The data type ID of the entity
   * @param entity The entity that was deleted
   */
  public void entityDeleted(String namespace, String dataTypeId, IMetaStoreElement entity);
}
