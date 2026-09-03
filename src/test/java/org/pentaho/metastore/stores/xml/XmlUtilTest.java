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

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class XmlUtilTest {

  @Test
  public void resolvesPathsWithinRoot() {
    Path root = Paths.get( "/tmp/metastore" );

    assertEquals( root.resolve( "namespace" ).resolve( "type" ),
        XmlUtil.getElementTypeFolderPath( root, "namespace", "type" ) );
  }

  @Test( expected = IllegalArgumentException.class )
  public void rejectsAbsolutePathSegment() {
    XmlUtil.getNamespaceFolderPath( Paths.get( "/tmp/metastore" ), "/tmp" );
  }

  @Test( expected = IllegalArgumentException.class )
  public void rejectsPathTraversalSegment() {
    XmlUtil.getElementTypeFolderPath( Paths.get( "/tmp/metastore" ), "namespace", "../../outside" );
  }
}
