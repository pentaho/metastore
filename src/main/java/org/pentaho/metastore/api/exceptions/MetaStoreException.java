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

/**
 * This exception is thrown in case of a general problem with the underlying store.
 * 
 * @author matt
 * 
 */
public class MetaStoreException extends Exception {
  private static final long serialVersionUID = -1562965693472591981L;

  public MetaStoreException() {
    super();
  }

  public MetaStoreException( String message ) {
    super( message );
  }

  public MetaStoreException( Throwable cause ) {
    super( cause );
  }

  public MetaStoreException( String message, Throwable cause ) {
    super( message, cause );
  }
}
