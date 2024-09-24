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

import junit.framework.TestCase;
import org.pentaho.metastore.api.IMetaStoreElementType;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * Created by saslan on 10/22/2015.
 */
public class MetaStoreElementTypeExistsExceptionTest extends TestCase {
  private MetaStoreElementTypeExistsException metaStoreElementTypeExistsException;
  private List<IMetaStoreElementType> elements;

  public void testSetDataTypes() throws Exception {
    elements = new ArrayList<>();
    IMetaStoreElementType elem1 = mock( IMetaStoreElementType.class );
    elements.add( elem1 );
    metaStoreElementTypeExistsException = new MetaStoreElementTypeExistsException( elements );
    List<IMetaStoreElementType> elements2 = new ArrayList<>();
    IMetaStoreElementType elem2 = mock( IMetaStoreElementType.class );
    elements2.add( elem2 );
    metaStoreElementTypeExistsException.setDataTypes( elements2 );
    assertEquals( metaStoreElementTypeExistsException.getDataTypes(), elements2 );

    //MetaStoreElementTypeExistsException( List<IMetaStoreElementType> dataTypes, String message ) constructor
    metaStoreElementTypeExistsException = new MetaStoreElementTypeExistsException( elements, "test" );
    metaStoreElementTypeExistsException.setDataTypes( elements2 );
    assertEquals( metaStoreElementTypeExistsException.getDataTypes(), elements2 );

    //MetaStoreElementTypeExistsException( List<IMetaStoreElementType> dataTypes, Throwable cause ) constructor
    metaStoreElementTypeExistsException = new MetaStoreElementTypeExistsException( elements, new Throwable() );
    metaStoreElementTypeExistsException.setDataTypes( elements2 );
    assertEquals( metaStoreElementTypeExistsException.getDataTypes(), elements2 );

    //MetaStoreElementTypeExistsException( List<IMetaStoreElementType> dataTypes, String message, Throwable cause ) constructor
    metaStoreElementTypeExistsException = new MetaStoreElementTypeExistsException( elements, "test", new Throwable() );
    metaStoreElementTypeExistsException.setDataTypes( elements2 );
    assertEquals( metaStoreElementTypeExistsException.getDataTypes(), elements2 );
  }
}
