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



package org.pentaho.metastore.stores.xml;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.pentaho.metastore.api.BaseMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreDependenciesExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreElementExistException;
import org.pentaho.metastore.api.exceptions.MetaStoreElementTypeExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.exceptions.MetaStoreNamespaceExistsException;

/**
 * An abstract base class for XML file-based metastores.
 *
 *
 * @param <T>
 *          a type of object that can be returned by certain abstract methods. Can be any type.
 */
public abstract class BaseXmlMetaStore<T> extends BaseMetaStore {

  private final XmlMetaStoreCache metaStoreCache;
  // root path. This is never interpreted by this class directly, but is used to
  // generate paths for subclasses to use.
  private final AtomicReference<Path> rootFolder;

  /**
   *
   * @param rootFolder
   *          the folder that should contain the metastore. note that XmlUtil.META_FOLDER_NAME will be added to this
   *          path.
   */
  protected BaseXmlMetaStore( String rootFolder ) {
    this( rootFolder, defaultCache() );
  }

  /**
   *
   * @param rootFolder
   *          the folder that should contain the metastore. note that XmlUtil.META_FOLDER_NAME will be added to this
   *          path.
   * @param metaStoreCacheImpl
   */
  protected BaseXmlMetaStore( String rootFolder, XmlMetaStoreCache metaStoreCacheImpl ) {
    metaStoreCache = metaStoreCacheImpl;
    this.rootFolder = new AtomicReference<>( Paths.get( rootFolder ).resolve( XmlUtil.META_FOLDER_NAME ).normalize() );
  }

  protected static XmlMetaStoreCache defaultCache() {
    return new AutomaticXmlMetaStoreCache();
  }

  /**
   * @return the rootFolder in use
   */
  public String getRootFolder() {
    return rootFolder.get().toString();
  }

  /**
   * Set the root folder. This will be the exact path that the metastore will operate in (namespaces will be created
   * immediately below this). It will not have XmlUtil.META_FOLDER_NAME appended.
   *
   *
   * @param rootFolder
   */
  public void setRootFolder( String rootFolder ) {
    this.rootFolder.set( Paths.get( rootFolder ).normalize() );
  }

  protected Path getRootFolderPath() {
    return rootFolder.get();
  }

  @Override
  public synchronized List<String> getNamespaces() throws MetaStoreException {
    lockStore();
    try {
      return listFolders( rootFolder.get().toString() );
    } finally {
      unlockStore();
    }
  }

  @Override
  public synchronized boolean namespaceExists( String namespace ) throws MetaStoreException {
    lockStore();
    try {
      Path path = XmlUtil.getNamespaceFolderPath( rootFolder.get(), namespace );
      return pathExists( path.toString() );
    } finally {
      unlockStore();
    }
  }

  @Override
  public synchronized void createNamespace( String namespace ) throws MetaStoreException {
    lockStore();
    try {
      Path path = XmlUtil.getNamespaceFolderPath( rootFolder.get(), namespace );
      if ( pathExists( path.toString() ) ) {
        throw new MetaStoreNamespaceExistsException( "The namespace with name '" + namespace + "' already exists." );
      }
      if ( !createDirectory( path.toString() ) ) {
        throw new MetaStoreException( "Unable to create XML meta store namespace folder: " + path );
      }
    } finally {
      unlockStore();
    }
  }

  @Override
  public synchronized void deleteNamespace( String namespace ) throws MetaStoreException {
    lockStore();
    try {
      Path path = XmlUtil.getNamespaceFolderPath( rootFolder.get(), namespace );
      if ( !pathExists( path.toString() ) ) {
        return;
      }
      List<IMetaStoreElementType> elementTypes = getElementTypes( namespace, false );

      if ( !elementTypes.isEmpty() ) {
        List<String> dependencies = new ArrayList<>( elementTypes.size() );
        for ( IMetaStoreElementType elementType : elementTypes ) {
          dependencies.add( elementType.getId() );
        }
        throw new MetaStoreDependenciesExistsException( dependencies,
            "Unable to delete the XML meta store namespace with name '" + namespace
                + "' as it still contains dependencies" );
      }

      if ( !deletePath( path.toString() ) ) {
        throw new MetaStoreException( "Unable to delete XML meta store namespace folder, check to see if it's empty" );
      }
    } finally {
      unlockStore();
    }
  }

