package org.pentaho.metastore.api.exceptions;

import java.util.List;

import org.pentaho.metastore.api.IMetaStoreElement;


/** 
 * This exception is thrown in case a data type is created in a metadata store when it already exists.
 * 
 * @author matt
 *
 */

public class MetaStoreElementExistException extends MetaStoreException {
  
  private static final long serialVersionUID = -1658192841342866261L;
  
  private List<IMetaStoreElement> entities;
  
  public MetaStoreElementExistException(List<IMetaStoreElement> entities) {
    super();
    this.entities = entities;
  }

  public MetaStoreElementExistException(List<IMetaStoreElement> entities, String message) {
    super(message);
    this.entities = entities;
  }

  public MetaStoreElementExistException(List<IMetaStoreElement> entities, Throwable cause) {
    super(cause);
    this.entities = entities;
  }
  
  public MetaStoreElementExistException(List<IMetaStoreElement> entities, String message, Throwable cause) {
    super(message, cause);
    this.entities = entities;
  }

  /**
   * @return the entities
   */
  public List<IMetaStoreElement> getEntities() {
    return entities;
  }

  /**
   * @param entities the entities to set
   */
  public void setEntities(List<IMetaStoreElement> entities) {
    this.entities = entities;
  }
}
