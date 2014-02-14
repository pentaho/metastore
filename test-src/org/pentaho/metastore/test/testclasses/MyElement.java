package org.pentaho.metastore.test.testclasses;

import java.util.Date;

import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

@MetaStoreElementType(
  name = "My element type",
  description = "This is my element type" )
public class MyElement {
  private String name;

  @MetaStoreAttribute( key = "my_attribute" )
  private String myAttribute;

  @MetaStoreAttribute
  private String anotherAttribute;

  @MetaStoreAttribute
  private int intAttribute;

  @MetaStoreAttribute
  private long longAttribute;

  @MetaStoreAttribute
  private boolean boolAttribute;

  @MetaStoreAttribute
  private Date dateAttribute;

  /** 
   * We need the empty constructor for our factory.  Having any other constructor is fine too, but this one is mandatory!
   */
  public MyElement() {
  }

  public MyElement( String name, String myAttribute, String anotherAttribute, int intAttribute, long longAttribute, boolean boolAttribute, Date dateAttribute ) {
    super();
    this.name = name;
    this.myAttribute = myAttribute;
    this.anotherAttribute = anotherAttribute;
    this.intAttribute = intAttribute;
    this.longAttribute = longAttribute;
    this.boolAttribute = boolAttribute;
    this.dateAttribute = dateAttribute;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getMyAttribute() {
    return myAttribute;
  }

  public void setMyAttribute( String myAttribute ) {
    this.myAttribute = myAttribute;
  }

  public String getAnotherAttribute() {
    return anotherAttribute;
  }

  public void setAnotherAttribute( String anotherAttribute ) {
    this.anotherAttribute = anotherAttribute;
  }

  public int getIntAttribute() {
    return intAttribute;
  }

  public void setIntAttribute( int intAttribute ) {
    this.intAttribute = intAttribute;
  }

  public boolean isBoolAttribute() {
    return boolAttribute;
  }

  public void setBoolAttribute( boolean boolAttribute ) {
    this.boolAttribute = boolAttribute;
  }

  public long getLongAttribute() {
    return longAttribute;
  }

  public void setLongAttribute( long longAttribute ) {
    this.longAttribute = longAttribute;
  }

  public Date getDateAttribute() {
    return dateAttribute;
  }

  public void setDateAttribute( Date dateAttribute ) {
    this.dateAttribute = dateAttribute;
  }
}