  @Override
  public synchronized List<IMetaStoreElementType> getElementTypes( String namespace ) throws MetaStoreException {
    return getElementTypes( namespace, true );
  }

  protected synchronized List<IMetaStoreElementType> getElementTypes( String namespace, boolean lock )
    throws MetaStoreException {
    if ( lock ) {
      lockStore();
    }
    try {
        List<String> elementTypeFolders =
          listFolders( XmlUtil.getNamespaceFolderPath( rootFolder.get(), namespace ).toString() );
      List<IMetaStoreElementType> elementTypes = new ArrayList<>( elementTypeFolders.size() );
      for ( String elementTypeFolder : elementTypeFolders ) {
        IMetaStoreElementType elementType = getElementType( namespace, elementTypeFolder, false );
        if ( elementType != null ) {
          elementTypes.add( elementType );
        }
      }
      return elementTypes;
    } finally {
      if ( lock ) {
        unlockStore();
      }
    }
  }

  @Override
  public synchronized List<String> getElementTypeIds( String namespace ) throws MetaStoreException {
    lockStore();
    try {
      return listFolders( XmlUtil.getNamespaceFolderPath( rootFolder.get(), namespace ).toString() );
    } finally {
      unlockStore();
    }
  }

  protected synchronized IMetaStoreElementType getElementType( String namespace, String elementTypeId, boolean lock )
    throws MetaStoreException {
    if ( lock ) {
      lockStore();
    }
    try {
      Path elementTypeFile = XmlUtil.getElementTypeFilePath( rootFolder.get(), namespace, elementTypeId );
      if ( pathExists( elementTypeFile.toString() ) ) {
        BaseXmlMetaStoreElementType elementType = newElementTypeFromFile( namespace, elementTypeFile.toString() );
        elementType.setMetaStoreName( getName() );
        return elementType;
      } else {
        return null;
      }
    } finally {
      if ( lock ) {
        unlockStore();
      }
    }
  }

  @Override
  public synchronized IMetaStoreElementType getElementType( String namespace, String elementTypeId )
    throws MetaStoreException {
    return getElementType( namespace, elementTypeId, true );
  }

  @Override
  public synchronized IMetaStoreElementType getElementTypeByName( String namespace, String elementTypeName )
    throws MetaStoreException {
    for ( IMetaStoreElementType elementType : getElementTypes( namespace ) ) {
      if ( elementType.getName() != null && elementType.getName().equalsIgnoreCase( elementTypeName ) ) {
        return elementType;
      }
    }
    return null;
  }

  @Override
  public synchronized IMetaStoreElementType getElementTypeByName( String namespace, String elementTypeName,
      boolean lock )
    throws MetaStoreException {
    for ( IMetaStoreElementType elementType : getElementTypes( namespace, lock ) ) {
      if ( elementType.getName() != null && elementType.getName().equalsIgnoreCase( elementTypeName ) ) {
        return elementType;
      }
    }
    return null;
  }

  /**
   * Creates an XML metastore attribute.
   *
   * @param id the attribute ID
   * @param value the attribute value
   * @return the new attribute
   * @throws MetaStoreException if the attribute cannot be created
   */
  public IMetaStoreAttribute newAttribute( String id, Object value ) throws MetaStoreException {
    return new XmlMetaStoreAttribute( id, value );
  }

