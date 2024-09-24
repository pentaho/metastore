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
package org.pentaho.metastore.util;

import junit.framework.TestCase;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by saslan on 10/22/2015.
 */
public class FileUtilTest extends TestCase {

  public void testCleanFolder() throws Exception {
    File mockFolder = mock( File.class );
    when( mockFolder.isDirectory() ).thenReturn( true );

    String[] folderList = new String[ 0 ];
    when( mockFolder.list() ).thenReturn( folderList );
    boolean folderDeleted = FileUtil.cleanFolder( mockFolder, true );
    assertEquals( folderDeleted, false );
    folderDeleted = FileUtil.cleanFolder( mockFolder, false );
    assertEquals( folderDeleted, true );
  }
}
