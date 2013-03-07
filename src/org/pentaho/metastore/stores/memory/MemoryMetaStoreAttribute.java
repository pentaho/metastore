package org.pentaho.metastore.stores.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.pentaho.metastore.api.IMetaStoreAttribute;

public class MemoryMetaStoreAttribute implements IMetaStoreAttribute {

  protected String id;
  protected Object value;

  protected Map<String, IMetaStoreAttribute> children;

  public MemoryMetaStoreAttribute() {
    this(null, null);
  }
  
  public MemoryMetaStoreAttribute(String id, Object value) {
    this.id = id;
    this.value = value;
    children = new HashMap<String, IMetaStoreAttribute>();
  }

  public MemoryMetaStoreAttribute(IMetaStoreAttribute attribute) {
    this(attribute.getId(), attribute.getValue());

    for (IMetaStoreAttribute childElement : attribute.getChildren()) {
      addChild(new MemoryMetaStoreAttribute(childElement));
    }
  }

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
  public List<IMetaStoreAttribute> getChildren() {
    return new ArrayList<IMetaStoreAttribute>(children.values());
  }

  /**
   * @param children the children to set
   */
  public void setChildren(List<IMetaStoreAttribute> children) {
    this.children.clear();
    for (IMetaStoreAttribute child : children) {
      this.children.put(child.getId(), child);
    }
  }

  @Override
  public void addChild(IMetaStoreAttribute attribute) {
    children.put(attribute.getId(), attribute);
  }

  @Override
  public void deleteChild(String attributeId) {
    Iterator<IMetaStoreAttribute> iterator = children.values().iterator();
    while (iterator.hasNext()) {
      IMetaStoreAttribute next = iterator.next();
      if (next.getId().equals(attributeId)) {
        iterator.remove();
        break;
      }
    }
  }

  /**
   * Remove all child attributes
   */
  public void clearChildren() {
    children.clear();
  }
  
  public IMetaStoreAttribute getChild(String id) {
    return children.get(id);
  }
}