  @Override
  public synchronized void createElementType( String namespace, IMetaStoreElementType elementType )
    throws MetaStoreException {
    lockStore();
    try {
      // In the case of a file, the ID is the name
      //
      if ( elementType.getId() == null ) {
        elementType.setId( elementType.getName() );
      }

        Path elementTypeFolder =
          XmlUtil.getElementTypeFolderPath( rootFolder.get(), namespace, elementType.getName() );
        Path elementTypeFilename = XmlUtil.getElementTypeFilePath( rootFolder.get(), namespace, elementType.getName() );
        if ( pathExists( elementTypeFolder.toString() ) && pathExists( elementTypeFilename.toString() ) ) {
        throw new MetaStoreElementTypeExistsException( getElementTypes( namespace, false ),
            "The specified element type already exists with the same ID" );
      }
      if ( !pathExists( elementTypeFolder.toString() ) && !createDirectory( elementTypeFolder.toString() ) ) {
        throw new MetaStoreException(
            "Unable to create XML meta store element type folder '" + elementTypeFolder + "'" );
      }

      // Copy the element type information to the XML meta store
      //
      BaseXmlMetaStoreElementType xmlType =
          newElementType( namespace, elementType.getId(), elementType.getName(), elementType.getDescription(),
            elementTypeFilename.toString() );
      xmlType.save();

      metaStoreCache.registerElementTypeIdForName( namespace, elementType.getName(), elementType.getId() );
      metaStoreCache.registerProcessedFile( elementTypeFolder.toString(), lastModified( elementTypeFolder.toString() ) );

      xmlType.setMetaStoreName( getName() );
      elementType.setMetaStoreName( getName() );
    } finally {
      unlockStore();
    }
  }

  @Override
  public synchronized void updateElementType( String namespace, IMetaStoreElementType elementType )
    throws MetaStoreException {
    lockStore();
    try {
        Path elementTypeFolder =
          XmlUtil.getElementTypeFolderPath( rootFolder.get(), namespace, elementType.getName() );
        if ( !pathExists( elementTypeFolder.toString() ) ) {
        throw new MetaStoreException(
            "The specified element type with ID '" + elementType.getId() + "' doesn't exists so we can't update it." );
      }

      Path elementTypeFilename = XmlUtil.getElementTypeFilePath( rootFolder.get(), namespace, elementType.getName() );

      // Save the element type information to the XML meta store
      //
      BaseXmlMetaStoreElementType xmlType =
          newElementType( namespace, elementType.getId(), elementType.getName(), elementType.getDescription(),
            elementTypeFilename.toString() );
      xmlType.save();

      metaStoreCache.registerElementTypeIdForName( namespace, elementType.getName(), elementType.getId() );
      metaStoreCache.registerProcessedFile( elementTypeFolder.toString(), lastModified( elementTypeFolder.toString() ) );
    } finally {
      unlockStore();
    }
  }

  @Override
  public synchronized void deleteElementType( String namespace, IMetaStoreElementType elementType )
    throws MetaStoreException {
    lockStore();
    try {
      Path elementTypeFilename = XmlUtil.getElementTypeFilePath( rootFolder.get(), namespace, elementType.getName() );
      if ( !pathExists( elementTypeFilename.toString() ) ) {
        return;
      }
      // Check if the element type has no remaining elements
      List<IMetaStoreElement> elements =
          getElements( namespace, elementType, false, true, new ArrayList<MetaStoreException>() );
      if ( !elements.isEmpty() ) {
        List<String> dependencies = new ArrayList<>();
        for ( IMetaStoreElement element : elements ) {
          dependencies.add( element.getId() );
        }
        throw new MetaStoreDependenciesExistsException( dependencies, "Unable to delete element type with name '"
            + elementType.getName() + "' in namespace '" + namespace + "' because there are still elements present" );
      }

      // Remove the elementType.xml file
      //
      if ( !deletePath( elementTypeFilename.toString() ) ) {
        throw new MetaStoreException( "Unable to delete element type XML file '" + elementTypeFilename + "'" );
      }

      // Remove the folder too, should be empty by now.
      //
        Path elementTypeFolder =
          XmlUtil.getElementTypeFolderPath( rootFolder.get(), namespace, elementType.getName() );
        if ( !deletePath( elementTypeFolder.toString() ) ) {
        throw new MetaStoreException( "Unable to delete element type XML folder '" + elementTypeFolder + "'" );
      }
      metaStoreCache.unregisterElementTypeId( namespace, elementType.getId() );
      metaStoreCache.unregisterProcessedFile( elementTypeFolder.toString() );
    } finally {
      unlockStore();
    }
  }

