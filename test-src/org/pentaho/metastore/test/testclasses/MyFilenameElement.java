package org.pentaho.metastore.test.testclasses;

public class MyFilenameElement {
  private String filename;
  private String size;
  private String gender;

  public MyFilenameElement( String filename, String size, String gender ) {
    super();
    this.filename = filename;
    this.size = size;
    this.gender = gender;
  }

  @Override
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( !( obj instanceof MyFilenameElement ) ) {
      return false;
    }
    MyFilenameElement my = (MyFilenameElement) obj;
    return filename.equals( my.filename ) && size.equals( my.size ) && gender.equals( my.gender );
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename( String filename ) {
    this.filename = filename;
  }

  public String getSize() {
    return size;
  }

  public void setSize( String size ) {
    this.size = size;
  }

  public String getGender() {
    return gender;
  }

  public void setGender( String gender ) {
    this.gender = gender;
  }

}
