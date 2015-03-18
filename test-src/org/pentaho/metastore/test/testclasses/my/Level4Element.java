package org.pentaho.metastore.test.testclasses.my;

import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

import java.util.List;

/**
 * @author Rowell Belen
 */
@MetaStoreElementType(
    name = "Level4Element",
    description = "My Level 4 Element" )
public class Level4Element {

  @MetaStoreAttribute
  private String name;

  @MetaStoreAttribute
  List<MyOtherElement> myElements;

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public List<MyOtherElement> getMyElements() {
    return myElements;
  }

  public void setMyElements( List<MyOtherElement> myElements ) {
    this.myElements = myElements;
  }
}