  @Override
  public List<IMetaStoreElement> getElements( String namespace, IMetaStoreElementType elementType )
    throws MetaStoreException {
    return getElements( namespace, elementType, true, true, null );
  }

  @Override
  public List<IMetaStoreElement> getElements( String namespace, IMetaStoreElementType elementType, boolean lock )
    throws MetaStoreException {
    return getElements( namespace, elementType, lock, true, null );
  }

  @Override
  public List<IMetaStoreElement> getElements( String namespace, IMetaStoreElementType elementType, boolean lock,
      List<MetaStoreException> exceptionList )
    throws MetaStoreException {
    return getElements( namespace, elementType, lock, true, exceptionList );
  }

  protected synchronized List<IMetaStoreElement> getElements( String namespace, IMetaStoreElementType elementType,
      boolean lock, boolean includeProcessedFiles, List<MetaStoreException> exceptionList )
    throws MetaStoreException {
    if ( lock ) {
      lockStore();
    }
    try {
        Path elementTypeFolder =
          XmlUtil.getElementTypeFolderPath( rootFolder.get(), namespace, elementType.getName() );
      Map<String, Long> processedFiles =
          includeProcessedFiles ? Collections.emptyMap() : metaStoreCache.getProcessedFiles();
      List<T> elementTypeFiles = listFiles( elementTypeFolder.toString(), processedFiles );
      List<IMetaStoreElement> elements = new ArrayList<>( elementTypeFiles.size() );
      for ( T elementTypeFile : elementTypeFiles ) {
        String elementId = getFilename( elementTypeFile );
        // File .type.xml doesn't hidden in OS Windows so better to ignore it explicitly
        if ( elementId.equals( XmlUtil.ELEMENT_TYPE_FILE_NAME ) ) {
          continue;
        }
        elementId = elementId.substring( 0, elementId.length() - 4 ); // remove .xml to get the ID
        try {
          elements.add( getElement( namespace, elementType, elementId, false ) );
        } catch ( Exception e ) {
          // If we are collecting exceptions instead of fatally exiting, add to the list
          // of exceptions and continue
          if ( exceptionList != null ) {
            exceptionList.add( new MetaStoreException( "Could not load metaStore element '" + elementId + "'", e ) );
          } else {
            // Strict run. abort list
            throw e;
          }
        }
      }

      return elements;
    } finally {
      if ( lock ) {
        unlockStore();
      }
    }
  }

  @Override
  public synchronized List<String> getElementIds( String namespace, IMetaStoreElementType elementType )
    throws MetaStoreException {
    lockStore();
    try {
        Path elementTypeFolder =
            XmlUtil.getElementTypeFolderPath( rootFolder.get(), namespace, elementType.getName() );
        List<T> elementTypeFiles = listFiles( elementTypeFolder.toString(), Collections.emptyMap() );
      List<String> elementIds = new ArrayList<>( elementTypeFiles.size() );
      for ( T elementTypeFile : elementTypeFiles ) {
        String elementId = getFilename( elementTypeFile );
        // File .type.xml doesn't hidden in OS Windows so better to ignore it explicitly
        if ( elementId.equals( XmlUtil.ELEMENT_TYPE_FILE_NAME ) ) {
          continue;
        }
        elementId = elementId.substring( 0, elementId.length() - 4 ); // remove .xml to get the ID
        elementIds.add( elementId );
      }

      return elementIds;
    } finally {
      unlockStore();
    }
  }

  @Override
  public IMetaStoreElement getElement( String namespace, IMetaStoreElementType elementType, String elementId )
    throws MetaStoreException {
    return getElement( namespace, elementType, elementId, true );
  }

  protected synchronized IMetaStoreElement getElement( String namespace, IMetaStoreElementType elementType,
      String elementId, boolean lock )
    throws MetaStoreException {
    if ( lock ) {
      lockStore();
    }
    try {
      Path elementFilename =
          XmlUtil.getElementFilePath( rootFolder.get(), namespace, elementType.getName(), elementId );
      if ( !pathExists( elementFilename.toString() ) ) {
        return null;
      }
      BaseXmlMetaStoreElement element = newElement( elementFilename.toString() );
      metaStoreCache.registerElementIdForName( namespace, elementType, element.getName(), elementId );
      metaStoreCache.registerProcessedFile( elementFilename.toString(), lastModified( elementFilename.toString() ) );
      return element;
    } finally {
      if ( lock ) {
        unlockStore();
      }
    }
  }

