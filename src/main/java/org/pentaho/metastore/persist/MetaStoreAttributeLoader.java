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
import org.pentaho.metastore.util.MetaStoreUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class MetaStoreAttributeLoader {

  private static final String OBJECT_FACTORY_CONTEXT = "_ObjectFactoryContext_";
  private static final String POJO_CHILD = "_POJO_";

  private final IMetaStore metaStore;
  private final ClassLoader classLoader;
  private final Map<String, List<?>> nameListMap;
  private final Map<String, MetaStoreFactory<?>> nameFactoryMap;
  private final Map<String, List<?>> filenameListMap;
  private final IMetaStoreObjectFactory objectFactory;
  private final DateTimeFormatter dateFormat;
  private final EnumMap<MetaStoreFactory.AttributeType, AttributeReader> attributeReaders;

  MetaStoreAttributeLoader( IMetaStore metaStore, ClassLoader classLoader, Map<String, List<?>> nameListMap,
                            Map<String, MetaStoreFactory<?>> nameFactoryMap, Map<String, List<?>> filenameListMap,
                            IMetaStoreObjectFactory objectFactory, DateTimeFormatter dateFormat ) {
    this.metaStore = metaStore;
    this.classLoader = classLoader;
    this.nameListMap = nameListMap;
    this.nameFactoryMap = nameFactoryMap;
    this.filenameListMap = filenameListMap;
    this.objectFactory = objectFactory;
    this.dateFormat = dateFormat;
    this.attributeReaders = createAttributeReaders();
  }

  void load( Object parentObject, IMetaStoreAttribute parentElement, Class<?> parentClass ) throws MetaStoreException {
    for ( Field field : MetaStoreFactory.getFields( parentClass ) ) {
      MetaStoreAttribute attributeAnnotation = field.getAnnotation( MetaStoreAttribute.class );
      if ( attributeAnnotation != null ) {
        IMetaStoreAttribute child = findChild( parentElement, field, attributeAnnotation );
        if ( hasContent( child ) ) {
          loadAttribute( parentObject, parentClass, field, attributeAnnotation, child );
        }
      }
    }
  }

  private void loadAttribute( Object parentObject, Class<?> parentClass, Field field,
                              MetaStoreAttribute attributeAnnotation, IMetaStoreAttribute child )
    throws MetaStoreException {
    String childValue = MetaStoreUtil.getAttributeString( child );
    if ( attributeAnnotation.password() ) {
      childValue = metaStore.getTwoWayPasswordEncoder().decode( childValue );
    }

    AttributeReader reader = attributeReaders.get( MetaStoreFactory.determineAttributeType( field, attributeAnnotation ) );
    if ( reader == null ) {
      throw new MetaStoreException( "No attribute reader found for field '" + field.getName() + "'" );
    }
    reader.read( parentObject, parentClass, field, attributeAnnotation, child, childValue );
  }

  private IMetaStoreAttribute findChild( IMetaStoreAttribute parentElement, Field field,
                                         MetaStoreAttribute attributeAnnotation ) {
    String key = attributeAnnotation.key();
    if ( key == null || key.isEmpty() ) {
      key = field.getName();
    }

    IMetaStoreAttribute child = parentElement.getChild( key );
    if ( child != null ) {
      return child;
    }
    return findMappedChild( parentElement, key );
  }

  private IMetaStoreAttribute findMappedChild( IMetaStoreAttribute parentElement, String key ) {
    for ( String mappedKey : MetaStoreKeyMap.get( key ) ) {
      IMetaStoreAttribute child = parentElement.getChild( mappedKey );
      if ( child != null ) {
        return child;
      }
    }
    return null;
  }

  private boolean hasContent( IMetaStoreAttribute child ) {
    return child != null && ( child.getValue() != null || !child.getChildren().isEmpty() );
  }

  private EnumMap<MetaStoreFactory.AttributeType, AttributeReader> createAttributeReaders() {
    EnumMap<MetaStoreFactory.AttributeType, AttributeReader> readers =
      new EnumMap<>( MetaStoreFactory.AttributeType.class );
    readers.put( MetaStoreFactory.AttributeType.STRING, this::loadString );
    readers.put( MetaStoreFactory.AttributeType.INTEGER, this::loadInteger );
    readers.put( MetaStoreFactory.AttributeType.LONG, this::loadLong );
    readers.put( MetaStoreFactory.AttributeType.BOOLEAN, this::loadBoolean );
    readers.put( MetaStoreFactory.AttributeType.ENUM, this::loadEnum );
    readers.put( MetaStoreFactory.AttributeType.DATE, this::loadDate );
    readers.put( MetaStoreFactory.AttributeType.LIST, this::loadList );
    readers.put( MetaStoreFactory.AttributeType.NAME_REFERENCE, this::loadNameReference );
    readers.put( MetaStoreFactory.AttributeType.FILENAME_REFERENCE, this::loadFilenameReference );
    readers.put( MetaStoreFactory.AttributeType.FACTORY_NAME_REFERENCE, this::loadFactoryNameReference );
    readers.put( MetaStoreFactory.AttributeType.POJO, this::loadPojoAttribute );
    return readers;
  }

  private void loadString( Object parentObject, Class<?> parentClass, Field field,
                           MetaStoreAttribute attributeAnnotation, IMetaStoreAttribute child, String childValue )
    throws MetaStoreException {
    setAttribute( parentObject, parentClass, field, String.class, childValue );
  }

  private void loadInteger( Object parentObject, Class<?> parentClass, Field field,
                            MetaStoreAttribute attributeAnnotation, IMetaStoreAttribute child, String childValue )
    throws MetaStoreException {
    setAttribute( parentObject, parentClass, field, int.class, Integer.valueOf( childValue ) );
  }

  private void loadLong( Object parentObject, Class<?> parentClass, Field field,
                         MetaStoreAttribute attributeAnnotation, IMetaStoreAttribute child, String childValue )
    throws MetaStoreException {
    setAttribute( parentObject, parentClass, field, long.class, Long.valueOf( childValue ) );
  }

  private void loadBoolean( Object parentObject, Class<?> parentClass, Field field,
                            MetaStoreAttribute attributeAnnotation, IMetaStoreAttribute child, String childValue )
    throws MetaStoreException {
    setAttribute( parentObject, parentClass, field, boolean.class, "Y".equalsIgnoreCase( childValue ) );
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  private void loadEnum( Object parentObject, Class<?> parentClass, Field field,
                         MetaStoreAttribute attributeAnnotation, IMetaStoreAttribute child, String childValue )
    throws MetaStoreException {
    Class<? extends Enum> enumClass = (Class<? extends Enum>) field.getType();
    Enum<?> enumValue = childValue == null || childValue.isEmpty() ? null : Enum.valueOf( enumClass, childValue );
    setAttribute( parentObject, parentClass, field, field.getType(), enumValue );
  }

  private void loadDate( Object parentObject, Class<?> parentClass, Field field,
                         MetaStoreAttribute attributeAnnotation, IMetaStoreAttribute child, String childValue )
    throws MetaStoreException {
    try {
      LocalDateTime localDateTime = childValue == null ? null : LocalDateTime.parse( childValue, dateFormat );
      Object dateValue = localDateTime;
      if ( java.util.Date.class.equals( field.getType() ) && localDateTime != null ) {
        dateValue = java.util.Date.from( localDateTime.atZone( ZoneId.systemDefault() ).toInstant() );
      }
      setAttribute( parentObject, parentClass, field, field.getType(), dateValue );
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unexpected date parsing problem with value: '" + childValue + "'", e );
    }
  }

  private void loadList( Object parentObject, Class<?> parentClass, Field field,
                         MetaStoreAttribute attributeAnnotation, IMetaStoreAttribute child, String childValue )
    throws MetaStoreException {
    try {
      if ( child.getValue() == null ) {
        return;
      }

      Method listGetMethod = parentClass.getMethod( MetaStoreFactory.getGetterMethodName( field.getName(), false ) );
      @SuppressWarnings( "unchecked" )
      List<Object> list = (List<Object>) listGetMethod.invoke( parentObject );
      String childClassName = child.getValue().toString();
      for ( int i = 0; i < child.getChildren().size(); i++ ) {
        IMetaStoreAttribute listChild = child.getChild( Integer.toString( i ) );
        if ( listChild != null ) {
          loadListItem( parentClass, field, attributeAnnotation, list, childClassName, listChild );
        }
      }
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to load list attribute for field '" + field.getName() + "'", e );
    }
  }

  private void loadListItem( Class<?> parentClass, Field field,
                             MetaStoreAttribute attributeAnnotation, List<Object> list, String childClassName,
                             IMetaStoreAttribute child ) throws MetaStoreException {
    if ( attributeAnnotation.factoryNameReference() ) {
      Object object = loadFactoryNameReference( parentClass, field, child, attributeAnnotation,
        MetaStoreUtil.getAttributeString( child ) );
      if ( object != null ) {
        list.add( object );
      }
      return;
    }

    if ( String.class.getName().equals( childClassName ) ) {
      String value = (String) child.getValue();
      if ( value != null ) {
        list.add( value );
      }
      return;
    }

    Object childObject = instantiate( childClassName, child );
    load( childObject, child, childObject.getClass() );
    list.add( childObject );
  }

  private Object instantiate( String className, IMetaStoreAttribute child ) throws MetaStoreException {
    if ( objectFactory == null ) {
      try {
        return classLoader.loadClass( className ).getDeclaredConstructor().newInstance();
      } catch ( ReflectiveOperationException e ) {
        throw new MetaStoreException( "Unable to instantiate class " + className, e );
      }
    }
    return objectFactory.instantiateClass( className, getObjectFactoryContext( child ) );
  }

  private void loadNameReference( Object parentObject, Class<?> parentClass, Field field,
                                  MetaStoreAttribute attributeAnnotation, IMetaStoreAttribute parentElement,
                                  String childValue ) throws MetaStoreException {
    try {
      if ( childValue == null || childValue.isEmpty() ) {
        return;
      }

      List<?> list = nameListMap.get( attributeAnnotation.nameListKey() );
      if ( list == null ) {
        throw new MetaStoreException( "Unable to find reference list for named objects with key '"
          + attributeAnnotation.nameListKey() + "', name reference '" + childValue + "' can not be looked up" );
      }

      for ( Object object : list ) {
        String objectName = (String) object.getClass().getMethod( "getName" ).invoke( object );
        if ( objectName.equals( childValue ) ) {
          String setter = getSetterMethodName( field.getName() );
          Method setterMethod = parentObject.getClass().getMethod( setter, object.getClass() );
          setterMethod.invoke( parentObject, object );
          break;
        }
      }
    } catch ( Exception e ) {
      throw new MetaStoreException( "Error lookup up reference for field '" + field.getName() + "'", e );
    }
  }

  private void loadFactoryNameReference( Object parentObject, Class<?> parentClass, Field field,
                                         MetaStoreAttribute attributeAnnotation, IMetaStoreAttribute child,
                                         String childValue ) throws MetaStoreException {
    Object object = loadFactoryNameReference( parentClass, field, child, attributeAnnotation, childValue );
    setAttribute( parentObject, parentClass, field, field.getType(), object );
  }

  private Object loadFactoryNameReference( Class<?> parentClass, Field field, IMetaStoreAttribute parentElement,
                                           MetaStoreAttribute attributeAnnotation, String childValue )
    throws MetaStoreException {
    try {
      if ( childValue == null ) {
        return null;
      }

      String name = childValue;
      IMetaStoreAttribute pojoChild = parentElement.getChild( POJO_CHILD );
      if ( pojoChild != null ) {
        Object pojo = loadPojo( parentClass, parentElement );
        if ( pojo != null ) {
          MetaStoreFactory.setAttributeValue( pojo.getClass(), pojo, "name", "setName", String.class, name );
        }
        return pojo;
      }

      if ( name.isEmpty() ) {
        return null;
      }

      MetaStoreFactory<?> factory = nameFactoryMap.get( attributeAnnotation.factoryNameKey() );
      if ( factory == null ) {
        throw new MetaStoreException( "Unable to find factory to load attribute for factory key '"
          + attributeAnnotation.factoryNameKey() + "', name reference '" + name + "' can not be looked up" );
      }
      return factory.loadElement( name );
    } catch ( Exception e ) {
      throw new MetaStoreException( "Error lookup up reference for field '" + field.getName() + "'", e );
    }
  }

  private void loadFilenameReference( Object parentObject, Class<?> parentClass, Field field,
                                      MetaStoreAttribute attributeAnnotation, IMetaStoreAttribute child,
                                      String childValue ) throws MetaStoreException {
    try {
      if ( childValue == null || childValue.isEmpty() ) {
        return;
      }

      List<?> list = filenameListMap.get( attributeAnnotation.filenameListKey() );
      if ( list == null ) {
        throw new MetaStoreException( "Unable to find reference list for named objects with key '"
          + attributeAnnotation.filenameListKey() + "', name reference '" + childValue + "' can not be looked up" );
      }

      for ( Object object : list ) {
        Method getFilenameMethod = object.getClass().getMethod( "getFilename" );
        String filename = (String) getFilenameMethod.invoke( object );
        if ( filename.equals( childValue ) ) {
          String setter = getSetterMethodName( field.getName() );
          Method setterMethod = parentObject.getClass().getMethod( setter, object.getClass() );
          setterMethod.invoke( parentObject, object );
          break;
        }
      }
    } catch ( Exception e ) {
      throw new MetaStoreException( "Error lookup up reference for field '" + field.getName() + "'", e );
    }
  }

  private void loadPojoAttribute( Object parentObject, Class<?> parentClass, Field field,
                                  MetaStoreAttribute attributeAnnotation, IMetaStoreAttribute child,
                                  String childValue ) throws MetaStoreException {
    Object pojo = loadPojo( parentClass, child );
    setAttribute( parentObject, parentClass, field, field.getType(), pojo );
  }

  private Object loadPojo( Class<?> parentClass, IMetaStoreAttribute child ) throws MetaStoreException {
    String pojoChildClassName;
    IMetaStoreAttribute pojoChild = child.getChild( POJO_CHILD );
    if ( pojoChild == null ) {
      pojoChildClassName = MetaStoreUtil.getAttributeString( child );
      pojoChild = child;
    } else {
      pojoChildClassName = pojoChild.getValue().toString();
    }

    if ( pojoChildClassName == null ) {
      return null;
    }

    try {
      Object pojoObject = instantiate( pojoChildClassName, child );
      load( pojoObject, pojoChild, pojoObject.getClass() );
      return pojoObject;
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to load POJO class " + pojoChildClassName + " in parent class: " + parentClass, e );
    }
  }

  private Map<String, String> getObjectFactoryContext( IMetaStoreAttribute parentElement ) {
    Map<String, String> context = new HashMap<>();
    if ( parentElement != null ) {
      IMetaStoreAttribute contextChild = parentElement.getChild( OBJECT_FACTORY_CONTEXT );
      if ( contextChild != null ) {
        for ( IMetaStoreAttribute child : contextChild.getChildren() ) {
          if ( child.getId() != null && child.getValue() != null ) {
            context.put( child.getId(), child.getValue().toString() );
          }
        }
      }
    }
    return context;
  }

  private void setAttribute( Object parentObject, Class<?> parentClass, Field field,
                             Class<?> valueClass, Object value ) throws MetaStoreException {
    MetaStoreFactory.setAttributeValue( parentClass, parentObject, field.getName(),
      getSetterMethodName( field.getName() ), valueClass, value );
  }

  private static String getSetterMethodName( String name ) {
    return "set" + name.substring( 0, 1 ).toUpperCase() + name.substring( 1 );
  }

  private interface AttributeReader {
    void read( Object parentObject, Class<?> parentClass, Field field, MetaStoreAttribute attributeAnnotation,
               IMetaStoreAttribute child, String childValue ) throws MetaStoreException;
  }

}
