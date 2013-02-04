package org.pentaho.metastore.api.exceptions;


/** 
 * This exception is thrown in case a namespace is created in a metadata store when it already exists.
 * 
 * @author matt
 *
 */

public class MetaStoreNamespaceExistsException extends MetaStoreException {
  
  private static final long serialVersionUID = 2614122560674499038L;
  
  public MetaStoreNamespaceExistsException() {
    super();
  }

  public MetaStoreNamespaceExistsException(String message) {
    super(message);
  }

  public MetaStoreNamespaceExistsException(Throwable cause) {
    super(cause);
  }
  
  public MetaStoreNamespaceExistsException(String message, Throwable cause) {
    super(message, cause);
  }  
}
