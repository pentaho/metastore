package org.pentaho.metastore.test.testclasses.factory_shared;

import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

@MetaStoreElementType( name = "Y", description = "Test class Y " )
public class Y {

  private String name;

  @MetaStoreAttribute
  private String description;

  public Y() {
  }

  public Y( String name, String description ) {
    this();
    this.name = name;
    this.description = description;
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
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription( String description ) {
    this.description = description;
  }
}
