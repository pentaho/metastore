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



package org.pentaho.metastore.persist;

import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

final class MetaStoreAttributeSaver {

  private static final String OBJECT_FACTORY_CONTEXT = "_ObjectFactoryContext_";
  private static final String POJO_CHILD = "_POJO_";

  private final IMetaStore metaStore;
  private final Map<String, MetaStoreFactory<?>> nameFactoryMap;
  private final IMetaStoreObjectFactory objectFactory;
  private final DateTimeFormatter dateFormat;
  private final EnumMap<MetaStoreFactory.AttributeType, AttributeWriter> attributeWriters;

  MetaStoreAttributeSaver( IMetaStore metaStore, Map<String, MetaStoreFactory<?>> nameFactoryMap,
                           IMetaStoreObjectFactory objectFactory, DateTimeFormatter dateFormat ) {
    this.metaStore = metaStore;
    this.nameFactoryMap = nameFactoryMap;
    this.objectFactory = objectFactory;
    this.dateFormat = dateFormat;
    this.attributeWriters = createAttributeWriters();
  }

  void save( Object parentObject, IMetaStoreAttribute parentElement, Class<?> parentClass )
    throws MetaStoreException {
    try {
      for ( Field field : MetaStoreFactory.getFields( parentClass ) ) {
        MetaStoreAttribute attributeAnnotation = field.getAnnotation( MetaStoreAttribute.class );
        if ( attributeAnnotation != null ) {
          saveAttribute( parentObject, parentElement, parentClass, field, attributeAnnotation );
        }
      }
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to save attributes of element id '" + parentElement.getId()
        + "', class " + parentClass.getName(), e );
    }
  }

  private void saveAttribute( Object parentObject, IMetaStoreAttribute parentElement, Class<?> parentClass,
                              Field field, MetaStoreAttribute attributeAnnotation ) throws MetaStoreException {
    String key = attributeAnnotation.key();
    if ( key == null || key.isEmpty() ) {
      key = field.getName();
    }

    AttributeWriter writer = attributeWriters.get( MetaStoreFactory.determineAttributeType( field, attributeAnnotation ) );
    if ( writer == null ) {
      throw new MetaStoreException( "No attribute writer found for field '" + field.getName() + "'" );
    }
    writer.write( parentObject, parentElement, parentClass, field, attributeAnnotation, key );
  }

  private EnumMap<MetaStoreFactory.AttributeType, AttributeWriter> createAttributeWriters() {
    EnumMap<MetaStoreFactory.AttributeType, AttributeWriter> writers =
      new EnumMap<>( MetaStoreFactory.AttributeType.class );
    writers.put( MetaStoreFactory.AttributeType.STRING, this::saveString );
    writers.put( MetaStoreFactory.AttributeType.INTEGER, this::saveInteger );
    writers.put( MetaStoreFactory.AttributeType.LONG, this::saveLong );
    writers.put( MetaStoreFactory.AttributeType.BOOLEAN, this::saveBoolean );
    writers.put( MetaStoreFactory.AttributeType.ENUM, this::saveEnum );
    writers.put( MetaStoreFactory.AttributeType.DATE, this::saveDate );
    writers.put( MetaStoreFactory.AttributeType.LIST, this::saveList );
    writers.put( MetaStoreFactory.AttributeType.NAME_REFERENCE, this::saveNameReference );
    writers.put( MetaStoreFactory.AttributeType.FILENAME_REFERENCE, this::saveFilenameReference );
    writers.put( MetaStoreFactory.AttributeType.FACTORY_NAME_REFERENCE, this::saveFactoryNameReference );
    writers.put( MetaStoreFactory.AttributeType.POJO, this::savePojoAttribute );
    return writers;
  }

  private void saveString( Object parentObject, IMetaStoreAttribute parentElement, Class<?> parentClass,
                           Field field, MetaStoreAttribute attributeAnnotation, String key )
    throws MetaStoreException {
    String value = (String) getAttributeValue( parentClass, parentObject, field.getName(),
      MetaStoreFactory.getGetterMethodName( field.getName(), false ) );
    if ( attributeAnnotation.password() ) {
      value = metaStore.getTwoWayPasswordEncoder().encode( value );
    }
    addAttribute( parentElement, key, value );
  }

  private void saveInteger( Object parentObject, IMetaStoreAttribute parentElement, Class<?> parentClass,
                            Field field, MetaStoreAttribute attributeAnnotation, String key )
    throws MetaStoreException {
    int value = (Integer) getAttributeValue( parentClass, parentObject, field.getName(),
      MetaStoreFactory.getGetterMethodName( field.getName(), false ) );
    addAttribute( parentElement, key, Integer.toString( value ) );
  }

  private void saveLong( Object parentObject, IMetaStoreAttribute parentElement, Class<?> parentClass,
                         Field field, MetaStoreAttribute attributeAnnotation, String key )
    throws MetaStoreException {
    long value = (Long) getAttributeValue( parentClass, parentObject, field.getName(),
      MetaStoreFactory.getGetterMethodName( field.getName(), false ) );
    addAttribute( parentElement, key, Long.toString( value ) );
  }

  private void saveBoolean( Object parentObject, IMetaStoreAttribute parentElement, Class<?> parentClass,
                            Field field, MetaStoreAttribute attributeAnnotation, String key )
    throws MetaStoreException {
    boolean value = (Boolean) getAttributeValue( parentClass, parentObject, field.getName(),
      MetaStoreFactory.getGetterMethodName( field.getName(), true ) );
    addAttribute( parentElement, key, value ? "Y" : "N" );
  }

  private void saveEnum( Object parentObject, IMetaStoreAttribute parentElement, Class<?> parentClass,
                         Field field, MetaStoreAttribute attributeAnnotation, String key )
    throws MetaStoreException {
    Object enumValue = getAttributeValue( parentClass, parentObject, field.getName(),
      MetaStoreFactory.getGetterMethodName( field.getName(), false ) );
    String name = null;
    if ( enumValue != null ) {
      name = (String) getAttributeValue( Enum.class, enumValue, field.getName(), "name" );
    }
    addAttribute( parentElement, key, name );
  }

  private void saveDate( Object parentObject, IMetaStoreAttribute parentElement, Class<?> parentClass,
                         Field field, MetaStoreAttribute attributeAnnotation, String key )
    throws MetaStoreException {
    Object dateValue = getAttributeValue( parentClass, parentObject, field.getName(),
      MetaStoreFactory.getGetterMethodName( field.getName(), false ) );
    String value = null;
    if ( dateValue instanceof java.util.Date date ) {
      value = dateFormat.format( LocalDateTime.ofInstant( date.toInstant(), ZoneId.systemDefault() ) );
    } else if ( dateValue != null ) {
      value = dateFormat.format( (LocalDateTime) dateValue );
    }
    addAttribute( parentElement, key, value );
  }

  @SuppressWarnings( "unchecked" )
  private void saveList( Object parentObject, IMetaStoreAttribute parentElement, Class<?> parentClass,
                         Field field, MetaStoreAttribute attributeAnnotation, String key )
    throws MetaStoreException {
    List<Object> list = (List<Object>) getAttributeValue( parentClass, parentObject, field.getName(),
      MetaStoreFactory.getGetterMethodName( field.getName(), false ) );
    IMetaStoreAttribute topChild = metaStore.newAttribute( key, null );
    parentElement.addChild( topChild );

    if ( !list.isEmpty() ) {
      Class<?> attributeClass = list.get( 0 ).getClass();
      topChild.setValue( attributeClass.getName() );
      for ( int i = 0; i < list.size(); i++ ) {
        saveListItem( attributeAnnotation, list.get( i ), attributeClass, topChild, i );
      }
    }
  }

  private void saveListItem( MetaStoreAttribute attributeAnnotation, Object object, Class<?> attributeClass,
                             IMetaStoreAttribute parentElement, int index )
    throws MetaStoreException {
    IMetaStoreAttribute childAttribute = metaStore.newAttribute( Integer.toString( index ), null );
    parentElement.addChild( childAttribute );

    if ( attributeAnnotation.factoryNameReference() ) {
      saveFactoryNameReference( attributeAnnotation, childAttribute, object );
    } else if ( object instanceof String ) {
      childAttribute.setValue( object );
    } else {
      if ( objectFactory != null ) {
        saveObjectFactoryContext( childAttribute, objectFactory.getContext( object ) );
      }
      save( object, childAttribute, attributeClass );
    }
  }

  private void saveNameReference( Object parentObject, IMetaStoreAttribute parentElement, Class<?> parentClass,
                                  Field field, MetaStoreAttribute attributeAnnotation, String key )
    throws MetaStoreException {
    Object namedObject = getAttributeValue( parentClass, parentObject, field.getName(),
      MetaStoreFactory.getGetterMethodName( field.getName(), false ) );
    String name = null;
    if ( namedObject != null ) {
      name = (String) getAttributeValue( namedObject.getClass(), namedObject, "name", "getName" );
    }
    addAttribute( parentElement, key, name );
  }

  private void saveFactoryNameReference( Object parentObject, IMetaStoreAttribute parentElement,
                                         Class<?> parentClass, Field field,
                                         MetaStoreAttribute attributeAnnotation, String key )
    throws MetaStoreException {
    Object namedObject = getAttributeValue( parentClass, parentObject, field.getName(),
      MetaStoreFactory.getGetterMethodName( field.getName(), false ) );
    if ( namedObject == null ) {
      return;
    }

    IMetaStoreAttribute reference = metaStore.newAttribute( key, null );
    parentElement.addChild( reference );
    saveFactoryNameReference( attributeAnnotation, reference, namedObject );
  }

  private void saveFactoryNameReference( MetaStoreAttribute attributeAnnotation, IMetaStoreAttribute targetElement,
                                         Object namedObject )
    throws MetaStoreException {
    Class<?> namedObjectClass = namedObject.getClass();
    String name = (String) getAttributeValue( namedObjectClass, namedObject, "name", "getName" );
    targetElement.setValue( name );

    String indicatorName = attributeAnnotation.factorySharedIndicatorName();
    if ( indicatorName != null && !indicatorName.isEmpty() ) {
      String isSharedMethod = MetaStoreFactory.getGetterMethodName( indicatorName, true );
      Boolean shared = (Boolean) getAttributeValue( namedObjectClass, namedObject, indicatorName, isSharedMethod );
      if ( shared == null ) {
        throw new MetaStoreException( "Shared indicator attribute is not available through '"
          + namedObjectClass.getName() + "." + isSharedMethod + "()'" );
      }
      if ( !shared ) {
        savePojo( targetElement, namedObject );
        return;
      }
    }

    MetaStoreFactory<?> factory = nameFactoryMap.get( attributeAnnotation.factoryNameKey() );
    try {
      Method method = factory.getClass().getMethod( "saveElement", Object.class );
      method.invoke( factory, namedObject );
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to save attribute element of class " + namedObjectClass, e );
    }
  }

  private void saveFilenameReference( Object parentObject, IMetaStoreAttribute parentElement, Class<?> parentClass,
                                      Field field, MetaStoreAttribute attributeAnnotation, String key )
    throws MetaStoreException {
    Object namedObject = getAttributeValue( parentClass, parentObject, field.getName(),
      MetaStoreFactory.getGetterMethodName( field.getName(), false ) );
    String filename = null;
    if ( namedObject != null ) {
      filename = (String) getAttributeValue( namedObject.getClass(), namedObject, "filename", "getFilename" );
    }
    addAttribute( parentElement, key, filename );
  }

  private void savePojoAttribute( Object parentObject, IMetaStoreAttribute parentElement, Class<?> parentClass,
                                  Field field, MetaStoreAttribute attributeAnnotation, String key )
    throws MetaStoreException {
    IMetaStoreAttribute pojoElement = metaStore.newAttribute( key, null );
    parentElement.addChild( pojoElement );
    Object pojo = getAttributeValue( parentClass, parentObject, field.getName(),
      MetaStoreFactory.getGetterMethodName( field.getName(), false ) );
    savePojo( pojoElement, pojo );
  }

  private void savePojo( IMetaStoreAttribute pojoElement, Object pojo ) throws MetaStoreException {
    if ( pojo == null ) {
      return;
    }

    if ( objectFactory != null ) {
      saveObjectFactoryContext( pojoElement, objectFactory.getContext( pojo ) );
    }

    IMetaStoreAttribute pojoChild = metaStore.newAttribute( POJO_CHILD, pojo.getClass().getName() );
    pojoElement.addChild( pojoChild );
    save( pojo, pojoChild, pojo.getClass() );
  }

  private void saveObjectFactoryContext( IMetaStoreAttribute parentElement, Map<String, String> context )
    throws MetaStoreException {
    if ( context == null || context.isEmpty() ) {
      return;
    }

    IMetaStoreAttribute contextAttribute = metaStore.newAttribute( OBJECT_FACTORY_CONTEXT, null );
    parentElement.addChild( contextAttribute );
    for ( Map.Entry<String, String> entry : context.entrySet() ) {
      contextAttribute.addChild( metaStore.newAttribute( entry.getKey(), entry.getValue() ) );
    }
  }

  private void addAttribute( IMetaStoreAttribute parentElement, String key, Object value ) throws MetaStoreException {
    parentElement.addChild( metaStore.newAttribute( key, value ) );
  }

  private Object getAttributeValue( Class<?> parentClass, Object object, String fieldName, String getterName )
    throws MetaStoreException {
    Method method = MetaStoreFactory.getDeclaredMethod( parentClass, getterName );
    if ( method == null ) {
      throw new MetaStoreException( "Unable to find getter for attribute field : " + fieldName
        + ". Expected '" + getterName + "'" );
    }

    try {
      return method.invoke( object );
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to get value using method '" + getterName + "' on class "
        + parentClass.getName(), e );
    }
  }

  private interface AttributeWriter {
    void write( Object parentObject, IMetaStoreAttribute parentElement, Class<?> parentClass, Field field,
                MetaStoreAttribute attributeAnnotation, String key ) throws MetaStoreException;
  }
}
