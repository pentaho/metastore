package org.pentaho.metastore.api;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.metastore.api.listeners.MetaStoreElementTypeListener;
import org.pentaho.metastore.api.listeners.MetaStoreElementListener;

public class BaseMetaStore {
  
  private String name;
  private String description;
  private String lifeCycle;
  private String customerName;
  private String projectName;
  
  protected List<MetaStoreElementTypeListener> dataTypeListeners;
  protected List<MetaStoreElementListener> entityListeners;
  
  public BaseMetaStore() {
    dataTypeListeners = new ArrayList<MetaStoreElementTypeListener>();
    entityListeners = new ArrayList<MetaStoreElementListener>();
  }
  
  /**
   * Add a listener to get informed of all changes to an entity
   * @param entityListener
   */
  public void addElementListener(MetaStoreElementListener entityListener) {
    entityListeners.add(entityListener);
  }
  
  public List<MetaStoreElementListener> getElementListeners() {
    return entityListeners;
  }
  
  
  public void removeElementListener(MetaStoreElementListener entityListener) {
    entityListeners.remove(entityListener);
  }
  
  /**
   * Add a listener to get informed of all changes to an dataType
   * @param dataTypeListener
   */
  public void addElementTypeListener(MetaStoreElementTypeListener dataTypeListener) {
    dataTypeListeners.add(dataTypeListener);
  }
  
  public List<MetaStoreElementTypeListener> getElementTypeListeners() {
    return dataTypeListeners;
  }
  
  
  public void removeElementTypeListener(MetaStoreElementTypeListener dataTypeListener) {
    dataTypeListeners.remove(dataTypeListener);
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
  /**
   * @return the lifeCycle
   */
  public String getLifeCycle() {
    return lifeCycle;
  }
  /**
   * @param lifeCycle the lifeCycle to set
   */
  public void setLifeCycle(String lifeCycle) {
    this.lifeCycle = lifeCycle;
  }
  /**
   * @return the customerName
   */
  public String getCustomerName() {
    return customerName;
  }
  /**
   * @param customerName the customerName to set
   */
  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }
  /**
   * @return the projectName
   */
  public String getProjectName() {
    return projectName;
  }
  /**
   * @param projectName the projectName to set
   */
  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }
  
}
