package org.pentaho.metastore.test.testclasses.my;

import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

/**
 * @author Rowell Belen
 */
@MetaStoreElementType(
    name = "Level3Element",
    description = "My Level 3 Element" )
public class Level3Element {

  @MetaStoreAttribute
  private String name;

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

}
