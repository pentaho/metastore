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
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates and loads metastore elements of a specified type.
 *
 * @param <T> the element type
 */
public class MetaStoreFactory<T> {

  enum AttributeType {
    STRING, INTEGER, LONG, DATE, BOOLEAN, LIST, NAME_REFERENCE, FILENAME_REFERENCE, FACTORY_NAME_REFERENCE, ENUM, POJO;
  }

  private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss.SSS" );

  protected IMetaStore metaStore;
  protected final Class<T> clazz;
  protected String namespace;

  protected Map<String, List<?>> nameListMap;
  protected Map<String, MetaStoreFactory<?>> nameFactoryMap;
  protected Map<String, List<?>> filenameListMap;

  protected IMetaStoreObjectFactory objectFactory;

  /**
   * Creates a factory for the specified element class.
   *
   * @param clazz the element class
   * @param metaStore the metastore that stores the elements
   * @param namespace the metastore namespace
   */
  public MetaStoreFactory( Class<T> clazz, IMetaStore metaStore, String namespace ) {
    this.metaStore = metaStore;
    this.clazz = clazz;
    this.namespace = namespace;
    nameListMap = new HashMap<String, List<?>>();
    filenameListMap = new HashMap<String, List<?>>();
    nameFactoryMap = new HashMap<String, MetaStoreFactory<?>>();
  }

  /**
   * Adds a list that resolves name references.
   *
   * @param nameListKey the key that identifies the list
   * @param nameList the list of named objects
   */
  public void addNameList( String nameListKey, List<?> nameList ) {
    nameListMap.put( nameListKey, nameList );
  }

  /**
   * Adds a factory that stores and loads factory name references.
   *
   * @param nameFactoryKey the key that identifies the factory
   * @param factory the factory for referenced objects
   */
  public void addNameFactory( String nameFactoryKey, MetaStoreFactory<?> factory ) {
    nameFactoryMap.put( nameFactoryKey, factory );
  }

  /**
   * Adds a list that resolves filename references.
   *
   * @param filenameListKey the key that identifies the list
   * @param filenameList the list of file objects
   */
  public void addFilenameList( String filenameListKey, List<?> filenameList ) {
    filenameListMap.put( filenameListKey, filenameList );
  }

  /**
   * Loads an element by name.
   *
   * @param name the element name
   * @return the loaded element, or {@code null} when the type or element does not exist
   * @throws MetaStoreException if the name is empty or the metastore cannot load the element
   */
  public T loadElement( String name ) throws MetaStoreException {

    if ( name == null || name.isEmpty() ) {
      throw new MetaStoreException( "You need to specify the name of an element to load" );
    }

    MetaStoreElementType elementTypeAnnotation = getElementTypeAnnotation();

    IMetaStoreElementType elementType = metaStore.getElementTypeByName( namespace, elementTypeAnnotation.name() );
    if ( elementType == null ) {
      return null;
    }

    IMetaStoreElement element = metaStore.getElementByName( namespace, elementType, name );
    if ( element == null ) {
      return null;
    }
    return loadElement( element );
  }

  /**
   * Loads an element by name with the specified lock setting.
   *
   * @param name the element name
   * @param lock whether metastore reads use locking
   * @return the loaded element, or {@code null} when the type or element does not exist
   * @throws MetaStoreException if the name is empty or the metastore cannot load the element
   */
  public T loadElement( String name, boolean lock ) throws MetaStoreException {

    if ( name == null || name.isEmpty() ) {
      throw new MetaStoreException( "You need to specify the name of an element to load" );
    }

    MetaStoreElementType elementTypeAnnotation = getElementTypeAnnotation();

    IMetaStoreElementType elementType = metaStore.getElementTypeByName( namespace, elementTypeAnnotation.name(), lock );
    if ( elementType == null ) {
      return null;
    }

    IMetaStoreElement element = metaStore.getElementByName( namespace, elementType, name, lock );
    if ( element == null ) {
      return null;
    }
    return loadElement( element );
  }

  /** Load an element from the metastore, straight into the appropriate class
   */
  private T loadElement( IMetaStoreElement element ) throws MetaStoreException {
    T object;

    try {
      object = clazz.getDeclaredConstructor().newInstance();
    } catch ( Exception e ) {
      throw new MetaStoreException( "Class " + clazz.getName() + " could not be instantiated. Make sure the empty constructor is present", e );
    }

    // Set the name of the object...
    //
    setAttributeValue( clazz, object, "name", "setName", String.class, element.getName() );

    createAttributeLoader().load( object, element, clazz );
    return object;
  }

