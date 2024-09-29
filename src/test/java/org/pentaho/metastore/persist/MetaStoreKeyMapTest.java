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


package org.pentaho.metastore.persist;

import junit.framework.TestCase;

/**
 * Created by saslan on 10/23/2015.
 */
public class MetaStoreKeyMapTest extends TestCase {

  public void testGet() throws Exception {
    String [] keys = MetaStoreKeyMap.get( "host_name" );
    assertEquals( keys[ 0 ], "hostname" );
    keys = MetaStoreKeyMap.get( "" );
    assertEquals( keys.length, 0 );
  }
}
