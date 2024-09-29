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
import org.pentaho.metastore.api.IMetaStoreElement;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * Created by saslan on 10/22/2015.
 */
public class MetaStoreElementExistExceptionTest extends TestCase {
  private MetaStoreElementExistException metaStoreElementExistException;
  private List<IMetaStoreElement> elements;

  public void testSetEntities() throws Exception {
    elements = new ArrayList<>();
    IMetaStoreElement elem1 = mock( IMetaStoreElement.class );
    elements.add( elem1 );
    metaStoreElementExistException = new MetaStoreElementExistException( elements );
    List<IMetaStoreElement> elements2 = new ArrayList<>();
    IMetaStoreElement elem2 = mock( IMetaStoreElement.class );
    elements2.add( elem2 );
    metaStoreElementExistException.setEntities( elements2 );
    assertEquals( metaStoreElementExistException.getEntities(), elements2 );

    //metaStoreElementExistException( List<IMetaStoreElement> entities, String message ) constructor
    metaStoreElementExistException = new MetaStoreElementExistException( elements, "test" );
    metaStoreElementExistException.setEntities( elements2 );
    assertEquals( metaStoreElementExistException.getEntities(), elements2 );

    //metaStoreElementExistException( List<IMetaStoreElement> entities, Throwable cause ) constructor
    metaStoreElementExistException = new MetaStoreElementExistException( elements, new Throwable() );
    metaStoreElementExistException.setEntities( elements2 );
    assertEquals( metaStoreElementExistException.getEntities(), elements2 );

    //metaStoreElementExistException( List<IMetaStoreElement> entities, String message, Throwable cause ) constructor
    metaStoreElementExistException = new MetaStoreElementExistException( elements, "test", new Throwable() );
    metaStoreElementExistException.setEntities( elements2 );
    assertEquals( metaStoreElementExistException.getEntities(), elements2 );
  }
}