  private MetaStoreAttributeLoader createAttributeLoader() {
    return new MetaStoreAttributeLoader( metaStore, clazz.getClassLoader(), nameListMap, nameFactoryMap,
      filenameListMap, objectFactory, dateFormat );
  }

  /**
   * Saves an element in the metastore.
   *
   * <p>The method creates the namespace and element type when they do not exist.</p>
   *
   * @param t the element to store
   * @throws MetaStoreException if the element has no name or the metastore cannot save it
   */
  public void saveElement( T t ) throws MetaStoreException {

    MetaStoreElementType elementTypeAnnotation = getElementTypeAnnotation();

    // Make sure the namespace exists...

    if ( !metaStore.namespaceExists( namespace ) ) {
      metaStore.createNamespace( namespace );
    }

    // Make sure the element type exists...

    IMetaStoreElementType elementType = metaStore.getElementTypeByName( namespace, elementTypeAnnotation.name() );
    if ( elementType == null ) {
      elementType = metaStore.newElementType( namespace );
      elementType.setName( elementTypeAnnotation.name() );
      elementType.setDescription( elementTypeAnnotation.description() );
      metaStore.createElementType( namespace, elementType );
    }

    // Now store the element itself
    // Verify if this is an update or a create...
    //

    String name = (String) getAttributeValue( clazz, t, "name", "getName" );
    if ( name == null || name.trim().isEmpty() ) {
      throw new MetaStoreException( "Unable to find name of element class object '" + t.toString() + "'" );
    }

    IMetaStoreElement element = metaStore.newElement();
    element.setName( name );
    element.setElementType( elementType );

    // Store the attributes
    //
    createAttributeSaver().save( t, element, clazz );

    // Now that we have the element populated, do a quick check to see if we need to update the element
    // or simply create a new element in the metastore.

    IMetaStoreElement existingElement = metaStore.getElementByName( namespace, elementType, name );
    if ( existingElement == null ) {
      metaStore.createElement( namespace, elementType, element );
    } else {
      metaStore.updateElement( namespace, elementType, existingElement.getId(), element );
    }
  }

  private MetaStoreAttributeSaver createAttributeSaver() {
    return new MetaStoreAttributeSaver( metaStore, nameFactoryMap, objectFactory, dateFormat );
  }

  /**
   * Loads all elements of this factory's type.
   *
   * @return all stored elements
   * @throws MetaStoreException if the metastore cannot load the elements
   */
  public List<T> getElements() throws MetaStoreException {
    return getElements( true, null );
  }

  /**
   * Loads all elements of this factory's type with the specified lock setting.
   *
   * @param lock whether metastore reads use locking
   * @return all stored elements
   * @throws MetaStoreException if the metastore cannot load the elements
   */
  public List<T> getElements( boolean lock ) throws MetaStoreException {
    return getElements( lock, null );
  }

  /**
   * Loads all elements of this factory's type and collects element errors when requested.
   *
   * @param lock whether metastore reads use locking
   * @param exceptionList list that receives element load errors, or {@code null} to throw the first error
   * @return all elements that load successfully
   * @throws MetaStoreException if the metastore cannot load the elements
   */
  public List<T> getElements( boolean lock, List<MetaStoreException> exceptionList ) throws MetaStoreException {
    MetaStoreElementType elementTypeAnnotation = getElementTypeAnnotation();
    IMetaStoreElementType elementType = metaStore.getElementTypeByName( namespace, elementTypeAnnotation.name(), lock );
    if ( elementType == null ) {
      return Collections.emptyList();
    }

    List<IMetaStoreElement> elements = metaStore.getElements( namespace, elementType, lock, exceptionList );
    List<T> list = new ArrayList<T>( elements.size() );
    for ( IMetaStoreElement metaStoreElement : elements ) {
      list.add( loadElement( metaStoreElement ) );
    }
    return list;
  }

