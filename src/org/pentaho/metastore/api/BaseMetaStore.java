package org.pentaho.metastore.api;

/**
 * This class implements common and/or default functionality between IMetaStore instances
 */
public abstract class BaseMetaStore implements IMetaStore {

  /** The name of this metastore. */
  protected String name;

  /** The description of this metastore. */
  protected String description;
  
  /**
   * Instantiates a new base meta store.
   */
  public BaseMetaStore() {
    passwordEncoder = new Base64TwoWayPasswordEncoder();
  }

  /**
   * Gets the name of this metastore.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of this metastore.
   *
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the description of this metastore.
   *
   * @return the description of this metastore
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description for this metastore.
   *
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
