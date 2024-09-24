/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2023 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.metastore.stores.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.metastore.api.BaseMetaStore;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreDependenciesExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreElementExistException;
import org.pentaho.metastore.api.exceptions.MetaStoreElementTypeExistsException;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.exceptions.MetaStoreNamespaceExistsException;
import java.util.Collections;

/**
 * An abstract base class for XML file-based metastores.
 *
 *
 * @param <T>
 *          a type of object that can be returned by certain abstract methods. Can be any type.
 */
public abstract class BaseXmlMetaStore<T> extends BaseMetaStore implements IMetaStore {

  private final XmlMetaStoreCache metaStoreCache;
  // root path. This is never interpreted by this class directly, but is used to
  // generate paths for subclasses to use.
  private volatile String rootFolder;

  /**
   *
   * @param rootFolder
   *          the folder that should contain the metastore. note that XmlUtil.META_FOLDER_NAME will be added to this
   *          path.
   */
  protected BaseXmlMetaStore( String rootFolder ) throws MetaStoreException {
    this( rootFolder, defaultCache() );
  }

  /**
   *
   * @param rootFolder
   *          the folder that should contain the metastore. note that XmlUtil.META_FOLDER_NAME will be added to this
   *          path.
   * @param metaStoreCacheImpl
   */
  protected BaseXmlMetaStore( String rootFolder, XmlMetaStoreCache metaStoreCacheImpl ) throws MetaStoreException {
    metaStoreCache = metaStoreCacheImpl;
    this.rootFolder = rootFolder + "/" + XmlUtil.META_FOLDER_NAME;
  }

  protected static XmlMetaStoreCache defaultCache() {
    return new AutomaticXmlMetaStoreCache();
  }

  /**
   * @return the rootFolder in use
   */
  public String getRootFolder() {
    return rootFolder;
  }

  /**
   * Set the root folder. This will be the exact path that the metastore will operate in (namespaces will be created
   * immediately below this). It will not have XmlUtil.META_FOLDER_NAME appended.
   *
   *
   * @param rootFolder
   */
  public void setRootFolder( String rootFolder ) {
    this.rootFolder = rootFolder;
  }

  @Override
  public synchronized List<String> getNamespaces() throws MetaStoreException {
    lockStore();
    try {
      return listFolders( rootFolder );
    } finally {
      unlockStore();
    }
  }

  @Override
  public synchronized boolean namespaceExists( String namespace ) throws MetaStoreException {
    lockStore();
    try {
      String path = XmlUtil.getNamespaceFolder( rootFolder, namespace );
      return pathExists( path );
    } finally {
      unlockStore();
    }
  }

