package org.pentaho.metastore.stores.xml;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlMetaStoreElementType implements IMetaStoreElementType {
  
  public static final String XML_TAG = "data-type";

  private String namespace;
  private String id;
  private String name;
  private String description;

  private String filename;
  
  /**
   * @param namespace
   * @param id
   * @param name
   * @param description
   */
  public XmlMetaStoreElementType(String namespace, String id, String name, String description) {
    this.namespace = namespace;
    this.id = id;
    this.name = name;
    this.description = description;
  }
  
  /**
   * Load an XML meta data store data type from file.
   * @param namespace the namespace 
   * @param filename the file to load from
   */
  public XmlMetaStoreElementType(String namespace, String filename) throws MetaStoreException {
    this.namespace = namespace;
    
    File file = new File(filename);
    this.id = file.getParentFile().getName();
    
    try {
    
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      Document document = documentBuilder.parse(file);
      Element dataTypeElement = document.getDocumentElement();
      
      name = dataTypeElement.getElementsByTagName("name").item(0).getNodeValue();
      description = dataTypeElement.getElementsByTagName("description").item(0).getNodeValue();      
    } catch(Exception e) {
      throw new MetaStoreException("Unable to load XML metastore data type from file '"+filename+"'", e);
    }
  }
  
  public void save() throws MetaStoreException {
    try {
      
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.newDocument();
      
      Element dataTypeElement = doc.createElement(XML_TAG);
      doc.appendChild(dataTypeElement);
      
      Element nameElement = doc.createElement("name");
      nameElement.appendChild(doc.createTextNode(name));
      dataTypeElement.appendChild(nameElement);
      
      Element descriptionElement = doc.createElement("description");
      descriptionElement.appendChild(doc.createTextNode(description));
      dataTypeElement.appendChild(descriptionElement);
      
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
      throw new MetaStoreException("Unable to save XML meta store data type with file '"+filename+"'", e);
    }
  }




  /**
   * @return the namespace
   */
  public String getNamespace() {
    return namespace;
  }
  /**
   * @param namespace the namespace to set
   */
  public void setNamespace(String namespace) {
    this.namespace = namespace;
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


}
