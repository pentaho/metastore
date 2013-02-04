package org.pentaho.metastore.api.exceptions;

import java.util.List;

import org.pentaho.metastore.api.IMetaStoreElementType;


/** 
 * This exception is thrown in case an entity is created in a metadata store when it already exists for the certain namespace and data type.
 * 
 * @author matt
 *
 */

public class MetaStoreElementTypeExistsException extends MetaStoreException {
  
  private static final long serialVersionUID = -1658192841342866261L;
  
  private List<IMetaStoreElementType> dataTypes;
  
  public MetaStoreElementTypeExistsException(List<IMetaStoreElementType> dataTypes) {
    super();
    this.dataTypes = dataTypes;
  }

  public MetaStoreElementTypeExistsException(List<IMetaStoreElementType> dataTypes, String message) {
    super(message);
    this.dataTypes = dataTypes;
  }

  public MetaStoreElementTypeExistsException(List<IMetaStoreElementType> dataTypes, Throwable cause) {
    super(cause);
    this.dataTypes = dataTypes;
  }
  
  public MetaStoreElementTypeExistsException(List<IMetaStoreElementType> dataTypes, String message, Throwable cause) {
    super(message, cause);
    this.dataTypes = dataTypes;
  }

  public void setDataTypes(List<IMetaStoreElementType> dataTypes) {
    this.dataTypes = dataTypes;
  }
  
  public List<IMetaStoreElementType> getDataTypes() {
    return dataTypes;
  }
}
