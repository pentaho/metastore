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



package org.pentaho.metastore.util;

/**
 * Defines standard Pentaho metastore names and descriptions.
 */
public class PentahoDefaults {

    /** The default Pentaho namespace. */
  public static final String NAMESPACE = "pentaho";

    /** The element type name for database connections. */
  public static final String DATABASE_CONNECTION_ELEMENT_TYPE_NAME = "Database connection";
    /** The element type description for database connections. */
  public static final String DATABASE_CONNECTION_ELEMENT_TYPE_DESCRIPTION =
      "This is the official central database connection metadata";

    /** The element type name for Kettle data services. */
  public static final String KETTLE_DATA_SERVICE_ELEMENT_TYPE_NAME = "Kettle Data Service";
    /** The element type description for Kettle data services. */
  public static final String KETTLE_DATA_SERVICE_ELEMENT_TYPE_DESCRIPTION =
      "The elements describing data services based upon transformation output";

}
