package org.pentaho.metastore.test.testclasses.my;

import org.pentaho.metastore.persist.MetaStoreAttribute;

public class MyElementAttr {

  @MetaStoreAttribute
  private String key;

  @MetaStoreAttribute
  private String value;

  @MetaStoreAttribute
  private String description;

  public MyElementAttr() {
    // Empty constructor needed for the meta store factory
  }

  public MyElementAttr( String key, String value, String description ) {
    this();
    this.key = key;
    this.value = value;
    this.description = description;
  }

  public String getKey() {
    return key;
  }

  public void setKey( String key ) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue( String value ) {
    this.value = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }
}
