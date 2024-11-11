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

package org.pentaho.metastore.stores.xml;

import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.test.XmlMetaStoreIT;

public class XmlMetaStoreWithPersistentCacheIT extends XmlMetaStoreIT {

  @Override
  protected XmlMetaStore createMetaStore() throws MetaStoreException {
    return new XmlMetaStore( new PersistentXmlMetaStoreCache() );
  }

}
