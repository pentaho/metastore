package org.pentaho.metastore.stores.memory;

import java.util.Iterator;
import java.util.List;

import org.pentaho.metastore.api.IMetaStoreElement;

public class MemoryMetaStoreElement implements IMetaStoreElement {

  private String id;
  private Object value;

  private List<IMetaStoreElement> children;

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the value
   */
  public Object getValue() {
    return value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(Object value) {
    this.value = value;
  }

  /**
   * @return the children
   */
  public List<IMetaStoreElement> getChildren() {
    return children;
  }

  /**
   * @param children the children to set
   */
  public void setChildren(List<IMetaStoreElement> children) {
    this.children = children;
  }

  @Override
  public void addChild(IMetaStoreElement entity) {
    children.add(entity);
  }

  @Override
  public void deleteChild(String entityId) {
    Iterator<IMetaStoreElement> iterator = children.iterator();
    while (iterator.hasNext()) {
      IMetaStoreElement next = iterator.next();
      if (next.getId().equals(entityId)) {
        iterator.remove();
        break;
      }
    }
  }
  
  
}
