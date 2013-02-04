package org.pentaho.metastore.api;

import org.pentaho.metastore.api.exceptions.MetaStoreException;

/**
 * This interface is used to describe the object that is stored in the meta store.
 * 
 * @author matt
 *
 */
public interface IMetaStoreElementType {
  
  /**
   * @return The namespace to which this data type belongs.
   */
  public String getNamespace();
  
  /**
   * @param namespace The namespace to set.
   */
  public void setNamespace(String namespace);
  
  /**
   * @return The ID of the data type, unique in a namespace
   */
  public String getId();
  
  /**
   * Set an ID on a data type
   * @param id
   */
  public void setId(String id);
  
  /**
   * @return The name of the data type
   */
  public String getName();
  
  /**
   * @param name The data type name to set
   */
  public void setName(String name);
  
  /**
   * @return The description of the data type
   */
  public String getDescription();
  
  /**
   * The description to set.
   * @param description
   */
  public void setDescription(String description);
  
  /**
   * Persist the data type definition
   * @throws MetaStoreException In case there is an error in the underlying store.
   * 
   */
  public void save() throws MetaStoreException;
}
