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

import java.util.List;

/**
 * @author Rowell Belen
 */
@MetaStoreElementType(
    name = "Level4Element",
    description = "My Level 4 Element" )
public class Level4Element {

  @MetaStoreAttribute
  private String name;

  @MetaStoreAttribute
  List<MyOtherElement> myElements;

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public List<MyOtherElement> getMyElements() {
    return myElements;
  }

  public void setMyElements( List<MyOtherElement> myElements ) {
    this.myElements = myElements;
  }
}
