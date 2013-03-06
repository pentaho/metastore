package org.pentaho.metastore.stores.xml;

import java.io.File;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlUtil {

  public static final String META_FOLDER_NAME = "metastore";
  public static final String ELEMENT_TYPE_FILE_NAME = ".type.xml";

  public static String getNodeValue(Node node) {
    if (node == null)
      return null;

    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.TEXT_NODE) {
        return child.getNodeValue();
      }
    }
    return null;
  }

  public static String getNamespaceFolder(String rootFolder, String namespace) {
    return rootFolder + File.separator + namespace;
  }

  public static String getElementTypeFolder(String rootFolder, String namespace, String elementTypeId) {
    return getNamespaceFolder(rootFolder, namespace) + File.separator + elementTypeId;
  }

  public static String getElementTypeFile(String rootFolder, String namespace, String elementTypeId) {
    return getElementTypeFolder(rootFolder, namespace, elementTypeId) + File.separator + ELEMENT_TYPE_FILE_NAME;
  }

  public static String getElementFile(String rootFolder, String namespace, String elementTypeId, String elementId) {
    return getElementTypeFolder(rootFolder, namespace, elementTypeId) + File.separator + elementId + ".xml";
  }
}
