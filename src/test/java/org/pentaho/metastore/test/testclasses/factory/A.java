/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */
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
