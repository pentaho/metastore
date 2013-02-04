package org.pentaho.metastore.stores.xml;

import java.io.File;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlUtil {

  public static final String META_FOLDER_NAME = "metastore";
  public static final String DATA_TYPE_FILE_NAME = ".type.xml";

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

  public static String getDataTypeFolder(String rootFolder, String namespace, String dataTypeId) {
    return getNamespaceFolder(rootFolder, namespace) + File.separator + dataTypeId;
  }

  public static String getDataTypeFile(String rootFolder, String namespace, String dataTypeId) {
    return getDataTypeFolder(rootFolder, namespace, dataTypeId) + File.separator + DATA_TYPE_FILE_NAME;
  }

  public static String getEntityFile(String rootFolder, String namespace, String dataTypeId, String entityId) {
    return getDataTypeFolder(rootFolder, namespace, dataTypeId) + File.separator + entityId + ".xml";
  }
}