  /**
   * Removes an element by name.
   *
   * @param name the element name
   * @throws MetaStoreException if the element type or element does not exist
   */
  public void deleteElement( String name ) throws MetaStoreException {
    MetaStoreElementType elementTypeAnnotation = getElementTypeAnnotation();
    IMetaStoreElementType elementType = metaStore.getElementTypeByName( namespace, elementTypeAnnotation.name() );
    if ( elementType == null ) {
      throw new MetaStoreException( "The element type '" + elementTypeAnnotation.name()
        + "' does not exist so the element with name '" + name + "' can not be deleted" );
    }

    IMetaStoreElement element = metaStore.getElementByName( namespace, elementType, name );
    if ( element == null ) {
      throw new MetaStoreException( "The element with name '" + name + "' does not exists so it can not be deleted" );
    }

    metaStore.deleteElement( namespace, elementType, element.getId() );
  }

  /**
   * Gets the names of all elements of this factory's type.
   *
   * @return all stored element names
   * @throws MetaStoreException if the metastore cannot load the names
   */
  public List<String> getElementNames() throws MetaStoreException {
    return getElementNames( true );
  }

  /**
   * Gets the names of all elements of this factory's type with the specified lock setting.
   *
   * @param lock whether metastore reads use locking
   * @return all stored element names
   * @throws MetaStoreException if the metastore cannot load the names
   */
  public List<String> getElementNames( boolean lock ) throws MetaStoreException {
    List<String> names = new ArrayList<String>();
    MetaStoreElementType elementTypeAnnotation = getElementTypeAnnotation();
    IMetaStoreElementType elementType = metaStore.getElementTypeByName( namespace, elementTypeAnnotation.name(), lock );
    if ( elementType == null ) {
      return names;
    }

    List<IMetaStoreElement> elements = metaStore.getElements( namespace, elementType, lock,
      new ArrayList<MetaStoreException>() );
    for ( IMetaStoreElement element : elements ) {
      names.add( element.getName() );
    }
    return names;
  }

  /**
   * Gets the metastore element type managed by this factory.
   *
   * @return the managed element type, or {@code null} when it does not exist
   * @throws MetaStoreException if the element class lacks its type annotation
   */
  public IMetaStoreElementType getElementType() throws MetaStoreException {
    MetaStoreElementType elementTypeAnnotation = getElementTypeAnnotation();
    return metaStore.getElementTypeByName( namespace, elementTypeAnnotation.name() );
  }

  static AttributeType determineAttributeType( Field field, MetaStoreAttribute annotation ) {
    Class<?> fieldClass = field.getType();
    if ( List.class.equals( fieldClass ) ) {
      return AttributeType.LIST;
    }
    if ( annotation.nameReference() ) {
      return AttributeType.NAME_REFERENCE;
    }
    if ( annotation.filenameReference() ) {
      return AttributeType.FILENAME_REFERENCE;
    }
    if ( annotation.factoryNameReference() ) {
      return AttributeType.FACTORY_NAME_REFERENCE;
    }
    if ( String.class.equals( fieldClass ) ) {
      return AttributeType.STRING;
    }
    if ( int.class.equals( fieldClass ) ) {
      return AttributeType.INTEGER;
    }
    if ( long.class.equals( fieldClass ) ) {
      return AttributeType.LONG;
    }
    if ( Date.class.equals( fieldClass ) || LocalDateTime.class.equals( fieldClass ) ) {
      return AttributeType.DATE;
    }
    if ( boolean.class.equals( fieldClass ) ) {
      return AttributeType.BOOLEAN;
    }
    if ( fieldClass.isEnum() ) {
      return AttributeType.ENUM;
    }
    return AttributeType.POJO;

    // throw new MetaStoreException( "Unable to recognize attribute type for class '" + fieldClass + "'" );
  }

  private MetaStoreElementType getElementTypeAnnotation() throws MetaStoreException {
    MetaStoreElementType elementTypeAnnotation = clazz.getAnnotation( MetaStoreElementType.class );
    if ( elementTypeAnnotation == null ) {
      throw new MetaStoreException( "The class you want to serialize needs to have the @MetaStoreElementType annotation" );
    }
    return elementTypeAnnotation;
  }

  /**
   * Set an attribute value in the specified object
   * @param parentClass The parent object class
   * @param object      The object to modify
   * @param fieldName   The field to modify
   * @param setterName  The setter method name
   * @param valueClass  The class value
   * @param value       The value to set
   * @throws MetaStoreException
   */
  static void setAttributeValue( Class<?> parentClass, Object object, String fieldName, String setterName, Class<?> valueClass, Object value ) throws MetaStoreException {
    Method method = getDeclaredMethod( parentClass, setterName, valueClass );
    if ( method == null ) {
      throw new MetaStoreException( "Unable to find setter for attribute field : " + fieldName + ". Expected '" + setterName + "'" );
    }

    try {
      method.invoke( object, value );
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to set value '" + value + "' using method '" + setterName + "'", e );
    }
  }

