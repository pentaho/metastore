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



package org.pentaho.metastore.api.exceptions;

import java.util.List;

/**
 * Signals that an operation cannot continue because dependent objects exist.
 */

public class MetaStoreDependenciesExistsException extends MetaStoreException {

  private static final long serialVersionUID = -1658192841342866261L;

  private final List<String> dependencies;

  /**
   * Creates an exception with dependencies.
   *
   * @param dependencies the dependent object identifiers
   */
  public MetaStoreDependenciesExistsException( List<String> dependencies ) {
    super();
    this.dependencies = dependencies;
  }

  /**
   * Creates an exception with dependencies and a message.
   *
   * @param dependencies the dependent object identifiers
   * @param message the exception message
   */
  public MetaStoreDependenciesExistsException( List<String> dependencies, String message ) {
    super( message );
    this.dependencies = dependencies;
  }

  /**
   * Creates an exception with dependencies and a cause.
   *
   * @param dependencies the dependent object identifiers
   * @param cause the cause
   */
  public MetaStoreDependenciesExistsException( List<String> dependencies, Throwable cause ) {
    super( cause );
    this.dependencies = dependencies;
  }

  /**
   * Creates an exception with dependencies, a message, and a cause.
   *
   * @param dependencies the dependent object identifiers
   * @param message the exception message
   * @param cause the cause
   */
  public MetaStoreDependenciesExistsException( List<String> dependencies, String message, Throwable cause ) {
    super( message, cause );
    this.dependencies = dependencies;
  }

  /**
   * Gets the dependent object identifiers.
   *
   * @return the dependent object identifiers
   */
  public List<String> getDependencies() {
    return dependencies;
  }
}
