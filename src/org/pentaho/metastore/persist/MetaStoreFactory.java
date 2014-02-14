package org.pentaho.metastore.persist;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.util.MetaStoreUtil;

public class MetaStoreFactory<T> {

  private enum AttributeType {
    STRING, INTEGER, LONG, DATE, BOOLEAN;
  }

  private IMetaStore metaStore;
  private final Class<T> clazz;
  private String namespace;

  private volatile SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" );

  public MetaStoreFactory( Class<T> clazz, IMetaStore metaStore, String namespace ) {
    this.metaStore = metaStore;
    this.clazz = clazz;
    this.namespace = namespace;
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
    setAttributeValue( object, "name", "setName", String.class, name );

    // Which are the attributes to load?
    //
    Field[] fields = clazz.getDeclaredFields();
    for ( Field field : fields ) {
      MetaStoreAttribute attributeAnnotation = field.getAnnotation( MetaStoreAttribute.class );
      if ( attributeAnnotation != null ) {
        String key = attributeAnnotation.key();
        if ( key == null || key.length() == 0 ) {
          key = field.getName();
        }

        AttributeType type = determineAttributeType( field );
        IMetaStoreAttribute child = element.getChild( key );
        if ( child != null && child.getValue() != null ) {
          String setterName = getSetterMethodName( field.getName() );
          String value = MetaStoreUtil.getAttributeString( child );
          switch ( type ) {
            case STRING:
              setAttributeValue( object, field.getName(), setterName, String.class, value );
              break;
            case INTEGER:
              setAttributeValue( object, field.getName(), setterName, int.class, Integer.valueOf( value ) );
              break;
            case LONG:
              setAttributeValue( object, field.getName(), setterName, long.class, Long.valueOf( value ) );
              break;
            case BOOLEAN:
              setAttributeValue( object, field.getName(), setterName, boolean.class, "Y".equalsIgnoreCase( value ) );
              break;
            case DATE:
              try {
                synchronized ( DATE_FORMAT ) {
                  Date date = value == null ? null : DATE_FORMAT.parse( value );
                  setAttributeValue( object, field.getName(), setterName, Date.class, date );
                }
              } catch ( Exception e ) {
                throw new MetaStoreException( "Unexpected date parsing problem with value: '" + value + "'", e );
              }
              break;
            default:
              throw new MetaStoreException( "Only String values are supported at this time" );

              // TODO: support other data types
          }
        }
      }
    }

    return object;
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

    String name = (String) getAttributeValue( t, "name", "getName" );
    if ( name == null ) {
      throw new MetaStoreException( "Unable to find name of element class object '" + t.toString() + "'" );
    }

    IMetaStoreElement element = metaStore.newElement();
    element.setName( name );
    element.setElementType( elementType );

    // Which are the attributes to store?
    //
    Field[] fields = clazz.getDeclaredFields();
    for ( Field field : fields ) {
      MetaStoreAttribute attributeAnnotation = field.getAnnotation( MetaStoreAttribute.class );
      if ( attributeAnnotation != null ) {
        String key = attributeAnnotation.key();
        if ( key == null || key.length() == 0 ) {
          key = field.getName();
        }

        AttributeType type = determineAttributeType( field );

        IMetaStoreAttribute child;
        switch ( type ) {
          case STRING:
            String value = (String) getAttributeValue( t, field.getName(), getGetterMethodName( field.getName(), false ) );
            child = metaStore.newAttribute( key, value );
            element.addChild( child );
            break;
          case INTEGER:
            int intValue = (Integer) getAttributeValue( t, field.getName(), getGetterMethodName( field.getName(), false ) );
            child = metaStore.newAttribute( key, Integer.toString( intValue ) );
            element.addChild( child );
            break;
          case LONG:
            long longValue = (Long) getAttributeValue( t, field.getName(), getGetterMethodName( field.getName(), false ) );
            child = metaStore.newAttribute( key, Long.toString( longValue ) );
            element.addChild( child );
            break;
          case BOOLEAN:
            boolean boolValue = (Boolean) getAttributeValue( t, field.getName(), getGetterMethodName( field.getName(), true ) );
            child = metaStore.newAttribute( key, boolValue ? "Y" : "N" );
            element.addChild( child );
            break;
          case DATE:
            Date dateValue = (Date) getAttributeValue( t, field.getName(), getGetterMethodName( field.getName(), false ) );
            child = metaStore.newAttribute( key, dateValue == null ? null : DATE_FORMAT.format( dateValue ) );
            element.addChild( child );
            break;
          default:
            throw new MetaStoreException( "Only String values are supported at this time" );
        }

        // TODO: support other field data types...

      }
    }

    // Now that we have the element populated, do a quick check to see if we need to update the element
    // or simply create a new element in the metastore.

    IMetaStoreElement existingElement = metaStore.getElementByName( namespace, elementType, name );
    if ( existingElement == null ) {
      metaStore.createElement( namespace, elementType, element );
    } else {
      metaStore.updateElement( namespace, elementType, existingElement.getId(), element );
    }
  }

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

  private AttributeType determineAttributeType( Field field ) throws MetaStoreException {
    Class<?> fieldClass = field.getType();
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
    throw new MetaStoreException( "Unable to recognize attribute type for class '" + fieldClass + "'" );
  }

  private MetaStoreElementType getElementTypeAnnotation() throws MetaStoreException {
    MetaStoreElementType elementTypeAnnotation = clazz.getAnnotation( MetaStoreElementType.class );
    if ( elementTypeAnnotation == null ) {
      throw new MetaStoreException( "The class you want to serialize needs to have the @MetaStoreElementType annotation" );
    }
    return elementTypeAnnotation;
  }

  private void setAttributeValue( Object object, String fieldName, String setterName, Class<?> valueClass, Object value ) throws MetaStoreException {
    Method method;
    try {
      method = clazz.getDeclaredMethod( setterName, valueClass );
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to find setter for attribute field : " + fieldName + ". Expected '" + setterName + "'", e );
    }

    try {
      method.invoke( object, value );
    } catch ( Exception e ) {
      throw new MetaStoreException( "Unable to set value '" + value + "' using method '" + setterName + "'", e );
    }
  }

  private Object getAttributeValue( T object, String fieldName, String getterName ) throws MetaStoreException {
    Method method;
    try {
      method = clazz.getDeclaredMethod( getterName );
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

}
