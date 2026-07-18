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


package org.pentaho.metastore.api.security;

import junit.framework.TestCase;

/**
 * Created by saslan on 10/22/2015.
 */
public class MetaStoreElementOwnerTypeTest extends TestCase {

  public void testGetOwnerType() throws Exception {
    MetaStoreElementOwnerType valueOf = MetaStoreElementOwnerType.getOwnerType( "" );
    assertEquals( valueOf, null );
    valueOf = MetaStoreElementOwnerType.getOwnerType( "USER" );
    assertEquals( valueOf.toString(), "USER" );
  }
}
