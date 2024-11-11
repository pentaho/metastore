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

import java.io.File;

public class FileUtil {

  /**
   * Delete a folder with files and possible sub-folders with files.
   * 
   * @param folder
   *          The folder to delete
   * @param removeParent
   *          remove parent folder
   * @return true if the folder was deleted, false if there was a problem with that.
   */
  public static boolean cleanFolder( File folder, boolean removeParent ) {
    if ( folder.isDirectory() ) {
      String[] filenames = folder.list();
      for ( String filename : filenames ) {
        File file = new File( folder, filename );
        if ( file.isDirectory() ) {
          boolean ok = cleanFolder( new File( folder, filename ), true );
          if ( !ok ) {
            return false;
          }
        } else {
          boolean ok = file.delete();
          if ( !ok ) {
            return false;
          }
        }
      }
    }

    // The empty folder can now be deleted.
    //
    if ( removeParent ) {
      return folder.delete();
    } else {
      return true;
    }
  }
}
