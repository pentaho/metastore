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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.metastore.stores.memory;

import java.util.HashMap;
import java.util.Map;

import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

public class MemoryMetaStoreElementType implements IMetaStoreElementType {

  private String namespace;
  private String id;
  private String name;
  private String description;
  private String metaStoreName;

  private Map<String, MemoryMetaStoreElement> elementMap;

  public MemoryMetaStoreElementType( String namespace ) {
    this.namespace = namespace;
    elementMap = new HashMap<String, MemoryMetaStoreElement>();
  }

  /**
   * Copy data from another meta store persistence type...
   * 
   * @param elementType
   *          The type to copy over.
   */
  public MemoryMetaStoreElementType( IMetaStoreElementType elementType ) {
    this( elementType.getNamespace() );
    this.id = elementType.getId();
    this.name = elementType.getName();
    this.description = elementType.getDescription();
  }

  @Override
  public void save() throws MetaStoreException {
    // Nothing to save.
  }

  public Map<String, MemoryMetaStoreElement> getElementMap() {
    return elementMap;
  }

  /**
   * @return the namespace
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * @param namespace
   *          the namespace to set
   */
  public void setNamespace( String namespace ) {
    this.namespace = namespace;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id
   *          the id to set
   */
  public void setId( String id ) {
    this.id = id;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName( String name ) {
    this.name = name;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description
   *          the description to set
   */
  public void setDescription( String description ) {
    this.description = description;
  }

  public String getMetaStoreName() {
    return metaStoreName;
  }

  public void setMetaStoreName( String metaStoreName ) {
    this.metaStoreName = metaStoreName;
  }

}
