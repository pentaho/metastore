package org.pentaho.metastore.util;

import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

/**
 * Generally useful methods for extracting data
 * 
 * @author matt
 *
 */
public class MetaStoreUtil {
  
  /**
   * Create the specified namespace if it doesn't exist.
   * @param metaStore
   * @param namespace
   * @throws MetaStoreException
   */
  public static void verifyNamespaceCreated(IMetaStore metaStore, String namespace) throws MetaStoreException {
    if (!metaStore.namespaceExists(namespace)) {
      metaStore.createNamespace(namespace);
    }
  }
  
  public static String getChildString(IMetaStoreAttribute attribute, String id) {
    IMetaStoreAttribute child = attribute.getChild(id);
    if (child==null) {
      return null;
    }
    
    return getAttributeString(child);
  }
  
  public static String getAttributeString(IMetaStoreAttribute attribute) {
    if (attribute.getValue()==null) {
      return null;
    }
    return attribute.getValue().toString();
  }
  
}
