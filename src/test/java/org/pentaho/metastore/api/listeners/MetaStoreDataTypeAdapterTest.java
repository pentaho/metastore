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


package org.pentaho.metastore.api.listeners;

import junit.framework.TestCase;
import org.pentaho.metastore.api.IMetaStoreElementType;

import static org.mockito.Mockito.mock;

/**
 * Created by saslan on 10/23/2015.
 */
public class MetaStoreDataTypeAdapterTest extends TestCase {
  private MetaStoreDataTypeAdapter metaStoreDataTypeAdapter;
  private IMetaStoreElementType iMetaStoreElementType;

  public void setUp() throws Exception {
    metaStoreDataTypeAdapter = new MetaStoreDataTypeAdapter();
    iMetaStoreElementType = mock( IMetaStoreElementType.class );
  }

  public void testDataTypeCreated() throws Exception {
    metaStoreDataTypeAdapter.dataTypeCreated( "namespace", iMetaStoreElementType );
  }

  public void testDataTypeDeleted() throws Exception {
    metaStoreDataTypeAdapter.dataTypeDeleted( "namespace", iMetaStoreElementType );
  }

  public void testDataTypeUpdated() throws Exception {
    IMetaStoreElementType newIMetaStoreElementType = mock( IMetaStoreElementType.class );
    metaStoreDataTypeAdapter.dataTypeUpdated( "namespace", iMetaStoreElementType, newIMetaStoreElementType );
  }
}
