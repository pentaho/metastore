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

import org.junit.Test;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class XmlMetaStoreElementTest {

  @Test
  public void equalsUsesIdForHashCode() {
    XmlMetaStoreElement first = new XmlMetaStoreElement();
    XmlMetaStoreElement second = new XmlMetaStoreElement();

    first.setId( "id" );
    second.setId( "id" );
    assertEquals( first, second );
    assertEquals( first.hashCode(), second.hashCode() );

    second.setId( "other-id" );
    assertNotEquals( first, second );
  }

  @Test
  public void saveReportsOriginalIOException() {
    String pattern = "1234567890";
    StringBuilder fileName = new StringBuilder( 310 );
    for ( int i = 0; i < 30; i++ ) {
      fileName.append( pattern );
    }
    String tempDir = System.getProperty( "java.io.tmpdir" );
    XmlMetaStoreElement xmse = new XmlMetaStoreElement();
    xmse.setFilename( tempDir + File.separator + fileName );
    try {
      xmse.save();
      fail();
    } catch ( MetaStoreException ex ) {
      assertTrue( ex.getCause() instanceof IOException );
      assertFalse( ex.getCause() instanceof FileNotFoundException );
      assertFalse( ex.getMessage().contains( "Annotation Group name is too long" ) );
    }
  }

}
