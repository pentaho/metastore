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



package org.pentaho.metastore.stores.delegate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreDependenciesExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreElementExistException;
import org.pentaho.metastore.api.exceptions.MetaStoreElementTypeExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.exceptions.MetaStoreNamespaceExistsException;
import org.pentaho.metastore.api.security.Base64TwoWayPasswordEncoder;
import org.pentaho.metastore.api.security.IMetaStoreElementOwner;
import org.pentaho.metastore.api.security.ITwoWayPasswordEncoder;
import org.pentaho.metastore.api.security.MetaStoreElementOwnerType;

/**
 * This class can be used as a wrapper around one or more meta stores. For example, if you have a local XML metastore, a
 * workgroup database metastore and an enterprise EE metastore, you can put them in reverse order in the meta stores
 * list.
 * 
 * There are 2 ways to work with the delegating meta store. The first is if you set an active meta store. That way, it
 * works as if you're working with the specified meta store.
 * 
 * If you didn't specify an active store, all namespaces and elements in all listed meta stores are considered. This
 * operating mode will prevent write operations.
 * 
 * That way, if you ask for the list of elements, you will get a unique list (by element ID) based on all stores.
 */
public class DelegatingMetaStore implements IMetaStore {

  /** Maps the name of the metastore to the physical implementation */
  protected List<IMetaStore> metaStoreList;

  /** The active metastore */
  protected String activeMetaStoreName;

  /** The two way password encoder to use */
  protected ITwoWayPasswordEncoder passwordEncoder;

  /**
   * Creates an empty delegating metastore.
   */
  public DelegatingMetaStore() {
    metaStoreList = new ArrayList<IMetaStore>();
    passwordEncoder = new Base64TwoWayPasswordEncoder();
  }

  /**
   * Creates a delegating metastore with the specified stores.
   *
   * @param stores the stores to delegate to, in read order
   */
  public DelegatingMetaStore( IMetaStore... stores ) {
    metaStoreList = new ArrayList<IMetaStore>( Arrays.asList( stores ) );
    passwordEncoder = new Base64TwoWayPasswordEncoder();
  }

  /**
   * Adds a metastore to the end of the delegation list.
   *
   * @param metaStore the metastore to add
   * @throws MetaStoreException if the store cannot provide its name
   */
  public void addMetaStore( IMetaStore metaStore ) throws MetaStoreException {
    metaStoreList.add( metaStore );
  }

  /**
   * Adds a metastore at the specified position.
   *
   * @param index the insertion position
   * @param metaStore the metastore to add
   * @throws MetaStoreException if the store cannot provide its name
   */
  public void addMetaStore( int index, IMetaStore metaStore ) throws MetaStoreException {
    metaStoreList.add( index, metaStore );
  }

  /**
   * Removes a metastore by its name.
   *
   * @param metaStore the metastore to remove
   * @return {@code true} when the metastore was removed
   * @throws MetaStoreException if the store cannot provide its name
   */
  public boolean removeMetaStore( IMetaStore metaStore ) throws MetaStoreException {
    return removeMetaStore( metaStore.getName() );
  }

  /**
   * Gets the configured metastore list.
   *
   * @return the metastore list
   */
  public List<IMetaStore> getMetaStoreList() {
    return metaStoreList;
  }

  /**
   * Sets the configured metastore list.
   *
   * @param metaStoreList the metastore list
   */
  public void setMetaStoreList( List<IMetaStore> metaStoreList ) {
    this.metaStoreList = metaStoreList;
  }

  private List<IMetaStore> getReadMetaStoreList() throws MetaStoreException {
    IMetaStore activeMetaStore;
    if ( activeMetaStoreName != null && ( activeMetaStore = getMetaStore( activeMetaStoreName ) ) != null ) {
      return Arrays.asList( activeMetaStore );
    }
    return metaStoreList;
  }

  private IMetaStore getWriteMetaStore() throws MetaStoreException {
    if ( activeMetaStoreName != null ) {
      IMetaStore activeMetaStore = getMetaStore( activeMetaStoreName );
      if ( activeMetaStore != null ) {
        return activeMetaStore;
      }
      throw new MetaStoreException( "Active metaStore could not be found but required for write operations." );
    }
    throw new MetaStoreException( "Active metaStore not set but required for write operations." );
  }