  @Override
  public synchronized void createNamespace( String namespace ) throws MetaStoreException {
    lockStore();
    try {
      String path = XmlUtil.getNamespaceFolder( rootFolder, namespace );
      if ( pathExists( path ) ) {
        throw new MetaStoreNamespaceExistsException( "The namespace with name '" + namespace + "' already exists." );
      }
      if ( !createDirectory( path ) ) {
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
      String path = XmlUtil.getNamespaceFolder( rootFolder, namespace );
      if ( !pathExists( path ) ) {
        return; // Should we throw an exception?
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

      if ( !deletePath( path ) ) {
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
      List<String> elementTypeFolders = listFolders( XmlUtil.getNamespaceFolder( rootFolder, namespace ) );
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
      return listFolders( XmlUtil.getNamespaceFolder( rootFolder, namespace ) );
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
      String elementTypeFile = XmlUtil.getElementTypeFile( rootFolder, namespace, elementTypeId );
      if ( pathExists( elementTypeFile ) ) {
        BaseXmlMetaStoreElementType elementType = newElementTypeFromFile( namespace, elementTypeFile );
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

      String elementTypeFolder = XmlUtil.getElementTypeFolder( rootFolder, namespace, elementType.getName() );
      String elementTypeFilename = XmlUtil.getElementTypeFile( rootFolder, namespace, elementType.getName() );
      if ( pathExists( elementTypeFolder ) && pathExists( elementTypeFilename ) ) {
        throw new MetaStoreElementTypeExistsException( getElementTypes( namespace, false ),
            "The specified element type already exists with the same ID" );
      }
      if ( !pathExists( elementTypeFolder ) && !createDirectory( elementTypeFolder ) ) {
        throw new MetaStoreException(
            "Unable to create XML meta store element type folder '" + elementTypeFolder + "'" );
      }

      // Copy the element type information to the XML meta store
      //
      BaseXmlMetaStoreElementType xmlType =
          newElementType( namespace, elementType.getId(), elementType.getName(), elementType.getDescription(),
            elementTypeFilename );
      xmlType.save();

      metaStoreCache.registerElementTypeIdForName( namespace, elementType.getName(), elementType.getId() );
      metaStoreCache.registerProcessedFile( elementTypeFolder, lastModified( elementTypeFolder ) );

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
      String elementTypeFolder = XmlUtil.getElementTypeFolder( rootFolder, namespace, elementType.getName() );
      if ( !pathExists( elementTypeFolder ) ) {
        throw new MetaStoreException(
            "The specified element type with ID '" + elementType.getId() + "' doesn't exists so we can't update it." );
      }

      String elementTypeFilename = XmlUtil.getElementTypeFile( rootFolder, namespace, elementType.getName() );

      // Save the element type information to the XML meta store
      //
      BaseXmlMetaStoreElementType xmlType =
          newElementType( namespace, elementType.getId(), elementType.getName(), elementType.getDescription(),
            elementTypeFilename );
      xmlType.save();

      metaStoreCache.registerElementTypeIdForName( namespace, elementType.getName(), elementType.getId() );
      metaStoreCache.registerProcessedFile( elementTypeFolder, lastModified( elementTypeFolder ) );
    } finally {
      unlockStore();
    }
  }

  @Override
  public synchronized void deleteElementType( String namespace, IMetaStoreElementType elementType )
    throws MetaStoreException {
    lockStore();
    try {
      String elementTypeFilename = XmlUtil.getElementTypeFile( rootFolder, namespace, elementType.getName() );
      if ( !pathExists( elementTypeFilename ) ) {
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
      if ( !deletePath( elementTypeFilename ) ) {
        throw new MetaStoreException( "Unable to delete element type XML file '" + elementTypeFilename + "'" );
      }

      // Remove the folder too, should be empty by now.
      //
      String elementTypeFolder = XmlUtil.getElementTypeFolder( rootFolder, namespace, elementType.getName() );
      if ( !deletePath( elementTypeFolder ) ) {
        throw new MetaStoreException( "Unable to delete element type XML folder '" + elementTypeFolder + "'" );
      }
      metaStoreCache.unregisterElementTypeId( namespace, elementType.getId() );
      metaStoreCache.unregisterProcessedFile( elementTypeFolder );
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
      String elementTypeFolder = XmlUtil.getElementTypeFolder( rootFolder, namespace, elementType.getName() );
      Map<String, Long> processedFiles =
          includeProcessedFiles ? Collections.emptyMap() : metaStoreCache.getProcessedFiles();
      List<T> elementTypeFiles = listFiles( elementTypeFolder, processedFiles );
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
      String elementTypeFolder = XmlUtil.getElementTypeFolder( rootFolder, namespace, elementType.getName() );
      Map<String, Long> processedFiles = metaStoreCache.getProcessedFiles();
      List<T> elementTypeFiles = listFiles( elementTypeFolder, processedFiles );
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
      String elementFilename = XmlUtil.getElementFile( rootFolder, namespace, elementType.getName(), elementId );
      if ( !pathExists( elementFilename ) ) {
        return null;
      }
      BaseXmlMetaStoreElement element = newElement( elementFilename );
      metaStoreCache.registerElementIdForName( namespace, elementType, element.getName(), elementId );
      metaStoreCache.registerProcessedFile( elementFilename, lastModified( elementFilename ) );
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
      String chachedElementId = metaStoreCache.getElementIdByName( namespace, elementType, name );
      if ( chachedElementId != null ) {
        IMetaStoreElement element = getElement( namespace, elementType, chachedElementId, false );
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

      String elementFilename = XmlUtil.getElementFile( rootFolder, namespace, elementType.getName(), element.getId() );
      if ( pathExists( elementFilename ) ) {
        throw new MetaStoreElementExistException(
            getElements( namespace, elementType, false, true, new ArrayList<MetaStoreException>() ),
            "The specified element already exists with the same ID: '" + element.getId() + "'" );
      }
      BaseXmlMetaStoreElement xmlElement = newElement( element );
      xmlElement.setFilename( elementFilename );
      xmlElement.save();

      metaStoreCache.registerElementIdForName( namespace, elementType, xmlElement.getName(), element.getId() );
      metaStoreCache.registerProcessedFile( elementFilename, lastModified( elementFilename ) );
      // In the case of the XML store, the name is the same as the ID
      //
      element.setId( xmlElement.getName() );
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
      String elementFilename =
          XmlUtil.getElementFile( rootFolder, namespace, elementType.getName(), element.getName() );
      if ( !pathExists( elementFilename ) ) {
        throw new MetaStoreException( "The specified element to update doesn't exist with ID: '" + elementId + "'" );
      }

      BaseXmlMetaStoreElement xmlElement = newElement( element );
      xmlElement.setFilename( elementFilename );
      xmlElement.setIdWithFilename( elementFilename );
      xmlElement.save();

      metaStoreCache.registerElementIdForName( namespace, elementType, xmlElement.getName(), xmlElement.getId() );
      metaStoreCache.registerProcessedFile( elementFilename, lastModified( elementFilename ) );
    } finally {
      unlockStore();
    }
  }

  @Override
  public synchronized void deleteElement( String namespace, IMetaStoreElementType elementType, String elementId )
    throws MetaStoreException {
    lockStore();
    try {
      String elementFilename = XmlUtil.getElementFile( rootFolder, namespace, elementType.getName(), elementId );
      if ( !pathExists( elementFilename ) ) {
        return;
      }

      if ( !deletePath( elementFilename ) ) {
        throw new MetaStoreException(
            "Unable to delete element with ID '" + elementId + "' in filename '" + elementFilename + "'" );
      }

      metaStoreCache.unregisterElementId( namespace, elementType, elementId );
      metaStoreCache.unregisterProcessedFile( elementFilename );
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
