package org.pentaho.metastore.persist;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.util.MetaStoreUtil;

public class MetaStoreFactory<T> {

  private enum AttributeType {
    STRING, INTEGER, LONG, DATE, BOOLEAN, LIST, NAME_REFERENCE, FILENAME_REFERENCE;
  }

  protected IMetaStore metaStore;
  protected final Class<T> clazz;
  protected String namespace;

  protected Map<String, List<?>> nameListMap;
  protected Map<String, List<?>> filenameListMap;

  private volatile SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" );

  public MetaStoreFactory( Class<T> clazz, IMetaStore metaStore, String namespace ) {
    this.metaStore = metaStore;
    this.clazz = clazz;
    this.namespace = namespace;
    nameListMap = new HashMap<String, List<?>>();
    filenameListMap = new HashMap<String, List<?>>();
  }

  public void addNameList( String nameListKey, List<?> nameList ) {
    nameListMap.put( nameListKey, nameList );
  }

  public void addFilenameList( String filenameListKey, List<?> filenameList ) {
    filenameListMap.put( filenameListKey, filenameList );
  }

  /** Load an element from the metastore, straight into the appropriate class 
   */
  public T loadElement( String name ) throws MetaStoreException {

    MetaStoreElementType elementTypeAnnotation = getElementTypeAnnotation();

    IMetaStoreElementType elementType = metaStore.getElementTypeByName( namespace, elementTypeAnnotation.name() );
    if ( elementType == null ) {
      return null;
    }

    IMetaStoreElement element = metaStore.getElementByName( namespace, elementType, name );
    if ( element == null ) {
      return null;
    }

    T object;

    try {
      object = clazz.newInstance();
    } catch ( Exception e ) {
      throw new MetaStoreException( "Class " + clazz.getName() + " could not be instantiated. Make sure the empty constructor is present", e );
    }

    // Set the name of the object...
    //
    setAttributeValue( clazz, object, "name", "setName", String.class, name );

    loadAttributes( object, element, clazz );
    return object;
  }

  private void loadAttributes( Object parentObject, IMetaStoreAttribute parentElement, Class<?> parentClass ) throws MetaStoreException {

    // Which are the attributes to load?
    //
    Field[] fields = parentClass.getDeclaredFields();
    for ( Field field : fields ) {
      MetaStoreAttribute attributeAnnotation = field.getAnnotation( MetaStoreAttribute.class );
      if ( attributeAnnotation != null ) {
        String key = attributeAnnotation.key();
        if ( key == null || key.length() == 0 ) {
          key = field.getName();
        }

        AttributeType type = determineAttributeType( field, attributeAnnotation );
        IMetaStoreAttribute child = parentElement.getChild( key );
        if ( child != null && ( child.getValue() != null || !child.getChildren().isEmpty() ) ) {
          String setterName = getSetterMethodName( field.getName() );
          String value = MetaStoreUtil.getAttributeString( child );
          if ( attributeAnnotation.password() ) {
            value = metaStore.getTwoWayPasswordEncoder().decode( value );
          }
          switch ( type ) {
            case STRING:
              setAttributeValue( parentClass, parentObject, field.getName(), setterName, String.class, value );
              break;
            case INTEGER:
              setAttributeValue( parentClass, parentObject, field.getName(), setterName, int.class, Integer.valueOf( value ) );
              break;
            case LONG:
              setAttributeValue( parentClass, parentObject, field.getName(), setterName, long.class, Long.valueOf( value ) );
              break;
            case BOOLEAN:
              setAttributeValue( parentClass, parentObject, field.getName(), setterName, boolean.class, "Y".equalsIgnoreCase( value ) );
              break;
            case DATE:
              try {
                synchronized ( DATE_FORMAT ) {
                  Date date = value == null ? null : DATE_FORMAT.parse( value );
                  setAttributeValue( parentClass, parentObject, field.getName(), setterName, Date.class, date );
                }
              } catch ( Exception e ) {
                throw new MetaStoreException( "Unexpected date parsing problem with value: '" + value + "'", e );
              }
              break;
            case LIST:
              loadAttributesList( parentClass, parentObject, field, child );
              break;
            case NAME_REFERENCE:
              loadNameReference( parentClass, parentObject, field, child, attributeAnnotation );
              break;
            case FILENAME_REFERENCE:
              loadFilenameReference( parentClass, parentObject, field, child, attributeAnnotation );
              break;
            default:
              throw new MetaStoreException( "Only String values are supported at this time" );

              // TODO: support other data types
          }
        }
      }
    }
  }

