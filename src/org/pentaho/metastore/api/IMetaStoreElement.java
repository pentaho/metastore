package org.pentaho.metastore.api;

import java.util.List;

public interface IMetaStoreElement {
  /**
   * @return The ID or key of the metastore entity
   */
  public String getId();
  
  /**
   * @param id The ID or key of the entity to set.
   */
  public void setId(String id);
  
  /**
   * @return The value of the entity
   */
  public Object getValue();
  
  /**
   * @param value The entity value to set. 
   */
  public void setValue(Object value);
  
  /**
   * @return A list of the child entities 
   */
  public List<IMetaStoreElement> getChildren();
  
  /**
   * @param entity The entity to add
   */
  public void addChild(IMetaStoreElement entity);
  
  /**
   * @param entityId The ID or key of the entity to delete
   */
  public void deleteChild(String entityId);
}