  /**
   * Removes a metastore by name.
   *
   * @param metaStoreName the metastore name
   * @return {@code true} when the metastore was removed
   * @throws MetaStoreException if a metastore cannot provide its name
   */
  public boolean removeMetaStore( String metaStoreName ) throws MetaStoreException {
    for ( Iterator<IMetaStore> it = metaStoreList.iterator(); it.hasNext(); ) {
      IMetaStore store = it.next();
      if ( store.getName().equalsIgnoreCase( metaStoreName ) ) {
        it.remove();
        if ( activeMetaStoreName != null && metaStoreName.equalsIgnoreCase( activeMetaStoreName ) ) {
          activeMetaStoreName = null;
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Sets the active metastore name for write operations.
   *
   * @param activeMetaStoreName the active metastore name
   */
  public void setActiveMetaStoreName( String activeMetaStoreName ) {
    this.activeMetaStoreName = activeMetaStoreName;
  }

  /**
   * Gets the active metastore name.
   *
   * @return the active metastore name, or {@code null} when no active store exists
   */
  public String getActiveMetaStoreName() {
    return activeMetaStoreName;
  }

  /**
   * Gets the active metastore.
   *
   * @return the active metastore, or {@code null} when no active store exists
   * @throws MetaStoreException if the active store cannot be found
   */
  public IMetaStore getActiveMetaStore() throws MetaStoreException {
    if ( activeMetaStoreName == null ) {
      return null;
    }

    IMetaStore metaStore = getMetaStore( activeMetaStoreName );
    return metaStore;
  }

  /**
   * Gets a configured metastore by name.
   *
   * @param metaStoreName the metastore name
   * @return the matching metastore, or {@code null} when none matches
   * @throws MetaStoreException if a metastore cannot provide its name
   */
  public IMetaStore getMetaStore( String metaStoreName ) throws MetaStoreException {
    for ( IMetaStore metaStore : metaStoreList ) {
      if ( metaStore.getName().equalsIgnoreCase( metaStoreName ) ) {
        return metaStore;
      }
    }
    return null;
  }

  @Override
  public boolean namespaceExists( String namespace ) throws MetaStoreException {
    for ( IMetaStore metaStore : getReadMetaStoreList() ) {
      if ( metaStore.namespaceExists( namespace ) ) {
        return true;
      }
    }
    return false;
  }

  @Override
  public List<String> getNamespaces() throws MetaStoreException {
    Set<String> namespaceSet = new HashSet<String>();
    List<String> namespaces = new ArrayList<String>();
    for ( IMetaStore metaStore : getReadMetaStoreList() ) {
      for ( String namespace : metaStore.getNamespaces() ) {
        if ( namespaceSet.add( namespace ) ) {
          namespaces.add( namespace );
        }
      }
    }
    return namespaces;
  }

  @Override
  public void createNamespace( String namespace ) throws MetaStoreException, MetaStoreNamespaceExistsException {
    IMetaStore metaStore = getWriteMetaStore();
    metaStore.createNamespace( namespace );
  }

  @Override
  public void deleteNamespace( String namespace ) throws MetaStoreException {
    IMetaStore metaStore = getWriteMetaStore();
    metaStore.deleteNamespace( namespace );
  }

  private IMetaStoreElementType getElementTypeByName( List<IMetaStoreElementType> types, String name ) {
    for ( IMetaStoreElementType type : types ) {
      if ( type.getName().equalsIgnoreCase( name ) ) {
        return type;
      }
    }
    return null;
  }

  @Override
  public List<IMetaStoreElementType> getElementTypes( String namespace ) throws MetaStoreException {
    List<IMetaStoreElementType> elementTypes = new ArrayList<IMetaStoreElementType>();
    for ( IMetaStore metaStore : getReadMetaStoreList() ) {
      for ( IMetaStoreElementType elementType : metaStore.getElementTypes( namespace ) ) {
        if ( getElementTypeByName( elementTypes, elementType.getName() ) == null ) {
          elementTypes.add( elementType );
        }
      }
    }
    return elementTypes;
  }

  @Override
  public List<String> getElementTypeIds( String namespace ) throws MetaStoreException {
    List<String> elementTypeIds = new ArrayList<String>();
    for ( IMetaStoreElementType elementType : getElementTypes( namespace ) ) {
      elementTypeIds.add( elementType.getId() );
    }
    return elementTypeIds;
  }

  @Override
  public IMetaStoreElementType getElementType( String namespace, String elementTypeId ) throws MetaStoreException {
    for ( IMetaStoreElementType type : getElementTypes( namespace ) ) {
      if ( type.getId().equals( elementTypeId ) ) {
        return type;
      }
    }
    return null;
  }

  @Override
  public IMetaStoreElementType getElementTypeByName( String namespace, String elementTypeName )
    throws MetaStoreException {
    return getElementTypeByName( getElementTypes( namespace ), elementTypeName );
  }

  @Override
  public void createElementType( String namespace, IMetaStoreElementType elementType ) throws MetaStoreException,
    MetaStoreElementTypeExistsException {
    IMetaStore metaStore = getWriteMetaStore();
    metaStore.createElementType( namespace, elementType );
  }

  @Override
  public void updateElementType( String namespace, IMetaStoreElementType elementType ) throws MetaStoreException {
    IMetaStore metaStore = getWriteMetaStore();
    metaStore.updateElementType( namespace, elementType );
  }

  @Override
  public void deleteElementType( String namespace, IMetaStoreElementType elementType ) throws MetaStoreException,
    MetaStoreDependenciesExistsException {
    IMetaStore metaStore = getWriteMetaStore();
    metaStore.deleteElementType( namespace, elementType );
  }

  private IMetaStoreElement getElementByName( List<IMetaStoreElement> elements, String name ) {
    for ( IMetaStoreElement element : elements ) {
      if ( element.getName().equals( name ) ) {
        return element;
      }
    }
    return null;
  }

  @Override
  public List<IMetaStoreElement> getElements( String namespace, IMetaStoreElementType elementType )
    throws MetaStoreException {

    return getElements( namespace, elementType, true, null );
  }

  @Override
  public List<IMetaStoreElement> getElements( String namespace, IMetaStoreElementType elementType, boolean lock,
                                              List<MetaStoreException> exceptionList ) throws MetaStoreException {
    List<IMetaStoreElement> elements = new ArrayList<IMetaStoreElement>();
    for ( IMetaStore metaStore : getReadMetaStoreList() ) {
      IMetaStoreElementType localElementType = metaStore.getElementTypeByName( namespace, elementType.getName() );
      if ( localElementType != null ) {
        for ( IMetaStoreElement element : metaStore.getElements( namespace, localElementType, lock, exceptionList ) ) {
          if ( getElementByName( elements, element.getName() ) == null ) {
            elements.add( element );
          }
        }
      }
    }
    return elements;
  }

  @Override
  public List<String> getElementIds( String namespace, IMetaStoreElementType elementType ) throws MetaStoreException {
    List<String> elementIds = new ArrayList<String>();
    for ( IMetaStoreElement element : getElements( namespace, elementType ) ) {
      elementIds.add( element.getId() );
    }
    return elementIds;
  }

  @Override
  public IMetaStoreElement getElement( String namespace, IMetaStoreElementType elementType, String elementId )
    throws MetaStoreException {
    for ( IMetaStore localStore : getReadMetaStoreList() ) {
      if ( elementType.getMetaStoreName() == null || elementType.getMetaStoreName().equals( localStore.getName() ) ) {
        IMetaStoreElementType localType = localStore.getElementTypeByName( namespace, elementType.getName() );
        if ( localType != null ) {
          for ( IMetaStoreElement element : localStore.getElements( namespace, localType ) ) {
            if ( element.getId().equals( elementId ) ) {
              return element;
            }
          }
        }
      }
    }
    return null;
  }

  @Override
  public IMetaStoreElement getElementByName( String namespace, IMetaStoreElementType elementType, String name )
    throws MetaStoreException {
    return getElementByName( getElements( namespace, elementType, true, new ArrayList<MetaStoreException>() ), name );
  }

  @Override
  public void createElement( String namespace, IMetaStoreElementType elementType, IMetaStoreElement element )
    throws MetaStoreException, MetaStoreElementExistException {
    getWriteMetaStore().createElement( namespace, elementType, element );
  }

  @Override
  public void deleteElement( String namespace, IMetaStoreElementType elementType, String elementId )
    throws MetaStoreException {
    getWriteMetaStore().deleteElement( namespace, elementType, elementId );
  }

  @Override
  public void updateElement( String namespace, IMetaStoreElementType elementType, String elementId,
      IMetaStoreElement element ) throws MetaStoreException {
    getWriteMetaStore().updateElement( namespace, elementType, elementId, element );
  }

  @Override
  public IMetaStoreElementType newElementType( String namespace ) throws MetaStoreException {
    return getWriteMetaStore().newElementType( namespace );
  }

  @Override
  public IMetaStoreElement newElement() throws MetaStoreException {
    return getWriteMetaStore().newElement();
  }

  @Override
  public IMetaStoreElement newElement( IMetaStoreElementType elementType, String id, Object value )
    throws MetaStoreException {
    return getWriteMetaStore().newElement( elementType, id, value );
  }

  /**
   * Creates a new attribute through the active metastore.
   *
   * @param id the attribute ID
   * @param value the attribute value
   * @return the new attribute
   * @throws MetaStoreException if no active metastore exists
   */
  public IMetaStoreAttribute newAttribute( String id, Object value ) throws MetaStoreException {
    return getWriteMetaStore().newAttribute( id, value );
  }

  @Override
  public IMetaStoreElementOwner newElementOwner( String name, MetaStoreElementOwnerType ownerType )
    throws MetaStoreException {
    return getWriteMetaStore().newElementOwner( name, ownerType );
  }

  @Override
  public String getName() throws MetaStoreException {
    IMetaStore activeMetaStore = getActiveMetaStore();
    if ( activeMetaStore != null ) {
      return activeMetaStore.getName();
    }
    return "DelegatingMetaStore";
  }

  @Override
  public String getDescription() throws MetaStoreException {
    IMetaStore activeMetaStore = getActiveMetaStore();
    if ( activeMetaStore != null ) {
      return activeMetaStore.getDescription();
    }
    return "The DelegatingMetaStore can act as a read-only aggregation of multiple MetaStores and can write if an active one is set";
  }

  @Override
  public void setTwoWayPasswordEncoder( ITwoWayPasswordEncoder encoder ) {
    this.passwordEncoder = encoder;
  }

  @Override
  public ITwoWayPasswordEncoder getTwoWayPasswordEncoder() {
    return passwordEncoder;
  }
}
