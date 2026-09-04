/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.metastore.persist;

import java.util.Map;

import org.pentaho.metastore.api.exceptions.MetaStoreException;

/**
 * Creates nested metastore objects and provides their contextual data.
 */
public interface IMetaStoreObjectFactory {

  /**
   * Creates an object of the specified class with the supplied context.
   *
   * @param className the class name
   * @param context the context for object creation
   * @return the created object
   * @throws MetaStoreException if the factory cannot create the object
   */
  public Object instantiateClass( String className, Map<String, String> context ) throws MetaStoreException;

  /**
   * Gets contextual data for a nested object.
   *
   * @param pluginObject the object that provides the context
   * @return the object context
   * @throws MetaStoreException if the factory cannot get the context
   */
  public Map<String, String> getContext( Object pluginObject ) throws MetaStoreException;

}
