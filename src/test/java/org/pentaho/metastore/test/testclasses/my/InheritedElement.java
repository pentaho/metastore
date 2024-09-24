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
        name = "custom",
        description = "This is Inherited element type" )
public class InheritedElement extends ParentElement {

  @MetaStoreAttribute
  private String property1;

  @MetaStoreAttribute
  private String property2;

  public String getProperty1() { return property1; }

  public String getProperty2() {
    return property2;
  }

  public void setProperty1(String property1) {
    this.property1 = property1;
  }

  public void setProperty2(String property2) {
    this.property2 = property2;
  }

  public InheritedElement() { }

  public InheritedElement(String property1, String property2 ) {
    this.property1 = property1;
    this.property2 = property2;
    }
}
