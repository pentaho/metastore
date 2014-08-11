package org.pentaho.metastore.test.testclasses.my;

public class MyNameElement {
  private String name;
  private String description;
  private String color;

  public MyNameElement( String name, String description, String color ) {
    super();
    this.name = name;
    this.description = description;
    this.color = color;
  }

  @Override
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( !( obj instanceof MyNameElement ) ) {
      return false;
    }
    MyNameElement my = (MyNameElement) obj;
    return name.equals( my.name ) && description.equals( my.description ) && color.equals( my.color );
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public String getColor() {
    return color;
  }

  public void setColor( String color ) {
    this.color = color;
  }
}
