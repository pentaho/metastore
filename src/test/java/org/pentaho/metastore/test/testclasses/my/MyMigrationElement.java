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
package org.pentaho.metastore.test.testclasses.my;

import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

@MetaStoreElementType(
    name = "My migration element type",
    description = "This is my migration element type" )
public class MyMigrationElement {

  public static final String MY_MIGRATION_STEP_NAME = "step_name";
  public static final String MY_MIGRATION_HOST_NAME = "host_name";
  public static final String MY_MIGRATION_FIELD_MAPPINGS = "field_mappings";
  public static final String MY_MIGRATION_PARAMETER_NAME = "parameter_name";
  public static final String MY_MIGRATION_SOURCE_FIELD_NAME = "source_field_name";
  public static final String MY_MIGRATION_TARGET_FIELD_NAME = "target_field_name";

  public MyMigrationElement() {
    // Required
  }

  public MyMigrationElement( String name ) {
    this.name = name;
  }

  protected String name;

  @MetaStoreAttribute( key = MY_MIGRATION_STEP_NAME )
  protected String stepName;

  @MetaStoreAttribute( key = MY_MIGRATION_HOST_NAME )
  protected String hostname;

  @MetaStoreAttribute( key = MY_MIGRATION_STEP_NAME )
  protected String stepname;

  @MetaStoreAttribute( key = MY_MIGRATION_FIELD_MAPPINGS )
  protected String fieldMappings;

  @MetaStoreAttribute( key = MY_MIGRATION_PARAMETER_NAME )
  protected String parameterName;

  @MetaStoreAttribute( key = MY_MIGRATION_SOURCE_FIELD_NAME )
  protected String sourceFieldName;

  @MetaStoreAttribute( key = MY_MIGRATION_TARGET_FIELD_NAME )
  protected String targetFieldName;

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getStepName() {
    return stepName;
  }

  public void setStepName( String stepName ) {
    this.stepName = stepName;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname( String hostname ) {
    this.hostname = hostname;
  }

  public String getStepname() {
    return stepname;
  }

  public void setStepname( String stepname ) {
    this.stepname = stepname;
  }

  public String getFieldMappings() {
    return fieldMappings;
  }

  public void setFieldMappings( String fieldMappings ) {
    this.fieldMappings = fieldMappings;
  }

  public String getParameterName() {
    return parameterName;
  }

  public void setParameterName( String parameterName ) {
    this.parameterName = parameterName;
  }

  public String getSourceFieldName() {
    return sourceFieldName;
  }

  public void setSourceFieldName( String sourceFieldName ) {
    this.sourceFieldName = sourceFieldName;
  }

  public String getTargetFieldName() {
    return targetFieldName;
  }

  public void setTargetFieldName( String targetFieldName ) {
    this.targetFieldName = targetFieldName;
  }
}