  @Override
  public IMetaStoreElement getElementByName( String namespace, IMetaStoreElementType elementType, String name )
    throws MetaStoreException {
    return getElementByName( namespace, elementType, name, true );
  }

  @Override
  public synchronized IMetaStoreElement getElementByName( String namespace, IMetaStoreElementType elementType,
      String name, boolean lock )
    throws MetaStoreException {
    if ( lock ) {
      lockStore();
    }
    try {
      String cachedElementId = metaStoreCache.getElementIdByName( namespace, elementType, name );
      if ( cachedElementId != null ) {
        IMetaStoreElement element = getElement( namespace, elementType, cachedElementId, false );
        if ( element != null && element.getName().equalsIgnoreCase( name ) ) {
          return element;
        }
      }

      for ( IMetaStoreElement element : getElements( namespace, elementType, false, false,
        new ArrayList<MetaStoreException>() ) ) {
        if ( element.getName() != null && element.getName().equalsIgnoreCase( name ) ) {
          return element;
        }
      }
      return null;
    } finally {
      if ( lock ) {
        unlockStore();
      }
    }
  }

  @Override
  public synchronized void createElement( String namespace, IMetaStoreElementType elementType,
      IMetaStoreElement element )
    throws MetaStoreException {
    lockStore();
    try {
      // In the case of a file, the ID is the name
      //
      if ( element.getId() == null ) {
        element.setId( element.getName() );
      }

        Path elementFilename =
          XmlUtil.getElementFilePath( rootFolder.get(), namespace, elementType.getName(), element.getId() );
        if ( pathExists( elementFilename.toString() ) ) {
        throw new MetaStoreElementExistException(
            getElements( namespace, elementType, false, true, new ArrayList<MetaStoreException>() ),
            "The specified element already exists with the same ID: '" + element.getId() + "'" );
      }
      BaseXmlMetaStoreElement xmlElement = newElement( element );
      xmlElement.setFilename( elementFilename.toString() );
      xmlElement.save();

      metaStoreCache.registerElementIdForName( namespace, elementType, xmlElement.getName(), element.getId() );
      metaStoreCache.registerProcessedFile( elementFilename.toString(), lastModified( elementFilename.toString() ) );
    } finally {
      unlockStore();
    }
  }

  @Override
  public synchronized void updateElement( String namespace, IMetaStoreElementType elementType, String elementId,
      IMetaStoreElement element )
    throws MetaStoreException {

    // verify that the element type belongs to this meta store
    //
    if ( elementType.getMetaStoreName() == null || !elementType.getMetaStoreName().equals( getName() ) ) {
      throw new MetaStoreException( "The element type '" + elementType.getName()
          + "' needs to explicitly belong to the meta store in which you are updating." );
    }

    lockStore();
    try {
        Path elementFilename =
          XmlUtil.getElementFilePath( rootFolder.get(), namespace, elementType.getName(), elementId );
        if ( !pathExists( elementFilename.toString() ) ) {
        throw new MetaStoreException( "The specified element to update doesn't exist with ID: '" + elementId + "'" );
      }

      BaseXmlMetaStoreElement xmlElement = newElement( element );
      xmlElement.setFilename( elementFilename.toString() );
      xmlElement.setIdWithFilename( elementFilename.toString() );
      xmlElement.save();

      metaStoreCache.registerElementIdForName( namespace, elementType, xmlElement.getName(), xmlElement.getId() );
      metaStoreCache.registerProcessedFile( elementFilename.toString(), lastModified( elementFilename.toString() ) );
    } finally {
      unlockStore();
    }
  }

