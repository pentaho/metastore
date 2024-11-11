/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.metastore.test.testclasses.my;


import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

@MetaStoreElementType(
    name = "ParentElement",
    description = "ParentElement" )
public class ParentElement {

  @MetaStoreAttribute
  private String name;

  @MetaStoreAttribute
  private ChildElement childElement;

  @MetaStoreAttribute
  private String property1;

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public ChildElement getChildElement() {
    return childElement;
  }

  public void setChildElement( ChildElement childElement ) {
    this.childElement = childElement;
  }

  public String getProperty1() {
    return property1;
  }

  public void setProperty1(String property1) {
    this.property1 = property1;
  }
}
