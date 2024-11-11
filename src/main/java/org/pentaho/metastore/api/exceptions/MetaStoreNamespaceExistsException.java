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
 * This exception is thrown in case a namespace is created in a metadata store when it already exists.
 * 
 * @author matt
 * 
 */

public class MetaStoreNamespaceExistsException extends MetaStoreException {

  private static final long serialVersionUID = 2614122560674499038L;

  public MetaStoreNamespaceExistsException() {
    super();
  }

  public MetaStoreNamespaceExistsException( String message ) {
    super( message );
  }

  public MetaStoreNamespaceExistsException( Throwable cause ) {
    super( cause );
  }

  public MetaStoreNamespaceExistsException( String message, Throwable cause ) {
    super( message, cause );
  }
}