  @Override
  public synchronized void deleteElement( String namespace, IMetaStoreElementType elementType, String elementId )
    throws MetaStoreException {
    lockStore();
    try {
        Path elementFilename =
          XmlUtil.getElementFilePath( rootFolder.get(), namespace, elementType.getName(), elementId );
        if ( !pathExists( elementFilename.toString() ) ) {
        return;
      }

      if ( !deletePath( elementFilename.toString() ) ) {
        throw new MetaStoreException(
            "Unable to delete element with ID '" + elementId + "' in filename '" + elementFilename + "'" );
      }

      metaStoreCache.unregisterElementId( namespace, elementType, elementId );
      metaStoreCache.unregisterProcessedFile( elementFilename.toString() );
    } finally {
      unlockStore();
    }
  }

  /**
   * Check if an absolute path exists
   *
   *
   * @param path
   *          absolute path
   *
   * @return boolean if the path exists
   */
  protected abstract boolean pathExists( String path ) throws MetaStoreException;

  /**
   * Create a directory at the provided absolute path
   *
   *
   * @param path
   *          absolute path
   * @return true if it was created, false if it already exists
   */
  protected abstract boolean createDirectory( String path ) throws MetaStoreException;

  /**
   * Delete the target at the provided absolute path. This may fail if the target is a directory and is not empty.
   *
   *
   * @param path
   *          absolute path
   *
   * @return boolean true if the object at the path was deleted, false otherwise
   */
  protected abstract boolean deletePath( String path ) throws MetaStoreException;

  /**
   * List folders at an absolute path
   *
   * @param folder
   * @return the non-hidden folders in the specified folder
   */
  protected abstract List<String> listFolders( String folder ) throws MetaStoreException;

  /**
   * Return the last modified time of the provided absolute path
   *
   *
   * @param path
   *          absolute path
   *
   * @return long the time in milliseconds or 0 if the path does not exist
   */
  protected abstract long lastModified( String path ) throws MetaStoreException;

  /**
   * List files in a given directory. Does not include folders. Result should not include any item already in
   * processedFiles with the same modifiedTime as the value. (by absolute path)
   *
   * @param folder
   *          absolute path to the folder
   * @param processedFiles
   *          Files that have already been processed, absolute path to lastModifiedTime
   *
   * @return List&lt;T&gt; List of file objects representing the non-hidden files in the specified folder
   */
  protected abstract List<T> listFiles( String folder, Map<String, Long> processedFiles ) throws MetaStoreException;

  /**
   * Get the filename from a file object.
   *
   *
   * @param file
   *          a file object
   *
   * @return String the filename of the file object
   */
  protected abstract String getFilename( T file ) throws MetaStoreException;

  /**
   * Create a new ElementType from the provide info
   *
   *
   * @param namespace
   * @param id
   * @param name
   * @param description
   * @param filename
   *          absolute path to the file
   *
   * @return IMetaStoreElementType
   */
  protected abstract BaseXmlMetaStoreElementType newElementType( String namespace, String id, String name,
      String description, String filename )
    throws MetaStoreException;

  /**
   * Create a new ElementType and read it from the file
   *
   *
   * @param filename
   *          absolute path to the file
   *
   * @return BaseXmlMetaStoreElementType
   */
  protected abstract BaseXmlMetaStoreElementType newElementTypeFromFile( String namespace, String filename )
    throws MetaStoreException;

  /**
   * Create a new MetastoreElement loaded from the provided absolute path.
   *
   *
   * @param filename
   *          absolute path
   *
   * @return BaseXmlMetaStoreElement
   */
  protected abstract BaseXmlMetaStoreElement newElement( String filename ) throws MetaStoreException;

  /**
   * Create a new MetastoreElement copied from the provided element
   *
   *
   * @param element
   *          element to copy from
   *
   * @return BaseXmlMetaStoreElement
   */
  protected abstract BaseXmlMetaStoreElement newElement( IMetaStoreElement element ) throws MetaStoreException;

  /**
   * Lock the metastore if possible.
   * 
   * @throws MetaStoreException
   *           in case we have to wait more than 10 seconds to acquire a lock
   */
  protected abstract void lockStore() throws MetaStoreException;

  protected abstract void unlockStore() throws MetaStoreException;
}
