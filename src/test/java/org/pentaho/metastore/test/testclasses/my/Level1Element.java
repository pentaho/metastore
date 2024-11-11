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
import org.pentaho.metastore.persist.MetaStoreElementType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rowell Belen
 */

@MetaStoreElementType(
    name = "Level1Element",
    description = "My Level 1 Element" )
public class Level1Element extends ArrayList {

  @MetaStoreAttribute
  private String name;

  @MetaStoreAttribute
  private List<MyOtherElement> otherElements = new ArrayList<MyOtherElement>(  );

  @MetaStoreAttribute
  private List<Level2Element> level2Elements = new ArrayList<Level2Element>(  );

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public List<Level2Element> getLevel2Elements() {
    return level2Elements;
  }

  public void setLevel2Elements( List<Level2Element> level2Elements ) {
    this.level2Elements = level2Elements;
  }

  public List<MyOtherElement> getOtherElements() {
    return otherElements;
  }

  public void setOtherElements( List<MyOtherElement> otherElements ) {
    this.otherElements = otherElements;
  }
}
