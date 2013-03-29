package org.pentaho.metastore.api;

import org.pentaho.metastore.api.security.Base64TwoWayPasswordEncoder;
import org.pentaho.metastore.api.security.ITwoWayPasswordEncoder;


public class BaseMetaStore {

  protected String name;

  protected String description;
  
  protected ITwoWayPasswordEncoder passwordEncoder;

  public BaseMetaStore() {
    passwordEncoder = new Base64TwoWayPasswordEncoder();
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
  public void setName(String name) {
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
  public void setDescription(String description) {
    this.description = description;
  }

  public ITwoWayPasswordEncoder getTwoWayPasswordEncoder() {
    return passwordEncoder;
  }

  public void setTwoWayPasswordEncoder(ITwoWayPasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

}
