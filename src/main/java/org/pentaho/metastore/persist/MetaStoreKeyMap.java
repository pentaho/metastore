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

import java.util.HashMap;
import java.util.Map;

/**
 * This class holds the metastore id values used for migrating metastore element ids
 */
public class MetaStoreKeyMap {

  public static Map<String, String[]> keyMap = new HashMap<String, String[]>();

  static {
    keyMap.put( "host_name", new String[]{ "hostname" } );
    keyMap.put( "server_name", new String[]{ "servername" } );
    keyMap.put( "step_name", new String[]{ "stepname", "stepName" } );
    keyMap.put( "field_mappings", new String[]{ "fieldMappings" } );
    keyMap.put( "parameter_name", new String[]{ "parameterName" }  );
    keyMap.put( "source_field_name", new String[]{ "sourceFieldName" } );
    keyMap.put( "target_field_name", new String[]{ "targetFieldName" } );
  }

  public static String[] get( String key ) {
    String[] keys = keyMap.get( key );

    if ( keys != null ) {
      return keys;
    }

    return new String[ 0 ];
  }
}
