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

/**
 * This exception is thrown in case a namespace is created in a metadata store when it already exists.
 */

public class MetaStoreNamespaceExistsException extends MetaStoreException {

  private static final long serialVersionUID = 2614122560674499038L;

  /**
   * Creates an exception without a message or cause.
   */
  public MetaStoreNamespaceExistsException() {
    super();
  }

  /**
   * Creates an exception with a message.
   *
   * @param message the exception message
   */
  public MetaStoreNamespaceExistsException( String message ) {
    super( message );
  }

  /**
   * Creates an exception with a cause.
   *
   * @param cause the cause
   */
  public MetaStoreNamespaceExistsException( Throwable cause ) {
    super( cause );
  }

  /**
   * Creates an exception with a message and cause.
   *
   * @param message the exception message
   * @param cause the cause
   */
  public MetaStoreNamespaceExistsException( String message, Throwable cause ) {
    super( message, cause );
  }
}
