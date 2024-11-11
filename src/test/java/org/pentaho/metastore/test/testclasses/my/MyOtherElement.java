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
    name = "My other element type",
    description = "Another element type to test with" )
public class MyOtherElement {
  private String name;

  @MetaStoreAttribute( key = "some_attribute" )
  private String someAttribute;

  /**
   * Empty constructor is needed by MetaStoreFactory to instantiate this object
   */
  public MyOtherElement() {
  }

  /**
   * @param name
   * @param someAttribute
   */
  public MyOtherElement( String name, String someAttribute ) {
    this();
    this.name = name;
    this.someAttribute = someAttribute;
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
  public void setName( String name ) {
    this.name = name;
  }

  /**
   * @return the someAttribute
   */
  public String getSomeAttribute() {
    return someAttribute;
  }

  /**
   * @param someAttribute the someAttribute to set
   */
  public void setSomeAttribute( String someAttribute ) {
    this.someAttribute = someAttribute;
  }
}
