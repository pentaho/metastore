package org.pentaho.metastore.api.listeners;

import org.pentaho.metastore.api.IMetaStoreElementType;

/**
 * Convenience class to facilitate creating data type listeners.
 * 
 * @author matt
 *
 */
public class MetaStoreDataTypeAdapter implements MetaStoreElementTypeListener {

  @Override
  public void dataTypeCreated(String namespace, IMetaStoreElementType dataType) {
  }

  @Override
  public void dataTypeDeleted(String namespace, IMetaStoreElementType dataType) {
  }

  @Override
  public void dataTypeUpdated(String namespace, IMetaStoreElementType oldDataType, IMetaStoreElementType newDataType) {
  }

}
