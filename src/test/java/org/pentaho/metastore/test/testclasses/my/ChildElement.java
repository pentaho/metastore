/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.metastore.test.testclasses.my;


import org.pentaho.metastore.persist.MetaStoreAttribute;

public class ChildElement {

  @MetaStoreAttribute
  private String property1;

  @MetaStoreAttribute
  private String property2;

  public String getProperty1() {
    return property1;
  }

  public void setProperty1( String property1 ) {
    this.property1 = property1;
  }

  public String getProperty2() {
    return property2;
  }

  public void setProperty2( String property2 ) {
    this.property2 = property2;
  }
}
