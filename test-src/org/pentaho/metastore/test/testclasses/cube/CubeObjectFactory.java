package org.pentaho.metastore.test.testclasses.cube;

import java.util.Map;

import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.persist.IMetaStoreObjectFactory;

public class CubeObjectFactory implements IMetaStoreObjectFactory {

  @Override
  public Object instantiateClass( String className, Map<String, String> context ) throws MetaStoreException {
    try {
      return Class.forName( className ).newInstance();
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to instantiate class: " + className, e );
    }
  }

  @Override
  public Map<String, String> getContext( Object pluginObject ) throws MetaStoreException {
    return null;
  }

}
