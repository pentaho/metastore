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


package org.pentaho.metastore.test;

import org.pentaho.metastore.stores.memory.MemoryMetaStore;

public class MemoryMetaStoreTest extends MetaStoreTestBase {

  private MemoryMetaStore metaStore;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    metaStore = new MemoryMetaStore();
    metaStore.setName( META_STORE_NAME );
  }

  public void test() throws Exception {
    super.testFunctionality( metaStore );
  }

  public void testParrallelRetrive() throws Exception {
    super.testParallelOneStore( metaStore );
  }

}
