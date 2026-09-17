/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.metastore.stores.xml;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Stores a metastore attribute in XML form.
 */
public class XmlMetaStoreAttribute implements IMetaStoreAttribute {

  /** The XML tag for an attribute. */
  public static final String XML_TAG = "attribute";

  protected String id;
  protected Object value;

  protected Map<String, IMetaStoreAttribute> children;

  protected String filename;

  /**
   * Creates an empty XML attribute.
   */
  public XmlMetaStoreAttribute() {
    children = new HashMap<String, IMetaStoreAttribute>();
    this.id = null;
    this.value = null;
  }

  /**
   * Creates an XML attribute with an ID and value.
   *
   * @param id the attribute ID
   * @param value the attribute value
   */
  public XmlMetaStoreAttribute( String id, Object value ) {
    this();
    this.id = id;
    this.value = value;
  }

  /**
  * Copies an attribute and its children.
  *
  * @param element the attribute to copy
   */
  public XmlMetaStoreAttribute( IMetaStoreAttribute element ) {
    this();
    id = element.getId();
    value = element.getValue();
    for ( IMetaStoreAttribute childElement : element.getChildren() ) {
      addChild( new XmlMetaStoreAttribute( childElement ) );
    }
  }

  protected void loadAttribute( Node attributeNode ) {
    NodeList elementNodes = attributeNode.getChildNodes();
    for ( int index = 0; index < elementNodes.getLength(); index++ ) {
      Node elementNode = elementNodes.item( index );
      String nodeName = elementNode.getNodeName();
      if ( nodeName == null ) {
        continue;
      }
      switch ( nodeName ) {
        case "id":
          id = XmlUtil.getNodeValue( elementNode );
          break;
        case "value":
          value = XmlUtil.getNodeValue( elementNode );
          break;
        case "type":
          loadTypedValue( elementNode );
          break;
        case "children":
          loadChildren( elementNode );
          break;
        default:
          break;
      }
    }
  }

  private void loadTypedValue( Node typeNode ) {
    String type = XmlUtil.getNodeValue( typeNode );
    if ( type == null ) {
      return;
    }
    switch ( type ) {
      case "Integer":
        value = Integer.valueOf( (String) value );
        break;
      case "Double":
        value = Double.valueOf( (String) value );
        break;
      case "Long":
        value = Long.valueOf( (String) value );
        break;
      default:
        break;
    }
  }

  private void loadChildren( Node childrenNode ) {
    NodeList childNodes = childrenNode.getChildNodes();
    for ( int childIndex = 0; childIndex < childNodes.getLength(); childIndex++ ) {
      Node childNode = childNodes.item( childIndex );
      if ( "child".equals( childNode.getNodeName() ) ) {
        XmlMetaStoreAttribute childElement = new XmlMetaStoreAttribute();
        childElement.loadAttribute( childNode );
        addChild( childElement );
      }
    }
  }

  @Override
  public void deleteChild( String entityId ) {
    Iterator<IMetaStoreAttribute> it = children.values().iterator();
    while ( it.hasNext() ) {
      IMetaStoreAttribute element = it.next();
      if ( element.getId().equals( entityId ) ) {
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
  public void setId( String id ) {
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
  public void setValue( Object value ) {
    this.value = value;
  }

  /**
   * @return the children
   */
  public List<IMetaStoreAttribute> getChildren() {
    return new ArrayList<IMetaStoreAttribute>( children.values() );
  }

  /**
   * @param children
   *          the children to set
   */
  public void setChildren( List<IMetaStoreAttribute> children ) {
    this.children.clear();
    for ( IMetaStoreAttribute child : children ) {
      this.children.put( child.getId(), child );
    }
  }

  /**
   * Adds a child attribute.
   *
   * @param element the child attribute
   */
  public void addChild( IMetaStoreAttribute element ) {
    children.put( element.getId(), element );
  }

  @Override
  public void clearChildren() {
    children.clear();
  }

  @Override
  public IMetaStoreAttribute getChild( String id ) {
    return children.get( id );
  }

  /**
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }

  /**
   * @param filename
   *          the filename to set
   */
  public void setFilename( String filename ) {
    this.filename = filename;
  }

  protected Path getFilenamePath() {
    return filename == null ? null : Paths.get( filename ).normalize();
  }

  protected void appendAttribute( IMetaStoreAttribute attribute, Document doc, Element parentElement )
    throws MetaStoreException {
    if ( attribute.getId() == null ) {
      throw new MetaStoreException( "An attribute has to be non-null" );
    }
    Element idElement = doc.createElement( "id" );
    idElement.appendChild( doc.createTextNode( attribute.getId() ) );
    parentElement.appendChild( idElement );

    Element valueElement = doc.createElement( "value" );
    valueElement
        .appendChild( doc.createTextNode( attribute.getValue() != null ? attribute.getValue().toString() : "" ) );
    parentElement.appendChild( valueElement );

    Element typeElement = doc.createElement( "type" );
    typeElement.appendChild( doc.createTextNode( getType( attribute.getValue() ) ) );
    parentElement.appendChild( typeElement );

    if ( !attribute.getChildren().isEmpty() ) {
      Element childrenElement = doc.createElement( "children" );
      parentElement.appendChild( childrenElement );
      for ( IMetaStoreAttribute childElement : attribute.getChildren() ) {
        Element child = doc.createElement( "child" );
        childrenElement.appendChild( child );
        appendAttribute( childElement, doc, child );
      }
    }
  }

  protected String getType( Object object ) {

    if ( object == null ) {
      return "String";
    }
    if ( object instanceof String ) {
      return "String";
    }
    if ( object instanceof Integer ) {
      return "Integer";
    }
    if ( object instanceof Long ) {
      return "Long";
    }
    if ( object instanceof Double ) {
      return "Double";
    }

    return "String";
  }

}
