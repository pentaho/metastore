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


package org.pentaho.metastore.stores.xml;

import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.test.XmlMetaStoreIT;

public class XmlMetaStoreWithPersistentCacheIT extends XmlMetaStoreIT {

  @Override
  protected XmlMetaStore createMetaStore() throws MetaStoreException {
    return new XmlMetaStore( new PersistentXmlMetaStoreCache() );
  }

  @Override
  protected XmlMetaStore createMetaStore( String rootFolder ) throws MetaStoreException {
    return new XmlMetaStore( rootFolder, new PersistentXmlMetaStoreCache() );
  }

}
