package org.pentaho.metastore.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
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
  
  /**
   * Get a sorted list of element names for the specified element type in the given namespace.
   * 
   * @param namespace
   * @param metaStore
   * @param elementType
   * @return
   * @throws MetaStoreException
   */
  public String[] getElementNames(String namespace, IMetaStore metaStore, IMetaStoreElementType elementType) throws MetaStoreException {
    List<String> names = new ArrayList<String>();
    
    List<IMetaStoreElement> elements = metaStore.getElements(namespace, elementType.getId());
    for (IMetaStoreElement element :  elements) {
      names.add(element.getName());
    }
    
    // Alphabetical sort
    //
    Collections.sort(names);
    
    return names.toArray(new String[names.size()]);
  }
}
