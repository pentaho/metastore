package org.pentaho.metastore.stores.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlMetaStoreAttribute implements IMetaStoreAttribute {

  public static final String XML_TAG = "attribute";
  
  protected String id;
  protected Object value;

  protected List<IMetaStoreAttribute> children;
  
  protected String filename;
  
  public XmlMetaStoreAttribute() {
    children = new ArrayList<IMetaStoreAttribute>();
    this.id = null;
    this.value = null;
  }
  
  public XmlMetaStoreAttribute(String id, Object value) {
    this();
    this.id = id;
    this.value = value;
  }


  
  /**
   * Duplicate the entity data into this structure.
   * @param entity
   */
  public XmlMetaStoreAttribute(IMetaStoreAttribute entity) {
    this();
    id = entity.getId();
    value = entity.getValue();
    for (IMetaStoreAttribute childEntity : entity.getChildren()) {
      addChild(new XmlMetaStoreAttribute(childEntity));
    }
  }

  protected void loadElement(Node dataTypeNode) {
    NodeList elementNodes = dataTypeNode.getChildNodes();
    for (int e=0;e<elementNodes.getLength();e++) {
      Node elementNode = elementNodes.item(e);
      if ("id".equals(elementNode.getNodeName())) {
        id = XmlUtil.getNodeValue(elementNode);
      } else if ("value".equals(elementNode.getNodeName())) {
        value = XmlUtil.getNodeValue(elementNode);
      } else if ("type".equals(elementNode.getNodeName())) {
        String type = XmlUtil.getNodeValue(elementNode);
        if ("Integer".equals(type)) {
          value = Integer.valueOf((String)value);
        } else if ("Double".equals(type)) {
          value = Double.valueOf((String)value);
        } else if ("Long".equals(type)) {
          value = Long.valueOf((String)value);
        } else {
          value = (String)value;
        }
      } else if ("children".equals(elementNode.getNodeName())) {
        NodeList childNodes = elementNode.getChildNodes();
        for (int c=0;c<childNodes.getLength();c++) {
          Node childNode = childNodes.item(c);
          if (childNode.getNodeName().equals("child")) {
            XmlMetaStoreAttribute childEntity = new XmlMetaStoreAttribute();
            childEntity.loadElement(childNode);
            getChildren().add(childEntity);
          }
        }
      }
    }
  }

  @Override
  public void deleteChild(String entityId) {
    Iterator<IMetaStoreAttribute> it = children.iterator();
    while (it.hasNext()) {
      IMetaStoreAttribute entity= it.next();
      if (entity.getId().equals(entityId)) {
        it.remove();
        return;
      }
    }
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id
   *          the id to set
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
   * @param value
   *          the value to set
   */
  public void setValue(Object value) {
    this.value = value;
  }

  /**
   * @return the children
   */
  public List<IMetaStoreAttribute> getChildren() {
    return children;
  }

  /**
   * @param children
   *          the children to set
   */
  public void setChildren(List<IMetaStoreAttribute> children) {
    this.children = children;
  }

  public void addChild(IMetaStoreAttribute entity) {
    children.add(entity);
  }

  /**
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }

  /**
   * @param filename the filename to set
   */
  public void setFilename(String filename) {
    this.filename = filename;
  }

  protected void appendElement(IMetaStoreAttribute entity, Document doc, Element parentElement) {
    Element idElement = doc.createElement("id");
    idElement.appendChild(doc.createTextNode(entity.getId()));
    parentElement.appendChild(idElement);
    
    Element valueElement = doc.createElement("value");
    valueElement.appendChild(doc.createTextNode(entity.getValue()!=null ? entity.getValue().toString() : ""));
    parentElement.appendChild(valueElement);
    
    Element typeElement = doc.createElement("type");
    typeElement.appendChild(doc.createTextNode(getType(entity.getValue())));
    parentElement.appendChild(typeElement);
    
    if (!entity.getChildren().isEmpty()) {
      Element childrenElement = doc.createElement("children");
      parentElement.appendChild(childrenElement);
      for (IMetaStoreAttribute childEntity : entity.getChildren()) {
        Element childElement = doc.createElement("child");
        childrenElement.appendChild(childElement);
        appendElement(childEntity, doc, childElement);
      }
    }
  }

  protected String getType(Object object) {
    
    if (object==null) return "String";
    if (object instanceof String) return "String";
    if (object instanceof Integer) return "Integer";
    if (object instanceof Long) return "Long";
    if (object instanceof Double) return "Double";
    
    return "String";
  }

}
