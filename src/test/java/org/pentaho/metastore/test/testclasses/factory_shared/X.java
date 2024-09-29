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

package org.pentaho.metastore.test.testclasses.factory_shared;

import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

import java.util.ArrayList;
import java.util.List;

@MetaStoreElementType( name = "X", description = "Test class X " )
public class X {

  public static final String FACTORY_Y = "FactoryY";

  private String name;

  @MetaStoreAttribute( factoryNameKey = FACTORY_Y, factoryNameReference = true )
  private List<Y> ys;

  @MetaStoreAttribute( factoryNameKey = FACTORY_Y, factoryNameReference = true )
  private Y y;

  public X() {
    ys = new ArrayList<Y>();
  }

  public X( String name ) {
    this();
    this.name = name;
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
   * @return the ys
   */
  public List<Y> getYs() {
    return ys;
  }

  /**
   * @param ys the bees to set
   */
  public void setYs( List<Y> ys ) {
    this.ys = ys;
  }

  /**
   * @return the b
   */
  public Y getY() {
    return y;
  }

  /**
   * @param y the b to set
   */
  public void setY( Y y ) {
    this.y = y;
  }
}
