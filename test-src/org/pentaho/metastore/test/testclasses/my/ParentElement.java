package org.pentaho.metastore.test.testclasses.my;


import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

@MetaStoreElementType(
  name = "ParentElement",
  description = "ParentElement" )
public class ParentElement {

  @MetaStoreAttribute
  private String name;

  @MetaStoreAttribute
  private ChildElement childElement;

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public ChildElement getChildElement() {
    return childElement;
  }

  public void setChildElement( ChildElement childElement ) {
    this.childElement = childElement;
  }
}
