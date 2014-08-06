package org.pentaho.metastore.test.testclasses.my;

import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

@MetaStoreElementType(
  name = "My other element type",
  description = "Another element type to test with" )
public class MyOtherElement {
  private String name;

  @MetaStoreAttribute( key = "some_attribute" )
  private String someAttribute;

  /**
   * Empty constructor is needed by MetaStoreFactory to instantiate this object
   */
  public MyOtherElement() {
  }

  /**
   * @param name
   * @param someAttribute
   */
  public MyOtherElement( String name, String someAttribute ) {
    this();
    this.name = name;
    this.someAttribute = someAttribute;
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
   * @return the someAttribute
   */
  public String getSomeAttribute() {
    return someAttribute;
  }

  /**
   * @param someAttribute the someAttribute to set
   */
  public void setSomeAttribute( String someAttribute ) {
    this.someAttribute = someAttribute;
  }
}
