package org.pentaho.metastore.persist;

import java.util.Map;

import org.pentaho.metastore.api.exceptions.MetaStoreException;

public interface IMetaStoreObjectFactory {

  /**
   * @param className the class to instantiate
   * @param context the context to use for the instatiation
   * @return the instantiated class 
   * 
   * */
  public Object instantiateClass( String className, Map<String, String> context ) throws MetaStoreException;

  /** Extract plugin contextual information from the specified plugin object 
   * @param pluginObject the object to analyze
   * */
  public Map<String, String> getContext( Object pluginObject ) throws MetaStoreException;

}
