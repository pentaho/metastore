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
