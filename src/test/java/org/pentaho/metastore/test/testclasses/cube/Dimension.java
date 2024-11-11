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

package org.pentaho.metastore.test.testclasses.cube;

import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

import java.util.ArrayList;
import java.util.List;

@MetaStoreElementType( name = "Dimension", description = "A dimension" )
public class Dimension {

  private String name;

  @MetaStoreAttribute
  private List<DimensionAttribute> attributes;

  @MetaStoreAttribute( key = "dimension_type" )
  private DimensionType dimensionType;

  @MetaStoreAttribute
  private boolean shared;

  public Dimension() {
    attributes = new ArrayList<DimensionAttribute>();
    shared = true;
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
   * @return the attributes
   */
  public List<DimensionAttribute> getAttributes() {
    return attributes;
  }

  /**
   * @param attributes the attributes to set
   */
  public void setAttributes( List<DimensionAttribute> attributes ) {
    this.attributes = attributes;
  }

  /**
   * @return the dimensionType
   */
  public DimensionType getDimensionType() {
    return dimensionType;
  }

  /**
   * @param dimensionType the dimensionType to set
   */
  public void setDimensionType( DimensionType dimensionType ) {
    this.dimensionType = dimensionType;
  }

  /**
   * @return the shared
   */
  public boolean isShared() {
    return shared;
  }

  /**
   * @param shared the shared to set
   */
  public void setShared( boolean shared ) {
    this.shared = shared;
  }
}