  private void loadAttributesList( Class<?> parentClass, Object parentObject, Field field, IMetaStoreAttribute parentElement ) throws MetaStoreException {
    try {

      if ( parentElement.getValue() == null ) {
        // nothing more to do, no elements saved
        return;
      }

      // What is the list object to populate?
      //
      String listGetter = getGetterMethodName( field.getName(), false );
      Method listGetMethod = parentClass.getMethod( listGetter );
      @SuppressWarnings( "unchecked" )
      List<Object> list = (List<Object>) listGetMethod.invoke( parentObject );

      String childClassName = parentElement.getValue().toString();
      Class<?> childClass = clazz.getClassLoader().loadClass( childClassName );

      List<IMetaStoreAttribute> children = parentElement.getChildren();
      for ( int i = 0; i < children.size(); i++ ) {
        IMetaStoreAttribute child = parentElement.getChild( Integer.toString( i ) );
        // Instantiate the class and load the attributes
        //
        Object childObject = childClass.newInstance();
        loadAttributes( childObject, child, childClass );
        list.add( childObject );
      }
    } catch ( Exception e ) {
      e.printStackTrace();
      throw new MetaStoreException( "Unable to load list attribute for field '" + field.getName() + "'", e );
    }

  }

  private void loadNameReference( Class<?> parentClass, Object parentObject, Field field, IMetaStoreAttribute parentElement, MetaStoreAttribute attributeAnnotation ) throws MetaStoreException {
    try {

      if ( parentElement.getValue() == null ) {
        // nothing more to do, no elements saved
        return;
      }

      // What is the name stored?
      //
      String name = parentElement.getValue().toString();
      if ( name.length() == 0 ) {
        // No name, no game
        return;
      }
      // What is the reference list to look up in?
      //
      List<?> list = nameListMap.get( attributeAnnotation.nameListKey() );
      if ( list == null ) {
        // No reference list, developer didn't provide a list!
        //
        throw new MetaStoreException( "Unable to find reference list for named objects with key '" + attributeAnnotation.nameListKey() + "', name reference '" + name + "' can not be looked up" );
      }

      for ( Object object : list ) {
        String verifyName = (String) object.getClass().getMethod( "getName" ).invoke( object );
        if ( verifyName.equals( name ) ) {
          // This is the object we want to set on the parent object...
          // Ex: setDatabaseMeta(), setNameElement()
          //
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

  private void loadFilenameReference( Class<?> parentClass, Object parentObject, Field field, IMetaStoreAttribute parentElement, MetaStoreAttribute attributeAnnotation ) throws MetaStoreException {
    try {

      if ( parentElement.getValue() == null ) {
        // nothing more to do, no elements saved
        return;
      }

      // What is the filename stored?
      //
      String filename = parentElement.getValue().toString();
      if ( filename.length() == 0 ) {
        // No name, no game
        return;
      }
      // What is the reference list to look up in?
      //
      List<?> list = filenameListMap.get( attributeAnnotation.filenameListKey() );
      if ( list == null ) {
        // No reference list, developer didn't provide a list!
        //
        throw new MetaStoreException( "Unable to find reference list for named objects with key '" + attributeAnnotation.filenameListKey() + "', name reference '" + filename + "' can not be looked up" );
      }

      for ( Object object : list ) {
        Method getNameMethod = object.getClass().getMethod( "getFilename" );
        String verifyName = (String) getNameMethod.invoke( object );
        if ( verifyName.equals( filename ) ) {
          // This is the object we want to set on the parent object...
          // Ex: setDatabaseMeta(), setNameElement()
          //
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

  /**
   * Save the specified class into the metastore.
   * Create the namespace and element type if needed...
   * 
   * @param t The element to store...
   * @throws MetaStoreException
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
    if ( name == null ) {
      throw new MetaStoreException( "Unable to find name of element class object '" + t.toString() + "'" );
    }

    IMetaStoreElement element = metaStore.newElement();
    element.setName( name );
    element.setElementType( elementType );

    // Store the attributes
    //
    saveAttributes( element, clazz, t );

    // Now that we have the element populated, do a quick check to see if we need to update the element
    // or simply create a new element in the metastore.

    IMetaStoreElement existingElement = metaStore.getElementByName( namespace, elementType, name );
    if ( existingElement == null ) {
      metaStore.createElement( namespace, elementType, element );
    } else {
      metaStore.updateElement( namespace, elementType, existingElement.getId(), element );
    }
  }

  private void saveAttributes( IMetaStoreAttribute parentElement, Class<?> parentClass, Object parentObject ) throws MetaStoreException {
    try {
      Field[] fields = parentClass.getDeclaredFields();
      for ( Field field : fields ) {
        MetaStoreAttribute attributeAnnotation = field.getAnnotation( MetaStoreAttribute.class );
        if ( attributeAnnotation != null ) {
          String key = attributeAnnotation.key();
          if ( key == null || key.length() == 0 ) {
            key = field.getName();
          }

          AttributeType type = determineAttributeType( field, attributeAnnotation );

          IMetaStoreAttribute child;
          switch ( type ) {
            case STRING:
              String value = (String) getAttributeValue( parentClass, parentObject, field.getName(), getGetterMethodName( field.getName(), false ) );
              if ( attributeAnnotation.password() ) {
                value = metaStore.getTwoWayPasswordEncoder().encode( value );
              }
              child = metaStore.newAttribute( key, value );
              parentElement.addChild( child );
              break;
            case INTEGER:
              int intValue = (Integer) getAttributeValue( parentClass, parentObject, field.getName(), getGetterMethodName( field.getName(), false ) );
              child = metaStore.newAttribute( key, Integer.toString( intValue ) );
              parentElement.addChild( child );
              break;
            case LONG:
              long longValue = (Long) getAttributeValue( parentClass, parentObject, field.getName(), getGetterMethodName( field.getName(), false ) );
              child = metaStore.newAttribute( key, Long.toString( longValue ) );
              parentElement.addChild( child );
              break;
            case BOOLEAN:
              boolean boolValue = (Boolean) getAttributeValue( parentClass, parentObject, field.getName(), getGetterMethodName( field.getName(), true ) );
              child = metaStore.newAttribute( key, boolValue ? "Y" : "N" );
              parentElement.addChild( child );
              break;
            case DATE:
              Date dateValue = (Date) getAttributeValue( parentClass, parentObject, field.getName(), getGetterMethodName( field.getName(), false ) );
              child = metaStore.newAttribute( key, dateValue == null ? null : DATE_FORMAT.format( dateValue ) );
              parentElement.addChild( child );
              break;
            case LIST:
              saveListAttribute( parentClass, parentElement, parentObject, field, key );
              break;
            case NAME_REFERENCE:
              saveNameReference( parentClass, parentElement, parentObject, field, key );
              break;
            case FILENAME_REFERENCE:
              saveFilenameReference( parentClass, parentElement, parentObject, field, key );
              break;
            default:
              throw new MetaStoreException( "Only String values are supported at this time" );
          }

          // TODO: support other field data types...

        }
      }
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to save attributes of element id '" + parentElement.getId() + "', class " + parentClass.getName(), e );
    }
  }

  @SuppressWarnings( "unchecked" )
  private void saveListAttribute( Class<?> parentClass, IMetaStoreAttribute parentElement, Object parentObject, Field field, String key ) throws MetaStoreException {
    List<Object> list = (List<Object>) getAttributeValue( parentClass, parentObject, field.getName(), getGetterMethodName( field.getName(), false ) );
    IMetaStoreAttribute topChild = metaStore.newAttribute( key, null );
    parentElement.addChild( topChild );

    if ( !list.isEmpty() ) {
      // Save the class name used as well, otherwise we can't re-inflate afterwards...
      //
      Class<?> attributeClass = list.get( 0 ).getClass();
      topChild.setValue( attributeClass.getName() );

      // Add one child to the topChild for each object in the list...

      for ( int i = 0; i < list.size(); i++ ) {
        Object object = list.get( i );
        IMetaStoreAttribute childAttribute = metaStore.newAttribute( Integer.toString( i ), null );
        saveAttributes( childAttribute, attributeClass, object );
        topChild.addChild( childAttribute );
      }
    }
  }

  private void saveNameReference( Class<?> parentClass, IMetaStoreAttribute parentElement, Object parentObject, Field field, String key ) throws MetaStoreException {
    // What is the object of which we need to store the name as a reference?
    //
    Object namedObject = getAttributeValue( parentClass, parentObject, field.getName(), getGetterMethodName( field.getName(), false ) );
    String name = null;
    if ( namedObject != null ) {
      name = (String) getAttributeValue( namedObject.getClass(), namedObject, "name", "getName" );
    }
    IMetaStoreAttribute nameChild = metaStore.newAttribute( key, name );
    parentElement.addChild( nameChild );
  }

  private void saveFilenameReference( Class<?> parentClass, IMetaStoreAttribute parentElement, Object parentObject, Field field, String key ) throws MetaStoreException {
    // What is the object of which we need to store the filename as a reference?
    //
    Object namedObject = getAttributeValue( parentClass, parentObject, field.getName(), getGetterMethodName( field.getName(), false ) );
    String name = null;
    if ( namedObject != null ) {
      name = (String) getAttributeValue( namedObject.getClass(), namedObject, "filename", "getFilename" );
    }
    IMetaStoreAttribute nameChild = metaStore.newAttribute( key, name );
    parentElement.addChild( nameChild );
  }

  /**
   * @return A list of all the de-serialized objects of this class in the metastore
   * @throws MetaStoreException
   */
  public List<T> getElements() throws MetaStoreException {
    List<T> list = new ArrayList<T>();

    for ( String name : getElementNames() ) {
      list.add( loadElement( name ) );
    }

    return list;
  }

  /**
   * Remove an element with a specific name from the metastore
   * @param name The name of the element to delete
   * @throws MetaStoreException In case either the element type or the element to delete doesn't exists
   */
  public void deleteElement( String name ) throws MetaStoreException {
    MetaStoreElementType elementTypeAnnotation = getElementTypeAnnotation();

    IMetaStoreElementType elementType = metaStore.getElementTypeByName( namespace, elementTypeAnnotation.name() );
    if ( elementType == null ) {
      throw new MetaStoreException( "The element type '" + elementTypeAnnotation.name() + "' does not exist so the element with name '" + name + "' can not be deleted" );
    }

    IMetaStoreElement element = metaStore.getElementByName( namespace, elementType, name );
    if ( element == null ) {
      throw new MetaStoreException( "The element with name '" + name + "' does not exists so it can not be deleted" );
    }

    metaStore.deleteElement( namespace, elementType, element.getId() );
  }

  /**
   * @return The list of element names 
   * @throws MetaStoreException
   */
  public List<String> getElementNames() throws MetaStoreException {
    List<String> names = new ArrayList<String>();

    MetaStoreElementType elementTypeAnnotation = getElementTypeAnnotation();

    IMetaStoreElementType elementType = metaStore.getElementTypeByName( namespace, elementTypeAnnotation.name() );
    if ( elementType == null ) {
      return names;
    }

    List<IMetaStoreElement> elements = metaStore.getElements( namespace, elementType );
    for ( IMetaStoreElement element : elements ) {
      names.add( element.getName() );
    }

    return names;
  }

  /**
   * @return The {@link IMetaStoreElementType} to reference in the {@link IMetaStore} API.
   * @throws MetaStoreException
   */
  public IMetaStoreElementType getElementType() throws MetaStoreException {
    MetaStoreElementType elementTypeAnnotation = getElementTypeAnnotation();
    return metaStore.getElementTypeByName( namespace, elementTypeAnnotation.name() );
  }

  private AttributeType determineAttributeType( Field field, MetaStoreAttribute annotation ) throws MetaStoreException {
    Class<?> fieldClass = field.getType();
    if ( annotation.nameReference() ) {
      return AttributeType.NAME_REFERENCE;
    }
    if ( annotation.filenameReference() ) {
      return AttributeType.FILENAME_REFERENCE;
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
    if ( Date.class.equals( fieldClass ) ) {
      return AttributeType.DATE;
    }
    if ( boolean.class.equals( fieldClass ) ) {
      return AttributeType.BOOLEAN;
    }
    if ( List.class.equals( fieldClass ) ) {
      return AttributeType.LIST;
    }
    throw new MetaStoreException( "Unable to recognize attribute type for class '" + fieldClass + "'" );
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
   * @param object The object to modify
   * @param fieldName The field to modify
   * @param setterName The setter method name
   * @param valueClass The class value
   * @param value The value to set
   * @throws MetaStoreException
   */
  private void setAttributeValue( Class<?> parentClass, Object object, String fieldName, String setterName, Class<?> valueClass, Object value ) throws MetaStoreException {
    Method method;
    try {
      method = parentClass.getDeclaredMethod( setterName, valueClass );
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to find setter for attribute field : " + fieldName + ". Expected '" + setterName + "'", e );
    }

    try {
      method.invoke( object, value );
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to set value '" + value + "' using method '" + setterName + "'", e );
    }
  }

  private Object getAttributeValue( Class<?> parentClass, Object object, String fieldName, String getterName ) throws MetaStoreException {
    Method method;
    try {
      method = parentClass.getDeclaredMethod( getterName );
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to find getter for attribute field : " + fieldName + ". Expected '" + getterName + "'", e );
    }

    try {
      Object value = method.invoke( object );
      return value;
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to get value using method '" + getterName + "'", e );
    }

  }

  /**
   * myAttribute ==>  setMyAttribute
   */
  private String getSetterMethodName( String name ) {

    StringBuilder setter = new StringBuilder();
    setter.append( "set" );
    setter.append( name.substring( 0, 1 ).toUpperCase() );
    setter.append( name.substring( 1 ) );

    return setter.toString();
  }

  /**
   * myAttribute ==>  getMyAttribute
   */
  private String getGetterMethodName( String name, boolean isBoolean ) {

    StringBuilder setter = new StringBuilder();
    setter.append( isBoolean ? "is" : "get" );
    setter.append( name.substring( 0, 1 ).toUpperCase() );
    setter.append( name.substring( 1 ) );

    return setter.toString();
  }

  public IMetaStore getMetaStore() {
    return metaStore;
  }

  public void setMetaStore( IMetaStore metaStore ) {
    this.metaStore = metaStore;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace( String namespace ) {
    this.namespace = namespace;
  }

  public Map<String, List<?>> getNamedListMap() {
    return nameListMap;
  }

  public void setNamedListMap( Map<String, List<?>> namedListMap ) {
    this.nameListMap = namedListMap;
  }

  public Map<String, List<?>> getNameListMap() {
    return nameListMap;
  }

  public void setNameListMap( Map<String, List<?>> nameListMap ) {
    this.nameListMap = nameListMap;
  }

  public Map<String, List<?>> getFilenameListMap() {
    return filenameListMap;
  }

  public void setFilenameListMap( Map<String, List<?>> filenameListMap ) {
    this.filenameListMap = filenameListMap;
  }

}
