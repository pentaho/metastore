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

package org.pentaho.metastore.test.testclasses.factory;

import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

import java.util.ArrayList;
import java.util.List;

@MetaStoreElementType( name = "A", description = "Test class A " )
public class A {

  public static final String FACTORY_B = "FactoryB";

  private String name;

  @MetaStoreAttribute( factoryNameKey = FACTORY_B, factoryNameReference = true, factorySharedIndicatorName = "shared" )
  private List<B> bees;

  @MetaStoreAttribute( factoryNameKey = FACTORY_B, factoryNameReference = true, factorySharedIndicatorName = "shared" )
  private B b;

  public A() {
    bees = new ArrayList<B>();
  }

  public A( String name ) {
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
   * @return the bees
   */
  public List<B> getBees() {
    return bees;
  }

  /**
   * @param bees the bees to set
   */
  public void setBees( List<B> bees ) {
    this.bees = bees;
  }

  /**
   * @return the b
   */
  public B getB() {
    return b;
  }

  /**
   * @param b the b to set
   */
  public void setB( B b ) {
    this.b = b;
  }
}
