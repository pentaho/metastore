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

package org.pentaho.metastore.api.listeners;

import org.pentaho.metastore.api.IMetaStoreElementType;

/**
 * Set of methods that are called in various parts of the meta store data type life-cycle.
 * 
 * @author matt
 * 
 */
public interface MetaStoreElementTypeListener {

  /**
   * This method will inform you of the creation of a data type.
   * 
   * @param namespace
   *          The namespace the data type is created in.
   * @param dataType
   *          the data type that was created.
   */
  public void dataTypeCreated( String namespace, IMetaStoreElementType dataType );

  /**
   * This method will is called when a data type is updated.
   * 
   * @param namespace
   *          The namespace the data type was updated in
   * @param oldDataType
   *          The old data type.
   * @param newDataType
   *          The new data type.
   */
  public void dataTypeUpdated( String namespace, IMetaStoreElementType oldDataType, IMetaStoreElementType newDataType );

  /**
   * This method will is called when a data type is deleted.
   * 
   * @param namespace
   *          The namespace the data type was deleted from
   * @param dataType
   *          The deleted data type.
   */
  public void dataTypeDeleted( String namespace, IMetaStoreElementType dataType );
}
