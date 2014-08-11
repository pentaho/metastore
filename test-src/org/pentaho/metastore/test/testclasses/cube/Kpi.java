package org.pentaho.metastore.test.testclasses.cube;

import org.pentaho.metastore.persist.MetaStoreAttribute;

public class Kpi {
  @MetaStoreAttribute
  private String name;

  @MetaStoreAttribute
  private String description;

  @MetaStoreAttribute
  private String otherDetails;

  public Kpi() {
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

  /**
   * @return the otherDetails
   */
  public String getOtherDetails() {
    return otherDetails;
  }

  /**
   * @param otherDetails the otherDetails to set
   */
  public void setOtherDetails( String otherDetails ) {
    this.otherDetails = otherDetails;
  }
}
