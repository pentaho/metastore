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


package org.pentaho.metastore.api.exceptions;

import java.util.List;

/**
 * This exception is thrown in case a data type is created in a metadata store when it already exists.
 * 
 * @author matt
 * 
 */

public class MetaStoreDependenciesExistsException extends MetaStoreException {

  private static final long serialVersionUID = -1658192841342866261L;

  private List<String> dependencies;

  public MetaStoreDependenciesExistsException( List<String> dependencies ) {
    super();
    this.dependencies = dependencies;
  }

  public MetaStoreDependenciesExistsException( List<String> dependencies, String message ) {
    super( message );
    this.dependencies = dependencies;
  }

  public MetaStoreDependenciesExistsException( List<String> dependencies, Throwable cause ) {
    super( cause );
    this.dependencies = dependencies;
  }

  public MetaStoreDependenciesExistsException( List<String> dependencies, String message, Throwable cause ) {
    super( message, cause );
    this.dependencies = dependencies;
  }

  public List<String> getDependencies() {
    return dependencies;
  }
}
