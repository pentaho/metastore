package org.pentaho.metastore.test.testclasses.factory;

import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

@MetaStoreElementType( name = "B", description = "Test class B " )
public class B {

  private String name;

  @MetaStoreAttribute
  private boolean shared;

  public B() {
  }

  public B( String name, boolean shared ) {
    this();
    this.name = name;
    this.shared = shared;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName( String name ) {
    this.name = name;
  }

  /**
   * @return the shared
   */
  public boolean isShared() {
    return shared;
  }

  /**
   * @param shared the shared to set
   */
  public void setShared( boolean shared ) {
    this.shared = shared;
  }
}