  private Object getAttributeValue( Class<?> parentClass, Object object, String fieldName, String getterName ) throws MetaStoreException {
    Method method = getDeclaredMethod( parentClass, getterName );
    if ( method == null ) {
      throw new MetaStoreException( "Unable to find getter for attribute field : " + fieldName + ". Expected '" + getterName + "'" );
    }

    try {
      return method.invoke( object );
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to get value using method '" + getterName + "' on class " + parentClass.getName(), e );
    }

  }

  static Method getDeclaredMethod( Class<?> parentClass, String name, Class<?>... parameterTypes ) {
    if ( parentClass == Object.class ) {
      return null;
    }
    try {
      return parentClass.getDeclaredMethod( name, parameterTypes );
    } catch ( NoSuchMethodException | SecurityException e ) {
      parentClass = parentClass.getSuperclass();
      return getDeclaredMethod( parentClass, name, parameterTypes );
    }
  }

  /**
   * myAttribute ==>  getMyAttribute
   */
  static String getGetterMethodName( String name, boolean isBoolean ) {

    StringBuilder setter = new StringBuilder();
    setter.append( isBoolean ? "is" : "get" );
    setter.append( name.substring( 0, 1 ).toUpperCase() );
    setter.append( name.substring( 1 ) );

    return setter.toString();
  }

  /**
   * Gets the metastore used by this factory.
   *
   * @return the metastore
   */
  public IMetaStore getMetaStore() {
    return metaStore;
  }

  /**
   * Sets the metastore used by this factory.
   *
   * @param metaStore the metastore
   */
  public void setMetaStore( IMetaStore metaStore ) {
    this.metaStore = metaStore;
  }

  /**
   * Gets the metastore namespace used by this factory.
   *
   * @return the namespace
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * Sets the metastore namespace used by this factory.
   *
   * @param namespace the namespace
   */
  public void setNamespace( String namespace ) {
    this.namespace = namespace;
  }

  /**
   * Gets the map of lists used to resolve name references.
   *
   * @return the name list map
   */
  public Map<String, List<?>> getNamedListMap() {
    return nameListMap;
  }

  /**
   * Sets the map of lists used to resolve name references.
   *
   * @param namedListMap the name list map
   */
  public void setNamedListMap( Map<String, List<?>> namedListMap ) {
    this.nameListMap = namedListMap;
  }

  /**
   * Gets the map of lists used to resolve name references.
   *
   * @return the name list map
   */
  public Map<String, List<?>> getNameListMap() {
    return nameListMap;
  }

  /**
   * Sets the map of lists used to resolve name references.
   *
   * @param nameListMap the name list map
   */
  public void setNameListMap( Map<String, List<?>> nameListMap ) {
    this.nameListMap = nameListMap;
  }

  /**
   * Gets the map of lists used to resolve filename references.
   *
   * @return the filename list map
   */
  public Map<String, List<?>> getFilenameListMap() {
    return filenameListMap;
  }

  /**
   * Sets the map of lists used to resolve filename references.
   *
   * @param filenameListMap the filename list map
   */
  public void setFilenameListMap( Map<String, List<?>> filenameListMap ) {
    this.filenameListMap = filenameListMap;
  }

  /**
   * Gets the object factory used to create nested objects.
   *
   * @return the object factory, or {@code null} when none is configured
   */
  public IMetaStoreObjectFactory getObjectFactory() {
    return objectFactory;
  }

  /**
   * Sets the object factory used to create nested objects.
   *
   * @param objectFactory the object factory
   */
  public void setObjectFactory( IMetaStoreObjectFactory objectFactory ) {
    this.objectFactory = objectFactory;
  }

  static Field[] getFields( Class<?> clazz ) {
    Set<String> visitedFieldNames = new HashSet<>();
    List<Field> fields = new ArrayList<>();
    while ( clazz != Object.class ) {
      for ( Field field : clazz.getDeclaredFields() ) {
        if ( !Modifier.isStatic( field.getModifiers() ) && !visitedFieldNames.contains( field.getName() ) ) {
          visitedFieldNames.add( field.getName() );
          fields.add( field );
        }
      }

      clazz = clazz.getSuperclass();
    }

    return fields.toArray( new Field[ 0 ] );
  }
}
