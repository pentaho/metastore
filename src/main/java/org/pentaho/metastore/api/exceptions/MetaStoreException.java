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
 * This exception is thrown in case of a general problem with the underlying store.
 */
public class MetaStoreException extends Exception {
  private static final long serialVersionUID = -1562965693472591981L;

  /**
   * Creates an exception without a message or cause.
   */
  public MetaStoreException() {
    super();
  }

  /**
   * Creates an exception with a message.
   *
   * @param message the exception message
   */
  public MetaStoreException( String message ) {
    super( message );
  }

  /**
   * Creates an exception with a cause.
   *
   * @param cause the cause
   */
  public MetaStoreException( Throwable cause ) {
    super( cause );
  }

  /**
   * Creates an exception with a message and cause.
   *
   * @param message the exception message
   * @param cause the cause
   */
  public MetaStoreException( String message, Throwable cause ) {
    super( message, cause );
  }
}
