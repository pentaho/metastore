package org.pentaho.metastore.stores.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlMetaStoreElement implements IMetaStoreElement {

  public static final String XML_TAG = "entity";
  
  private String id;
  private Object value;

  private List<IMetaStoreElement> children;
  
  private String filename;
  
  public XmlMetaStoreElement() {
    children = new ArrayList<IMetaStoreElement>();
    this.id = null;
    this.value = null;
  }
  
  public XmlMetaStoreElement(String id, Object value) {
    this();
    this.id = id;
    this.value = value;
  }

  /**
   * Load entity data recursively from an XML file...
   * @param filename The file to load the entity (with children) from.
   * @throws MetaStoreException In case there is a problem reading the file.
   */
  public XmlMetaStoreElement(String filename) throws MetaStoreException {
    this();
    File file = new File(filename);
    this.id = file.getName();

    try {
      
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      Document document = documentBuilder.parse(file);
      Element dataTypeElement = document.getDocumentElement();
      
      loadElement(dataTypeElement);
    } catch(Exception e) {
      throw new MetaStoreException("Unable to load XML metastore entity from file '"+filename+"'", e);
    }
  }
  
  /**
   * Duplicate the entity data into this structure.
   * @param entity
   */
  public XmlMetaStoreElement(IMetaStoreElement entity) {
    this();
    id = entity.getId();
    value = entity.getValue();
    for (IMetaStoreElement childEntity : entity.getChildren()) {
      addChild(new XmlMetaStoreElement(childEntity));
    }
  }

  private void loadElement(Node dataTypeNode) {
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
            XmlMetaStoreElement childEntity = new XmlMetaStoreElement();
            childEntity.loadElement(childNode);
            getChildren().add(childEntity);
          }
        }
      }
    }
  }

  @Override
  public void deleteChild(String entityId) {
    Iterator<IMetaStoreElement> it = children.iterator();
    while (it.hasNext()) {
      IMetaStoreElement entity= it.next();
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
  public List<IMetaStoreElement> getChildren() {
    return children;
  }

  /**
   * @param children
   *          the children to set
   */
  public void setChildren(List<IMetaStoreElement> children) {
    this.children = children;
  }

  @Override
  public void addChild(IMetaStoreElement entity) {
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

  public void save() throws MetaStoreException {
    
    try {
      
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.newDocument();
      
      Element entityElement = doc.createElement(XML_TAG);
      doc.appendChild(entityElement);

      appendEntityElement(this, doc, entityElement);
      
      // Write the document content into the data type XML file
      //
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformerFactory.setAttribute("indent-number", 2);
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      DOMSource source = new DOMSource(doc);
      StreamResult result = new StreamResult(new File(filename));
      
      // Do the actual saving...
      transformer.transform(source, result);
    } catch(Exception e) {
      throw new MetaStoreException("Unable to save XML meta store entity to file '"+filename+"'", e);
    }
  }

  private void appendEntityElement(IMetaStoreElement entity, Document doc, Element parentElement) {
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
      for (IMetaStoreElement childEntity : entity.getChildren()) {
        Element childElement = doc.createElement("child");
        childrenElement.appendChild(childElement);
        appendEntityElement(childEntity, doc, childElement);
      }
    }
  }

  private String getType(Object object) {
    
    if (object==null) return "String";
    if (object instanceof String) return "String";
    if (object instanceof Integer) return "Integer";
    if (object instanceof Long) return "Long";
    if (object instanceof Double) return "Double";
    
    return "String";
  }

}
