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
import org.pentaho.metastore.api.IMetaStoreElement;

import static org.mockito.Mockito.mock;

/**
 * Created by saslan on 10/23/2015.
 */
public class MetaStoreEntityAdapterTest extends TestCase {
  private MetaStoreEntityAdapter metaStoreEntityAdapter;
  private IMetaStoreElement iMetaStoreElement;

  public void setUp() throws Exception {
    metaStoreEntityAdapter = new MetaStoreEntityAdapter();
    iMetaStoreElement = mock( IMetaStoreElement.class );
  }

  public void testElementDeleted() throws Exception {
    metaStoreEntityAdapter.elementDeleted( "namespace", "dataTypeId", iMetaStoreElement );
  }

  public void testElementCreated() throws Exception {
    metaStoreEntityAdapter.elementCreated( "namespace", "dataTypeId", iMetaStoreElement );
  }

  public void testElementUpdated() throws Exception {
    IMetaStoreElement newIMetaStoreElement = mock( IMetaStoreElement.class );
    metaStoreEntityAdapter.elementUpdated( "namespace", "dataTypeId", iMetaStoreElement, newIMetaStoreElement );

  }
}
