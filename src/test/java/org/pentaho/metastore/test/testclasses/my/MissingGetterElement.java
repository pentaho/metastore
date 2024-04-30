package org.pentaho.metastore.test.testclasses.my;

import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

@MetaStoreElementType(
        name = "custom",
        description = "This is Missing Getter element type" )
public class MissingGetterElement {

  @MetaStoreAttribute
  private String property1;

  @MetaStoreAttribute
  private String property2;

  public String getProperty1() { return property1; }

  public MissingGetterElement( String property1, String property2 ) {
    this.property1 = property1;
    this.property2 = property2;
    }
}
