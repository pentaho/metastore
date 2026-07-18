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

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by saslan on 10/22/2015.
 */
public class MetaStoreDependenciesExistsExceptionTest extends TestCase {
  private MetaStoreDependenciesExistsException metaStoreDependenciesExistsException;
  private List<String> elements;

  public void testGetDependencies() throws Exception {
    elements = new ArrayList<>();
    elements.add( "string" );
    metaStoreDependenciesExistsException = new MetaStoreDependenciesExistsException( elements );
    assertEquals( metaStoreDependenciesExistsException.getDependencies(), elements );

    //metaStoreDependenciesExistsException( List<String> entities, String message ) constructor
    metaStoreDependenciesExistsException = new MetaStoreDependenciesExistsException( elements, "test" );
    assertEquals( metaStoreDependenciesExistsException.getDependencies(), elements );

    //metaStoreDependenciesExistsException( List<String> entities, Throwable cause ) constructor
    metaStoreDependenciesExistsException = new MetaStoreDependenciesExistsException( elements, new Throwable() );
    assertEquals( metaStoreDependenciesExistsException.getDependencies(), elements );

    //metaStoreDependenciesExistsException( List<String> entities, String message, Throwable cause ) constructor
    metaStoreDependenciesExistsException = new MetaStoreDependenciesExistsException( elements, "test", new Throwable() );
    assertEquals( metaStoreDependenciesExistsException.getDependencies(), elements );
  }
}
